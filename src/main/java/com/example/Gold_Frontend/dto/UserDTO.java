package com.example.Gold_Frontend.dto;

import lombok.Data;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@Data
public class UserDTO {
    private Integer userId; // Changed to Integer
    private String name;
    private String email;
    private BigDecimal balance; // Changed to BigDecimal for precision
    private String createdAt;

    @JsonProperty("_links")
    private Map<String, Map<String, String>> links;

    // Helper to get the address API URL from the HAL links
    public String getAddressUrl() {
        if (links != null && links.containsKey("address")) {
            return links.get("address").get("href");
        }
        return null;
    }
}