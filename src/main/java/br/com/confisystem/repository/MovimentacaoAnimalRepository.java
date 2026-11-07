package br.com.confisystem.repository;

import br.com.confisystem.entity.MovimentacaoAnimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimentacaoAnimalRepository extends JpaRepository<MovimentacaoAnimal, Integer> {

    @Query("""
            select m from MovimentacaoAnimal m
            left join m.piqueteOrigem o
            left join m.piqueteDestino d
            where o.id = :piqueteId or d.id = :piqueteId
            order by m.data desc, m.id desc
            """)
    List<MovimentacaoAnimal> listarPorPiquete(@Param("piqueteId") Integer piqueteId);
}
