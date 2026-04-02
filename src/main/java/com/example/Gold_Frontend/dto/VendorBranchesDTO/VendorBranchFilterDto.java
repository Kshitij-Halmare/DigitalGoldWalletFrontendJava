package com.example.Gold_Frontend.dto.VendorBranchesDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for branch filter criteria
 * Used to pass filter parameters through the view layer and maintain state across requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorBranchFilterDto {

    // Text search
    private String citySearch;      // Partial city name search

    // Location filters
    private String city;            // Exact city match
    private String state;           // Exact state match
    private String country;         // Exact country match
    private String postalCode;      // Exact postal code match

    // Quantity filters
    private BigDecimal qtyMin;      // Minimum quantity threshold
    private BigDecimal qtyMax;      // Maximum quantity threshold

    // Sorting
    private String sort;            // Sort order: "asc", "desc", "none"

    /**
     * Checks if any filter is active (not null/empty)
     */
    public boolean hasActiveFilters() {
        return (citySearch != null && !citySearch.isEmpty()) ||
                (city != null && !city.isEmpty()) ||
                (state != null && !state.isEmpty()) ||
                (country != null && !country.isEmpty()) ||
                (postalCode != null && !postalCode.isEmpty()) ||
                (qtyMin != null) ||
                (qtyMax != null);
    }

    /**
     * Returns a human-readable description of active filters
     */
    public String getFilterSummary() {
        StringBuilder summary = new StringBuilder();

        if (citySearch != null && !citySearch.isEmpty()) {
            summary.append("City: ").append(citySearch).append(" | ");
        }
        if (city != null && !city.isEmpty()) {
            summary.append("City (exact): ").append(city).append(" | ");
        }
        if (state != null && !state.isEmpty()) {
            summary.append("State: ").append(state).append(" | ");
        }
        if (country != null && !country.isEmpty()) {
            summary.append("Country: ").append(country).append(" | ");
        }
        if (postalCode != null && !postalCode.isEmpty()) {
            summary.append("Postal: ").append(postalCode).append(" | ");
        }
        if (qtyMin != null || qtyMax != null) {
            summary.append("Qty: ");
            if (qtyMin != null) summary.append(qtyMin);
            else summary.append("0");
            summary.append(" - ");
            if (qtyMax != null) summary.append(qtyMax);
            else summary.append("∞");
            summary.append(" | ");
        }

        if (summary.length() > 0) {
            summary.setLength(summary.length() - 3); // Remove trailing " | "
            return summary.toString();
        }
        return "No filters applied";
    }
}