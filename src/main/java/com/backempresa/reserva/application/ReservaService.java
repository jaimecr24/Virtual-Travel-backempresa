package com.backempresa.reserva.application;

import com.backempresa.reserva.domain.Reserva;
import com.backempresa.reserva.infrastructure.CorreoInputDto;
import com.backempresa.reserva.infrastructure.ReservaDisponibleOutputDto;
import com.backempresa.reserva.infrastructure.ReservaInputDto;
import com.backempresa.reserva.infrastructure.ReservaOutputDto;

import java.util.Date;
import java.util.List;

public interface ReservaService {
    List<Reserva> findAll();
    Reserva findById(long id);
    Reserva findByIdentificador(String identificador);
    List<ReservaDisponibleOutputDto> findDisponible(String destino, String fechaInferior, String fechaSuperior, String horaInferior, String horaSuperior);
    List<ReservaOutputDto> findReservas(String destino, String fechaInferior, String fechaSuperior, String horaInferior, String horaSuperior);
    ReservaOutputDto findReserva(CorreoInputDto correoDto);
    long getPlazasLibres(String destino, Date fecha, float hora);
    ReservaOutputDto add(ReservaInputDto inputDto);
    ReservaOutputDto add(ReservaOutputDto outputDto);
    void del(long id);
}
