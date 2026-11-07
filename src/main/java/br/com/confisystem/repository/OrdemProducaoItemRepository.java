package br.com.confisystem.repository;

import br.com.confisystem.entity.OrdemProducaoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrdemProducaoItemRepository extends JpaRepository<OrdemProducaoItem, Integer> {

    List<OrdemProducaoItem> findByOrdemIdOrderByQuantidadeTotalKgDesc(Integer ordemId);

    @Query("""
            select coalesce(sum(i.quantidadeTotalKg), 0)
            from OrdemProducaoItem i
            where i.materiaPrima.id = :materiaPrimaId
              and i.ordem.status = 'FINALIZADA'
            """)
    BigDecimal somarQuantidadeConsumidaPorMateriaPrima(@Param("materiaPrimaId") Integer materiaPrimaId);

}
