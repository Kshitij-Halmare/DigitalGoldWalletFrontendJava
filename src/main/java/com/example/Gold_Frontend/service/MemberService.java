//package com.example.Gold_Frontend.service;
//
//import com.example.Gold_Frontend.dto.AddressDTO;
//import com.example.Gold_Frontend.dto.PhysicalTransactionDTO;
//import com.example.Gold_Frontend.dto.UserDTO;
//import com.example.Gold_Frontend.dto.UserResponseDTO;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestClient;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class MemberService {
//
//    private final RestClient restClient;
//
//    public MemberService(@Value("${backend.base-url}") String baseUrl) {
//        this.restClient = RestClient.create(baseUrl);
//    }
//
//    // ── Read ─────────────────────────────────────────────────────────────────────
//
//    public UserResponseDTO getUsers(int page, int size) {
//        return restClient.get()
//                .uri("/users?page={page}&size={size}", page, size)
//                .retrieve()
//                .body(UserResponseDTO.class);
//    }
//
//    public UserDTO getUserById(Integer id) {
//        return restClient.get()
//                .uri("/users/{id}", id)
//                .retrieve()
//                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
//                    throw new RuntimeException("User not found");
//                })
//                .body(UserDTO.class);
//    }
//
//    public AddressDTO getAddressByUrl(String url) {
//        if (url == null) return null;
//        try {
//            return restClient.get()
//                    .uri(url)
//                    .retrieve()
//                    .body(AddressDTO.class);
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    public List<PhysicalTransactionDTO> getPhysicalTransactions(Integer userId) {
//        return restClient.get()
//                .uri("/user/physical/{id}", userId)
//                .retrieve()
//                .body(new ParameterizedTypeReference<>() {});
//    }
//
//    // ── Write ────────────────────────────────────────────────────────────────────
//
//    public void depositGold(Integer userId, BigDecimal amount) {
//        restClient.put()
//                .uri(builder -> builder
//                        .path("/user/deposit/{id}")
//                        .queryParam("amount", amount)
//                        .build(userId))
//                .retrieve()
//                .toBodilessEntity();
//    }
//
//    /**
//     * PATCH /users/{id} — update name and/or email.
//     * Spring Data REST accepts a PATCH with a partial JSON body.
//     */
//    public void updateUser(Integer userId, String name, String email) {
//        restClient.patch()
//                .uri("/users/{id}", userId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Map.of("name", name, "email", email))
//                .retrieve()
//                .toBodilessEntity();
//    }
//
//    /**
//     * Create a new user together with their address in one service call.
//     *
//     * Flow (mirrors your original JS):
//     * 1. POST /addresses  → get address HAL href
//     * 2. POST /users      → get user HAL self href
//     * 3. PUT  /users/{id}/address  (text/uri-list) → link address to user
//     */
//    public void createUserWithAddress(String name, String email,
//                                      String street, String city,
//                                      String state, String postalCode,
//                                      String country) {
//
//        // 1. Create address and get URI from Location header
//        String addrHref = restClient.post()
//                .uri("/addresses")
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(new AddressDTO(street, city, state, postalCode, country))
//                .retrieve()
//                .toEntity(Void.class) // We don't need a body, just the headers
//                .getHeaders()
//                .getLocation()
//                .toString();
//
//        // 2. Create user and get URI from Location header
//        String userSelfHref = restClient.post()
//                .uri("/users")
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Map.of("name", name, "email", email))
//                .retrieve()
//                .toEntity(Void.class)
//                .getHeaders()
//                .getLocation()
//                .toString();
//
//        // 3. Link address → user
//        restClient.put()
//                .uri(userSelfHref + "/address")
//                .contentType(MediaType.parseMediaType("text/uri-list"))
//                .body(addrHref)
//                .retrieve()
//                .toBodilessEntity();
//    }
//}
package com.example.Gold_Frontend.service;

import com.example.Gold_Frontend.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class MemberService {

    private final RestClient restClient;

    public MemberService(@Value("${backend.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public UserResponseDTO getUsers(int page, int size) {
        return restClient.get().uri("/users?page={page}&size={size}", page, size).retrieve().body(UserResponseDTO.class);
    }

    public UserDTO getUserById(Long id) {
        return restClient.get().uri("/users/{id}", id).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> { throw new RuntimeException("User not found"); })
                .body(UserDTO.class);
    }

    public AddressDTO getAddressByUrl(String url) {
        if (url == null) return null;
        try { return restClient.get().uri(url).retrieve().body(AddressDTO.class); } catch (Exception e) { return null; }
    }

    public List<PhysicalTransactionDTO> getPhysicalTransactions(Integer userId) {
        return restClient.get().uri("/user/physical/{id}", userId).retrieve().body(new ParameterizedTypeReference<>() {});
    }

    public void depositGold(Integer userId, BigDecimal amount) {
        restClient.put().uri(builder -> builder.path("/user/deposit/{id}").queryParam("amount", amount).build(userId)).retrieve().toBodilessEntity();
    }

    public void updateUser(Integer userId, String name, String email) {
        restClient.patch().uri("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON).body(Map.of("name", name, "email", email)).retrieve().toBodilessEntity();
    }

    private String extractHref(ResponseEntity<Map> entity) {
        if (entity.getHeaders().getLocation() != null) return entity.getHeaders().getLocation().toString();
        Map body = entity.getBody();
        if (body != null && body.containsKey("_links")) {
            Map links = (Map) body.get("_links");
            if (links.containsKey("self")) return (String) ((Map) links.get("self")).get("href");
        }
        throw new RuntimeException("Backend did not return a valid resource URL");
    }

    public void createUserWithAddress(String name, String email, String street, String city, String state, String postalCode, String country) {
        AddressDTO addressPayload = new AddressDTO(); addressPayload.setStreet(street); addressPayload.setCity(city); addressPayload.setState(state); addressPayload.setPostalCode(postalCode); addressPayload.setCountry(country);
        @SuppressWarnings("unchecked") ResponseEntity<Map> addrEntity = restClient.post().uri("/addresses").contentType(MediaType.APPLICATION_JSON).body(addressPayload).retrieve().toEntity(Map.class);
        String addrHref = extractHref(addrEntity);

        @SuppressWarnings("unchecked") ResponseEntity<Map> userEntity = restClient.post().uri("/users").contentType(MediaType.APPLICATION_JSON).body(Map.of("name", name, "email", email)).retrieve().toEntity(Map.class);
        String userSelfHref = extractHref(userEntity);

        restClient.put().uri(userSelfHref + "/address").contentType(MediaType.parseMediaType("text/uri-list")).body(addrHref).retrieve().toBodilessEntity();
    }

    public boolean isEmailTakenByOther(String email, Integer excludeUserId) {
        try {
            @SuppressWarnings("unchecked") Map<String, Object> result = restClient.get().uri("/users/search/findByEmailIgnoreCase?email={email}", email).retrieve().body(Map.class);
            if (result == null) return false;
            @SuppressWarnings("unchecked") Map<String, Map<String, String>> links = (Map<String, Map<String, String>>) result.get("_links");
            if (links == null) return false;
            String selfHref = links.get("self").get("href");
            Integer foundId = Integer.parseInt(selfHref.substring(selfHref.lastIndexOf('/') + 1));
            return !foundId.equals(excludeUserId);
        } catch (Exception e) { return false; }
    }

    public List<DashboardHoldingDTO> getDashboardHoldings(String holdingsUrl) {
        if (holdingsUrl == null) return List.of();
        try {
            Map<String, Object> response = restClient.get().uri(holdingsUrl.split("\\{")[0]).retrieve().body(new ParameterizedTypeReference<>() {});
            if (response == null || !response.containsKey("_embedded")) return List.of();
            Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
            List<Map<String, Object>> rawHoldings = (List<Map<String, Object>>) embedded.getOrDefault("virtualGoldHoldings", embedded.get("holdings"));
            if (rawHoldings == null) return List.of();

            return rawHoldings.stream().map(raw -> {
                DashboardHoldingDTO dto = new DashboardHoldingDTO();
                BigDecimal qty = new BigDecimal(raw.getOrDefault("quantity", "0").toString());
                dto.setQuantity(qty);
                Map<String, Object> vendorData = fetchVendorFromHalLinks(raw);                dto.setVendorName((String) vendorData.getOrDefault("vendorName", "Unknown Vendor"));
                BigDecimal price = new BigDecimal(vendorData.getOrDefault("currentGoldPrice", "0").toString());
                dto.setCurrentPrice(price);
                dto.setTotalValue(qty.multiply(price));
                return dto;
            }).toList();
        } catch (Exception e) { return List.of(); }
    }

    public List<DashboardTransactionDTO> getDashboardTransactions(String txnsUrl) {
        if (txnsUrl == null) return List.of();
        try {
            Map<String, Object> response = restClient.get().uri(txnsUrl.split("\\{")[0]).retrieve().body(new ParameterizedTypeReference<>() {});
            if (response == null || !response.containsKey("_embedded")) return List.of();
            Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
            List<Map<String, Object>> rawTxns = (List<Map<String, Object>>) embedded.getOrDefault("transactions", embedded.get("transactionHistories"));
            if (rawTxns == null) return List.of();

            return rawTxns.stream().map(raw -> {
                DashboardTransactionDTO dto = new DashboardTransactionDTO();
                dto.setQuantity(new BigDecimal(raw.getOrDefault("quantity", "0").toString()));
                dto.setAmount(new BigDecimal(raw.getOrDefault("amount", "0").toString()));
                dto.setTransactionType((String) raw.get("transactionType"));
                dto.setCreatedAt((String) raw.get("createdAt"));
                Map<String, Object> vendorData = fetchVendorFromHalLinks(raw);                dto.setVendorName((String) vendorData.getOrDefault("vendorName", "Unknown Vendor"));
                return dto;
            }).toList();
        } catch (Exception e) { return List.of(); }
    }

    private Map<String, Object> fetchVendorFromHalLinks(Map<String, Object> rawEntity) {
        try {
            // 1. Safely extract the _links map
            @SuppressWarnings("unchecked")
            Map<String, Object> links = (Map<String, Object>) rawEntity.get("_links");
            if (links == null) return fallbackVendor();

            // 2. Extract the branch link
            @SuppressWarnings("unchecked")
            Map<String, String> branchLink = (Map<String, String>) links.get("branch");
            if (branchLink == null || !branchLink.containsKey("href")) return fallbackVendor();

            String branchUrl = branchLink.get("href").split("\\{")[0];

            // 3. Fetch the Branch
            Map<String, Object> branch = restClient.get()
                    .uri(branchUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            // 4. Extract the vendor link from the Branch
            @SuppressWarnings("unchecked")
            Map<String, Object> branchLinks = (Map<String, Object>) branch.get("_links");
            @SuppressWarnings("unchecked")
            Map<String, String> vendorLink = (Map<String, String>) branchLinks.get("vendors");

            String vendorUrl = vendorLink.get("href").split("\\{")[0];

            // 5. Fetch and return the Vendor
            return restClient.get()
                    .uri(vendorUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
            return fallbackVendor();
        }
    }

    // Helper to return default values if the HAL links are broken or missing
    private Map<String, Object> fallbackVendor() {
        return Map.of("vendorName", "Unknown", "currentGoldPrice", "0");
    }
}