package com.backempresa.shared;

import com.backempresa.reserva.infrastructure.ReservaOutputDto;
import com.backempresa.reserva.infrastructure.ReservaOutputDtoSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    // Definimos un listenerFactory para recibir mensajes de tipo String en Kafka

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaTemplate<String,String> kafkaStringTemplate()
    {
        Map<String,Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        ProducerFactory<String,String> producerFactory =
                new DefaultKafkaProducerFactory<String,String>(props, new StringSerializer(), new StringSerializer());

        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, ReservaOutputDto> kafkaReservaTemplate()
    {
        Map<String,Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ReservaOutputDtoSerializer.class);

        ProducerFactory<String,ReservaOutputDto> producerFactory =
                new DefaultKafkaProducerFactory<String,ReservaOutputDto>(props, new StringSerializer(), new ReservaOutputDtoSerializer());

        return new KafkaTemplate<>(producerFactory);
    }
}
