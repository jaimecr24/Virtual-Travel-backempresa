package com.backempresa.reserva.application;

import com.backempresa.reserva.domain.Reserva;
import com.backempresa.reserva.infrastructure.ReservaDisponibleOutputDto;
import com.backempresa.reserva.infrastructure.ReservaInputDto;
import com.backempresa.reserva.infrastructure.ReservaOutputDto;

import java.util.List;

public interface ReservaService {
    List<Reserva> findAll();
    Reserva findById(long id);
    List<ReservaDisponibleOutputDto> findDisponible(String destino, String fechaInferior, String fechaSuperior, String horaInferior, String horaSuperior);
    ReservaOutputDto add(ReservaInputDto inputDto);
    ReservaOutputDto add(ReservaOutputDto outputDto);
    Reserva put(long id, Reserva reserva);
    void del(long id);
}
