package com.backempresa.reserva.infrastructure;

import com.backempresa.autobus.domain.Autobus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.SimpleDateFormat;

@Getter
@Setter
@NoArgsConstructor
public class ReservaDisponibleOutputDto {
    private String ciudadDestino;
    private String fechaReserva;
    private Float horaReserva;
    private int plazasLibres;

    public ReservaDisponibleOutputDto(Autobus bus) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        this.ciudadDestino = bus.getDestino().getNombreDestino();
        this.fechaReserva = sdf.format(bus.getFecha());
        this.horaReserva = bus.getHoraSalida();
        this.plazasLibres = bus.getPlazasLibres();
    }
}
