    package com.example.Gold_Frontend.dto.VendorBranchesDTO;

    import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;

    /**
     * DTO for Vendor Branches
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class VendorBranchesDTO {

        private Integer branchId;
        private BigDecimal quantity;
        private AddressDTO address;
        private VendorDTO vendors;
        private LocalDateTime createdAt;

        /**
         * Address DTO
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class AddressDTO {
            private Integer addressId;
            private String street;
            private String city;
            private String state;
            private String postalCode;
            private String country;

            // Additional convenience constructor without addressId
            public AddressDTO(String street, String city, String state, String postalCode, String country) {
                this.street = street;
                this.city = city;
                this.state = state;
                this.postalCode = postalCode;
                this.country = country;
            }
        }

        /**
         * Vendor DTO
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class VendorDTO {
            private Integer vendorId;
            private String vendorName;
        }
    }