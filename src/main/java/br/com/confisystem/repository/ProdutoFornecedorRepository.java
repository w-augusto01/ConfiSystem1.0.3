package br.com.confisystem.repository;

import br.com.confisystem.entity.ProdutoFornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProdutoFornecedorRepository extends JpaRepository<ProdutoFornecedor, Integer> {

    Optional<ProdutoFornecedor> findByFornecedorIdAndCodigoProduto(Integer fornecedorId, String codigoProduto);

}
