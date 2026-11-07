package br.com.confisystem.repository;

import br.com.confisystem.entity.CategoriaMateriaPrima;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaMateriaPrimaRepository extends JpaRepository<CategoriaMateriaPrima, Integer> {

    List<CategoriaMateriaPrima> findByAtivoTrueOrderByNome();

}
