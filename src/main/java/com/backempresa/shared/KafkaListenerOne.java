package com.backempresa.shared;

import com.backempresa.autobus.application.AutobusService;
import com.backempresa.autobus.domain.Autobus;
import com.backempresa.reserva.application.ReservaService;
import com.backempresa.reserva.infrastructure.ReservaOutputDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

@Component
public class KafkaListenerOne {

    @Autowired
    KafkaMessageProducer kafkaMessageProducer;

    @Autowired
    ReservaService reservaService;

    @Autowired
    AutobusService autobusService;

    @Autowired
    PostOffice postOffice;

    @Value(value="${server.port}")
    String port;

    @KafkaListener(topics = "reservas", groupId = "backempresa", topicPartitions = {
            @TopicPartition(topic = "reservas", partitionOffsets = { @PartitionOffset(partition = "0", initialOffset = "0")} )
    }) // Partición 0: mensajes a backempresa. Partición 1: mensajes a backweb.
    public void listenReservasWeb(ReservaOutputDto outputDto) {
        System.out.println("Backempresa ("+port+"): recibido mensaje en partición 0: " + outputDto.toString());
        ReservaOutputDto tempDto;
        try {
            tempDto = reservaService.add(outputDto);
            if (tempDto==null) {
                System.out.println("Reserva ya existente");
                // Al levantarse de nuevo backempresa, vuelve a leer desde cero,
                // pero al no agregarse registros nuevos, no tiene ningún efecto.
            } else {
                System.out.println("Reserva añadida en la base de datos de backempresa, con identificador "+tempDto.getIdentificador());
                // Mensaje a los backweb para que actualicen el número de reservas del autobús
                String idBus = tempDto.getIdentificador().substring(0,autobusService.ID_LENGTH);
                Autobus bus = autobusService.findById(idBus);
                kafkaMessageProducer.sendMessage("comandos",0,"UPDATE:" // Enviamos mensaje para que backweb se sincronice
                        +tempDto.getIdentificador()+":"
                        +String.format("%02d", bus.getPlazasLibres()));
                postOffice.sendMessage(tempDto); // Enviamos e-mail de confirmación
            }
        } catch (NotPlaceException ex) {
            System.out.println("Backempresa ("+port+"): "+ex.getMessage());
            kafkaMessageProducer.sendMessage("comandos", 0, "REJECT:"
                    +outputDto.getIdentificador()+":00");
            // Mensaje a backweb para actualizar plazas disponibles a 0 y marcar la reserva como rechazada.
            // TODO: Este error es por fallo de sincronización. Debe pedir de nuevo las reservas del autobús y actualizarse.
        }
    }
}
