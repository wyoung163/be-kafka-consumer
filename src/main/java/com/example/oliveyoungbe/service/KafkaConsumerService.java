package com.example.oliveyoungbe.service;

import com.example.oliveyoungbe.dto.TicketRequest;
import com.example.oliveyoungbe.dto.TicketBooking;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final StringRedisTemplate redisTemplate;
    private static final String WAITING_LIST_KEY = "waiting_list";
    private static final String ENTER_LIST_KEY = "enter_list";
    private static final String BOOKING_LIST_KEY = "booking_list";
    private static final int MAX_CAPACITY = 100; // 최대 입장 가능 인원

    //예매 요청 메시지 소비 (대기열 추가 및 입장 처리)
    @KafkaListener(topics = "${kafka.topic.typeRequest}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTicketRequest(@Payload TicketRequest ticketRequest,
                                     @Headers MessageHeaders messageHeaders,
                                     Acknowledgment acknowledgment) throws Exception {

        // 대기열 추가
        boolean isSuccess = addToWaitingList(ticketRequest);
        if(!isSuccess) {
            // 에러 처리
            throw new Exception("예매 요청 실패");
        }

        // 입장 가능 여부 확인 후 입장 처리
        isSuccess = processEntryList();
        if(isSuccess) {
            acknowledgment.acknowledge(); // Kafka 메시지 정상 처리 후 커밋
            System.out.println("예매 요청 성공: " + ticketRequest.getUuid());
        }
    }

    //예매 완료 메시지 소비 (예약 확정 및 대기열 정리)
    @KafkaListener(topics = "${kafka.topic.typeBooking}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTicketBooking(@Payload TicketBooking ticketBooking,
                                     @Headers MessageHeaders messageHeaders,
                                     Acknowledgment acknowledgment) throws Exception {
        // 예매 확정 및 대기열 정리
        boolean isSuccess = finalizeBooking(ticketBooking);
        if(!isSuccess) {
            throw new Exception("예매 완료 실패");
        }
        acknowledgment.acknowledge(); // Kafka 메시지 정상 처리 후 커밋
        System.out.println("예매 완료: " + ticketBooking.getUuid());
    }

    //대기열에 사용자 추가
    private boolean addToWaitingList(TicketRequest ticketRequest) {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        String uuid = ticketRequest.getUuid();
        long timestamp = Long.parseLong(ticketRequest.getTimestamp());
        if (uuid != null && zSetOperations != null) {
            Boolean success = zSetOperations.add(WAITING_LIST_KEY, uuid, (double) timestamp);
            if (Boolean.TRUE.equals(success)) {
                System.out.println("대기열 추가: UUID=" + uuid + ", timestamp=" + timestamp);
                return true;
            } else {
                System.out.println("대기열 추가 실패: UUID=" + uuid);
                return false;
            }
        }
        return false;
    }

    //입장 가능 여부 확인 후 입장 처리
    private boolean processEntryList() {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Long enterSize = redisTemplate.opsForSet().size(ENTER_LIST_KEY);

        if (zSetOperations != null && enterSize != null && enterSize < MAX_CAPACITY) {
            Set<String> firstUser = zSetOperations.range(WAITING_LIST_KEY, 0, 0);
            String uuid = firstUser.iterator().next();
            if(removeFromWaitingList(uuid)) {
                redisTemplate.opsForSet().add(ENTER_LIST_KEY, uuid);
                System.out.println("입장 완료: UUID=" + uuid);
                return true;
            } else {
                System.out.println("입장 처리 실패: UUID=" + uuid);
                return false;
            }
        } else {
            System.out.println("입장 불가: 현재 입장 인원 초과 (" + enterSize + "/" + MAX_CAPACITY + ")");
            return false;
        }
    }

    //대기열에서 사용자 제거
    private boolean removeFromWaitingList(String uuid) {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        if (zSetOperations != null && uuid != null) {
            Long removed = zSetOperations.remove(WAITING_LIST_KEY, uuid);
            return removed != null && removed > 0;
        }
        return false;
    }

    // 예매 완료 처리 (예약 확정 및 대기열 정리)
    private boolean finalizeBooking(TicketBooking ticketBooking) {
        String uuid = ticketBooking.getUuid();
        boolean removedFromEnterList = removeFromEnterList(uuid);

        if (removedFromEnterList) {
            redisTemplate.opsForHash().put(BOOKING_LIST_KEY, uuid, ticketBooking);
            System.out.println("예매 완료: UUID=" + uuid);
            return true;
        } else {
            System.out.println("예매 완료 실패: UUID=" + uuid);
            return false;
        }
    }

    // 입장 목록에서 사용자 제거
    private boolean removeFromEnterList(String uuid) {
        if (uuid != null) {
            Long removed = redisTemplate.opsForSet().remove(ENTER_LIST_KEY, uuid);
            return removed != null && removed > 0;
        }
        return false;
    }
}
