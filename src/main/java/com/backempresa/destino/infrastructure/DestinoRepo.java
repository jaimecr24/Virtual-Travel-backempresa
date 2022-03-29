package com.backempresa.destino.infrastructure;

import com.backempresa.destino.domain.Destino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DestinoRepo extends JpaRepository<Destino,Long> {
    List<Destino> findByNombreDestino(String nombreDestino);
}
