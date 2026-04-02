package com.example.Gold_Frontend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PhysicalTransactionDTO {
    private Integer transactionId;
    private BigDecimal quantity;
    private BigDecimal amount;
    private String transactionType;
    private String transactionStatus;
    private String createdAt;
    private BranchDTO branch;

    @Data
    public static class BranchDTO {
        private AddressDTO address; // Uses your existing AddressDTO
        private VendorDTO vendors;
    }

    @Data
    public static class VendorDTO {
        private String vendorName;
        private String contactEmail;
        private String contactPhone;
        private String contactPersonName;
    }
}