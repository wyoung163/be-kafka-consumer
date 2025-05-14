package com.example.oliveyoungbe.entity;

import jakarta.annotation.Nullable;
import lombok.Data;

import javax.persistence.Entity;
import java.sql.Timestamp;

@Entity
public class TicketRequest {
    @Nullable
    private String uuid;
    private String eventId;
    private String timestamp;
}