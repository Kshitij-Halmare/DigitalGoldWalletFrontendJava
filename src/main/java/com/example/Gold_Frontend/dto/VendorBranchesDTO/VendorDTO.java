package com.example.Gold_Frontend.dto.VendorBranchesDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VendorDTO {
    private Integer vendorId;
    private String vendorName;
}