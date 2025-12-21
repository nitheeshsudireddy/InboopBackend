package com.inboop.backend.order.dto;

import com.inboop.backend.lead.enums.ChannelType;
import com.inboop.backend.order.entity.Order;
import com.inboop.backend.order.entity.OrderItem;
import com.inboop.backend.order.entity.OrderTimeline;
import com.inboop.backend.order.enums.OrderStatus;
import com.inboop.backend.order.enums.PaymentMethod;
import com.inboop.backend.order.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for order detail view (full order information).
 */
public class OrderDetailDto {

    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerHandle;
    private ChannelType channel;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private String currency;
    private Long assignedToUserId;
    private String assignedToUserName;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

    // Detail-specific fields
    private Long conversationId;
    private Long leadId;
    private List<OrderItemDto> items = new ArrayList<>();
    private ShippingAddressDto shippingAddress;
    private TrackingInfoDto tracking;
    private String notes;
    private List<OrderTimelineEventDto> timeline = new ArrayList<>();

    public OrderDetailDto() {}

    public static OrderDetailDto fromEntity(Order order) {
        OrderDetailDto dto = new OrderDetailDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setCustomerName(order.getCustomerName());
        dto.setCustomerHandle(order.getCustomerHandle());
        dto.setChannel(order.getChannel());
        dto.setOrderStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCurrency(order.getCurrency());
        if (order.getAssignedTo() != null) {
            dto.setAssignedToUserId(order.getAssignedTo().getId());
            dto.setAssignedToUserName(order.getAssignedTo().getName());
        }
        dto.setCreatedAt(order.getCreatedAt());
        dto.setLastUpdatedAt(order.getUpdatedAt());

        // Detail fields
        if (order.getConversation() != null) {
            dto.setConversationId(order.getConversation().getId());
        }
        if (order.getLead() != null) {
            dto.setLeadId(order.getLead().getId());
        }
        dto.setNotes(order.getNotes());

        // Items
        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                    .map(OrderItemDto::fromEntity)
                    .collect(Collectors.toList()));
        }

        // Shipping address
        dto.setShippingAddress(ShippingAddressDto.fromEntity(order));

        // Tracking
        dto.setTracking(TrackingInfoDto.fromEntity(order));

        // Timeline
        if (order.getTimeline() != null) {
            dto.setTimeline(order.getTimeline().stream()
                    .map(OrderTimelineEventDto::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerHandle() {
        return customerHandle;
    }

    public void setCustomerHandle(String customerHandle) {
        this.customerHandle = customerHandle;
    }

    public ChannelType getChannel() {
        return channel;
    }

    public void setChannel(ChannelType channel) {
        this.channel = channel;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

    public String getAssignedToUserName() {
        return assignedToUserName;
    }

    public void setAssignedToUserName(String assignedToUserName) {
        this.assignedToUserName = assignedToUserName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }

    public ShippingAddressDto getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddressDto shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public TrackingInfoDto getTracking() {
        return tracking;
    }

    public void setTracking(TrackingInfoDto tracking) {
        this.tracking = tracking;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<OrderTimelineEventDto> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<OrderTimelineEventDto> timeline) {
        this.timeline = timeline;
    }

    // Nested DTOs
    public static class OrderItemDto {
        private Long id;
        private String name;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;

        public static OrderItemDto fromEntity(OrderItem item) {
            OrderItemDto dto = new OrderItemDto();
            dto.setId(item.getId());
            dto.setName(item.getName());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setLineTotal(item.getTotalPrice());
            return dto;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getLineTotal() {
            return lineTotal;
        }

        public void setLineTotal(BigDecimal lineTotal) {
            this.lineTotal = lineTotal;
        }
    }

    public static class ShippingAddressDto {
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String postalCode;
        private String country;

        public static ShippingAddressDto fromEntity(Order order) {
            ShippingAddressDto dto = new ShippingAddressDto();
            dto.setLine1(order.getShippingAddressLine1());
            dto.setLine2(order.getShippingAddressLine2());
            dto.setCity(order.getShippingCity());
            dto.setState(order.getShippingState());
            dto.setPostalCode(order.getShippingPostalCode());
            dto.setCountry(order.getShippingCountry());
            return dto;
        }

        public String getLine1() {
            return line1;
        }

        public void setLine1(String line1) {
            this.line1 = line1;
        }

        public String getLine2() {
            return line2;
        }

        public void setLine2(String line2) {
            this.line2 = line2;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }

    public static class TrackingInfoDto {
        private String carrier;
        private String trackingId;
        private String trackingUrl;

        public static TrackingInfoDto fromEntity(Order order) {
            if (order.getTrackingNumber() == null && order.getCarrier() == null) {
                return null;
            }
            TrackingInfoDto dto = new TrackingInfoDto();
            dto.setCarrier(order.getCarrier());
            dto.setTrackingId(order.getTrackingNumber());
            dto.setTrackingUrl(order.getTrackingUrl());
            return dto;
        }

        public String getCarrier() {
            return carrier;
        }

        public void setCarrier(String carrier) {
            this.carrier = carrier;
        }

        public String getTrackingId() {
            return trackingId;
        }

        public void setTrackingId(String trackingId) {
            this.trackingId = trackingId;
        }

        public String getTrackingUrl() {
            return trackingUrl;
        }

        public void setTrackingUrl(String trackingUrl) {
            this.trackingUrl = trackingUrl;
        }
    }

    public static class OrderTimelineEventDto {
        private Long id;
        private String type; // STATUS_CHANGE, PAYMENT_UPDATE, etc.
        private String description;
        private String actorType; // USER, SYSTEM
        private Long actorUserId;
        private String actorUserName;
        private String source; // MANUAL, API, WEBHOOK
        private LocalDateTime createdAt;

        public static OrderTimelineEventDto fromEntity(OrderTimeline event) {
            OrderTimelineEventDto dto = new OrderTimelineEventDto();
            dto.setId(event.getId());
            dto.setType("STATUS_CHANGE");
            dto.setDescription(buildDescription(event));
            if (event.getPerformedBy() != null) {
                dto.setActorType("USER");
                dto.setActorUserId(event.getPerformedBy().getId());
                dto.setActorUserName(event.getPerformedBy().getName());
            } else {
                dto.setActorType("SYSTEM");
            }
            dto.setSource("MANUAL");
            dto.setCreatedAt(event.getCreatedAt());
            return dto;
        }

        private static String buildDescription(OrderTimeline event) {
            StringBuilder desc = new StringBuilder();
            desc.append("Status changed to ").append(event.getStatus());
            if (event.getPaymentStatus() != null) {
                desc.append(", Payment: ").append(event.getPaymentStatus());
            }
            if (event.getNote() != null && !event.getNote().isEmpty()) {
                desc.append(" - ").append(event.getNote());
            }
            return desc.toString();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getActorType() {
            return actorType;
        }

        public void setActorType(String actorType) {
            this.actorType = actorType;
        }

        public Long getActorUserId() {
            return actorUserId;
        }

        public void setActorUserId(Long actorUserId) {
            this.actorUserId = actorUserId;
        }

        public String getActorUserName() {
            return actorUserName;
        }

        public void setActorUserName(String actorUserName) {
            this.actorUserName = actorUserName;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}
