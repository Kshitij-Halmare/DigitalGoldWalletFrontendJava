package com.example.Gold_Frontend.controller;

import com.example.Gold_Frontend.service.VendorBranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/vendors")
public class VendorBranchViewController {

    @Autowired
    private VendorBranchService service;

    @GetMapping("/{id}/branches")
    public String getBranches(@PathVariable Integer id, Model model) {

        Map vendor = service.getVendor(id);

        List<Map<String, Object>> branches = service.getBranches(id);

        Map<String, Set<String>> filters = service.extractFilters(branches);

        model.addAttribute("vendor", vendor);
        model.addAttribute("branches", branches);
        model.addAttribute("cities", filters.get("cities"));
        model.addAttribute("states", filters.get("states"));
        model.addAttribute("countries", filters.get("countries"));

        return "vendor-branches";
    }
}