package com.backempresa.reserva.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

public class ReservaOutputDtoSerializer implements Serializer<ReservaOutputDto> {

    @Override
    public byte[] serialize(String topic, ReservaOutputDto data) {
        byte[] serializedBytes = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            serializedBytes = objectMapper.writeValueAsString(data).getBytes();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return serializedBytes;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }

}
