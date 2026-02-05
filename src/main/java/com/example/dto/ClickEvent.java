package com.example.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickEvent {
    private String shortCode;
    private Long urlId;
    private String ipAddress;
    private String userAgent;
    private String referer;
    private Instant timestamp;
}
