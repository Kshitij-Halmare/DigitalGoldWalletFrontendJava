package com.example.Gold_Frontend.controller;

import com.example.Gold_Frontend.dto.AddressDTO;
import com.example.Gold_Frontend.dto.PhysicalTransactionDTO;
import com.example.Gold_Frontend.dto.UserDTO;
import com.example.Gold_Frontend.dto.UserResponseDTO;
import com.example.Gold_Frontend.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/members")
    public String listMembers(Model model, @RequestParam(defaultValue = "0") int page) {
        UserResponseDTO response = memberService.getUsers(page, 20);

        // Use the new getter name 'getEmbedded()'
        if (response != null && response.getEmbedded() != null) {
            model.addAttribute("users", response.getEmbedded().getUserses());
            model.addAttribute("currentPage", response.getPage().getNumber());
            model.addAttribute("totalPages", response.getPage().getTotalPages());
        } else {
            model.addAttribute("users", java.util.Collections.emptyList());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
        }
        return "members";
    }

    @GetMapping("/members/{id}")
    public String viewMemberProfile(@PathVariable("id") Integer id, Model model) {
        // 1. Fetch Basic Info
        UserDTO user = memberService.getUserById(Long.valueOf(id));
        model.addAttribute("user", user);
        model.addAttribute("address", memberService.getAddressByUrl(user.getAddressUrl()));

        // 2. Fetch Physical Assets
        List<PhysicalTransactionDTO> assets = memberService.getPhysicalTransactions(id);
        model.addAttribute("assets", assets);

        // 3. Calculate Stats for the "Cards"
        BigDecimal totalQty = assets.stream()
                .map(a -> a.getQuantity() != null ? a.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVal = assets.stream()
                .map(a -> a.getAmount() != null ? a.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalQty", totalQty);
        model.addAttribute("totalVal", totalVal);
        model.addAttribute("totalOrders", assets.size());

        return "member-details";
    }
}
