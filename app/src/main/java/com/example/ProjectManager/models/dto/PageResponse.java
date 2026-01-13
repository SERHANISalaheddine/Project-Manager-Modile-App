package com.example.ProjectManager.models.dto;

/**
 * Response wrapper for paginated API responses.
 */
public class PageResponse<T> {
    private java.util.List<T> content;
    private int page;
    private int number;  // Spring uses 'number' for current page
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public java.util.List<T> getContent() {
        return content;
    }

    public void setContent(java.util.List<T> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
    
    public int getNumber() {
        return number;
    }
    
    public void setNumber(int number) {
        this.number = number;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}
