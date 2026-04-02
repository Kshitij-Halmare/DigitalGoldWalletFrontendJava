package com.example.Gold_Frontend.controller;

import com.example.Gold_Frontend.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ActionController {

    @Autowired
    private MemberService memberService;

    // ── Deposit ──────────────────────────────────────────────────────────────────
    @PutMapping("/deposit/{id}")
    public ResponseEntity<Void> deposit(@PathVariable Integer id,
                                        @RequestParam BigDecimal amount) {
        memberService.depositGold(id, amount);
        return ResponseEntity.ok().build();
    }

    // ── Update user name / email ─────────────────────────────────────────────────
    @PatchMapping("/users/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Integer id,
                                           @RequestBody Map<String, String> body) {
        memberService.updateUser(id, body.get("name"), body.get("email"));
        return ResponseEntity.ok().build();
    }

    // ── Create new user (with address) ──────────────────────────────────────────
    @PostMapping("/users")
    public ResponseEntity<Void> createUser(@RequestBody Map<String, String> body) {
        memberService.createUserWithAddress(
                body.get("name"),
                body.get("email"),
                body.get("street"),
                body.get("city"),
                body.get("state"),
                body.get("postalCode"),
                body.get("country")
        );
        return ResponseEntity.ok().build();
    }
}