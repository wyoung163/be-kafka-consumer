package com.example.oliveyoungbe.controller;

import java.util.HashMap;
import java.util.Map;

import com.example.oliveyoungbe.config.KafkaCosumerConfig;
import com.example.oliveyoungbe.dto.TicketRequest;
import com.example.oliveyoungbe.service.KafkaConsumerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

	@GetMapping("/health/v1")
	public Map<String, String> healthCheck() {
		Map<String, String> response = new HashMap<>();
		response.put("status", "OK");

		return response;
	}
}
