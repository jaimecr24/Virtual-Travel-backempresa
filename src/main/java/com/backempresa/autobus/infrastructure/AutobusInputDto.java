package com.backempresa.autobus.infrastructure;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AutobusInputDto {
    private long idDestino;
    private Date fecha;
    private Float horaSalida;
    private int plazasLibres = 40;
}
