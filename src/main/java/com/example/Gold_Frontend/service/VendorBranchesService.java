package com.example.Gold_Frontend.service;

import com.example.Gold_Frontend.dto.TransactionDTO;
import com.example.Gold_Frontend.dto.VendorBranchesDTO.VendorBranchesDTO;
import com.example.Gold_Frontend.dto.VendorBranchesDTO.VendorBranchesDTO.AddressDTO;
import com.example.Gold_Frontend.dto.VendorBranchesDTO.VendorBranchesDTO.VendorDTO;
import com.example.Gold_Frontend.dto.VendorBranchesDTO.PagedBranchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VendorBranchesService {

    private final RestClient restClient;
    private static final int PAGE_SIZE = 10;

    public VendorBranchesService(@Value("${backend.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    // ------------------- FETCH BRANCHES (paged from backend) -------------------

    /**
     * Fetches a single page from Spring Data REST.
     * Spring Data REST pagination params: ?page=0&size=10
     * Returns a PagedBranchResult containing the branch list + page metadata.
     */
    public PagedBranchResult findAllPaged(int page) {
        String url = UriComponentsBuilder.fromPath("/vendorBranches")
                .queryParam("page", page)
                .queryParam("size", PAGE_SIZE)
                .toUriString();
        return fetchPagedBranches(url);
    }

    /**
     * Fetches ALL branches (no pagination cap) — used only for building
     * filter dropdowns (distinct cities, states, countries).
     */
    public List<VendorBranchesDTO> findAll() {
        return fetchBranches("/vendorBranches?size=1000");
    }

    public Optional<VendorBranchesDTO> findById(Integer id) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = restClient.get()
                    .uri("/vendorBranches/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            if (map == null) return Optional.empty();
            return Optional.of(mapToVendorBranchesDTO(map));

        } catch (RestClientException e) {
            return Optional.empty();
        }
    }

    // ------------------- UPDATE BRANCH -------------------

    public void updateBranch(Integer branchId, String quantity,
                             String street, String city, String state,
                             String postalCode, String country) {
        updateBranchQuantity(branchId, quantity);
        Integer addressId = getAddressIdByBranchId(branchId);
        if (addressId != null) {
            updateAddress(addressId, street, city, state, postalCode, country);
        } else {
            throw new RuntimeException("Address ID not found for branch " + branchId);
        }
    }

    public void updateBranchQuantity(Integer branchId, String quantity) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            if (quantity != null && !quantity.isBlank()) {
                body.put("quantity", new BigDecimal(quantity));
            }
            restClient.patch()
                    .uri("/vendorBranches/{id}", branchId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update branch quantity: " + e.getMessage(), e);
        }
    }

    // ------------------- UPDATE ADDRESS -------------------

    public void updateAddress(Integer addressId, String street, String city,
                              String state, String postalCode, String country) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            if (street     != null && !street.isBlank())     body.put("street",     street);
            if (city       != null && !city.isBlank())       body.put("city",       city);
            if (state      != null && !state.isBlank())      body.put("state",      state);
            if (postalCode != null && !postalCode.isBlank()) body.put("postalCode", postalCode);
            if (country    != null && !country.isBlank())    body.put("country",    country);

            restClient.patch()
                    .uri("/addresses/{id}", addressId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update address: " + e.getMessage(), e);
        }
    }

    // ------------------- GET ADDRESS ID FOR BRANCH -------------------

    public Integer getAddressIdByBranchId(Integer branchId) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri("/addresses/{id}", branchId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            System.out.println("FULL RESPONSE: " + response);

            if (response == null) return null;

            Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
            System.out.println("EMBEDDED: " + embedded);

            if (embedded == null) return null;

            List<Map<String, Object>> branches =
                    (List<Map<String, Object>>) embedded.get("vendorBranches");
            System.out.println("BRANCHES: " + branches);

            if (branches == null || branches.isEmpty()) return null;

            Map<String, Object> firstBranch = branches.get(0);

            Map<String, Object> address =
                    (Map<String, Object>) firstBranch.get("address");
            System.out.println("ADDRESS: " + address);

            if (address == null) return null;

            Object rawId = address.get("addressId");
            System.out.println("RAW ID: " + rawId);

            if (rawId == null) return null;

            if (rawId instanceof Number) return ((Number) rawId).intValue();
            if (rawId instanceof String) return Integer.parseInt((String) rawId);

            throw new RuntimeException("Unknown addressId type: " + rawId.getClass());

        } catch (Exception e) {
            System.out.println("❌ ERROR OCCURRED:");
            e.printStackTrace();
            return null;
        }
    }
    // ------------------- TRANSACTIONS -------------------

    public List<TransactionDTO> findTransactionsByBranchId(Integer branchId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri("/vendorBranches/{id}/transactions", branchId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            if (response == null) return List.of();

            @SuppressWarnings("unchecked")
            Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
            if (embedded == null || !embedded.containsKey("transactionHistories")) return List.of();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawList =
                    (List<Map<String, Object>>) embedded.get("transactionHistories");

            List<TransactionDTO> result = new ArrayList<>();
            for (Map<String, Object> tx : rawList) {
                TransactionDTO dto = new TransactionDTO();
                dto.setTransactionStatus((String) tx.get("transactionStatus"));
                dto.setTransactionType((String) tx.get("transactionType"));
                Object qty = tx.get("quantity");
                dto.setQuantity(qty != null ? ((Number) qty).doubleValue() : 0.0);
                Object amt = tx.get("amount");
                dto.setAmount(amt != null ? ((Number) amt).doubleValue() : 0.0);
                dto.setCreatedAt((String) tx.get("createdAt"));
                @SuppressWarnings("unchecked")
                Map<String, Object> user = (Map<String, Object>) tx.get("user");
                if (user != null) {
                    dto.setUserName((String) user.get("name"));
                    Object uid = user.get("userId");
                    dto.setUserId(uid != null ? ((Number) uid).intValue() : null);
                }
                result.add(dto);
            }
            return result;

        } catch (RestClientException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // ------------------- SUM QUANTITY -------------------

    public BigDecimal sumQuantity(List<VendorBranchesDTO> branches) {
        return branches.stream()
                .map(VendorBranchesDTO::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ------------------- HELPERS -------------------

    /**
     * Fetches a paged response from Spring Data REST.
     * Spring Data REST HAL page response shape:
     * {
     *   _embedded: { vendorBranches: [...] },
     *   _links: { self, first, prev, next, last },
     *   page: { size, totalElements, totalPages, number }
     * }
     */
    @SuppressWarnings("unchecked")
    private PagedBranchResult fetchPagedBranches(String url) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            if (response == null) return PagedBranchResult.empty();

            // --- branch list ---
            List<VendorBranchesDTO> branches = List.of();
            Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
            if (embedded != null && embedded.containsKey("vendorBranches")) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) embedded.get("vendorBranches");
                branches = list.stream().map(this::mapToVendorBranchesDTO).collect(Collectors.toList());
            }

            // --- page metadata from Spring Data REST ---
            // response.page = { size, totalElements, totalPages, number }
            Map<String, Object> pageMeta = (Map<String, Object>) response.get("page");
            int totalElements = 0;
            int totalPages    = 0;
            int currentPage   = 0;
            if (pageMeta != null) {
                totalElements = ((Number) pageMeta.get("totalElements")).intValue();
                totalPages    = ((Number) pageMeta.get("totalPages")).intValue();
                currentPage   = ((Number) pageMeta.get("number")).intValue();
            }

            return new PagedBranchResult(branches, totalElements, totalPages, currentPage, PAGE_SIZE);

        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch paged branches: " + url, e);
        }
    }

    private List<VendorBranchesDTO> fetchBranches(String url) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);

            if (response == null) return List.of();

            @SuppressWarnings("unchecked")
            Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
            if (embedded != null && embedded.containsKey("vendorBranches")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) embedded.get("vendorBranches");
                return list.stream().map(this::mapToVendorBranchesDTO).collect(Collectors.toList());
            }
            return List.of();

        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch branches: " + url, e);
        }
    }

    private VendorBranchesDTO mapToVendorBranchesDTO(Map<String, Object> map) {
        AddressDTO addressDTO = null;
        @SuppressWarnings("unchecked")
        Map<String, Object> addrMap = (Map<String, Object>) map.get("address");
        if (addrMap != null) {
            addressDTO = new AddressDTO(
                    (String) addrMap.get("street"),
                    (String) addrMap.get("city"),
                    (String) addrMap.get("state"),
                    (String) addrMap.get("postalCode"),
                    (String) addrMap.get("country")
            );
        }

        VendorDTO vendorDTO = null;
        @SuppressWarnings("unchecked")
        Map<String, Object> vendorMap = (Map<String, Object>) map.get("vendors");
        if (vendorMap != null) {
            vendorDTO = new VendorDTO(
                    (Integer) vendorMap.get("vendorId"),
                    (String)  vendorMap.get("vendorName")
            );
        }

        BigDecimal quantity = BigDecimal.ZERO;
        if (map.get("quantity") != null) {
            quantity = BigDecimal.valueOf(((Number) map.get("quantity")).doubleValue());
        }

        return new VendorBranchesDTO(
                (Integer) map.get("branchId"),
                quantity,
                addressDTO,
                vendorDTO,
                null
        );
    }
}