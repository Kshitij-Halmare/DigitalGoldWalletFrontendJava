package com.example.Gold_Frontend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class VendorService {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://172.16.160.128:8080/vendors";

    public Map getVendorById(Integer id) {
        String url = BASE_URL + "/" + id + "?projection=vendorDetails";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return response.getBody();
    }

    public List<Map<String, Object>> getAllVendors(int page) {

        String url = BASE_URL + "?page=" + page + "&size=20&projection=vendorDetails";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map body = response.getBody();

        List<Map<String, Object>> vendors = (List<Map<String, Object>>) ((Map) body.get("_embedded")).get("vendors");

        // Extract ID from _links
        vendors.forEach(v -> {
            Map links = (Map) v.get("_links");
            Map self = (Map) links.get("self");
            String href = (String) self.get("href");
            String idVal = href.substring(href.lastIndexOf("/") + 1);
            v.put("id", idVal);
        });

        return vendors;
    }

    public int getTotalPages(int page) {
        String url = BASE_URL + "?page=" + page + "&size=20";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map body = response.getBody();

        return (int) ((Map) body.get("page")).get("totalPages");
    }

    public void addVendor(Object vendorDto) {
        restTemplate.postForEntity(BASE_URL, vendorDto, Object.class);
    }

    public void updateVendor(Integer id, Object vendorDto) {
        restTemplate.put(BASE_URL + "/" + id, vendorDto);
    }
}