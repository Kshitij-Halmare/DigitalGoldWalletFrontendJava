package com.example.Gold_Frontend.controller;

import com.example.Gold_Frontend.dto.TransactionDTO;
import com.example.Gold_Frontend.dto.VendorBranchesDTO.VendorBranchesDTO;
import com.example.Gold_Frontend.dto.VendorBranchesDTO.VendorBranchFilterDto;
import com.example.Gold_Frontend.dto.VendorBranchesDTO.PagedBranchResult;
import com.example.Gold_Frontend.service.VendorBranchesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

@Controller
@RequestMapping("/vendorBranches")
@RequiredArgsConstructor
public class VendorBranchController {

    private final VendorBranchesService branchesService;

    // ==================== LIST ====================

    @GetMapping
    public String listBranches(
            @RequestParam(required = false) String citySearch,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String postalCode,
            @RequestParam(required = false) BigDecimal qtyMin,
            @RequestParam(required = false) BigDecimal qtyMax,
            @RequestParam(required = false, defaultValue = "none") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            Model model) {

        try {
            // ── 1. Use backend native pagination (Spring Data REST ?page=N&size=10) ──
            //    Filtering & sorting still happen client-side (no query params on backend)
            //    so we fetch the requested page directly.
            PagedBranchResult pagedResult = branchesService.findAllPaged(page);

            // ── 2. Apply in-memory filters + sort to the current page's content ──
            List<VendorBranchesDTO> pageContent = pagedResult.getContent();
            pageContent = filterBranches(pageContent, citySearch, city, state, country, postalCode, qtyMin, qtyMax);
            pageContent = sortBranches(pageContent, sort);

            // ── 3. Dropdown options — still built from the full dataset ──
            List<VendorBranchesDTO> allBranches = branchesService.findAll();

            List<String> allCities = allBranches.stream()
                    .map(b -> b.getAddress() != null ? b.getAddress().getCity() : null)
                    .filter(c -> c != null).distinct().sorted().collect(Collectors.toList());

            List<String> allStates = allBranches.stream()
                    .map(b -> b.getAddress() != null ? b.getAddress().getState() : null)
                    .filter(s -> s != null).distinct().sorted().collect(Collectors.toList());

            List<String> allCountries = allBranches.stream()
                    .map(b -> b.getAddress() != null ? b.getAddress().getCountry() : null)
                    .filter(c -> c != null).distinct().sorted().collect(Collectors.toList());

            // ── 4. Model attributes ──
            model.addAttribute("branchPage",    pageContent);
            model.addAttribute("pagedResult",   pagedResult);          // carries totalPages, currentPage etc.
            model.addAttribute("activeFilter",  determineActiveFilter(citySearch, city, state, country, postalCode, qtyMin, qtyMax));
            model.addAttribute("totalElements", pagedResult.getTotalElements());
            model.addAttribute("currentPage",   pagedResult.getCurrentPage());
            model.addAttribute("totalPages",    pagedResult.getTotalPages());
            model.addAttribute("totalGoldQty",  branchesService.sumQuantity(allBranches));
            model.addAttribute("allCities",     allCities);
            model.addAttribute("allStates",     allStates);
            model.addAttribute("allCountries",  allCountries);
            model.addAttribute("filter", new VendorBranchFilterDto(
                    citySearch, city, state, country, postalCode, qtyMin, qtyMax, sort));

        } catch (Exception e) {
            model.addAttribute("error", "Failed to load branches: " + e.getMessage());
        }

        return "vendorBranches/list";
    }

    // ==================== DETAIL ====================

    @GetMapping("/{id}")
    public String getBranchDetail(@PathVariable Integer id, Model model) {
        VendorBranchesDTO branch = branchesService.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found: " + id));
        model.addAttribute("branch", branch);

        List<TransactionDTO> transactions = branchesService.findTransactionsByBranchId(id);
        model.addAttribute("transactions", transactions);

        double totalQty    = transactions.stream().mapToDouble(t -> t.getQuantity()  != null ? t.getQuantity()  : 0).sum();
        double totalAmount = transactions.stream().mapToDouble(t -> t.getAmount()    != null ? t.getAmount()    : 0).sum();
        long   successful  = transactions.stream().filter(t -> "SUCCESS".equals(t.getTransactionStatus())).count();

        model.addAttribute("totalQty",    totalQty);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("successful",  successful);

        return "vendorBranches/detail";
    }

    // ==================== FORM POST ====================

    @PostMapping("/{id}")
    public String updateBranch(@PathVariable Integer id,
                               @RequestParam String quantity,
                               @RequestParam String street,
                               @RequestParam String city,
                               @RequestParam String state,
                               @RequestParam String postalCode,
                               @RequestParam String country,
                               RedirectAttributes redirectAttributes) {
        try {
            branchesService.updateBranch(id, quantity, street, city, state, postalCode, country);
            redirectAttributes.addFlashAttribute("successMsg", "Branch updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Update failed: " + e.getMessage());
        }
        return "redirect:/vendorBranches";
    }

    // ==================== HELPERS ====================

    private List<VendorBranchesDTO> filterBranches(
            List<VendorBranchesDTO> branches,
            String citySearch, String city, String state, String country,
            String postalCode, BigDecimal qtyMin, BigDecimal qtyMax) {

        return branches.stream().filter(b -> {
            if (isSet(citySearch)) {
                String bc = b.getAddress() != null ? b.getAddress().getCity() : "";
                if (!bc.toLowerCase().contains(citySearch.toLowerCase())) return false;
            }
            if (isSet(city)) {
                String bc = b.getAddress() != null ? b.getAddress().getCity() : "";
                if (!bc.equalsIgnoreCase(city)) return false;
            }
            if (isSet(state)) {
                String bs = b.getAddress() != null ? b.getAddress().getState() : "";
                if (!bs.equalsIgnoreCase(state)) return false;
            }
            if (isSet(country)) {
                String bc = b.getAddress() != null ? b.getAddress().getCountry() : "";
                if (!bc.equalsIgnoreCase(country)) return false;
            }
            if (isSet(postalCode)) {
                String bp = b.getAddress() != null ? b.getAddress().getPostalCode() : "";
                if (!bp.equalsIgnoreCase(postalCode)) return false;
            }
            if (qtyMin != null || qtyMax != null) {
                BigDecimal qty = b.getQuantity() != null ? b.getQuantity() : BigDecimal.ZERO;
                if (qtyMin != null && qty.compareTo(qtyMin) < 0) return false;
                if (qtyMax != null && qty.compareTo(qtyMax) > 0) return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    private List<VendorBranchesDTO> sortBranches(List<VendorBranchesDTO> branches, String sort) {
        if (sort == null || sort.equals("none")) return branches;
        Comparator<VendorBranchesDTO> cmp = Comparator.comparing(
                b -> b.getQuantity() != null ? b.getQuantity() : BigDecimal.ZERO);
        if ("desc".equals(sort)) cmp = cmp.reversed();
        return branches.stream().sorted(cmp).collect(Collectors.toList());
    }

    private String determineActiveFilter(String citySearch, String city, String state,
                                         String country, String postalCode,
                                         BigDecimal qtyMin, BigDecimal qtyMax) {
        if (isSet(citySearch)) return "City contains \"" + citySearch + "\"";
        if (isSet(postalCode)) return "Postal: " + postalCode;
        if (qtyMin != null && qtyMax != null) return "Qty " + qtyMin + " – " + qtyMax + " g";
        if (isSet(city) && isSet(state)) return city + ", " + state;
        if (isSet(city))    return "City: " + city;
        if (isSet(state))   return "State: " + state;
        if (isSet(country)) return "Country: " + country;
        return "All Branches";
    }

    private boolean isSet(String s) {
        return s != null && !s.isBlank();
    }

    public static class ErrorResponse {
        public String message;
        public ErrorResponse(String message) { this.message = message; }
    }
}