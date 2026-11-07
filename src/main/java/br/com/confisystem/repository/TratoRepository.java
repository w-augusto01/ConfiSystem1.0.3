package br.com.confisystem.repository;

import br.com.confisystem.entity.Trato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TratoRepository extends JpaRepository<Trato, Integer> {

    List<Trato> findByPiqueteIdOrderByDataDesc(Integer piqueteId);

    List<Trato> findTop2ByPiqueteIdOrderByDataDesc(Integer piqueteId);

    Optional<Trato> findTopByPiqueteIdOrderByDataDesc(Integer piqueteId);

    @Query("select distinct t.racao from Trato t order by t.racao")
    List<String> listarRacoesUtilizadas();

    @Query("""
            select coalesce(sum(t.quantidadeKg), 0)
            from Trato t
            where t.formulacaoRacao.id = :formulaId
            """)
    BigDecimal somarQuantidadeUsadaPorFormula(@Param("formulaId") Integer formulaId);

}
