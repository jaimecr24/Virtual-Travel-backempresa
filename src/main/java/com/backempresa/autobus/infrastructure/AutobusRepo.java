package com.backempresa.autobus.infrastructure;

import com.backempresa.autobus.domain.Autobus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutobusRepo extends JpaRepository<Autobus,String> {
}
