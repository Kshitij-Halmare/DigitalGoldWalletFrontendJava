//package com.example.Gold_Frontend.controller;
//
//import com.example.Gold_Frontend.dto.AddressDTO;
//import com.example.Gold_Frontend.dto.PhysicalTransactionDTO;
//import com.example.Gold_Frontend.dto.UserDTO;
//import com.example.Gold_Frontend.dto.UserResponseDTO;
//import com.example.Gold_Frontend.service.MemberService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@Controller
//public class MemberController {
//
//    @Autowired
//    private MemberService memberService;
//
//    @GetMapping("/members")
//    public String listMembers(Model model, @RequestParam(defaultValue = "0") int page) {
//        UserResponseDTO response = memberService.getUsers(page, 20);
//
//        // Use the new getter name 'getEmbedded()'
//        if (response != null && response.getEmbedded() != null) {
//            model.addAttribute("users", response.getEmbedded().getUserses());
//            model.addAttribute("currentPage", response.getPage().getNumber());
//            model.addAttribute("totalPages", response.getPage().getTotalPages());
//        } else {
//            model.addAttribute("users", java.util.Collections.emptyList());
//            model.addAttribute("currentPage", 0);
//            model.addAttribute("totalPages", 0);
//        }
//        return "members";
//    }
//
//    @GetMapping("/members/{id}")
//    public String viewMemberProfile(@PathVariable("id") Integer id, Model model) {
//        // 1. Fetch Basic Info
//        UserDTO user = memberService.getUserById(Integer.valueOf(id));
//        model.addAttribute("user", user);
//        model.addAttribute("address", memberService.getAddressByUrl(user.getAddressUrl()));
//
//        // 2. Fetch Physical Assets
//        List<PhysicalTransactionDTO> assets = memberService.getPhysicalTransactions(id);
//        model.addAttribute("assets", assets);
//
//        // 3. Calculate Stats for the "Cards"
//        BigDecimal totalQty = assets.stream()
//                .map(a -> a.getQuantity() != null ? a.getQuantity() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        BigDecimal totalVal = assets.stream()
//                .map(a -> a.getAmount() != null ? a.getAmount() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        model.addAttribute("totalQty", totalQty);
//        model.addAttribute("totalVal", totalVal);
//        model.addAttribute("totalOrders", assets.size());
//
//        return "member-details";
//    }
//}
package com.example.Gold_Frontend.controller;

import com.example.Gold_Frontend.dto.*;
import com.example.Gold_Frontend.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MemberController {

    @Autowired
    private MemberService memberService;

    // Helper to keep user on the page they were viewing (Physical vs Virtual)
    private String getRedirectUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return (referer != null && referer.contains("virtual-members")) ? "redirect:/virtual-members" : "redirect:/members";
    }

    // Ensures the Thymeleaf model ALWAYS has empty maps so it never crashes on load
    private void ensureFormState(Model model) {
        if (!model.containsAttribute("addForm")) model.addAttribute("addForm", new HashMap<String, String>());
        if (!model.containsAttribute("addErrors")) model.addAttribute("addErrors", new HashMap<String, String>());
        if (!model.containsAttribute("editForm")) model.addAttribute("editForm", new HashMap<String, String>());
        if (!model.containsAttribute("editErrors")) model.addAttribute("editErrors", new HashMap<String, String>());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 1. MASTER TABLES
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/members")
    public String listMembers(Model model, @RequestParam(defaultValue = "0") int page) {
        UserResponseDTO response = memberService.getUsers(page, 20);
        populateMembersModel(model, response);
        model.addAttribute("detailUrlPrefix", "/members/");
        ensureFormState(model);
        return "members";
    }

    @GetMapping("/virtual-members")
    public String listVirtualMembers(Model model, @RequestParam(defaultValue = "0") int page) {
        UserResponseDTO response = memberService.getUsers(page, 20);
        populateMembersModel(model, response);
        model.addAttribute("detailUrlPrefix", "/virtual-members/");
        ensureFormState(model);
        return "members";
    }

    private void populateMembersModel(Model model, UserResponseDTO response) {
        if (response != null && response.getEmbedded() != null) {
            model.addAttribute("users", response.getEmbedded().getUserses());
            model.addAttribute("currentPage", response.getPage().getNumber());
            model.addAttribute("totalPages", response.getPage().getTotalPages());
        } else {
            model.addAttribute("users", java.util.Collections.emptyList());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 2. DETAIL PAGES
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/members/{id}")
    public String viewMemberProfile(@PathVariable("id") Integer id, Model model) {
        UserDTO user = memberService.getUserById(Long.valueOf(id));
        model.addAttribute("user", user);
        model.addAttribute("address", memberService.getAddressByUrl(user.getAddressUrl()));

        List<PhysicalTransactionDTO> assets = memberService.getPhysicalTransactions(id);
        model.addAttribute("assets", assets);

        BigDecimal totalQty = assets.stream().map(a -> a.getQuantity() != null ? a.getQuantity() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalVal = assets.stream().map(a -> a.getAmount() != null ? a.getAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalQty", totalQty);
        model.addAttribute("totalVal", totalVal);
        model.addAttribute("totalOrders", assets.size());

        return "member-details";
    }

    @GetMapping("/virtual-members/{id}")
    public String viewMemberDashboard(@PathVariable("id") Integer id, Model model) {
        UserDTO user = memberService.getUserById(Long.valueOf(id));
        model.addAttribute("user", user);
        model.addAttribute("address", memberService.getAddressByUrl(user.getAddressUrl()));

        String holdingsUrl = null;
        String txnsUrl = null;
        if (user.getLinks() != null) {
            if (user.getLinks().containsKey("virtualGoldHoldings")) holdingsUrl = user.getLinks().get("virtualGoldHoldings").get("href");
            if (user.getLinks().containsKey("transactions")) txnsUrl = user.getLinks().get("transactions").get("href");
        }

        List<DashboardHoldingDTO> holdings = memberService.getDashboardHoldings(holdingsUrl);
        BigDecimal totalGold = holdings.stream().map(h -> h.getQuantity() != null ? h.getQuantity() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalValue = holdings.stream().map(h -> h.getTotalValue() != null ? h.getTotalValue() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DashboardTransactionDTO> transactions = memberService.getDashboardTransactions(txnsUrl);

        model.addAttribute("holdings", holdings);
        model.addAttribute("totalGold", totalGold);
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("transactions", transactions);

        return "user-dashboard";
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 3. ADD / EDIT FORMS
    // ──────────────────────────────────────────────────────────────────────────
    @PostMapping("/members/{id}/edit")
    public String submitEdit(@PathVariable Integer id, @RequestParam String name, @RequestParam String email, @RequestParam String originalEmail,
                             HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Map<String, String> errors = new HashMap<>();

        if (name == null || name.isBlank() || !name.matches("^[A-Za-z\\s]+$")) errors.put("name", "Name must contain letters only");
        if (email == null || email.isBlank() || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) errors.put("email", "Invalid email address");
        if (!email.equalsIgnoreCase(originalEmail) && memberService.isEmailTakenByOther(email, id)) errors.put("email", "Email already registered");

        Map<String, String> form = new HashMap<>();
        form.put("id", String.valueOf(id)); form.put("name", name); form.put("email", email); form.put("originalEmail", originalEmail);

        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("modal", "edit");
            redirectAttributes.addFlashAttribute("editErrors", errors);
            redirectAttributes.addFlashAttribute("editForm", form);
            return getRedirectUrl(request);
        }

        try {
            memberService.updateUser(id, name.trim(), email.trim());
            redirectAttributes.addFlashAttribute("success", "Member updated successfully");
        } catch (Exception e) {
            errors.put("email", "Update failed: " + e.getMessage());
            redirectAttributes.addFlashAttribute("modal", "edit");
            redirectAttributes.addFlashAttribute("editErrors", errors);
            redirectAttributes.addFlashAttribute("editForm", form);
        }

        return getRedirectUrl(request);
    }

    @PostMapping("/members/new")
    public String submitAdd(@RequestParam String name, @RequestParam String email, @RequestParam String street, @RequestParam String city,
                            @RequestParam String state, @RequestParam String postalCode, @RequestParam String country,
                            HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Map<String, String> errors = new HashMap<>();

        if (name == null || name.isBlank() || !name.matches("^[A-Za-z\\s]+$")) errors.put("name", "Letters only");
        if (email == null || email.isBlank() || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) errors.put("email", "Invalid email");
        if (street == null || street.isBlank()) errors.put("street", "Required");
        if (city == null || city.isBlank() || !city.matches("^[A-Za-z\\s]+$")) errors.put("city", "Letters only");
        if (state == null || state.isBlank() || !state.matches("^[A-Za-z\\s]+$")) errors.put("state", "Letters only");
        if (postalCode == null || postalCode.isBlank() || !postalCode.matches("^[0-9]+$")) errors.put("postalCode", "Numbers only");
        if (country == null || country.isBlank() || !country.matches("^[A-Za-z\\s]+$")) errors.put("country", "Letters only");
        if (!errors.containsKey("email") && memberService.isEmailTakenByOther(email, null)) errors.put("email", "Email already registered");

        Map<String, String> formState = new HashMap<>();
        formState.put("name", name); formState.put("email", email); formState.put("street", street);
        formState.put("city", city); formState.put("state", state); formState.put("postalCode", postalCode); formState.put("country", country);

        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("modal", "add");
            redirectAttributes.addFlashAttribute("addErrors", errors);
            redirectAttributes.addFlashAttribute("addForm", formState);
            return getRedirectUrl(request);
        }

        try {
            memberService.createUserWithAddress(name.trim(), email.trim(), street.trim(), city.trim(), state.trim(), postalCode.trim(), country.trim());
            redirectAttributes.addFlashAttribute("success", "Member created successfully");
        } catch (Exception e) {
            errors.put("email", "Failed to create member: " + e.getMessage());
            redirectAttributes.addFlashAttribute("modal", "add");
            redirectAttributes.addFlashAttribute("addErrors", errors);
            redirectAttributes.addFlashAttribute("addForm", formState);
        }

        return getRedirectUrl(request);
    }
}