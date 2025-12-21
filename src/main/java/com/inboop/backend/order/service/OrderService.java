package com.inboop.backend.order.service;

import com.inboop.backend.auth.entity.User;
import com.inboop.backend.auth.repository.UserRepository;
import com.inboop.backend.lead.enums.ChannelType;
import com.inboop.backend.order.dto.*;
import com.inboop.backend.order.entity.Order;
import com.inboop.backend.order.entity.OrderTimeline;
import com.inboop.backend.order.enums.OrderStatus;
import com.inboop.backend.order.enums.PaymentMethod;
import com.inboop.backend.order.enums.PaymentStatus;
import com.inboop.backend.order.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for order management operations.
 */
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, EntityManager entityManager) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    /**
     * List orders with filtering and pagination.
     */
    public PagedOrderResponse listOrders(
            Long businessId,
            Long currentUserId,
            OrderStatus status,
            PaymentStatus paymentStatus,
            PaymentMethod paymentMethod,
            ChannelType channel,
            String assignedTo, // "me", "unassigned", "any", or userId
            String searchQuery,
            LocalDate createdFrom,
            LocalDate createdTo,
            String sort,
            int page,
            int pageSize
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Order> countRoot = countQuery.from(Order.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(buildPredicates(cb, countRoot, businessId, currentUserId, status, paymentStatus,
                paymentMethod, channel, assignedTo, searchQuery, createdFrom, createdTo).toArray(new Predicate[0]));
        Long totalItems = entityManager.createQuery(countQuery).getSingleResult();

        // Data query
        CriteriaQuery<Order> dataQuery = cb.createQuery(Order.class);
        Root<Order> dataRoot = dataQuery.from(Order.class);
        dataRoot.fetch("assignedTo", JoinType.LEFT);
        dataQuery.where(buildPredicates(cb, dataRoot, businessId, currentUserId, status, paymentStatus,
                paymentMethod, channel, assignedTo, searchQuery, createdFrom, createdTo).toArray(new Predicate[0]));

        // Sorting
        if (sort != null && sort.equals("createdAt_asc")) {
            dataQuery.orderBy(cb.asc(dataRoot.get("createdAt")));
        } else {
            dataQuery.orderBy(cb.desc(dataRoot.get("createdAt")));
        }

        TypedQuery<Order> query = entityManager.createQuery(dataQuery);
        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        List<OrderListItemDto> items = query.getResultList().stream()
                .map(OrderListItemDto::fromEntity)
                .collect(Collectors.toList());

        return new PagedOrderResponse(items, page, pageSize, totalItems);
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<Order> root,
            Long businessId,
            Long currentUserId,
            OrderStatus status,
            PaymentStatus paymentStatus,
            PaymentMethod paymentMethod,
            ChannelType channel,
            String assignedTo,
            String searchQuery,
            LocalDate createdFrom,
            LocalDate createdTo
    ) {
        List<Predicate> predicates = new ArrayList<>();

        // Business filter (required for multi-tenant)
        if (businessId != null) {
            predicates.add(cb.equal(root.get("business").get("id"), businessId));
        }

        // Exclude deleted orders
        predicates.add(cb.isNull(root.get("deletedAt")));

        // Status filter
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        // Payment status filter
        if (paymentStatus != null) {
            predicates.add(cb.equal(root.get("paymentStatus"), paymentStatus));
        }

        // Payment method filter
        if (paymentMethod != null) {
            predicates.add(cb.equal(root.get("paymentMethod"), paymentMethod));
        }

        // Channel filter
        if (channel != null) {
            predicates.add(cb.equal(root.get("channel"), channel));
        }

        // Assignment filter
        if (assignedTo != null) {
            switch (assignedTo) {
                case "me":
                    predicates.add(cb.equal(root.get("assignedTo").get("id"), currentUserId));
                    break;
                case "unassigned":
                    predicates.add(cb.isNull(root.get("assignedTo")));
                    break;
                case "any":
                    // No filter
                    break;
                default:
                    // Assume it's a user ID
                    try {
                        Long userId = Long.parseLong(assignedTo);
                        predicates.add(cb.equal(root.get("assignedTo").get("id"), userId));
                    } catch (NumberFormatException e) {
                        // Invalid user ID, ignore
                    }
            }
        }

        // Search query (order number, customer name/handle, tracking number)
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String pattern = "%" + searchQuery.toLowerCase() + "%";
            Predicate orderNumberMatch = cb.like(cb.lower(root.get("orderNumber")), pattern);
            Predicate customerNameMatch = cb.like(cb.lower(root.get("customerName")), pattern);
            Predicate customerHandleMatch = cb.like(cb.lower(root.get("customerHandle")), pattern);
            Predicate trackingMatch = cb.like(cb.lower(root.get("trackingNumber")), pattern);
            predicates.add(cb.or(orderNumberMatch, customerNameMatch, customerHandleMatch, trackingMatch));
        }

        // Date range filters
        if (createdFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom.atStartOfDay()));
        }
        if (createdTo != null) {
            predicates.add(cb.lessThan(root.get("createdAt"), createdTo.plusDays(1).atStartOfDay()));
        }

        return predicates;
    }

    /**
     * Get order detail by ID.
     */
    public OrderDetailDto getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return OrderDetailDto.fromEntity(order);
    }

    /**
     * Confirm an order.
     * Allowed transition: NEW -> CONFIRMED
     */
    public OrderDetailDto confirmOrder(Long orderId, User performedBy) {
        Order order = getOrderOrThrow(orderId);
        validateTransition(order, OrderStatus.CONFIRMED);

        order.setStatus(OrderStatus.CONFIRMED);
        addTimelineEntry(order, OrderStatus.CONFIRMED, null, "Order confirmed", performedBy);

        return OrderDetailDto.fromEntity(orderRepository.save(order));
    }

    /**
     * Ship an order.
     * Allowed transition: CONFIRMED -> SHIPPED
     */
    public OrderDetailDto shipOrder(Long orderId, OrderActionRequest.ShipOrderRequest request, User performedBy) {
        Order order = getOrderOrThrow(orderId);
        validateTransition(order, OrderStatus.SHIPPED);

        order.setStatus(OrderStatus.SHIPPED);
        order.setShippedAt(LocalDateTime.now());
        if (request != null) {
            order.setCarrier(request.getCarrier());
            order.setTrackingNumber(request.getTrackingNumber());
            order.setTrackingUrl(request.getTrackingUrl());
        }

        String note = request != null && request.getNote() != null ? request.getNote() : "Order shipped";
        addTimelineEntry(order, OrderStatus.SHIPPED, null, note, performedBy);

        return OrderDetailDto.fromEntity(orderRepository.save(order));
    }

    /**
     * Mark order as delivered.
     * Allowed transition: SHIPPED -> DELIVERED
     */
    public OrderDetailDto deliverOrder(Long orderId, User performedBy) {
        Order order = getOrderOrThrow(orderId);
        validateTransition(order, OrderStatus.DELIVERED);

        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        addTimelineEntry(order, OrderStatus.DELIVERED, null, "Order delivered", performedBy);

        return OrderDetailDto.fromEntity(orderRepository.save(order));
    }

    /**
     * Cancel an order.
     * Allowed from: NEW, CONFIRMED, SHIPPED (not from DELIVERED or already CANCELLED)
     */
    public OrderDetailDto cancelOrder(Long orderId, OrderActionRequest.CancelOrderRequest request, User performedBy) {
        Order order = getOrderOrThrow(orderId);

        if (order.isTerminal()) {
            throw new IllegalStateException("Cannot cancel order in terminal state: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());

        String note = request != null && request.getReason() != null
                ? "Order cancelled: " + request.getReason()
                : "Order cancelled";
        addTimelineEntry(order, OrderStatus.CANCELLED, null, note, performedBy);

        return OrderDetailDto.fromEntity(orderRepository.save(order));
    }

    /**
     * Refund an order.
     * Only allowed if paymentStatus is PAID.
     */
    public OrderDetailDto refundOrder(Long orderId, OrderActionRequest.RefundOrderRequest request, User performedBy) {
        Order order = getOrderOrThrow(orderId);

        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("Cannot refund order that is not PAID. Current status: " + order.getPaymentStatus());
        }

        order.setPaymentStatus(PaymentStatus.REFUNDED);

        String note = request != null && request.getReason() != null
                ? "Payment refunded: " + request.getReason()
                : "Payment refunded";
        addTimelineEntry(order, order.getStatus(), PaymentStatus.REFUNDED, note, performedBy);

        return OrderDetailDto.fromEntity(orderRepository.save(order));
    }

    /**
     * Update payment status.
     * PaymentStatus is independent of OrderStatus.
     */
    public OrderDetailDto updatePaymentStatus(Long orderId, PaymentStatus newStatus, User performedBy) {
        Order order = getOrderOrThrow(orderId);

        PaymentStatus oldStatus = order.getPaymentStatus();
        order.setPaymentStatus(newStatus);

        if (newStatus == PaymentStatus.PAID) {
            order.setPaidAt(LocalDateTime.now());
        }

        String note = "Payment status changed from " + oldStatus + " to " + newStatus;
        addTimelineEntry(order, order.getStatus(), newStatus, note, performedBy);

        return OrderDetailDto.fromEntity(orderRepository.save(order));
    }

    /**
     * Assign order to a user.
     */
    public OrderDetailDto assignOrder(Long orderId, Long assignedToUserId, User performedBy) {
        Order order = getOrderOrThrow(orderId);

        User previousAssignee = order.getAssignedTo();
        User newAssignee = null;

        if (assignedToUserId != null) {
            newAssignee = userRepository.findById(assignedToUserId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + assignedToUserId));
        }

        order.setAssignedTo(newAssignee);

        String note;
        if (newAssignee == null) {
            note = "Order unassigned" + (previousAssignee != null ? " from " + previousAssignee.getName() : "");
        } else if (previousAssignee == null) {
            note = "Order assigned to " + newAssignee.getName();
        } else {
            note = "Order reassigned from " + previousAssignee.getName() + " to " + newAssignee.getName();
        }
        addTimelineEntry(order, order.getStatus(), null, note, performedBy);

        return OrderDetailDto.fromEntity(orderRepository.save(order));
    }

    // Helper methods

    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    /**
     * Validate order status transitions.
     * Valid transitions:
     * - NEW -> CONFIRMED
     * - CONFIRMED -> SHIPPED
     * - SHIPPED -> DELIVERED
     * - Any non-terminal -> CANCELLED
     */
    private void validateTransition(Order order, OrderStatus newStatus) {
        OrderStatus current = order.getStatus();

        if (order.isTerminal()) {
            throw new IllegalStateException("Order is in terminal state: " + current);
        }

        boolean valid = switch (newStatus) {
            case CONFIRMED -> current == OrderStatus.NEW;
            case SHIPPED -> current == OrderStatus.CONFIRMED;
            case DELIVERED -> current == OrderStatus.SHIPPED;
            case CANCELLED -> !order.isTerminal();
            default -> false;
        };

        if (!valid) {
            throw new IllegalStateException(
                    "Invalid transition from " + current + " to " + newStatus);
        }
    }

    private void addTimelineEntry(Order order, OrderStatus status, PaymentStatus paymentStatus, String note, User performedBy) {
        OrderTimeline entry = new OrderTimeline();
        entry.setStatus(status);
        entry.setPaymentStatus(paymentStatus);
        entry.setNote(note);
        entry.setPerformedBy(performedBy);
        order.addTimelineEntry(entry);
    }
}
