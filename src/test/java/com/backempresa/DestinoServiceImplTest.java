package com.backempresa;

import com.backempresa.destino.application.DestinoService;
import com.backempresa.destino.application.DestinoServiceImpl;
import com.backempresa.destino.domain.Destino;
import com.backempresa.destino.infrastructure.DestinoInputDto;
import com.backempresa.destino.infrastructure.DestinoRepo;
import com.backempresa.shared.NotFoundException;
import com.backempresa.shared.UnprocesableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.backempresa.destino.application.DestinoService.ID_LENGTH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DestinoServiceImplTest {

    DestinoRepo destinoRepo = mock(DestinoRepo.class);
    DestinoService destinoService = new DestinoServiceImpl(destinoRepo);

    Destino dst = new Destino();
    DestinoInputDto inputDto = new DestinoInputDto();
    String idDestino = "VAL";
    String nombreDestino = "Valencia";

    @Test
    void testFindAll() {

        // Testing con 1 elemento
        List<Destino> myList = new ArrayList<>();
        myList.add(dst);
        when(destinoRepo.findAll()).thenReturn(myList);
        List<Destino> res = destinoService.findAll();
        assertEquals(1, res.size());

        // Testing con 0 elementos
        myList = new ArrayList<>();
        when(destinoRepo.findAll()).thenReturn(myList);
        res = destinoService.findAll();
        assertEquals(new ArrayList<>(), res);
    }

    @Test
    void testFindById() {
        dst.setId(idDestino);
        Optional<Destino> optDst = Optional.of(dst);

        when(destinoRepo.findById(idDestino)).thenReturn(optDst);
        Destino res = destinoService.findById(idDestino);
        assertEquals(dst, res);

        when(destinoRepo.findById(idDestino)).thenReturn(Optional.empty());
        Throwable exception = assertThrows(NotFoundException.class, () -> destinoService.findById(idDestino));
        assertEquals("Destino "+idDestino+" no encontrado", exception.getMessage());
    }

    @Test
    void TestAdd() {
        inputDto.setId(idDestino);
        inputDto.setNombre(nombreDestino);
        dst.setId(idDestino);
        dst.setNombreDestino(nombreDestino);

        when(destinoRepo.findById(dst.getId())).thenReturn(Optional.empty());
        Destino res = destinoService.add(inputDto);
        assertEquals(dst.getId(), res.getId());
        assertEquals(dst.getNombreDestino(), res.getNombreDestino());

        inputDto.setId(null);
        Throwable exception = assertThrows(UnprocesableException.class, () -> destinoService.add(inputDto));
        assertEquals("Debe especificar un id de "+ID_LENGTH+" caracteres", exception.getMessage());
    }

    @Test
    void TestPatch() {
        inputDto.setId(idDestino);
        inputDto.setNombre("OtroNombre");
        dst.setId(idDestino);
        dst.setNombreDestino(nombreDestino);

        when(destinoRepo.findById(idDestino)).thenReturn(Optional.of(dst));
        Destino res = destinoService.patch(idDestino,inputDto);
        assertEquals(idDestino,res.getId());
        assertEquals("OtroNombre",res.getNombreDestino());
    }

    @Test
    void TestDel() {
        when(destinoRepo.findById(idDestino)).thenReturn(Optional.of(dst));
        assertDoesNotThrow(() -> destinoService.del(idDestino));
    }
}
