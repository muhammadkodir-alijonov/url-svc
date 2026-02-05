package com.example.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlListResponse {
    private List<UrlResponse> data;
    private int page;
    private int size;
    private long total;
    private int totalPages;
}
