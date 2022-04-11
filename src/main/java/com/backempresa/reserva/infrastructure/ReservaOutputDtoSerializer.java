package com.backempresa.reserva.infrastructure;

import com.backempresa.shared.UnprocesableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;

public class ReservaOutputDtoSerializer implements Serializer<ReservaOutputDto> {

    @Override
    public byte[] serialize(String topic, ReservaOutputDto data) {
        byte[] serializedBytes = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            serializedBytes = objectMapper.writeValueAsString(data).getBytes();
        }
        catch (IOException e) {
            throw new UnprocesableException("Error al serializar ReservaOutputDto: "+e.getMessage());
        }
        return serializedBytes;
    }
}
