package com.backempresa.reserva.domain;

import com.backempresa.autobus.domain.Autobus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Reserva {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long idReserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAutobus")
    private Autobus autobus;

    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private Date fechaRegistro;
    private STATUS status;

    public enum STATUS { ACEPTADA, RECHAZADA, CONFIRMADA };
}
