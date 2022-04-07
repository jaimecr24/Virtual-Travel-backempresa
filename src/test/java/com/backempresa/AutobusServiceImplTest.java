package com.backempresa;

import com.backempresa.autobus.application.AutobusService;
import com.backempresa.autobus.application.AutobusServiceImpl;
import com.backempresa.autobus.domain.Autobus;
import com.backempresa.autobus.infrastructure.AutobusInputDto;
import com.backempresa.autobus.infrastructure.AutobusRepo;
import com.backempresa.destino.application.DestinoService;
import com.backempresa.destino.application.DestinoServiceImpl;
import com.backempresa.destino.domain.Destino;
import com.backempresa.shared.NotFoundException;
import com.backempresa.shared.UnprocesableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

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
public class AutobusServiceImplTest {

    DestinoService destinoService = mock(DestinoServiceImpl.class);
    AutobusRepo autobusRepo = mock(AutobusRepo.class);
    SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat sdf2 = new SimpleDateFormat("ddMMyyyy");
    SimpleDateFormat sdf3 = new SimpleDateFormat("ddMMyy");

    AutobusService autobusService = new AutobusServiceImpl(autobusRepo, destinoService, sdf1, sdf2, sdf3);

    Autobus bus = new Autobus();
    AutobusInputDto inputDto = new AutobusInputDto();
    String idDestino = "VAL";
    String idAutobus = "VAL02042212";
    Date fecha = new Date();
    Float horaSalida = 12F;
    int plazasLibres = 2;

    @Test
    void testFindAll() {

        // Testing con 1 elemento
        List<Autobus> myList = new ArrayList<>();
        myList.add(bus);
        when(autobusRepo.findAll()).thenReturn(myList);
        List<Autobus> res = autobusService.findAll();
        assertEquals(1, res.size());

        // Testing con 0 elementos
        myList = new ArrayList<>();
        when(autobusRepo.findAll()).thenReturn(myList);
        res = autobusService.findAll();
        assertEquals(new ArrayList<>(), res);
    }

    @Test
    void testFindById() {
        bus.setId(idAutobus);
        Optional<Autobus> optBus = Optional.of(bus);

        // Búsqueda de un elemento existente
        when(autobusRepo.findById(idAutobus)).thenReturn(optBus);
        Autobus res = autobusService.findById(idAutobus);
        assertEquals(bus, res);

        // Búsqueda de un elemento inexistente
        when(autobusRepo.findById(idAutobus)).thenReturn(Optional.empty());
        Throwable exception = assertThrows(NotFoundException.class, () -> autobusService.findById(idAutobus));
        assertEquals("El autobús "+idAutobus+" no existe", exception.getMessage());
    }

    @Test
    void testAdd() {
        inputDto.setIdDestino(idDestino);
        inputDto.setFecha(fecha);
        inputDto.setHoraSalida(horaSalida);
        inputDto.setPlazasLibres(plazasLibres);
        String id = autobusService.getIdBus(idDestino,fecha,horaSalida);
        Destino ds = new Destino();
        ds.setId(idDestino);

        when(destinoService.findById(idDestino)).thenReturn(ds);
        when(autobusRepo.findById(id)).thenReturn(Optional.empty());

        // Añadir autobús ok
        Autobus res = autobusService.add(inputDto);
        assertEquals(id, res.getId());
        assertEquals(idDestino, res.getDestino().getId());
        assertEquals(fecha, res.getFecha());
        assertEquals(horaSalida, res.getHoraSalida());
        assertEquals(MAX_PLAZAS, res.getMaxPlazas());
        assertEquals(plazasLibres, res.getPlazasLibres());

        // El autobús ya existe
        when(autobusRepo.findById(id)).thenReturn(Optional.of(bus));
        Throwable exception = assertThrows(UnprocesableException.class, () -> autobusService.add(inputDto));
        assertTrue(exception.getMessage().contains("Ya existe un autobús"));
    }
}
