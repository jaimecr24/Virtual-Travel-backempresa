package com.backempresa.autobus.application;

import com.backempresa.autobus.domain.Autobus;
import com.backempresa.autobus.infrastructure.AutobusInputDto;

import java.util.Date;
import java.util.List;

public interface AutobusService {
    int ID_LENGTH=11;
    List<Autobus> findAll();
    Autobus findById(String id);
    Autobus add(AutobusInputDto inputDto);
    void del(String id);
    String getIdBus(String idDestino, Date fecha, Float hora);
}
