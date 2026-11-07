package br.com.confisystem.repository;

import br.com.confisystem.entity.FormulacaoRacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FormulacaoRacaoRepository extends JpaRepository<FormulacaoRacao, Integer> {

    List<FormulacaoRacao> findAllByOrderByNome();

    Optional<FormulacaoRacao> findByNomeIgnoreCase(String nome);

}
