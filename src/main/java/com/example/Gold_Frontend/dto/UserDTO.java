package com.example.Gold_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class UserDTO {
    private Integer userId;
    private String name;
    private String email;
    private BigDecimal balance;
    private String createdAt;

    @JsonProperty("_links")
    private Map<String, Map<String, String>> links;

    // Custom Getter: If SDR hides userId, extract it from the HAL self link!
    public Integer getUserId() {
        if (this.userId != null) {
            return this.userId;
        }
        if (links != null && links.containsKey("self")) {
            String href = links.get("self").get("href");
            String[] parts = href.split("/");
            try {
                return Integer.parseInt(parts[parts.length - 1]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // Helper to get the address API URL from the HAL links
    public String getAddressUrl() {
        if (links != null && links.containsKey("address")) {
            return links.get("address").get("href");
        }
        return null;
    }
}