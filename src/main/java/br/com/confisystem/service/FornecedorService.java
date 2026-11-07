package br.com.confisystem.service;

import br.com.confisystem.entity.Fornecedor;
import br.com.confisystem.repository.FornecedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FornecedorService {

    private final FornecedorRepository repository;

    public FornecedorService(FornecedorRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Fornecedor salvar(Fornecedor fornecedor) {
        fornecedor.setAtivo(true);
        return repository.save(fornecedor);
    }

    @Transactional
    public Fornecedor atualizar(Fornecedor fornecedor) {
        return repository.save(fornecedor);
    }

    @Transactional
    public void desativar(Integer id) {
        repository.findById(id).ifPresent(fornecedor -> {
            fornecedor.setAtivo(false);
            repository.save(fornecedor);
        });
    }

    public List<Fornecedor> listar() {
        return repository.findAllByOrderByIdAsc();
    }

    public List<Fornecedor> listarAtivos() {
        return repository.findByAtivoTrueOrderByIdAsc();
    }

    public List<Fornecedor> pesquisar(String texto, boolean somenteAtivos) {
        if (somenteAtivos) {
            return repository.pesquisarAtivos(texto);
        }
        return repository.pesquisar(texto);
    }

}
