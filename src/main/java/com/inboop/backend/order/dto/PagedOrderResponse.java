package com.inboop.backend.order.dto;

import java.util.List;

/**
 * Paginated response for order list.
 */
public class PagedOrderResponse {

    private List<OrderListItemDto> items;
    private int page;
    private int pageSize;
    private long totalItems;
    private int totalPages;

    public PagedOrderResponse() {}

    public PagedOrderResponse(List<OrderListItemDto> items, int page, int pageSize, long totalItems) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }

    public List<OrderListItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderListItemDto> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
