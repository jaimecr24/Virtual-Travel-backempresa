package com.backempresa.shared;

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
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String comando)
    {
        String topic = "comandos";

        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, 0, "0", comando);
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
}
