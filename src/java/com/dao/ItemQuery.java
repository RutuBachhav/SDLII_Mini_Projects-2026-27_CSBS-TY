package com.dao;

/**
 * The filters a browse/search request can carry. Kept as a small object so the
 * servlet, the DAO and the JSP all talk about the same thing.
 */
public class ItemQuery {

    private String keyword = "";
    private String type = "";        // LOST, FOUND or empty for both
    private int categoryId;          // 0 for all
    private String location = "";
    private String status = "OPEN";  // OPEN, RESOLVED, CLOSED or ALL
    private String dateFrom = "";
    private String dateTo = "";
    private String sort = "newest";  // newest or oldest
    private int page = 1;
    private int pageSize = 9;
    private int reporterId;          // 0 for everybody

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword == null ? "" : keyword.trim(); }

    public String getType() { return type; }
    public void setType(String type) { this.type = type == null ? "" : type.trim().toUpperCase(); }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location == null ? "" : location.trim(); }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status == null ? "" : status.trim().toUpperCase(); }

    public String getDateFrom() { return dateFrom; }
    public void setDateFrom(String dateFrom) { this.dateFrom = dateFrom == null ? "" : dateFrom.trim(); }

    public String getDateTo() { return dateTo; }
    public void setDateTo(String dateTo) { this.dateTo = dateTo == null ? "" : dateTo.trim(); }

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = "oldest".equals(sort) ? "oldest" : "newest"; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = Math.max(1, page); }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = Math.min(48, Math.max(1, pageSize)); }

    public int getReporterId() { return reporterId; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }

    public int getOffset() { return (page - 1) * pageSize; }

    /** True when the user actually narrowed anything down. */
    public boolean isFiltered() {
        return !keyword.isEmpty() || !type.isEmpty() || categoryId > 0
                || !location.isEmpty() || !dateFrom.isEmpty() || !dateTo.isEmpty()
                || !"OPEN".equals(status);
    }
}
