package com.backempresa;

import com.backempresa.autobus.application.AutobusService;
import com.backempresa.autobus.infrastructure.AutobusInputDto;
import com.backempresa.destino.application.DestinoService;
import com.backempresa.destino.domain.Destino;
import com.backempresa.destino.infrastructure.DestinoInputDto;
import com.backempresa.persona.application.PersonaService;
import com.backempresa.persona.domain.Persona;
import com.backempresa.reserva.application.ReservaService;
import com.backempresa.reserva.infrastructure.CorreoInputDto;
import com.backempresa.reserva.infrastructure.ReservaInputDto;
import com.backempresa.reserva.infrastructure.ReservaOutputDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReservaControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private DestinoService destinoService;

    @Autowired
    private AutobusService autobusService;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private SimpleDateFormat sdf2;

    private Integer idPersona1;
    private final String usuario1="usuario1";
    private String token;
    private String idDestino="VAL";
    private final String nombreDestino="Valencia";
    private final String fechaStr="01052022";
    private String email="email@email.com";
    private String idBus;
    private Long idRsv;

    @BeforeAll
    void starting() throws Exception {
        Date fecha = sdf2.parse(fechaStr);
        assertTrue(personaService.findAll().isEmpty());
        idPersona1 = personaService.add(new Persona(null,usuario1,"123456")).getId_persona();
        destinoService.add(new DestinoInputDto(idDestino,nombreDestino));
        idBus = autobusService.add(new AutobusInputDto(idDestino, fecha, 12F, 2)).getId();
        idRsv = reservaService.add(new ReservaInputDto(idDestino,"nombre1","apellido1","111111",email,fecha,12F)).getIdReserva();
        MvcResult res = mockMvc.perform(post("/api/v0/token/")
                        .header("user",usuario1)
                        .header("password","123456").with(csrf()))
                .andExpect(status().isOk()).andReturn();
        token = res.getResponse().getContentAsString();
    }

    @Test
    @DisplayName("Testing POST token")
    void testLogin() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/v0/token/")
                .header("user",usuario1)
                .header("password","123456").with(csrf()))
                .andExpect(status().isOk()).andReturn();
        String mytoken = res.getResponse().getContentAsString();
        assertTrue(mytoken.contains("Bearer"));
        res = mockMvc.perform(post("/api/v0/token/")
                        .header("user",usuario1)
                        .header("password","1000").with(csrf()))
                .andExpect(status().isUnprocessableEntity()).andReturn();
    }

    @Test
    @DisplayName("Testing GET token")
    void testCheckToken() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/v0/token/"+token)
                .contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isOk()).andReturn();
        assertEquals(res.getResponse().getStatus(), HttpStatus.OK.value());
        res = mockMvc.perform(get("/api/v0/token/"+"nada")
                .contentType(MediaType.APPLICATION_JSON).with(csrf())).andExpect(status().isForbidden()).andReturn();
        assertEquals(res.getResponse().getStatus(), HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("Testing GET usuarios")
    void testGetUsuarios() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/v0/usuarios/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization",token).with(csrf()))
                .andExpect(status().isOk()).andReturn();
        String contenido = res.getResponse().getContentAsString();
        List<Persona> personas= new ObjectMapper().readValue(contenido, new TypeReference<>() {	}); // Use TypeReference to map the List.
        assertFalse(personas.isEmpty());
        Assertions.assertEquals(usuario1, personas.get(0).getUsuario());
    }

    @Test
    @DisplayName("Testing GET correos")
    void testGetReservasByInterval() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/v0/correos/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .param("ciudadDestino",nombreDestino)
                        .param("fechaInferior",fechaStr).with(csrf()))
                .andExpect(status().isOk()).andReturn();
        String contenido = res.getResponse().getContentAsString();
        List<ReservaOutputDto> listDto = new ObjectMapper().readValue(contenido, new TypeReference<>() {	}); // Use TypeReference to map the List.
        assertFalse(listDto.isEmpty());
        Assertions.assertEquals(nombreDestino, listDto.get(0).getCiudadDestino());
    }

    @Test
    @DisplayName("Testing GET reservas")
    void testGetReservas() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/v0/reservas/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .param("ciudadDestino",nombreDestino)
                        .param("fecha",fechaStr)
                        .param("hora","12").with(csrf()))
                .andExpect(status().isOk()).andReturn();
        String contenido = res.getResponse().getContentAsString();
        List<ReservaOutputDto> listDto = new ObjectMapper().readValue(contenido, new TypeReference<>() {	}); // Use TypeReference to map the List.
        assertFalse(listDto.isEmpty());
        Assertions.assertEquals(nombreDestino, listDto.get(0).getCiudadDestino());
    }

    @Test
    @DisplayName("Testing PUT correos")
    void testforwardMail() throws Exception {
        Date fecha = sdf2.parse(fechaStr);
        CorreoInputDto inputDto = new CorreoInputDto(nombreDestino,email,fecha,12F);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        MvcResult res = mockMvc.perform(put("/api/v0/correos/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(inputDto))
                        .header("Authorization",token).with(csrf()))
                .andExpect(status().isOk()).andReturn();
        String contenido = res.getResponse().getContentAsString();
        ReservaOutputDto outDto = new ObjectMapper().readValue(contenido, new TypeReference<>() {	}); // Use TypeReference to map the List.
        Assertions.assertEquals(nombreDestino, outDto.getCiudadDestino());
    }

    @Test
    @DisplayName("Testing POST reserva")
    void testAddReserva() throws Exception {
        Date fecha = sdf2.parse(fechaStr);
        ReservaInputDto inputDto = new ReservaInputDto(idDestino,"nombre2","apellido","111111",email,fecha,12F);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        MvcResult res = mockMvc.perform(post("/api/v0/reserva/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(inputDto))
                        .header("Authorization",token).with(csrf()))
                .andExpect(status().isOk()).andReturn();
        String contenido = res.getResponse().getContentAsString();
        ReservaOutputDto outDto = new ObjectMapper().readValue(contenido, new TypeReference<>() {	}); // Use TypeReference to map the List.
        Assertions.assertEquals(nombreDestino, outDto.getCiudadDestino());
        reservaService.del(outDto.getIdReserva()); // La eliminamos
        assertEquals(1, reservaService.findAll().size());
    }

    @Test
    @DisplayName("Testing GET plazas")
    void testGetPlazasLibres() throws Exception {
        MvcResult res = mockMvc.perform(get("/api/v0/plazas/"+nombreDestino)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .param("fecha",fechaStr)
                        .param("hora","12").with(csrf()))
                .andExpect(status().isOk()).andReturn();
        String contenido = res.getResponse().getContentAsString();
        Integer plazas = new ObjectMapper().readValue(contenido, new TypeReference<>() {	}); // Use TypeReference to map the List.
        Assertions.assertEquals(1, plazas);
        res = mockMvc.perform(get("/api/v0/plazas/"+"Guadalajara")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .param("fecha",fechaStr)
                        .param("hora","12").with(csrf()))
                .andExpect(status().isNotFound()).andReturn();
    }

    @AfterAll
    void cleaning() {
        personaService.del(idPersona1);
        reservaService.del(idRsv);
        autobusService.del(idBus);
        destinoService.del(idDestino);
        assertTrue(personaService.findAll().isEmpty());
        assertTrue(reservaService.findAll().isEmpty());
        assertTrue(autobusService.findAll().isEmpty());
        assertTrue(destinoService.findAll().isEmpty());
    }
}
