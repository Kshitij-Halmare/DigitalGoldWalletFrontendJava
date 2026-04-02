package com.example.Gold_Frontend.dto.VendorBranchesDTO;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class updateVendorBranchesDTO {
    private BigDecimal quantity; // optional
    private AddressDTO address;  // nested address object

    @Data
    public static class AddressDTO {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }
}