package br.com.confisystem.repository;

import br.com.confisystem.entity.FormulacaoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormulacaoItemRepository extends JpaRepository<FormulacaoItem, Integer> {

    List<FormulacaoItem> findByFormulacaoIdOrderByPercentualDesc(Integer formulacaoId);

}
