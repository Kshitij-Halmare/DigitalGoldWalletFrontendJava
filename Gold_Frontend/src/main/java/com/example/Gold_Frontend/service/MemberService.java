package com.example.Gold_Frontend.service;

import com.example.Gold_Frontend.dto.AddressDTO;
import com.example.Gold_Frontend.dto.PhysicalTransactionDTO;
import com.example.Gold_Frontend.dto.UserDTO;
import com.example.Gold_Frontend.dto.UserResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
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

    // ── Read ─────────────────────────────────────────────────────────────────────

    public UserResponseDTO getUsers(int page, int size) {
        return restClient.get()
                .uri("/users?page={page}&size={size}", page, size)
                .retrieve()
                .body(UserResponseDTO.class);
    }

    public UserDTO getUserById(Integer id) {
        return restClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new RuntimeException("User not found");
                })
                .body(UserDTO.class);
    }

    public AddressDTO getAddressByUrl(String url) {
        if (url == null) return null;
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(AddressDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    public List<PhysicalTransactionDTO> getPhysicalTransactions(Integer userId) {
        return restClient.get()
                .uri("/user/physical/{id}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    // ── Write ────────────────────────────────────────────────────────────────────

    public void depositGold(Integer userId, BigDecimal amount) {
        restClient.put()
                .uri(builder -> builder
                        .path("/user/deposit/{id}")
                        .queryParam("amount", amount)
                        .build(userId))
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * PATCH /users/{id} — update name and/or email.
     * Spring Data REST accepts a PATCH with a partial JSON body.
     */
    public void updateUser(Integer userId, String name, String email) {
        restClient.patch()
                .uri("/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("name", name, "email", email))
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Create a new user together with their address in one service call.
     *
     * Flow (mirrors your original JS):
     * 1. POST /addresses  → get address HAL href
     * 2. POST /users      → get user HAL self href
     * 3. PUT  /users/{id}/address  (text/uri-list) → link address to user
     */
    public void createUserWithAddress(String name, String email,
                                      String street, String city,
                                      String state, String postalCode,
                                      String country) {

        // 1. Create address and get URI from Location header
        String addrHref = restClient.post()
                .uri("/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AddressDTO(street, city, state, postalCode, country))
                .retrieve()
                .toEntity(Void.class) // We don't need a body, just the headers
                .getHeaders()
                .getLocation()
                .toString();

        // 2. Create user and get URI from Location header
        String userSelfHref = restClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("name", name, "email", email))
                .retrieve()
                .toEntity(Void.class)
                .getHeaders()
                .getLocation()
                .toString();

        // 3. Link address → user
        restClient.put()
                .uri(userSelfHref + "/address")
                .contentType(MediaType.parseMediaType("text/uri-list"))
                .body(addrHref)
                .retrieve()
                .toBodilessEntity();
    }
}