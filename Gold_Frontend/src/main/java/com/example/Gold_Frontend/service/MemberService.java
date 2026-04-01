package com.example.Gold_Frontend.service;

import com.example.Gold_Frontend.dto.AddressDTO;
import com.example.Gold_Frontend.dto.PhysicalTransactionDTO;
import com.example.Gold_Frontend.dto.UserDTO;
import com.example.Gold_Frontend.dto.UserResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MemberService {

    private final RestClient restClient;

    public MemberService(@Value("${backend.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public UserResponseDTO getUsers(int page, int size) {
        return restClient.get()
                .uri("/users?page={page}&size={size}", page, size)
                .retrieve()
                .body(UserResponseDTO.class);
    }

    public UserDTO getUserById(Long id) {
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
            return null; // Return null if address is not set for user
        }
    }

    public List<PhysicalTransactionDTO> getPhysicalTransactions(Integer userId) {
        return restClient.get()
                .uri("/user/physical/{id}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PhysicalTransactionDTO>>() {});
    }

    public void depositGold(Integer userId, BigDecimal amount) {
        // This matches: PUT http://localhost:8080/user/deposit/{id}?amount=500
        restClient.put()
                .uri(builder -> builder
                        .path("/user/deposit/{id}")
                        .queryParam("amount", amount)
                        .build(userId))
                .retrieve()
                .toBodilessEntity();
    }


}