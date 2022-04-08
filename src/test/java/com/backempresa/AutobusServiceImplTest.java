package com.backempresa;

import com.backempresa.autobus.application.AutobusService;
import com.backempresa.autobus.domain.Autobus;
import com.backempresa.autobus.infrastructure.AutobusInputDto;
import com.backempresa.destino.application.DestinoService;
import com.backempresa.destino.infrastructure.DestinoInputDto;
import com.backempresa.shared.NotFoundException;
import com.backempresa.shared.UnprocesableException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.backempresa.autobus.application.AutobusService.MAX_PLAZAS;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AutobusServiceImplTest {

    @Autowired
    AutobusService autobusService;

    @Autowired
    DestinoService destinoService;

    @Autowired
    SimpleDateFormat sdf3;

    private final String idDestino1 = "VAL";
    private final String fechaStr = "010522";
    private final Float horaSalida = 12F;

    @BeforeAll
    void starting() throws ParseException {

        assertTrue(destinoService.findAll().isEmpty());
        assertTrue(autobusService.findAll().isEmpty());
        // Añadimos un destino
        String nombreDestino1 = "Valencia";
        DestinoInputDto dstInputDto1 = new DestinoInputDto(idDestino1, nombreDestino1);
        destinoService.add(dstInputDto1);
        // Comprobamos que no hay ningún autobus en la base de datos
        assertTrue(autobusService.findAll().isEmpty());
        // Añadimos dos autobuses en horas consecutivas para las pruebas siguientes
        Date fecha = sdf3.parse(fechaStr);
        AutobusInputDto inputDto1 = new AutobusInputDto(idDestino1, fecha, horaSalida,2);
        AutobusInputDto inputDto2 = new AutobusInputDto(idDestino1, fecha, horaSalida+1,2);
        autobusService.add(inputDto1);
        autobusService.add(inputDto2);
    }

    @Test
    void testFindAll() {
        assertEquals(2,autobusService.findAll().size());
    }

    @Test
    void testFindById() throws ParseException {
        Date fecha = sdf3.parse(fechaStr);
        String idBus = autobusService.getIdBus(idDestino1, fecha, horaSalida);
        // Búsqueda de un elemento existente
        Autobus res = autobusService.findById(idBus);
        assertEquals(idBus, res.getId());
        // Búsqueda de un elemento inexistente
        assertThrows(NotFoundException.class, () -> autobusService.findById("AAA"));
    }

    @Test
    void testAddDel() throws ParseException {
        Date fecha = sdf3.parse(fechaStr);
        // Añadimos un autobús una hora más tarde.
        String idBus = autobusService.getIdBus(idDestino1, fecha, horaSalida+2); // Lo necesitamos para eliminarlo después.
        Autobus bus = autobusService.add(new AutobusInputDto(idDestino1, fecha, horaSalida+2, 2));
        // Después de añadir uno, comprobamos el número total de autobuses.
        assertEquals(3,autobusService.findAll().size());
        assertEquals(MAX_PLAZAS, bus.getMaxPlazas());
        // El autobús ya existe
        assertThrows(UnprocesableException.class, () -> autobusService.add(new AutobusInputDto(idDestino1, fecha, horaSalida, 2)));
        // Eliminamos el que hemos añadido y comprobamos el número de autobuses
        autobusService.del(idBus);
        assertEquals(2,autobusService.findAll().size());
        Throwable exception = assertThrows(NotFoundException.class, () -> autobusService.del(idBus));
    }

    @AfterAll
    void cleaning() throws ParseException {
        Date fecha = sdf3.parse(fechaStr);
        String idBus1 = autobusService.getIdBus(idDestino1, fecha, horaSalida);
        String idBus2 = autobusService.getIdBus(idDestino1, fecha, horaSalida+1);
        autobusService.del(idBus1);
        autobusService.del(idBus2);
        destinoService.del(idDestino1);
        assertTrue(autobusService.findAll().isEmpty());
        assertTrue(destinoService.findAll().isEmpty());
    }
}
