package com.backempresa.reserva.infrastructure;

import com.backempresa.shared.UnprocesableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

public class ReservaOutputDtoDeserializer implements Deserializer<ReservaOutputDto> {

    @Override
    public ReservaOutputDto deserialize(String s, byte[] bytes) {
        ObjectMapper mapper = new ObjectMapper();
        ReservaOutputDto reservaOutputDto = null;
        try {
            reservaOutputDto = mapper.readValue(bytes, ReservaOutputDto.class);
        } catch (Exception e) {
            throw new UnprocesableException("Error al deserializar ReservaOutputDto: "+e.getMessage());
        }
        return reservaOutputDto;
    }
}
