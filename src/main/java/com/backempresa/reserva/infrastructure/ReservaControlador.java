package com.backempresa.reserva.infrastructure;

import com.backempresa.autobus.application.AutobusService;
import com.backempresa.autobus.domain.Autobus;
import com.backempresa.destino.application.DestinoService;
import com.backempresa.destino.domain.Destino;
import com.backempresa.persona.application.PersonaService;
import com.backempresa.persona.domain.Persona;
import com.backempresa.reserva.application.ReservaService;
import com.backempresa.shared.KafkaMessageProducer;
import com.backempresa.shared.NotFoundException;
import com.backempresa.shared.PostOffice;
import com.backempresa.shared.UnprocesableException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v0")
public class ReservaControlador {

    @Autowired
    AutobusService autobusService;

    @Autowired
    ReservaService reservaService;

    @Autowired
    DestinoService destinoService;

    @Autowired
    PersonaService personaService;

    @Autowired
    PostOffice postOffice;

    @Autowired
    KafkaMessageProducer kafkaMessageProducer;

    @Autowired
    SimpleDateFormat sdf2;

    // Crear token seguridad
    @PostMapping("token")
    public ResponseEntity<String> login(
            @RequestHeader("user") String usuario,
            @RequestHeader("password") String pwd)
            throws NotFoundException, UnprocesableException
    {
        Persona p = personaService.findByUsuario(usuario);
        if (!p.getPassword().equals(pwd)) throw new UnprocesableException("El password no es correcto");
        return new ResponseEntity<>(getJWTToken(usuario,"ROLE_USER"), HttpStatus.OK);
    }

    // Comprueba validez de un token
    @GetMapping("token/{token}")
    public ResponseEntity<Void> checkToken(@PathVariable String token){
        if (this.verifyToken(token))
            return new ResponseEntity<>(HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    // Devuelve lista con todos los usuarios registrados en la base de datos
    @GetMapping("usuarios")
    public ResponseEntity<List<Persona>> findAllUsers(){
        return new ResponseEntity<>(personaService.findAll(), HttpStatus.OK);
    }

    // Obtiene la lista de reservas realizadas por destino, intervalo de fechas y horas
    @GetMapping("correos")
    public ResponseEntity<List<ReservaOutputDto>> getReservasByInterval(
            @RequestParam(name="ciudadDestino") String ciudadDestino,
            @RequestParam(name="fechaInferior") String fechaInferior,
            @RequestParam(name="fechaSuperior", required = false) String fechaSuperior,
            @RequestParam(name="horaInferior", required = false) String horaInferior,
            @RequestParam(name="horaSuperior", required = false) String horaSuperior)
    {
        return new ResponseEntity<>(reservaService.findReservas(ciudadDestino, fechaInferior, fechaSuperior, horaInferior, horaSuperior), HttpStatus.OK);
    }

    // Lista de reservas realizadas para un destino, fecha y hora
    @GetMapping("reservas")
    public ResponseEntity<List<ReservaOutputDto>> getReservas(
            @RequestParam(name="ciudadDestino") String ciudadDestino,
            @RequestParam(name="fecha") String fecha,
            @RequestParam(name="hora") String hora)
    {
        return new ResponseEntity<>(reservaService.findReservas(ciudadDestino, fecha, fecha, hora, hora), HttpStatus.OK);
    }

    @PutMapping("correos")
    public ResponseEntity<ReservaOutputDto> forwardMail(@RequestBody CorreoInputDto correoDto)
    {
       // Obtenemos los datos y reenviamos el correo.
       ReservaOutputDto rsvDto = reservaService.findReserva(correoDto);
       postOffice.sendMessage(rsvDto);
       return new ResponseEntity<>(rsvDto,HttpStatus.OK);
    }

    // Endpoint para añadir directamente reservas a la bd de empresa.
    // La petición debe contener el token Auth
    @PostMapping("reserva")
    public ResponseEntity<ReservaOutputDto> addReserva(@RequestBody ReservaInputDto inputDto)
    {
        ReservaOutputDto outputDto = reservaService.add(inputDto);
        // Enviamos email:
        postOffice.sendMessage(outputDto);
        // Enviamos mensaje de kafka por reservas(0) para que la escuchen todos los backweb
        kafkaMessageProducer.sendMessage("reservas",1, outputDto);
        return new ResponseEntity<>(outputDto,HttpStatus.OK);
    }

    @GetMapping("plazas/{destino}")
    public ResponseEntity<Integer> getPlazasLibres(
            @PathVariable String destino,
            @RequestParam(name="fecha") String fecha,
            @RequestParam(name="hora") String hora) throws Exception {
        // Formato de fecha: ddMMyyyy
        List<Destino> listDst = destinoService.findByDestino(destino);
        if (listDst.isEmpty()) throw new NotFoundException("Destino no encontrado");
        String idBus = autobusService.getIdBus(listDst.get(0).getId(), sdf2.parse(fecha), Float.parseFloat(hora));
        Autobus bus = autobusService.findById(idBus);
        return new ResponseEntity<>(bus.getPlazasLibres(), HttpStatus.OK);
    }

    private String getJWTToken(String username, String rol)
    {
        String secretKey = "mySecretKey";
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList(rol);
        String token = Jwts
                .builder()
                .setId("softtekJWT")
                .setSubject(username)
                .claim("authorities",
                        grantedAuthorities.stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(SignatureAlgorithm.HS512,
                        secretKey.getBytes()).compact();
        return "Bearer " + token;
    }

    private boolean verifyToken(String token){
        final String SECRET = "mySecretKey";
        final String PREFIX = "Bearer ";
        try {
            String jwtToken = token.replace(PREFIX,"");
            Claims claims = Jwts.parser().setSigningKey(SECRET.getBytes()).parseClaimsJws(jwtToken).getBody();
            return true;
        } catch(Exception e) {
            return false;
        }
    }
}
