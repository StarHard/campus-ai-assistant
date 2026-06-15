package com.campus.ai.dto;
import java.io.Serializable;

public class PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String sortField;
    private String sortOrder = "asc";
    public Integer getPageNum() { return pageNum; } public void setPageNum(Integer v) { this.pageNum = v; }
    public Integer getPageSize() { return pageSize; } public void setPageSize(Integer v) { this.pageSize = v; }
    public String getSortField() { return sortField; } public void setSortField(String v) { this.sortField = v; }
    public String getSortOrder() { return sortOrder; } public void setSortOrder(String v) { this.sortOrder = v; }
}
