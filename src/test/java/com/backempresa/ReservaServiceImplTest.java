package com.backempresa;

import com.backempresa.autobus.application.AutobusService;
import com.backempresa.autobus.application.AutobusServiceImpl;
import com.backempresa.autobus.domain.Autobus;
import com.backempresa.destino.application.DestinoService;
import com.backempresa.destino.application.DestinoServiceImpl;
import com.backempresa.destino.domain.Destino;
import com.backempresa.reserva.application.ReservaService;
import com.backempresa.reserva.application.ReservaServiceImpl;
import com.backempresa.reserva.domain.Reserva;
import com.backempresa.reserva.infrastructure.ReservaDisponibleOutputDto;
import com.backempresa.reserva.infrastructure.ReservaInputDto;
import com.backempresa.reserva.infrastructure.ReservaOutputDto;
import com.backempresa.reserva.infrastructure.ReservaRepo;
import com.backempresa.shared.NotFoundException;
import com.backempresa.shared.NotPlaceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.backempresa.autobus.application.AutobusService.MAX_PLAZAS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReservaServiceImplTest {

    ReservaRepo reservaRepo = mock(ReservaRepo.class);
    DestinoService destinoService = mock(DestinoServiceImpl.class);
    AutobusService autobusService = mock(AutobusServiceImpl.class);
    SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat sdf2 = new SimpleDateFormat("ddMMyyyy");
    SimpleDateFormat sdf3 = new SimpleDateFormat("ddMMyy");

    ReservaService reservaService = new ReservaServiceImpl(
            reservaRepo, destinoService, autobusService, sdf1, sdf2, sdf3);

    Reserva rsv = new Reserva();
    ReservaInputDto rsvInputDto = new ReservaInputDto();
    ReservaOutputDto rsvOutputDto = new ReservaOutputDto();
    List<Destino> listDst = new ArrayList<>();
    List<Autobus> listBus = new ArrayList<>();

    @Test
    void testFindAll() {
        // Testing con 1 elemento
        List<Reserva> myList = new ArrayList<>();
        myList.add(rsv);
        when(reservaRepo.findAll()).thenReturn(myList);
        List<Reserva> res = reservaService.findAll();
        assertEquals(1, res.size());

        // Testing con 0 elementos
        myList = new ArrayList<>();
        when(reservaRepo.findAll()).thenReturn(myList);
        res = reservaService.findAll();
        assertEquals(new ArrayList<>(), res);
    }

    @Test
    void testFindById() {
        long id = 1L;

        rsv.setIdReserva(id);
        Optional<Reserva> optRes = Optional.of(rsv);

        // Búsqueda de un elemento existente
        when(reservaRepo.findById(id)).thenReturn(optRes);
        Reserva res = reservaService.findById(id);
        assertEquals(rsv, res);

        // Búsqueda de un elemento inexistente
        when(reservaRepo.findById(id)).thenReturn(Optional.empty());
        Throwable exception = assertThrows(NotFoundException.class, () -> reservaService.findById(id));
        assertEquals("Reserva "+id+" no encontrada.", exception.getMessage());
    }

    @Test
    void testFindByIdentificador() {
        String identificador = "VAL0102202200";
        rsv.setIdentificador(identificador);
        Optional<Reserva> optRes = Optional.of(rsv);

        // Búsqueda elemento existente
        when(reservaRepo.findByIdentificador(identificador)).thenReturn(optRes);
        Reserva res = reservaService.findByIdentificador(identificador);
        assertEquals(rsv, res);

        // Búsqueda elemento inexistente
        when(reservaRepo.findByIdentificador(identificador)).thenReturn(Optional.empty());
        Throwable exception = assertThrows(NotFoundException.class, () -> reservaService.findByIdentificador(identificador));
        assertEquals("Identificador "+identificador+" no encontrado", exception.getMessage());
    }

    // Datos necesarios para las pruebas de findDisponible y findReservas
    String idDestino = "VAL";
    String destino = "Valencia";
    String fechaInf = "01012022";
    String fechaSup = "01122022";
    String horaInf = "00";
    String horaSup = "23";
    Float hora = 12F;

    @Test
    void testFindDisponible() throws ParseException {

        Date fecha = sdf2.parse("01052022");
        Date fecha2 = sdf2.parse("02052022"); // Un día más que fecha
        Autobus bus = new Autobus();
        Destino dst = new Destino();
        dst.setNombreDestino(destino);
        dst.setId(idDestino);
        bus.setDestino(dst);
        bus.setId(idDestino + sdf2.format(fecha) + String.format("%02d",hora.intValue()));
        bus.setFecha(fecha);
        bus.setHoraSalida(hora);
        listDst.clear(); listDst.add(dst);
        listBus.clear(); listBus.add(bus);
        dst.setAutobuses(listBus);

        // Devuelve una lista con un único elemento: dst
        when(destinoService.findByDestino(destino)).thenReturn(listDst);
        List<ReservaDisponibleOutputDto> listDisp;

        // Búsqueda con 1 plaza libre
        bus.setPlazasLibres(1);
        listDisp = reservaService.findDisponible(destino, fechaInf, fechaSup, horaInf, horaSup);
        assertEquals(1,listDisp.size());

        // Búsqueda con 0 plazas libres
        bus.setPlazasLibres(0);
        listDisp = reservaService.findDisponible(destino, fechaInf, fechaSup, horaInf, horaSup);
        assertEquals(0,listDisp.size());

        // Búsqueda con 1 plaza libre, pero con fechaSup, horaInf y horaSup == null
        bus.setPlazasLibres(1);
        listDisp = reservaService.findDisponible(destino, fechaInf, null, null, null);
        assertEquals(1,listDisp.size());

        // Búsqueda poniendo en fechaInf el límite superior
        listDisp = reservaService.findDisponible(destino, fechaSup, null, null, null);
        assertEquals(0,listDisp.size());

        // Búsqueda con 2 autobuses en la lista
        Autobus bus2 = new Autobus();
        bus2.setDestino(dst);
        bus2.setId(idDestino + sdf2.format(fecha2) + String.format("%02d",hora.intValue()+1));
        bus2.setFecha(fecha2);
        bus2.setHoraSalida(hora+1);
        bus2.setPlazasLibres(2);
        listBus.add(bus2);
        listDisp = reservaService.findDisponible(destino, fechaInf, fechaSup, horaInf, horaSup);
        assertEquals(2,listDisp.size());

        // Búsqueda en un rango menor de horas para dejar fuera a bus2
        String horaStr = String.format("%02d",hora.intValue());
        listDisp = reservaService.findDisponible(destino, fechaInf, fechaSup, horaStr, horaStr);
        assertEquals(1,listDisp.size());
        assertEquals(hora, listDisp.get(0).getHoraReserva());  // Comprobamos que la hora en el resultado es la misma.

        // Búsqueda en un rango menor de fechas para dejar fuera a bus1
        String fechaStr = sdf2.format(fecha2);
        listDisp = reservaService.findDisponible(destino, fechaStr, fechaStr, horaInf, horaSup);
        assertEquals(1,listDisp.size());
        assertEquals(bus2.getFecha(), sdf1.parse(listDisp.get(0).getFechaReserva()));  // Comprobamos que la fecha del resultado es la bus2
    }

    @Test
    void testFindReservas() throws ParseException {
        // Creamos dos autobuses para un mismo destino
        Date fecha = sdf2.parse("01052022");
        Date fecha2 = sdf2.parse("02052022"); // Un día más que fecha
        Autobus bus = new Autobus();
        Destino dst = new Destino();
        dst.setNombreDestino(destino);
        dst.setId(idDestino);
        bus.setDestino(dst);
        bus.setId(idDestino + sdf2.format(fecha) + String.format("%02d",hora.intValue()));
        bus.setFecha(fecha);
        bus.setHoraSalida(hora);
        listDst.clear(); listDst.add(dst);
        listBus.clear(); listBus.add(bus);
        Autobus bus2 = new Autobus();
        bus2.setDestino(dst);
        bus2.setId(idDestino + sdf2.format(fecha2) + String.format("%02d",hora.intValue()+1));
        bus2.setFecha(fecha2);
        bus2.setHoraSalida(hora+1);
        bus2.setPlazasLibres(2);
        listBus.add(bus2);
        dst.setAutobuses(listBus);

        // Anotamos dos reservas. Una en cada autobús
        Reserva rsv1 = new Reserva();
        Reserva rsv2 = new Reserva();
        rsv1.setIdentificador(bus.getId()+"01");
        rsv1.setAutobus(bus);
        rsv2.setIdentificador(bus2.getId()+"01");
        rsv2.setAutobus(bus2);
        List<Reserva> listRsv1 = new ArrayList<>(); listRsv1.add(rsv1);
        List<Reserva> listRsv2 = new ArrayList<>(); listRsv2.add(rsv2);
        bus.setReservas(listRsv1);
        bus2.setReservas(listRsv2);

        listDst.clear(); listDst.add(dst);
        when(destinoService.findByDestino(destino)).thenReturn(listDst);
        List<ReservaOutputDto> res = reservaService.findReservas(destino, fechaInf, fechaSup, horaInf, horaSup);
        assertEquals(2, res.size());
    }

    @Test
    void testAddInputDto() throws ParseException {
        Date fecha = sdf2.parse("01052022");
        rsvInputDto.setIdDestino(idDestino);
        rsvInputDto.setNombre("nombre1");
        rsvInputDto.setApellido("apellido1");
        rsvInputDto.setEmail("email1@email.com");
        rsvInputDto.setTelefono("111-11-111");
        rsvInputDto.setFechaReserva(sdf2.parse("01052022"));
        rsvInputDto.setHoraSalida(12F);
        Destino dst = new Destino();
        dst.setNombreDestino(destino);
        dst.setId(idDestino);
        Autobus bus = new Autobus();
        String idBus = idDestino + sdf2.format(fecha) + "12";
        bus.setId(idBus);
        bus.setPlazasLibres(1); // 1 plaza libre
        bus.setMaxPlazas(MAX_PLAZAS);
        bus.setDestino(dst);
        bus.setFecha(fecha);
        bus.setHoraSalida(12F);
        when(destinoService.findById(idDestino)).thenReturn(dst);
        when(autobusService.getIdBus(idDestino, fecha, 12F)).thenReturn(idBus);
        when(autobusService.findById(idBus)).thenReturn(bus);

        ReservaOutputDto res = reservaService.add(rsvInputDto);
        assertEquals("CONFIRMADA",res.getStatus());

        bus.setPlazasLibres(0);
        Throwable ex = assertThrows(NotPlaceException.class, () -> reservaService.add(rsvInputDto));
        assertTrue(ex.getMessage().contains("reserva rechazada"));
    }
}
