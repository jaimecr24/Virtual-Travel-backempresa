package com.backempresa.persona.infrastructure;

import com.backempresa.persona.domain.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonaRepo extends JpaRepository<Persona,Integer> {
    Optional<Persona> findByUsuario(String usuario);
}
