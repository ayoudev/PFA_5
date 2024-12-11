package com.pfa.backend.repositories;

import com.pfa.backend.entities.CarteNationale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarteNationaleRepository extends JpaRepository<CarteNationale, Long> {
}
