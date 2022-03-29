package com.backempresa.persona.application;

import com.backempresa.persona.domain.Persona;

import java.util.List;

public interface PersonaService {
    List<Persona> findAll();
    Persona findById(int id);
    Persona findByUsuario(String usuario);
    Persona add(Persona persona);
    void del(int id);
}
