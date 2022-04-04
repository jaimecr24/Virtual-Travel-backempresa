package com.backempresa.reserva.infrastructure;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ReservaOutputDto {
    private long idReserva;
    private String identificador;
    private String ciudadDestino;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String fechaReserva;
    private Float horaReserva;
    private String status; // Necesario para el backweb
}
