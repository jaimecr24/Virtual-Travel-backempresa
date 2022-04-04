package com.backempresa.shared;

import com.backempresa.reserva.application.ReservaService;
import com.backempresa.reserva.infrastructure.ReservaOutputDto;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.AsyncResponse;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class KafkaListenerOne {

    @Autowired
    KafkaMessageProducer kafkaMessageProducer;

    @Autowired
    ReservaService reservaService;

    @Value(value="${server.port}")
    String port;

    private Mailer mailer = MailerBuilder
            .withSMTPServer("smtp.mailtrap.io", 2525, "401dd4926d850f", "738ee9ea1b7e39")
            .withTransportStrategy(TransportStrategy.SMTP).buildMailer();

    @KafkaListener(topics = "reservas", groupId = "backempresa", topicPartitions = {
            @TopicPartition(topic = "reservas", partitionOffsets = { @PartitionOffset(partition = "0", initialOffset = "0")} )
    })
    public void listenReservasWeb(ReservaOutputDto reserva) {
        System.out.println("Backempresa ("+port+"): recibido mensaje en partición 0: " + reserva.toString());
        try {
            ReservaOutputDto outputDto = reservaService.add(reserva);
            if (outputDto==null) {
                System.out.println("Reserva ya existente");
            } else {
                sendMessage(outputDto); // Enviamos e-mail de confirmación
                kafkaMessageProducer.sendMessage("UPDATE" // Enviamos mensaje para que backweb se sincronice
                        +outputDto.getIdentificador()+":"
                        +String.format("%02d",reservaService.getPlazasLibres(
                                outputDto.getCiudadDestino(), outputDto.getFechaReserva(),outputDto.getHoraReserva())));
            }
        } catch (NotPlaceException ex) {
            System.out.println("Backempresa: "+ex.getMessage());
            //TODO: Enviar mensaje a backweb para actualizar plazas disponibles y marcar la reserva como rechazada.
        }
    }

    private void sendMessage(ReservaOutputDto outDto){

        Email email = EmailBuilder.startingBlank()
                .from("From", "backempresa@vtravel.com")
                .to("To", outDto.getEmail())
                .withSubject("RESERVA "+outDto.getCiudadDestino()+" "+outDto.getStatus())
                .withPlainText(((Objects.equals(outDto.getStatus(), "CONFIRMADA")) ? "Reserva CONFIRMADA: " : "Reserva CANCELADA")+
                        "\nDestino: "+outDto.getCiudadDestino()+
                        "\nFecha: "+outDto.getFechaReserva()+
                        "\nHora: "+outDto.getHoraReserva()+
                        "\nIdentificador: "+outDto.getIdentificador()+
                        "\n\nGracias por confiar en Virtual-Travel")
                .buildEmail();

        AsyncResponse response = mailer.sendMail(email,true); // True for async message

        assert response != null;
        response.onSuccess(()->System.out.println("Message sent"));
    }
}
