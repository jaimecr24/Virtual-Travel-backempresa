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
import com.backempresa.shared.NotPlaceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private final SimpleDateFormat sdf2 = new SimpleDateFormat("ddMMyyyy");
    private final SimpleDateFormat sdf3 = new SimpleDateFormat("ddMMyy");

    @Override
    public List<Reserva> findAll() {
        return reservaRepo.findAll();
    }

    @Override
    public Reserva findById(long id) {
        return reservaRepo.findById(id).orElseThrow(()->new NotFoundException("Reserva "+id+" no encontrada."));
    }

    @Override
    public Reserva findByIdentificador(String identificador) {
        return reservaRepo.findByIdentificador(identificador).orElseThrow(()->new NotFoundException("Identificador "+identificador+" no encontrado"));
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
    public ReservaOutputDto add(ReservaInputDto inputDto) throws NotFoundException, NotPlaceException {
        // Crea un objeto Reserva con los datos indicados en inputDto
        // Si hay sitio libre la reserva queda CONFIRMADA y añadida a la base de datos, sino se lanza excepción
        Reserva rsv = this.toReserva(inputDto);
        Autobus bus = rsv.getAutobus();
        int plazas = bus.getPlazasLibres();
        if (plazas==0) throw new NotPlaceException("No queda sitio en el autobús: reserva rechazada");
        rsv.setStatus(Reserva.STATUS.CONFIRMADA); // En backempresa la reserva pasa automáticamente a confirmada.
        rsv.setIdentificador(this.getIdentificadorReserva(bus));
        rsv.setFechaRegistro(new Date());
        bus.setPlazasLibres(plazas-1); // Actualizamos plazas disponibles.
        reservaRepo.save(rsv);
        return this.toOutputDto(rsv);
    }

    @Override
    @Transactional
    public ReservaOutputDto add(ReservaOutputDto outputDto) throws NotFoundException, NotPlaceException {
        // Aquí se añaden a la base de datos de backempresa las reservas ya aceptadas por backweb
        // Si la reserva no existe:
        // Crea objeto Reserva con los datos indicados en outputDto
        // No quedan plazas libres, status será RECHAZADA y NO se añade a la base de datos local.
        Optional<Reserva> optRsv = reservaRepo.findByIdentificador(outputDto.getIdentificador());
        if (optRsv.isEmpty()) {
            Reserva rsv = this.toReserva(outputDto);
            rsv.setFechaRegistro(new Date());
            int plazas = rsv.getAutobus().getPlazasLibres();
            if (plazas==0) throw new NotPlaceException("No queda sitio en el autobús: reserva cancelada");
            rsv.getAutobus().setPlazasLibres(plazas-1); // Actualizamos el número de plazas disponibles.
            reservaRepo.save(rsv);
            return this.toOutputDto(rsv);
        }
        else return null; // Si ya existe, no hacemos nada y devolvemos null para indicarlo.
    }

    @Override
    @Transactional
    public long getPlazasLibres(String destino, String fecha, float hora) {
        List<Destino> dstList = destinoService.findByDestino(destino);
        if (dstList.size()==0) throw new NotFoundException("Destino no encontrado");
        Destino dst = dstList.get(0);
        List<Autobus> busList = dst.getAutobuses();
        Optional<Autobus> optBus = busList.stream().filter(e
                -> sdf.format(e.getFecha()).equals(fecha) && e.getHoraSalida()==hora).findFirst();
        return optBus.map(Autobus::getPlazasLibres).orElse(0);
    }

    private String getIdentificadorReserva(Autobus bus){
        // Obtiene el identificador de la próxima reserva en el bus, limitado a 99 reservas por bus
        // Si se necesitan más, debe cambiarse el formato del último elemento del identificador a %03d
        return bus.getDestino().getNombreDestino().substring(0,3).toUpperCase()
                + sdf3.format(bus.getFecha())
                + String.format("%02d",bus.getHoraSalida().intValue())
                + String.format("%02d",this.numReservasAceptadas(bus));
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
        return rsv;
    }

    public Reserva toReserva(ReservaOutputDto outputDto) throws NotFoundException {
        // Crea una reserva con los datos de outputDto
        Reserva rsv = new Reserva();
        List<Destino> ldst = destinoService.findByDestino(outputDto.getCiudadDestino());
        if (ldst.isEmpty()) throw new NotFoundException("Destino no contrado: "+outputDto.getCiudadDestino());
        List<Autobus> autobuses = ldst.get(0).getAutobuses();
        Optional<Autobus> myBus =
                autobuses.stream().filter(e ->
                        sdf.format(e.getFecha()).equals(outputDto.getFechaReserva())
                                && Objects.equals(e.getHoraSalida(), outputDto.getHoraReserva())).findFirst();
        if (myBus.isEmpty()) throw new NotFoundException("No hay ningún autobús el "+outputDto.getFechaReserva()+" a las "+outputDto.getHoraReserva());
        rsv.setNombre(outputDto.getNombre());
        rsv.setApellido(outputDto.getApellido());
        rsv.setEmail(outputDto.getEmail());
        rsv.setTelefono(outputDto.getTelefono());
        rsv.setAutobus(myBus.get());
        rsv.setIdentificador(outputDto.getIdentificador());
        switch (outputDto.getStatus()) {
            case "ACEPTADA": rsv.setStatus(Reserva.STATUS.ACEPTADA);
            case "RECHAZADA": rsv.setStatus(Reserva.STATUS.RECHAZADA);
            case "CONFIRMADA": rsv.setStatus(Reserva.STATUS.CONFIRMADA);
        }
        return rsv;
    }

    public ReservaOutputDto toOutputDto(Reserva rsv) {
        ReservaOutputDto outDto = new ReservaOutputDto();
        outDto.setIdReserva(rsv.getIdReserva());
        outDto.setIdentificador(rsv.getIdentificador());
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

