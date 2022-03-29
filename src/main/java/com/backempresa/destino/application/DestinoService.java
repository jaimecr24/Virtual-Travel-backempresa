package com.backempresa.destino.application;

import com.backempresa.destino.domain.Destino;
import com.backempresa.destino.infrastructure.DestinoInputDto;

import java.util.List;

public interface DestinoService {
    List<Destino> findAll();
    Destino findById(long id);
    List<Destino> findByDestino(String destino);
    Destino add(DestinoInputDto inputDto);
    Destino put(long id, DestinoInputDto inputDto);
    void del(long id);
}
