package com.backempresa;

import com.backempresa.autobus.application.AutobusService;
import com.backempresa.autobus.domain.Autobus;
import com.backempresa.autobus.infrastructure.AutobusInputDto;
import com.backempresa.destino.application.DestinoService;
import com.backempresa.destino.domain.Destino;
import com.backempresa.destino.infrastructure.DestinoInputDto;
import com.backempresa.persona.application.PersonaService;
import com.backempresa.persona.domain.Persona;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootApplication
public class BackempresaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackempresaApplication.class, args);
	}
/*
	@Bean
	CommandLineRunner init(
			DestinoService destinoService,
			AutobusService autobusService,
			PersonaService personaService)
	{
		return args ->
		{
			//Usuarios registrados que podrán obtener token
			personaService.add(new Persona(null,"usuario1","123456"));
			personaService.add(new Persona(null,"usuario2","123456"));
			personaService.add(new Persona(null,"usuario3","123456"));

			// Viajes a las ciudades indicadas para todos los días del mes 04 de 2022 a las horas indicadas.
			// En total se añadirán 4x4x30 = 480 registros a Autobus
			String[] destinos = {"Valencia","Madrid","Barcelona","Bilbao"};
			String[] idDestinos = {"VAL","MAD","BAR","BIL"};
			Float[] salidas = { 8f, 12f, 16f, 20f };
			String anyo = "2022";
			String mes = "04";
			SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
			DestinoInputDto dsInputDto = new DestinoInputDto();
			for (int i=0; i<destinos.length; i++) {
				dsInputDto.setId(idDestinos[i]);
				dsInputDto.setNombre(destinos[i]);
				Destino ds = destinoService.add(dsInputDto);
				for (int j=1; j<=30; j++) {
					String dateInString = String.format("%02d",j)+mes+anyo;
					Date fecha = sdf.parse(dateInString);
					for (Float hora:salidas) {
						AutobusInputDto busInputDto = new AutobusInputDto();
						busInputDto.setIdDestino(ds.getId());
						busInputDto.setFecha(fecha);
						busInputDto.setHoraSalida(hora);
						busInputDto.setPlazasLibres(autobusService.MAX_PLAZAS);
						autobusService.add(busInputDto);
					}
				}
			}
		};
	}
*/
	@Bean(name = "sdf1")
	SimpleDateFormat sdf1() {
		return new SimpleDateFormat("dd-MM-yyyy");
	}

	@Bean(name = "sdf2")
	SimpleDateFormat sdf2() {
		return new SimpleDateFormat("ddMMyyyy");
	}

	@Bean(name = "sdf3")
	SimpleDateFormat sdf3() {
		return new SimpleDateFormat("ddMMyy");
	}
}
