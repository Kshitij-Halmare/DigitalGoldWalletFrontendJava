package com.example.Gold_Frontend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardTransactionDTO {
    private String vendorName;
    private BigDecimal quantity;
    private BigDecimal amount;
    private String transactionType;
    private String createdAt;
}
