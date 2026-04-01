package com.example.Gold_Frontend.controller;

import com.example.Gold_Frontend.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
public class ActionController {

    @Autowired
    private MemberService memberService;

    @PutMapping("/deposit/{id}")
    public ResponseEntity<Void> deposit(@PathVariable Integer id, @RequestParam BigDecimal amount) {
        memberService.depositGold(id, amount);
        return ResponseEntity.ok().build();
    }
}