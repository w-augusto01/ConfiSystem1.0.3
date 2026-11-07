package br.com.confisystem.service;

import br.com.confisystem.dto.NfeImportada;
import br.com.confisystem.entity.Fornecedor;
import br.com.confisystem.entity.MateriaPrima;
import br.com.confisystem.entity.ProdutoFornecedor;
import br.com.confisystem.repository.CompraRepository;
import br.com.confisystem.repository.FornecedorRepository;
import br.com.confisystem.repository.ProdutoFornecedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

@Service
public class NfeImportService {

    private final FornecedorRepository fornecedorRepository;
    private final ProdutoFornecedorRepository produtoFornecedorRepository;
    private final CompraRepository compraRepository;

    public NfeImportService(
            FornecedorRepository fornecedorRepository,
            ProdutoFornecedorRepository produtoFornecedorRepository,
            CompraRepository compraRepository
    ) {
        this.fornecedorRepository = fornecedorRepository;
        this.produtoFornecedorRepository = produtoFornecedorRepository;
        this.compraRepository = compraRepository;
    }

    @Transactional(readOnly = true)
    public NfeImportada importar(File arquivo) {

        NfeImportada nfe;

        try (InputStream entrada = new FileInputStream(arquivo)) {
            nfe = NfeXmlParser.ler(entrada);
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível abrir o arquivo selecionado.", e);
        }

        if (nfe.getChaveAcesso() != null) {
            compraRepository.findByChaveAcessoNfe(nfe.getChaveAcesso()).ifPresent(existente -> {
                throw new IllegalArgumentException(
                        "Esta nota (nº " + nfe.getNumero() + ") já foi importada anteriormente, em "
                                + existente.getData() + ".");
            });
        }

        Fornecedor fornecedor = null;

        if (nfe.getDocumentoEmitente() != null) {
            fornecedor = fornecedorRepository
                    .buscarPorCnpjSomenteDigitos(nfe.getDocumentoEmitente())
                    .orElse(null);
        }

        for (NfeImportada.Item item : nfe.getItens()) {

            NfeXmlParser.sugerirFator(item);

            if (fornecedor != null) {
                produtoFornecedorRepository
                        .findByFornecedorIdAndCodigoProduto(fornecedor.getId(), item.getCodigoProduto())
                        .ifPresent(mapa -> {
                            item.setMateriaPrima(mapa.getMateriaPrima());
                            item.setFatorParaKg(mapa.getFatorParaKg());
                            item.setOrigemFator("cadastro (de-para salvo)");
                            item.setMapeado(true);
                        });
            }
        }

        nfe.setFornecedorResolvido(fornecedor);

        return nfe;
    }

    @Transactional
    public void confirmarMapeamento(
            Fornecedor fornecedor,
            String codigoProduto,
            String descricaoProduto,
            MateriaPrima materiaPrima,
            String unidadeNota,
            BigDecimal fatorParaKg
    ) {
        ProdutoFornecedor mapa = produtoFornecedorRepository
                .findByFornecedorIdAndCodigoProduto(fornecedor.getId(), codigoProduto)
                .orElseGet(ProdutoFornecedor::new);

        mapa.setFornecedor(fornecedor);
        mapa.setCodigoProduto(codigoProduto);
        mapa.setDescricaoProduto(descricaoProduto);
        mapa.setMateriaPrima(materiaPrima);
        mapa.setUnidadeNota(unidadeNota);
        mapa.setFatorParaKg(fatorParaKg);

        produtoFornecedorRepository.save(mapa);
    }
}
