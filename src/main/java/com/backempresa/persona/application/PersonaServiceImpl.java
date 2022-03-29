package com.backempresa.persona.application;

import com.backempresa.persona.domain.Persona;
import com.backempresa.persona.infrastructure.PersonaRepo;
import com.backempresa.shared.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonaServiceImpl implements PersonaService {

    @Autowired
    PersonaRepo personaRepo;

    @Override
    public List<Persona> findAll() {
        return personaRepo.findAll();
    }

    @Override
    public Persona findById(int id) {
        return personaRepo.findById(id).orElseThrow(()-> new NotFoundException("Persona "+id+" no existe"));
    }

    @Override
    public Persona findByUsuario(String usuario) {
        return personaRepo.findByUsuario(usuario).orElseThrow(()-> new NotFoundException("Usuario "+usuario+"no existe"));
    }

    @Override
    public Persona add(Persona persona) {
        return personaRepo.save(persona);
    }

    @Override
    public void del(int id) {
        Persona p = personaRepo.findById(id).orElseThrow(()-> new NotFoundException("Persona "+id+" no existe"));
        personaRepo.delete(p);
    }
}
