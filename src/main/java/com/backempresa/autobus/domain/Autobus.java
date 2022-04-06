package com.backempresa.autobus.domain;

import com.backempresa.destino.domain.Destino;
import com.backempresa.reserva.domain.Reserva;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Autobus {

    @Id
    private String id; // String compuesto por: idDestino+ddMMyy+HH (longitud 11)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idDestino")
    private Destino destino;

    private Date fecha;
    private Float horaSalida;
    private int plazasLibres;
    private int maxPlazas;

    @OneToMany(mappedBy = "autobus")
    private List<Reserva> reservas;
}
