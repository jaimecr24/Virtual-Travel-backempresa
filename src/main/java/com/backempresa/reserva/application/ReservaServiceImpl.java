package com.backempresa.reserva.application;

import com.backempresa.autobus.application.AutobusService;
import com.backempresa.autobus.domain.Autobus;
import com.backempresa.destino.application.DestinoService;
import com.backempresa.destino.domain.Destino;
import com.backempresa.reserva.domain.Reserva;
import com.backempresa.reserva.infrastructure.*;
import com.backempresa.shared.NotFoundException;
import com.backempresa.shared.NotPlaceException;
import com.backempresa.shared.UnprocesableException;
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

    @Autowired
    AutobusService autobusService;

    @Autowired
    SimpleDateFormat sdf1, sdf2, sdf3;

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
        if (dstList.size()!=1) throw new NotFoundException("Destino no encontrado o duplicado");
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
        } catch(ParseException e) {
            throw new UnprocesableException("Error en el formato de las fechas: "+e.getMessage());
        }
    }

    @Override
    public List<ReservaOutputDto> findReservas(String destino, String fechaInferior, String fechaSuperior, String horaInferior, String horaSuperior)
    {
        List<Destino> dstList = destinoService.findByDestino(destino);
        if (dstList.size()==0) return new ArrayList<>();
        Destino dst = dstList.get(0);
        List<Autobus> busList = dst.getAutobuses();
        try {
            Date fInf = sdf2.parse(fechaInferior);
            Date fSup = (fechaSuperior != null) ? sdf2.parse(fechaSuperior) : null;
            Float hInf = (horaInferior != null) ? Float.parseFloat(horaInferior) : 0F;
            Float hSup = (horaSuperior != null) ? Float.parseFloat(horaSuperior) : 24F;
            Optional<List<Reserva>> reservas = busList.stream().filter(e ->
                            e.getFecha().compareTo(fInf) >= 0
                                    && (fSup==null || e.getFecha().compareTo(fSup)<=0)
                                    && e.getHoraSalida()>=hInf && e.getHoraSalida()<=hSup).map(Autobus::getReservas)
                    .reduce((l1,l2)-> { l1.addAll(l2); return l1; });
            return reservas.map(reservaList -> reservaList.stream().map(this::toOutputDto).collect(Collectors.toList()))
                    .orElseGet(ArrayList::new);
        } catch(ParseException e) {
            throw new UnprocesableException("Error en el formato de las fechas: "+e.getMessage());
        }
    }

    @Override
    public ReservaOutputDto findReserva(CorreoInputDto correoDto) {
        List<Destino> dstList = destinoService.findByDestino(correoDto.getCiudadDestino());
        if (dstList.size()==0) throw new NotFoundException("Reserva no encontrada");
        Destino dst = dstList.get(0);
        String idBus = autobusService.getIdBus(dst.getId(),correoDto.getFechaReserva(),correoDto.getHoraReserva());
        Autobus bus = autobusService.findById(idBus);
        // Buscamos la primera que coincida con el email de correoDto
        Optional<Reserva> optRsv = bus.getReservas().stream().filter(e->e.getEmail().equals(correoDto.getEmail())).findFirst();

        return optRsv.map(this::toOutputDto).orElseThrow(()->new NotFoundException("Reserva no encontrada"));
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
            rsv.setStatus(Reserva.STATUS.CONFIRMADA); // Reservas aceptadas en backweb y confirmadas en backempresa
            reservaRepo.save(rsv);
            return this.toOutputDto(rsv);
        }
        else return null; // Si ya existe, no hacemos nada y devolvemos null para indicarlo.
    }

    @Override
    @Transactional
    public long getPlazasLibres(String destino, Date fecha, float hora) {
        List<Destino> dstList = destinoService.findByDestino(destino);
        if (dstList.size()==0) throw new NotFoundException("Destino no encontrado");
        Destino dst = dstList.get(0);
        String idBus = autobusService.getIdBus(dst.getId(),fecha,hora);
        Autobus bus = autobusService.findById(idBus);
        return bus.getPlazasLibres();
    }

    @Override
    public void del(long id) {
        // TODO: Poner estado de la reserva en CANCELADA ??
    }

    private String getIdentificadorReserva(Autobus bus){
        // Obtiene el identificador de la próxima reserva en el bus, limitado a 99 reservas por bus
        // Si se necesitan más, debe cambiarse el formato del último elemento del identificador a %03d
        return bus.getId() + String.format("%02d",bus.getMaxPlazas()-bus.getPlazasLibres()+1);
    }

    private long numReservasAceptadas(Autobus bus) {
        return bus.getReservas().stream().filter(e ->
                e.getStatus() == Reserva.STATUS.ACEPTADA || e.getStatus() == Reserva.STATUS.CONFIRMADA).count();
    }

    public Reserva toReserva(ReservaInputDto inputDto) throws NotFoundException {
        // Creamos el objeto
        Reserva rsv = new Reserva();
        // Buscamos el objeto Destino
        Destino dst = destinoService.findById(inputDto.getIdDestino());
        // Recuperamos el autobús con el día y la hora indicadas
        String idBus = autobusService.getIdBus(inputDto.getIdDestino(),inputDto.getFechaReserva(),inputDto.getHoraSalida());
        Autobus bus = autobusService.findById(idBus);
        // Asignamos los campos del objeto Reserva
        rsv.setNombre(inputDto.getNombre());
        rsv.setApellido(inputDto.getApellido());
        rsv.setEmail(inputDto.getEmail());
        rsv.setTelefono(inputDto.getTelefono());
        rsv.setAutobus(bus);
        // fechaReserva e idReserva se completan en el momento de añadir la reserva a la bd.
        return rsv;
    }

    public Reserva toReserva(ReservaOutputDto outputDto) throws NotFoundException {
        // Crea una reserva con los datos de outputDto
        String idBus = this.getIdBus(outputDto.getIdentificador());
        Autobus bus = autobusService.findById(idBus);
        Reserva rsv = new Reserva();
        rsv.setNombre(outputDto.getNombre());
        rsv.setApellido(outputDto.getApellido());
        rsv.setEmail(outputDto.getEmail());
        rsv.setTelefono(outputDto.getTelefono());
        rsv.setAutobus(bus);
        rsv.setIdentificador(outputDto.getIdentificador());
        switch (outputDto.getStatus()) {
            case "ACEPTADA": rsv.setStatus(Reserva.STATUS.ACEPTADA); break;
            case "RECHAZADA": rsv.setStatus(Reserva.STATUS.RECHAZADA); break;
            case "CONFIRMADA": rsv.setStatus(Reserva.STATUS.CONFIRMADA); break;
        }
        return rsv;
    }

    private String getIdBus(String identificadorReserva) {
        return identificadorReserva.substring(0, autobusService.ID_LENGTH);
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
        outDto.setFechaReserva(sdf1.format(rsv.getAutobus().getFecha()));
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

