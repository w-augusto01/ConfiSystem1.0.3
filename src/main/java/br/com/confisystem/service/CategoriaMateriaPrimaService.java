package br.com.confisystem.service;

import br.com.confisystem.entity.CategoriaMateriaPrima;
import br.com.confisystem.repository.CategoriaMateriaPrimaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaMateriaPrimaService {

    private final CategoriaMateriaPrimaRepository repository;

    public CategoriaMateriaPrimaService(CategoriaMateriaPrimaRepository repository) {
        this.repository = repository;
    }

    public List<CategoriaMateriaPrima> listar() {
        return repository.findByAtivoTrueOrderByNome();
    }

}
