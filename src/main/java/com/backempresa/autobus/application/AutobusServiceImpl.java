package com.backempresa.autobus.application;

import com.backempresa.autobus.domain.Autobus;
import com.backempresa.autobus.infrastructure.AutobusInputDto;
import com.backempresa.autobus.infrastructure.AutobusRepo;
import com.backempresa.destino.application.DestinoService;
import com.backempresa.destino.domain.Destino;
import com.backempresa.shared.NotFoundException;
import com.backempresa.shared.UnprocesableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class AutobusServiceImpl implements AutobusService{

    AutobusRepo autobusRepo;
    DestinoService destinoService;
    SimpleDateFormat sdf1, sdf2, sdf3;
    public AutobusServiceImpl(
            AutobusRepo autobusRepo,
            DestinoService destinoService,
            SimpleDateFormat sdf1, SimpleDateFormat sdf2, SimpleDateFormat sdf3) {
        this.autobusRepo = autobusRepo;
        this.destinoService = destinoService;
        this.sdf1 = sdf1;
        this.sdf2 = sdf2;
        this.sdf3 = sdf3;
    }

    @Override
    public List<Autobus> findAll() {
        return autobusRepo.findAll();
    }

    @Override
    public Autobus findById(String id) {
        return autobusRepo.findById(id).orElseThrow(()->new NotFoundException("El autobús "+id+" no existe"));
    }

    @Override
    public Autobus add(AutobusInputDto inputDto) {
        // Añade un nuevo autobús para un destino un día y hora
        Destino ds = destinoService.findById(inputDto.getIdDestino());
        if (inputDto.getFecha()==null) throw new UnprocesableException("Debe indicar la fecha");
        if (inputDto.getHoraSalida()==null) throw new UnprocesableException("Debe indicar la hora");
        // Obtenemos el id a partir del destino + fecha + hora de salida
        String id = this.getIdBus(inputDto.getIdDestino(),inputDto.getFecha(),inputDto.getHoraSalida());
        if (autobusRepo.findById(id).isPresent())
            throw new UnprocesableException("Ya existe un autobús a "+ds.getNombreDestino()
                    +" el día "+sdf1.format(inputDto.getFecha())
                    +" a las "+String.format("%02d",inputDto.getHoraSalida().intValue())+"H");
        Autobus bus = this.toAutobus(inputDto, ds);
        bus.setId(id);
        autobusRepo.save(bus);
        return bus;
    }

    @Override
    public void del(String id) {
        Autobus bus = this.findById(id);
        autobusRepo.delete(bus);
    }

    @Override
    public String getIdBus(String idDestino, Date fecha, Float hora) {
        if (idDestino==null || fecha==null || hora==null) throw new UnprocesableException("Valores de entrada nulos en getIdBus");
        return idDestino + sdf3.format(fecha) + String.format("%02d",hora.intValue());  // Formato XXXddMMYYHH
    }

    public Autobus toAutobus(AutobusInputDto inputDto, Destino ds) {
        Autobus bus = new Autobus();
        bus.setDestino(ds);
        bus.setFecha(inputDto.getFecha());
        bus.setHoraSalida(inputDto.getHoraSalida());
        bus.setPlazasLibres(inputDto.getPlazasLibres());
        bus.setMaxPlazas(MAX_PLAZAS);
        return bus;
    }
}

