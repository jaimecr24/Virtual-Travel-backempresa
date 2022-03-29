package com.backempresa.reserva.application;

import com.backempresa.autobus.application.AutobusService;
import com.backempresa.autobus.domain.Autobus;
import com.backempresa.destino.application.DestinoService;
import com.backempresa.destino.domain.Destino;
import com.backempresa.reserva.domain.Reserva;
import com.backempresa.reserva.infrastructure.ReservaDisponibleOutputDto;
import com.backempresa.reserva.infrastructure.ReservaInputDto;
import com.backempresa.reserva.infrastructure.ReservaOutputDto;
import com.backempresa.reserva.infrastructure.ReservaRepo;
import com.backempresa.shared.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservaServiceImpl implements ReservaService {

    @Autowired
    ReservaRepo reservaRepo;

    @Autowired
    DestinoService destinoService;

    @Autowired
    AutobusService autobusService;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private final SimpleDateFormat sdf2 = new SimpleDateFormat("ddMMyyyy");

    @Override
    public List<Reserva> findAll() {
        return reservaRepo.findAll();
    }

    @Override
    public Reserva findById(long id) {
        return reservaRepo.findById(id).orElseThrow(()->new NotFoundException("Reserva "+id+" no encontrada."));
    }

    @Override
    public List<ReservaDisponibleOutputDto> findDisponible(String destino, String fechaInferior, String fechaSuperior, String horaInferior, String horaSuperior) {
        List<Destino> dstList = destinoService.findByDestino(destino);
        //TODO: Si especificamos que el campo nombreDestino es único, el retorno de esta función será un único objeto, no una lista.
        if (dstList.size()==0) throw new NotFoundException("Destino no encontrado");
        Destino dst = dstList.get(0);
        List<Autobus> busList = dst.getAutobuses();
        try {
            Date fInf = sdf2.parse(fechaInferior);
            Date fSup = (fechaSuperior != null) ? sdf2.parse(fechaSuperior) : null;
            Float hInf = (horaInferior != null) ? Float.parseFloat(horaInferior) : 0F;
            Float hSup = (horaSuperior != null) ? Float.parseFloat(horaSuperior) : 24F;
            return busList.stream().filter(e ->
                            e.getFecha().compareTo(fInf) >= 0
                                    && (fSup==null || e.getFecha().compareTo(fSup)<=0)
                                    && e.getHoraSalida()>=hInf && e.getHoraSalida()<=hSup)
                    .map(ReservaDisponibleOutputDto::new).collect(Collectors.toList());
        } catch(Exception e) {
            if (e.getClass()== ParseException.class) {
                //TODO
            }
            return new ArrayList<>();
        }
    }

    @Override
    public ReservaOutputDto add(ReservaInputDto inputDto) throws NotFoundException {
        // Crea un objeto Reserva con los datos indicados en inputDto
        // El status de la reserva será ACEPTADA/RECHAZADA según las plazas libres y las ya aceptadas para ese autobús.
        Reserva rsv = this.toReserva(inputDto);
        Autobus bus = rsv.getAutobus();
        rsv.setStatus((bus.getPlazasLibres() - numReservasAceptadas(bus) > 0) ? Reserva.STATUS.ACEPTADA : Reserva.STATUS.RECHAZADA);
        rsv.setFechaRegistro(new Date());
        reservaRepo.save(rsv);
        return this.toOutputDto(rsv);
    }

    private long numReservasAceptadas(Autobus bus) {
        return bus.getReservas().stream().filter(e -> e.getStatus() == Reserva.STATUS.ACEPTADA).count();
    }

    @Override
    public Reserva put(long id, Reserva reserva) {
        return null;
    }

    @Override
    public void del(long id) {
        // TODO: Poner estado de la reserva en CANCELADA ??
    }

    public Reserva toReserva(ReservaInputDto inputDto) throws NotFoundException {
        // Creamos el objeto
        Reserva rsv = new Reserva();
        // Buscamos el objeto Destino
        Destino dst = destinoService.findById(inputDto.getIdDestino());
        // Recuperamos el autobús con el día y la hora indicadas
        List<Autobus> autobuses = dst.getAutobuses();
        Optional<Autobus> myBus =
                autobuses.stream().filter(e ->
                        sdf.format(e.getFecha()).equals(sdf.format(inputDto.getFechaReserva()))
                                && Objects.equals(e.getHoraSalida(), inputDto.getHoraSalida())).findFirst();
        if (myBus.isEmpty()) throw new NotFoundException("No hay ningún autobús el "+inputDto.getFechaReserva()+" a las "+inputDto.getHoraSalida());
        // Asignamos los campos del objeto Reserva
        rsv.setNombre(inputDto.getNombre());
        rsv.setApellido(inputDto.getApellido());
        rsv.setEmail(inputDto.getEmail());
        rsv.setTelefono(inputDto.getTelefono());
        rsv.setAutobus(myBus.get());
        // fechaReserva e idReserva se completan en el momento de añadir la reserva a la bd.
        return rsv;
    }

    public ReservaOutputDto toOutputDto(Reserva rsv) {
        ReservaOutputDto outDto = new ReservaOutputDto();
        outDto.setIdReserva(rsv.getIdReserva());
        outDto.setCiudadDestino(rsv.getAutobus().getDestino().getNombreDestino());
        outDto.setNombre(rsv.getNombre());
        outDto.setApellido(rsv.getApellido());
        outDto.setEmail(rsv.getEmail());
        outDto.setTelefono(rsv.getTelefono());
        outDto.setFechaReserva(sdf.format(rsv.getAutobus().getFecha()));
        outDto.setHoraReserva(rsv.getAutobus().getHoraSalida());
        switch (rsv.getStatus()) {
            case ACEPTADA: outDto.setStatus("ACEPTADA"); break;
            case RECHAZADA: outDto.setStatus("RECHAZADA"); break;
            case CONFIRMADA: outDto.setStatus("CONFIRMADA"); break;
            default: outDto.setStatus("INDEFINIDA");
        }
        return outDto;
    }
}

