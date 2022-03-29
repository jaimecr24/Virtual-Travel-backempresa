package com.backempresa.destino.domain;

import com.backempresa.autobus.domain.Autobus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Destino {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long idDestino;

    private String nombreDestino;

    @OneToMany(mappedBy = "destino")
    private List<Autobus> autobuses = new ArrayList<>();

}
