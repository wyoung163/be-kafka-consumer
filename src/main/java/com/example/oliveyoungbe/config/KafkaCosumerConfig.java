package com.example.oliveyoungbe.config;

import com.example.oliveyoungbe.dto.TicketBooking;
import com.example.oliveyoungbe.dto.TicketRequest;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaCosumerConfig {
    @Value("${spring.kafka.bootstrap-server}")
    private String kafkaBootstrapServer;

    @Value("${spring.kafka.consumer.group-id")
    private String groupId;

    @Bean
    public ConsumerFactory<String, TicketRequest> ticketRequestConsumerFactory() {
        JsonDeserializer<TicketRequest> deserializer = gcmTicketRequestJsonDeserializer();
        return new DefaultKafkaConsumerFactory<>(
                consumerRequestFactoryConfig(deserializer),
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConsumerFactory<? super String, ? super TicketBooking> ticketBookingConsumerFactory() {
        JsonDeserializer<TicketBooking> deserializer = gcmTicketBookingJsonDeserializer();
        return new DefaultKafkaConsumerFactory<>(
                consumerBookingFactoryConfig(deserializer),
                new StringDeserializer(),
                deserializer);
    }

    private Map<String, Object> consumerRequestFactoryConfig(JsonDeserializer<TicketRequest> deserializer) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
        return configProps;
    }

    private Map<String, Object> consumerBookingFactoryConfig(JsonDeserializer<TicketBooking> deserializer) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
        return configProps;
    }

    private JsonDeserializer<TicketRequest> gcmTicketRequestJsonDeserializer() {
        JsonDeserializer<TicketRequest> deserializer = new JsonDeserializer<>(TicketRequest.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);
        return deserializer;
    }

    private JsonDeserializer<TicketBooking> gcmTicketBookingJsonDeserializer() {
        JsonDeserializer<TicketBooking> deserializer = new JsonDeserializer<>(TicketBooking.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);
        return deserializer;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TicketRequest>
    kafkaListenerRequestContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TicketRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(ticketRequestConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TicketBooking>
    kafkaListenerBookingContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TicketBooking> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(ticketBookingConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
