package com.backempresa.reserva.infrastructure;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class CorreoInputDto {
    private String ciudadDestino;
    private String email;
    private Date fechaReserva;
    private Float horaReserva;
}
