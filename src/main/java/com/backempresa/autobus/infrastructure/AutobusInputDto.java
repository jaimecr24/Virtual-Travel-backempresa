package com.backempresa.autobus.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class AutobusInputDto {
    private String idDestino;
    private Date fecha;
    private Float horaSalida;
    private int plazasLibres;
    private int maxPlazas;
}
