package com.example.Gold_Frontend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import jakarta.annotation.PostConstruct;

import java.util.*;

@Controller
public class VendorController {

    private final RestTemplate restTemplate;

    @Value("${backend.base-url}")
    private String backendBaseUrl;

    public VendorController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        System.out.println("✅ Backend URL is: [" + backendBaseUrl + "]");
    }

    // ====================== PAGE 1 — Vendors List ======================
    @GetMapping("/sjvendors")
    public String vendorsPage(Model model) {

        String url = backendBaseUrl + "/vendors?projection=vendorDetails";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/hal+json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class
            );

            Map body = response.getBody();
            Map embedded = (Map) body.get("_embedded");
            List<Map<String, Object>> vendors = embedded != null
                    ? (List<Map<String, Object>>) embedded.get("vendors")
                    : Collections.emptyList();

            // Extract vendorId from _links.self.href
            for (Map<String, Object> vendor : vendors) {
                try {
                    Map links = (Map) vendor.get("_links");
                    Map self = (Map) links.get("self");
                    String href = (String) self.get("href");
                    String id = href.replaceAll(".*/vendors/(\\d+).*", "$1");
                    vendor.put("vendorId", id);
                } catch (Exception e) {
                    System.out.println("⚠️ Could not extract vendorId: " + e.getMessage());
                }
            }

            model.addAttribute("vendors", vendors);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("vendors", Collections.emptyList());
            model.addAttribute("error", "Could not connect to backend: " + e.getMessage());
        }

        return "sjvendors";
    }

    // ====================== PAGE 2 — Transactions / Holdings ======================
    @GetMapping("/vendor-details")
    public String vendorDetailsPage(
            @RequestParam String id,
            @RequestParam String type,
            Model model) {

        model.addAttribute("vendorId", id);
        model.addAttribute("type", type);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/hal+json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            if ("virtual".equals(type)) {

                String url = backendBaseUrl +
                        "/virtual_gold_holdings/search/findByVendor?vendorId=" + id +
                        "&projection=virtualGoldHolding";

                ResponseEntity<Map> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, Map.class
                );

                Map embedded = (Map) response.getBody().get("_embedded");
                List<Map<String, Object>> holdings = embedded != null
                        ? (List<Map<String, Object>>) embedded.get("holdings")
                        : Collections.emptyList();

                // Flatten _embedded.user to top level for each holding
                for (Map<String, Object> h : holdings) {
                    try {
                        Map<String, Object> innerEmbedded = (Map<String, Object>) h.get("_embedded");
                        if (innerEmbedded != null) {
                            Map<String, Object> user = (Map<String, Object>) innerEmbedded.get("user");
                            h.put("user", user);
                        }
                    } catch (Exception ignored) {}
                }

                model.addAttribute("holdings", holdings);

            } else {

                String url = backendBaseUrl +
                        "/physicalgoldtransaction/search/findByVendor?vendorId=" + id +
                        "&projection=physicalGoldTransaction";

                ResponseEntity<Map> response = restTemplate.exchange(
                        url, HttpMethod.GET, entity, Map.class
                );

                Map embedded = (Map) response.getBody().get("_embedded");
                List<Map<String, Object>> transactions = embedded != null
                        ? (List<Map<String, Object>>) embedded.get("physicalGoldTransactionses")
                        : Collections.emptyList();

                // Flatten _embedded.user and _embedded.deliveryAddress to top level
                for (Map<String, Object> t : transactions) {
                    try {
                        Map<String, Object> innerEmbedded = (Map<String, Object>) t.get("_embedded");
                        if (innerEmbedded != null) {
                            Map<String, Object> user = (Map<String, Object>) innerEmbedded.get("user");
                            t.put("user", user);

                            Map<String, Object> address = (Map<String, Object>) innerEmbedded.get("deliveryAddress");
                            t.put("deliveryAddress", address);
                        }
                    } catch (Exception ignored) {}

                    // Extract transactionId from _links
                    try {
                        Map links = (Map) t.get("_links");
                        Map self = (Map) links.get("self");
                        String href = (String) self.get("href");
                        String txId = href.substring(href.lastIndexOf("/") + 1);
                        t.put("transactionId", txId);
                    } catch (Exception ignored) {}
                }

                model.addAttribute("transactions", transactions);
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Could not load data: " + e.getMessage());
        }

        return "vendor-details";
    }
}