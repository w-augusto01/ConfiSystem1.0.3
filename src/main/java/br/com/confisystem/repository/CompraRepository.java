package br.com.confisystem.repository;

import br.com.confisystem.entity.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Integer> {

    List<Compra> findByAtivoTrueOrderByDataDesc();

    Optional<Compra> findByChaveAcessoNfe(String chaveAcessoNfe);

}
