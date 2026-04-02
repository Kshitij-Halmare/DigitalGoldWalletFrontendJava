package com.example.Gold_Frontend.dto.VendorBranchesDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor  // Generates constructor with all fields
@NoArgsConstructor   // Needed for Jackson deserialization
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressDTO {
    private Integer addressId;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}