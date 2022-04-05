package com.backempresa.shared;

import com.backempresa.reserva.application.ReservaService;
import com.backempresa.reserva.infrastructure.ReservaOutputDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@Component
public class KafkaListenerOne {

    @Autowired
    KafkaMessageProducer kafkaMessageProducer;

    @Autowired
    ReservaService reservaService;

    @Autowired
    PostOffice postOffice;

    @Value(value="${server.port}")
    String port;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    @KafkaListener(topics = "reservas", groupId = "backempresa", topicPartitions = {
            @TopicPartition(topic = "reservas", partitionOffsets = { @PartitionOffset(partition = "0", initialOffset = "0")} )
    })
    public void listenReservasWeb(ReservaOutputDto reserva) {
        System.out.println("Backempresa ("+port+"): recibido mensaje en partición 0: " + reserva.toString());
        try {
            ReservaOutputDto outputDto = reservaService.add(reserva);
            if (outputDto==null) {
                System.out.println("Reserva ya existente");
                // Al levantarse de nuevo backempresa, vuelve a leer desde cero,
                // pero al no agregarse registros nuevos, no tiene ningún efecto.
            } else {
                postOffice.sendMessage(outputDto); // Enviamos e-mail de confirmación
                kafkaMessageProducer.sendMessage("UPDATE:" // Enviamos mensaje para que backweb se sincronice
                        +outputDto.getIdentificador()+":"
                        +String.format("%02d",reservaService.getPlazasLibres(
                                outputDto.getCiudadDestino(), sdf.parse(outputDto.getFechaReserva()),outputDto.getHoraReserva())));
            }
        } catch (NotPlaceException ex) {
            System.out.println("Backempresa ("+port+"): "+ex.getMessage());
            //TODO: Enviar mensaje a backweb para actualizar plazas disponibles y marcar la reserva como rechazada.
        } catch (ParseException ex) {
            System.out.println("Error en formato de fecha: "+ex.getMessage());
        }
    }
}
