package com.backempresa;

import com.backempresa.autobus.application.AutobusService;
import com.backempresa.autobus.infrastructure.AutobusInputDto;
import com.backempresa.destino.application.DestinoService;
import com.backempresa.destino.infrastructure.DestinoInputDto;
import com.backempresa.reserva.application.ReservaService;
import com.backempresa.reserva.domain.Reserva;
import com.backempresa.reserva.infrastructure.*;
import com.backempresa.shared.NotFoundException;
import com.backempresa.shared.NotPlaceException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReservaServiceImplTest {

    @Autowired
    ReservaService reservaService;

    @Autowired
    AutobusService autobusService;

    @Autowired
    DestinoService destinoService;

    @Autowired
    SimpleDateFormat sdf1, sdf2, sdf3;

    private final String idDestino1 = "VAL";
    private final String nombreDestino1 = "Valencia";
    private final String fechaStr = "010522";
    private final Float horaSalida = 12F;
    private final String email1 = "email1@email.com";
    long idReserva1;
    long idReserva2;

    @BeforeAll
    void starting() throws ParseException {

        assertTrue(reservaService.findAll().isEmpty());
        assertTrue(autobusService.findAll().isEmpty());
        assertTrue(destinoService.findAll().isEmpty());
        // Añadimos un destino
        destinoService.add(new DestinoInputDto(idDestino1, nombreDestino1));
        // Añadimos dos autobuses a ese destino, cada uno con dos plazas libres.
        Date fecha = sdf3.parse(fechaStr);
        AutobusInputDto inputDto1 = new AutobusInputDto(idDestino1, fecha, horaSalida,2);
        AutobusInputDto inputDto2 = new AutobusInputDto(idDestino1, fecha, horaSalida+1,2);
        autobusService.add(inputDto1);
        autobusService.add(inputDto2);
        // Añadimos una reserva en cada autobús
        assertTrue(reservaService.findAll().isEmpty());
        ReservaInputDto rsvDto1 = new ReservaInputDto(idDestino1,"nombre1","apellido1","111111",email1,fecha,horaSalida);
        ReservaInputDto rsvDto2 = new ReservaInputDto(idDestino1,"nombre1","apellido1","111111",email1,fecha,horaSalida+1);
        idReserva1 = reservaService.add(rsvDto1).getIdReserva();
        idReserva2 = reservaService.add(rsvDto2).getIdReserva();
        // Ahora queda 1 plaza disponible en cada autobús
    }

    @Test
    void testFindAll() {
        assertEquals(2, reservaService.findAll().size());
    }

    @Test
    void testFindById() {
        // Búsqueda de un elemento existente
        assertEquals(idReserva1, reservaService.findById(idReserva1).getIdReserva());
        // Búsqueda de un elemento inexistente
        assertThrows(NotFoundException.class, () -> reservaService.findById(99999L));
    }

    @Test
    void testFindByIdentificador() {
        String identificador = reservaService.findById(idReserva1).getIdentificador();
        // Búsqueda elemento existente
        assertEquals(identificador, reservaService.findByIdentificador(identificador).getIdentificador());
        // Búsqueda elemento inexistente
        assertThrows(NotFoundException.class, () -> reservaService.findByIdentificador("UAUAUAUA"));
    }

    // Datos necesarios para las pruebas de findDisponible y findReservas
    private final String fechaInf = "01012022";
    private final String fechaSup = "01122022";
    private final String horaInf = "00";
    private final String horaSup = "23";
    private final String fechaStr2 = "020522"; // Un día después de fechaStr

    @Test
    @Transactional
    void testFindDisponible() throws ParseException {
        String destino = "Lugo";
        List<ReservaDisponibleOutputDto> listDisp;
        Date fecha = sdf3.parse(fechaStr);

        // Destino inexistente
        assertTrue(reservaService.findDisponible(destino, fechaInf, fechaSup, horaInf, horaSup).isEmpty());

        // Búsqueda con plazas libres (quedaba 1 plaza en cada autobús)
        listDisp = reservaService.findDisponible(nombreDestino1, fechaInf, fechaSup, horaInf, horaSup);
        assertEquals(2,listDisp.size());

        // Añadimos una reserva en el primer autobus para dejar el número de plazas a 0, y hacemos nueva búsqueda
        ReservaInputDto rsvDto = new ReservaInputDto(idDestino1,"nombre1","apellido1","111111","email1@email.com",fecha,horaSalida);
        long idReserva3 = reservaService.add(rsvDto).getIdReserva();
        listDisp = reservaService.findDisponible(nombreDestino1, fechaInf, fechaSup, horaInf, horaSup);
        assertEquals(1,listDisp.size());
        reservaService.del(idReserva3);

        // Búsqueda con fechaSup, horaInf y horaSup == null
        listDisp = reservaService.findDisponible(nombreDestino1, fechaInf, null, null, null);
        assertEquals(2,listDisp.size()); // Comprobamos además que la eliminación ha aumentado el número de plazas.

        // Búsqueda poniendo en fechaInf el límite superior
        listDisp = reservaService.findDisponible(nombreDestino1, fechaSup, null, null, null);
        assertTrue(listDisp.isEmpty());

        // Búsqueda en un rango menor de horas para dejar fuera uno de los autobuses
        String horaStr = String.format("%02d",horaSalida.intValue());
        listDisp = reservaService.findDisponible(nombreDestino1, fechaInf, fechaSup, horaStr, horaStr);
        assertEquals(1,listDisp.size());
        assertEquals(horaSalida, listDisp.get(0).getHoraReserva());  // Comprobamos que la hora en el resultado es la misma.

        String fechaStr = sdf2.format(fecha);
        listDisp = reservaService.findDisponible(nombreDestino1, fechaStr, fechaStr, horaInf, horaSup);
        assertEquals(2,listDisp.size());
        assertEquals(sdf1.format(fecha), listDisp.get(0).getFechaReserva());  // Comprobamos que la fecha del resultado es la bus2
    }

    @Test
    @Transactional
    void testFindReservas() throws ParseException {
        List<ReservaOutputDto> res = reservaService.findReservas(nombreDestino1, fechaInf, fechaSup, horaInf, horaSup);
        assertEquals(2, res.size());
    }

    @Test
    @Transactional
    void testFindReserva() throws ParseException {
        Date fecha = sdf3.parse(fechaStr);
        CorreoInputDto correoDto = new CorreoInputDto(nombreDestino1,email1,fecha,horaSalida);
        ReservaOutputDto res = reservaService.findReserva(correoDto);
        assertEquals(email1,res.getEmail());
    }

    @Test
    void testAddDelInputDto() throws ParseException {
        Date fecha = sdf3.parse(fechaStr);
        // Añadimos una reserva en el primer autobus.
        ReservaInputDto rsvDto = new ReservaInputDto(idDestino1,"nombre1","apellido1","111111",email1,fecha,horaSalida);
        ReservaOutputDto outDto = reservaService.add(rsvDto);
        assertEquals(3, reservaService.findAll().size());
        assertEquals("CONFIRMADA", outDto.getStatus());
        // Quedan 0 plazas: debe producirse una excepción al intentar añadir otra reserva para el mismo autobús
        assertThrows(NotPlaceException.class, () -> reservaService.add(rsvDto));
        // Eliminamos la reserva.
        reservaService.del(outDto.getIdReserva());
        assertEquals(2, reservaService.findAll().size());
    }

    @Test
    void testAddOutputDto() throws ParseException {
        // Si intentamos añadir una reserva ya existente, devuelve null
        Reserva rsv = reservaService.findById(idReserva1);
        ReservaOutputDto rsvDto = new ReservaOutputDto(
                rsv.getIdReserva(), rsv.getIdentificador(), nombreDestino1,
                "nombre1", "apellido1", "11111", email1, fechaStr, horaSalida, "ACEPTADA");
        ReservaOutputDto res = reservaService.add(rsvDto);
        assertNull(res);

        //Sino la añade y devuelve el ReservaOutputDto
        reservaService.del(idReserva1);
        res = reservaService.add(rsvDto);
        assertNotNull(res);
        idReserva1 = res.getIdReserva();
    }

    @AfterAll
    void cleaning() throws ParseException {
        Date fecha = sdf3.parse(fechaStr);
        reservaService.del(idReserva1);
        reservaService.del(idReserva2);
        autobusService.del(autobusService.getIdBus(idDestino1, fecha, horaSalida));
        autobusService.del(autobusService.getIdBus(idDestino1, fecha, horaSalida+1));
        destinoService.del(idDestino1);
        assertTrue(reservaService.findAll().isEmpty());
        assertTrue(autobusService.findAll().isEmpty());
        assertTrue(destinoService.findAll().isEmpty());
    }

}
