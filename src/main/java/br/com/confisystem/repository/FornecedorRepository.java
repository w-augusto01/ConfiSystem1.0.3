package br.com.confisystem.repository;

import br.com.confisystem.entity.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Integer> {

    @Query(value = "select * from fornecedores " +
            "where regexp_replace(coalesce(cnpj, ''), '[^0-9]', '', 'g') = :cnpj " +
            "limit 1", nativeQuery = true)
    Optional<Fornecedor> buscarPorCnpjSomenteDigitos(@Param("cnpj") String cnpj);

    List<Fornecedor> findAllByOrderByIdAsc();

    List<Fornecedor> findByAtivoTrueOrderByIdAsc();

    @Query("""
            select f from Fornecedor f
            where lower(f.nome) like lower(concat('%', :texto, '%'))
               or f.cnpj like concat('%', :texto, '%')
            order by f.nome
            """)
    List<Fornecedor> pesquisar(@Param("texto") String texto);

    @Query("""
            select f from Fornecedor f
            where f.ativo = true
              and (
                  lower(f.nome) like lower(concat('%', :texto, '%'))
                  or f.cnpj like concat('%', :texto, '%')
              )
            order by f.nome
            """)
    List<Fornecedor> pesquisarAtivos(@Param("texto") String texto);

}
