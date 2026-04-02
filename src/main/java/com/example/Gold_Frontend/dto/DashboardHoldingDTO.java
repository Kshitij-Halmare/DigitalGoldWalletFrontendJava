package com.example.Gold_Frontend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardHoldingDTO {
    private String vendorName;
    private BigDecimal quantity;
    private BigDecimal currentPrice;
    private BigDecimal totalValue;
}