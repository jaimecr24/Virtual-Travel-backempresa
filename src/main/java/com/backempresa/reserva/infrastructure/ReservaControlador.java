package com.backempresa.reserva.infrastructure;

import com.backempresa.persona.application.PersonaService;
import com.backempresa.persona.domain.Persona;
import com.backempresa.reserva.application.ReservaService;
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v0")
public class ReservaControlador {

    @Autowired
    ReservaService reservaService;

    @Autowired
    PersonaService personaService;

    @Autowired
    PostOffice postOffice;

    private final SimpleDateFormat sdf2 = new SimpleDateFormat("ddMMyyyy");

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
    //TODO: ¿Porqué correos si se devuelve una lista de ReservaOutputDto?
    // Guardar lista de correos enviados? Pero es la misma que de reservas...
    @GetMapping("correos")
    public ResponseEntity<List<ReservaOutputDto>> getReservasByInterval(
            @RequestHeader("authorize") String token,
            @RequestParam(name="ciudadDestino") String ciudadDestino,
            @RequestParam(name="fechaInferior") String fechaInferior,
            @RequestParam(name="fechaSuperior", required = false) String fechaSuperior,
            @RequestParam(name="horaInferior", required = false) String horaInferior,
            @RequestParam(name="horaSuperior", required = false) String horaSuperior)
    {
        if (this.verifyToken(token)) {
            return new ResponseEntity<>(reservaService.findReservas(ciudadDestino, fechaInferior, fechaSuperior, horaInferior, horaSuperior), HttpStatus.OK);
        } else
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    // Lista de reservas realizadas para un destino, fecha y hora
    @GetMapping("reservas")
    public ResponseEntity<List<ReservaOutputDto>> getReservas(
            @RequestHeader("authorize") String token,
            @RequestParam(name="ciudadDestino") String ciudadDestino,
            @RequestParam(name="fecha") String fecha,
            @RequestParam(name="hora") String hora)
    {
        if (this.verifyToken(token)) {
            return new ResponseEntity<>(reservaService.findReservas(ciudadDestino, fecha, fecha, hora, hora), HttpStatus.OK);
        } else
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @PutMapping("correos")
    public ResponseEntity<ReservaOutputDto> forwardMail(
            @RequestBody CorreoInputDto correoDto,
            @RequestHeader("authorize") String token)
    {
       if (this.verifyToken(token)) { // Obtenemos los datos y reenviamos el correo.
           ReservaOutputDto rsvDto = reservaService.findReserva(correoDto);
           postOffice.sendMessage(rsvDto);
           return new ResponseEntity<>(rsvDto,HttpStatus.OK);
       } else
           return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    // Endpoint para añadir directamente reservas a la bd de empresa.
    @PostMapping("reserva")
    public ResponseEntity<ReservaOutputDto> addReserva(
            @RequestHeader("authorize") String token,
            @RequestBody ReservaInputDto inputDto)
    {
        if (this.verifyToken(token)) {
            return new ResponseEntity<>(reservaService.add(inputDto),HttpStatus.OK);
        } else
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("plazas/{destino}")
    public ResponseEntity<Long> getPlazasLibres(
            @PathVariable String destino,
            @RequestParam(name="fecha") String fecha,
            @RequestParam(name="hora") String hora) throws ParseException {
        // Formato de fecha: ddMMyyyy
        return new ResponseEntity<>(
                reservaService.getPlazasLibres(destino, sdf2.parse(fecha), Float.parseFloat(hora)),
                HttpStatus.OK);
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
