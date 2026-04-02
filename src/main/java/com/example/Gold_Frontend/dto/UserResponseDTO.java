package com.example.Gold_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class UserResponseDTO {

    // This tells Jackson: "Look for '_embedded' in JSON and put it in this variable"
    @JsonProperty("_embedded")
    private EmbeddedData embedded;

    private PageData page;

    @Data
    public static class EmbeddedData {
        private List<UserDTO> userses;
    }

    @Data
    public static class PageData {
        private int size;
        private int totalElements;
        private int totalPages;
        private int number;
    }
}
