package com.example.Gold_Frontend.controller;

import com.example.Gold_Frontend.dto.VendorDto;
import com.example.Gold_Frontend.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/vendors")
public class VendorViewController {

    @Autowired
    private VendorService vendorService;

    @GetMapping
    public String getVendors(
            @RequestParam(defaultValue = "0") int page, Model model) {

        List<Map<String, Object>> vendors = vendorService.getAllVendors(page);
        int totalPages = vendorService.getTotalPages(page);

        model.addAttribute("vendors", vendors);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "vendors";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("vendor", new VendorDto());
        return "add-vendor";
    }

    @PostMapping("/add")
    public String saveVendor(@ModelAttribute VendorDto vendorDto) {

        vendorService.addVendor(vendorDto);

        return "redirect:/vendors";
    }

    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable Integer id, Model model) {

        Map vendorMap = vendorService.getVendorById(id);

        VendorDto vendor = new VendorDto();

        vendor.setVendorName((String) vendorMap.get("vendorName"));
        vendor.setDescription((String) vendorMap.get("description"));
        vendor.setContactPersonName((String) vendorMap.get("contactPersonName"));
        vendor.setContactEmail((String) vendorMap.get("contactEmail"));
        vendor.setContactPhone((String) vendorMap.get("contactPhone"));
        vendor.setWebsiteUrl((String) vendorMap.get("websiteUrl"));


        if (vendorMap.get("totalGoldQuantity") != null)
            vendor.setTotalGoldQuantity(new BigDecimal(vendorMap.get("totalGoldQuantity").toString()));

        if (vendorMap.get("currentGoldPrice") != null)
            vendor.setCurrentGoldPrice(new BigDecimal(vendorMap.get("currentGoldPrice").toString()));

        vendor.setId(id);

        model.addAttribute("vendor", vendor);

        return "update-vendor";
    }

    @PostMapping("/update/{id}")
    public String updateVendor(@PathVariable Integer id,
                               @ModelAttribute VendorDto vendorDto) {

        vendorService.updateVendor(id, vendorDto);

        return "redirect:/vendors";
    }
}