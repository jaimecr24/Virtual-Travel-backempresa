package com.backempresa.autobus.application;

import com.backempresa.autobus.domain.Autobus;
import com.backempresa.autobus.infrastructure.AutobusInputDto;
import com.backempresa.autobus.infrastructure.AutobusRepo;
import com.backempresa.destino.application.DestinoService;
import com.backempresa.destino.domain.Destino;
import com.backempresa.shared.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutobusServiceImpl implements AutobusService{

    @Autowired
    AutobusRepo autobusRepo;

    @Autowired
    DestinoService destinoService;

    @Override
    public List<Autobus> findAll() {
        return autobusRepo.findAll();
    }

    @Override
    public Autobus findById(long id) {
        return autobusRepo.findById(id).orElseThrow(()->new NotFoundException("El autobús "+id+" no existe"));
    }

    @Override
    public Autobus add(AutobusInputDto inputDto) {
        Destino ds = destinoService.findById(inputDto.getIdDestino());
        Autobus bus = this.toAutobus(inputDto, ds);
        autobusRepo.save(bus);
        return bus;
    }

    @Override
    public Autobus put(long id, AutobusInputDto inputDto) {
        return null;
    }

    @Override
    public void del(long id) {
        Autobus bus = this.findById(id);
        autobusRepo.delete(bus);
    }

    public Autobus toAutobus(AutobusInputDto inputDto, Destino ds) {
        Autobus bus = new Autobus();
        bus.setDestino(ds);
        bus.setFecha(inputDto.getFecha());
        bus.setHoraSalida(inputDto.getHoraSalida());
        bus.setPlazasLibres(inputDto.getPlazasLibres());
        return bus;
    }
}

