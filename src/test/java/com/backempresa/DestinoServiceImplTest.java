package com.backempresa;

import com.backempresa.destino.application.DestinoService;
import com.backempresa.destino.domain.Destino;
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

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DestinoServiceImplTest {

    @Autowired
    private DestinoService destinoService;

    private final String idDestino1 = "VAL";
    private final String nombreDestino1 = "Valencia";
    private final String idDestino2 = "MAD";
    private final String nombreDestino2 = "Madrid";

    @BeforeAll
    void starting() {
        // Comprobamos que no hay ningún destino en la base de datos
        assertTrue(destinoService.findAll().isEmpty());
        // Añadimos dos para las pruebas siguientes
        DestinoInputDto inputDto1 = new DestinoInputDto(idDestino1, nombreDestino1);
        DestinoInputDto inputDto2 = new DestinoInputDto(idDestino2, nombreDestino2);
        destinoService.add(inputDto1);
        destinoService.add(inputDto2);
    }

    @Test
    void testFindAll() {
        assertEquals(2, destinoService.findAll().size());
    }

    @Test
    void testFindById() {
        // Prueba de un elemento existente
        Destino res = destinoService.findById(idDestino1);
        assertEquals(nombreDestino1, res.getNombreDestino());

        // Prueba de un elemento inexistente
        Throwable exception = assertThrows(NotFoundException.class, () -> destinoService.findById("ABCDE"));
        assertTrue(exception.getMessage().contains("ABCDE"));
    }

    @Test
    void testFindByDestino() {
        // Prueba de un elemento existente
        List<Destino> res = destinoService.findByDestino(nombreDestino2);
        assertFalse(res.isEmpty());
        assertEquals(idDestino2, res.get(0).getId());

        // Prueba de un elemento inexistente
        assertTrue(destinoService.findByDestino("ABCDE").isEmpty());
    }

    @Test
    void TestAddDel() {
        destinoService.add(new DestinoInputDto("BAR","Barcelona"));
        assertEquals(3,destinoService.findAll().size());
        destinoService.del("BAR");
        assertEquals(2,destinoService.findAll().size());

        assertThrows(NotFoundException.class, () -> destinoService.del("BAR"));
        assertThrows(UnprocesableException.class, () -> destinoService.add(new DestinoInputDto(null,"nombre")));
        assertThrows(UnprocesableException.class, () -> destinoService.add(new DestinoInputDto("id", null)));
        assertThrows(UnprocesableException.class, () -> destinoService.add(new DestinoInputDto(idDestino1,"nombre")));
    }

    @Test
    void TestPatch() {
        String cambioNombre = "Salamanca";
        Destino dst = destinoService.patch(idDestino1, new DestinoInputDto(idDestino1, cambioNombre));
        List<Destino> res = destinoService.findByDestino(cambioNombre);
        assertFalse(res.isEmpty());
        assertEquals(idDestino1, res.get(0).getId());
        // Lo dejamos como estaba antes.
        destinoService.patch(idDestino1, new DestinoInputDto(idDestino1,nombreDestino1));
    }

    @AfterAll
    void cleaning() {
        destinoService.del(idDestino1);
        destinoService.del(idDestino2);
        assertTrue(destinoService.findAll().isEmpty());
    }
}
