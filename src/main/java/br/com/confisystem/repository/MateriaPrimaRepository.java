package br.com.confisystem.repository;

import br.com.confisystem.entity.MateriaPrima;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MateriaPrimaRepository extends JpaRepository<MateriaPrima, Integer> {

    List<MateriaPrima> findByAtivoTrueOrderByNome();

    List<MateriaPrima> findAllByOrderByNome();

    @Query("""
            select mp from MateriaPrima mp
            where lower(mp.nome) like lower(concat('%', :texto, '%'))
            order by mp.nome
            """)
    List<MateriaPrima> pesquisar(@Param("texto") String texto);

    @Query("""
            select mp from MateriaPrima mp
            where mp.ativo = true
              and lower(mp.nome) like lower(concat('%', :texto, '%'))
            order by mp.nome
            """)
    List<MateriaPrima> pesquisarAtivos(@Param("texto") String texto);

}
