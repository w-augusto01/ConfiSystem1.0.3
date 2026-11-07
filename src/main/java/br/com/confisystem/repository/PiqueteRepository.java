package br.com.confisystem.repository;

import br.com.confisystem.entity.Piquete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PiqueteRepository extends JpaRepository<Piquete, Integer> {

    List<Piquete> findByAtivoTrueOrderByNome();

}
