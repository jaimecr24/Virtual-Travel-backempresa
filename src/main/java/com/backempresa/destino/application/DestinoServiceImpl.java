package com.backempresa.destino.application;

import com.backempresa.destino.domain.Destino;
import com.backempresa.destino.infrastructure.DestinoInputDto;
import com.backempresa.destino.infrastructure.DestinoRepo;
import com.backempresa.shared.NotFoundException;
import com.backempresa.shared.UnprocesableException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DestinoServiceImpl implements DestinoService{

    DestinoRepo destinoRepo;
    public DestinoServiceImpl(DestinoRepo destinoRepo) {  // Equivalente a "@Autowired DestinoRepo destinoRepo;"
        this.destinoRepo = destinoRepo;
    }

    @Override
    public List<Destino> findAll() {
        return destinoRepo.findAll();
    }

    @Override
    public Destino findById(String id) {
        return destinoRepo.findById(id).orElseThrow(()->new NotFoundException("Destino "+id+" no encontrado"));
    }

    @Override
    public List<Destino> findByDestino(String destino) {
        return destinoRepo.findByNombreDestino(destino);
    }

    @Override
    public Destino add(DestinoInputDto inputDto) {
        // Crea un nuevo destino con la lista de autobuses vacía.
        if (inputDto.getId()==null || inputDto.getId().length()!=ID_LENGTH)
            throw new UnprocesableException("Debe especificar un id de "+ID_LENGTH+" caracteres");
        if (destinoRepo.findById(inputDto.getId()).isPresent())
            throw new UnprocesableException("El id de destino ya existe");
        if (inputDto.getNombre()==null) throw new UnprocesableException("El nombre del destino no puede ser nulo");
        Destino ds = this.toDestino(inputDto);
        destinoRepo.save(ds);
        return ds;
    }

    @Override
    public Destino patch(String id, DestinoInputDto inputDto) {
        // Permite modificar el nombre de un destino.
        // Solo se modifican los campos que no sean nulo.
        Destino ds = this.findById(id);
        if (inputDto.getId()!=null) {
            if (inputDto.getId().length()!=ID_LENGTH)
                throw new UnprocesableException("El id debe tener "+ID_LENGTH+" caracteres");
            if (!inputDto.getId().equals(ds.getId()) && destinoRepo.findById(inputDto.getId()).isPresent())
                throw new UnprocesableException("No puede modificar el id a un valor ya existente");
            ds.setId(inputDto.getId());
        }
        if (inputDto.getNombre()!=null) ds.setNombreDestino(inputDto.getNombre());
        destinoRepo.save(ds);
        return ds;
    }

    @Override
    public void del(String id) {
        Destino ds = this.findById(id);
        destinoRepo.delete(ds);
    }

    private Destino toDestino(DestinoInputDto inputDto) {
        Destino ds = new Destino();
        ds.setId(inputDto.getId());
        ds.setNombreDestino(inputDto.getNombre());
        ds.setAutobuses(new ArrayList<>());
        return ds;
    }
}

