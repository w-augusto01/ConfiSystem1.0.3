package br.com.confisystem.repository;

import br.com.confisystem.entity.CompraItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CompraItemRepository extends JpaRepository<CompraItem, Integer> {

    List<CompraItem> findByMateriaPrimaIdOrderByCompraDataDesc(Integer materiaPrimaId);

    Optional<CompraItem> findTopByMateriaPrimaIdOrderByCompraDataDesc(Integer materiaPrimaId);

    @Query("""
            select coalesce(sum(ci.quantidade), 0)
            from CompraItem ci
            where ci.materiaPrima.id = :materiaPrimaId
            """)
    BigDecimal somarQuantidadeCompradaPorMateriaPrima(@Param("materiaPrimaId") Integer materiaPrimaId);

}
