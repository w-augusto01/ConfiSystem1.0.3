package br.com.confisystem.repository;

import br.com.confisystem.entity.OrdemProducao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrdemProducaoRepository extends JpaRepository<OrdemProducao, Integer> {

    List<OrdemProducao> findAllByOrderByDataCriacaoDesc();

    List<OrdemProducao> findByStatusOrderByDataCriacaoDesc(String status);

    boolean existsByFormulacaoId(Integer formulacaoId);

    List<OrdemProducao> findByFormulacaoId(Integer formulacaoId);

    @Query("""
            select coalesce(sum(o.quantidadeTotalKg), 0)
            from OrdemProducao o
            where o.formulacao.id = :formulaId
              and o.status = 'FINALIZADA'
            """)
    BigDecimal somarQuantidadeFinalizadaPorFormula(@Param("formulaId") Integer formulaId);


    @Query("""
            select coalesce(sum(o.custoTotal), 0)
            from OrdemProducao o
            where o.formulacao.id = :formulaId
              and o.status = 'FINALIZADA'
            """)
    BigDecimal somarCustoFinalizadoPorFormula(@Param("formulaId") Integer formulaId);

}
