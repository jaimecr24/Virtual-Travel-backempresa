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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class KafkaListenerOne {

    @Autowired
    ReservaService reservaService;

    //@Value(value="${server.port}")
    String port="8080";

    private Mailer mailer = MailerBuilder
            .withSMTPServer("smtp.mailtrap.io", 2525, "401dd4926d850f", "738ee9ea1b7e39")
            .withTransportStrategy(TransportStrategy.SMTP).buildMailer();

    @KafkaListener(topics = "reservas", groupId = "mygroup", topicPartitions = {
            @TopicPartition(topic = "reservas", partitionOffsets = { @PartitionOffset(partition = "0", initialOffset = "0")} )
    })
    public void listenTopic1(ReservaOutputDto reserva) {
        System.out.println("Backempresa: recibido mensaje en partición 0: " + reserva.toString());
        try {
            ReservaOutputDto outputDto = reservaService.add(reserva);
            sendMessage(outputDto);
        } catch (NotPlaceException ex) {
            System.out.println("Backempresa: "+ex.getMessage());
            // Error de sincronización: Enviar mensaje a backweb para que actualice sus datos de reservas.
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
                        "\nIdentificador: "+outDto.getIdReserva()+
                        "\n\nGracias por confiar en Virtual-Travel")
                .buildEmail();

        AsyncResponse response = mailer.sendMail(email,true); // True for async message

        assert response != null;
        response.onSuccess(()->System.out.println("Message sent"));
    }
}
