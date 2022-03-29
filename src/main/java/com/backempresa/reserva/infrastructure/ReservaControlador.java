package com.backempresa.reserva.infrastructure;

import com.backempresa.persona.application.PersonaService;
import com.backempresa.persona.domain.Persona;
import com.backempresa.reserva.application.ReservaService;
import com.backempresa.shared.NotFoundException;
import com.backempresa.shared.UnprocesableException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v0")
public class ReservaControlador {

    @Autowired
    ReservaService reservaService;

    @Autowired
    PersonaService personaService;

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

    @GetMapping("usuarios")
    public ResponseEntity<List<Persona>> findAllUsers(){
        return new ResponseEntity<>(personaService.findAll(), HttpStatus.OK);
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
}
