package com.backempresa.shared;

import com.backempresa.reserva.infrastructure.ReservaOutputDto;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.AsyncResponse;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PostOffice {

    private Mailer mailer = MailerBuilder
            .withSMTPServer("smtp.mailtrap.io", 2525, "401dd4926d850f", "738ee9ea1b7e39")
            .withTransportStrategy(TransportStrategy.SMTP).buildMailer();

    public void sendMessage(ReservaOutputDto outDto){

    Email email =
        EmailBuilder.startingBlank()
            .from("From", "backempresa@vtravel.com")
            .to("To", outDto.getEmail())
            .withSubject("RESERVA " + outDto.getCiudadDestino() + " " + outDto.getStatus())
            .withPlainText(
                "Reserva "
                    + outDto.getStatus()
                    + "\nDestino: "
                    + outDto.getCiudadDestino()
                    + "\nFecha: "
                    + outDto.getFechaReserva()
                    + "\nHora: "
                    + outDto.getHoraReserva()
                    + ((outDto.getStatus().equals("RECHAZADA"))
                        ? "\nSu reserva ha sido cancelada por falta de sitio.\nLamentamos los inconvenientes."
                        : "\nIdentificador: "+outDto.getIdentificador()+"\n\nGracias por confiar en Virtual-Travel"))
            .buildEmail();

        AsyncResponse response = mailer.sendMail(email,true); // True for async message

        assert response != null;
        response.onSuccess(()->System.out.println("Sent email to "+outDto.getEmail()));
    }
}
