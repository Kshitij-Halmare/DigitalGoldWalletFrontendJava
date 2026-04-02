package com.example.Gold_Frontend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class VendorBranchService {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:8080/vendors";

    public Map getVendor(Integer id) {
        ResponseEntity<Map> response =
                restTemplate.getForEntity(BASE_URL + "/" + id + "?projection=vendorDetails", Map.class);

        return response.getBody();
    }

    public List<Map<String, Object>> getBranches(Integer id) {

        String url =
                "http://localhost:8080/vendorBranches/search/findByVendorsVendorId?vendorId="
                        + id + "&projection=branchDetails";

        ResponseEntity<Map> response =
                restTemplate.getForEntity(url, Map.class);

        return (List<Map<String, Object>>)
                ((Map) response.getBody().get("_embedded")).get("vendorBranches");
    }

    public Map<String, Set<String>> extractFilters(List<Map<String, Object>> branches) {

        Set<String> cities = new HashSet<>();
        Set<String> states = new HashSet<>();
        Set<String> countries = new HashSet<>();

        for (Map b : branches) {
            Map address = (Map) b.get("address");

            if (address != null) {
                cities.add((String) address.get("city"));
                states.add((String) address.get("state"));
                countries.add((String) address.get("country"));
            }
        }

        Map<String, Set<String>> filters = new HashMap<>();
        filters.put("cities", cities);
        filters.put("states", states);
        filters.put("countries", countries);

        return filters;
    }
}