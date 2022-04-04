package com.backempresa.reserva.infrastructure;

import com.backempresa.reserva.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservaRepo extends JpaRepository<Reserva,Long> {
    Optional<Reserva> findByIdentificador(String identificador);
}
