package com.backempresa.shared;

import com.backempresa.reserva.domain.Reserva;
import com.backempresa.reserva.infrastructure.ReservaOutputDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class KafkaMessageProducer {

    // Backempresa sólo envia comandos de sincronización por el topic "comandos"

    @Autowired
    private KafkaTemplate<String, String> kafkaStringTemplate;

    @Autowired
    private KafkaTemplate<String, ReservaOutputDto> kafkaReservaTemplate;

    public void sendMessage(String topic, int particion, String comando)
    {
        ListenableFuture<SendResult<String, String>> future = kafkaStringTemplate.send(topic, particion, "0", comando);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                String topic = result.getProducerRecord().topic();
                System.out.println("Sent message=[" + comando + "] in " + topic + " with offset=[" + result.getRecordMetadata().offset() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                System.err.println("Unable to send message=[" + comando + "] due to : " + ex.getMessage());
            }
        });
    }

    public void sendMessage(String topic, int particion, ReservaOutputDto outputDto)
    {
        ListenableFuture<SendResult<String, ReservaOutputDto>> future =
                kafkaReservaTemplate.send(topic, particion, String.valueOf(outputDto.getIdReserva()), outputDto);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, ReservaOutputDto> result) {
                String topic = result.getProducerRecord().topic();
                System.out.println("Sent message=[" + outputDto + "] in " + topic + " with offset=[" + result.getRecordMetadata().offset() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                System.err.println("Unable to send message=[" + outputDto + "] due to : " + ex.getMessage());
            }
        });
    }
}
