package com.example.Gold_Frontend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VendorDto {
    private Integer id;
    private String vendorName;
    private String description;
    private BigDecimal totalGoldQuantity;
    private BigDecimal  currentGoldPrice;
    private String contactPersonName;
    private String contactEmail;
    private String contactPhone;
    private String websiteUrl;
}
