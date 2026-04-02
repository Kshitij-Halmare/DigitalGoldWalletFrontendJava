package com.example.Gold_Frontend.dto.VendorBranchesDTO;

import java.util.List;

/**
 * Carries a single page of VendorBranchesDTO plus the page metadata
 * returned by Spring Data REST in the "page" object of every HAL response.
 *
 * Spring Data REST page metadata shape:
 * {
 *   "page": {
 *     "size": 10,
 *     "totalElements": 47,
 *     "totalPages": 5,
 *     "number": 0          ← 0-based current page
 *   }
 * }
 */
public class PagedBranchResult {

    private final List<VendorBranchesDTO> content;
    private final int totalElements;
    private final int totalPages;
    private final int currentPage;   // 0-based (matches Spring Data REST)
    private final int pageSize;

    public PagedBranchResult(List<VendorBranchesDTO> content,
                             int totalElements,
                             int totalPages,
                             int currentPage,
                             int pageSize) {
        this.content       = content;
        this.totalElements = totalElements;
        this.totalPages    = totalPages;
        this.currentPage   = currentPage;
        this.pageSize      = pageSize;
    }

    public static PagedBranchResult empty() {
        return new PagedBranchResult(List.of(), 0, 0, 0, 10);
    }

    // ── derived helpers used in the controller / template ──────────────────

    /** True when a "previous" page exists. */
    public boolean isHasPrevious() { return currentPage > 0; }

    /** True when a "next" page exists. */
    public boolean isHasNext() { return currentPage < totalPages - 1; }

    public int getPreviousPage() { return currentPage - 1; }
    public int getNextPage()     { return currentPage + 1; }

    /**
     * Returns a window of page numbers to show in the pagination bar.
     * Always includes first (0) and last page, plus up to 2 neighbours
     * of the current page, with -1 used as a "…" sentinel.
     */
    public List<Integer> getPageWindow() {
        List<Integer> pages = new java.util.ArrayList<>();
        if (totalPages <= 7) {
            for (int i = 0; i < totalPages; i++) pages.add(i);
            return pages;
        }
        // always show first
        pages.add(0);
        if (currentPage > 2)        pages.add(-1);   // left ellipsis
        for (int i = Math.max(1, currentPage - 1);
             i <= Math.min(totalPages - 2, currentPage + 1); i++) {
            pages.add(i);
        }
        if (currentPage < totalPages - 3) pages.add(-1); // right ellipsis
        pages.add(totalPages - 1);
        return pages;
    }

    // ── getters ────────────────────────────────────────────────────────────

    public List<VendorBranchesDTO> getContent()       { return content; }
    public int getTotalElements()                      { return totalElements; }
    public int getTotalPages()                         { return totalPages; }
    public int getCurrentPage()                        { return currentPage; }
    public int getPageSize()                           { return pageSize; }
}