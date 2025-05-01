package com.example.oliveyoungbe.dto;

import jakarta.annotation.Nullable;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class TicketRequestDto {
    @Nullable
    private String uuid;
    private String eventId;
    private String timestamp;
}