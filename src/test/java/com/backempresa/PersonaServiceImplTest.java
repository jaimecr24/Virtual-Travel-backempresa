package com.backempresa;

import com.backempresa.persona.application.PersonaService;
import com.backempresa.persona.domain.Persona;
import com.backempresa.shared.NotFoundException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersonaServiceImplTest {

    @Autowired
    PersonaService personaService;

    Integer idPersona1;
    Integer idPersona2;
    String usuario1="usuario1";
    String usuario2="usuario2";

    @BeforeAll
    void TestAdd() {
        assertTrue(personaService.findAll().isEmpty());
        idPersona1 = personaService.add(new Persona(null,usuario1,"123456")).getId_persona();
        idPersona2 = personaService.add(new Persona(null,usuario2,"123456")).getId_persona();
    }

    @Test
    void testFind() {
        assertEquals(2,personaService.findAll().size());
        assertEquals(idPersona1, personaService.findById(idPersona1).getId_persona());
        assertEquals(idPersona2, personaService.findById(idPersona2).getId_persona());
        assertEquals(usuario1, personaService.findByUsuario(usuario1).getUsuario());
        assertEquals(usuario2, personaService.findByUsuario(usuario2).getUsuario());
    }

    @AfterAll
    void TestDel() {
        personaService.del(idPersona2);
        assertThrows(NotFoundException.class, () -> personaService.findById(idPersona2));
        assertThrows(NotFoundException.class, () -> personaService.findByUsuario(usuario2));
        personaService.del(idPersona1);
        assertTrue(personaService.findAll().isEmpty());
    }
}
