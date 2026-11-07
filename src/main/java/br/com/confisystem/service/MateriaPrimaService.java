package br.com.confisystem.service;

import br.com.confisystem.entity.MateriaPrima;
import br.com.confisystem.repository.MateriaPrimaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MateriaPrimaService {

    private final MateriaPrimaRepository repository;

    public MateriaPrimaService(MateriaPrimaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MateriaPrima salvar(MateriaPrima materiaPrima) {
        materiaPrima.setAtivo(true);
        return repository.save(materiaPrima);
    }

    @Transactional
    public MateriaPrima atualizar(MateriaPrima materiaPrima) {
        return repository.save(materiaPrima);
    }

    public MateriaPrima buscarPorId(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public void desativar(Integer id) {
        repository.findById(id).ifPresent(materiaPrima -> {
            materiaPrima.setAtivo(false);
            repository.save(materiaPrima);
        });
    }

    public List<MateriaPrima> listar() {
        return repository.findByAtivoTrueOrderByNome();
    }

    public List<MateriaPrima> listarTodos() {
        return repository.findAllByOrderByNome();
    }

    public List<MateriaPrima> pesquisar(String texto, boolean somenteAtivos) {

        if (texto == null || texto.trim().isEmpty()) {
            return somenteAtivos ? listar() : listarTodos();
        }

        return somenteAtivos ? repository.pesquisarAtivos(texto) : repository.pesquisar(texto);
    }

}
