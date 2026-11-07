package br.com.confisystem.service;

import br.com.confisystem.entity.Compra;
import br.com.confisystem.entity.CompraItem;
import br.com.confisystem.repository.CompraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
public class CompraService {

    private static final int ESCALA_MOEDA = 2;
    private static final int ESCALA_CUSTO_KG = 4;

    private final CompraRepository repository;

    public CompraService(CompraRepository repository) {
        this.repository = repository;
    }

    public List<Compra> listar() {
        return repository.findByAtivoTrueOrderByDataDesc();
    }

    public Compra buscarPorId(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public void calcularItens(
            List<CompraItem> itens,
            BigDecimal frete,
            BigDecimal seguro,
            BigDecimal outrasDespesas,
            BigDecimal desconto
    ) {
        BigDecimal valorProdutos = calcularValorProdutos(itens);

        BigDecimal despesasTotais = nz(frete)
                .add(nz(seguro))
                .add(nz(outrasDespesas))
                .subtract(nz(desconto));

        for (CompraItem item : itens) {

            BigDecimal valorProduto = nz(item.getValorProduto());

            BigDecimal proporcao = valorProdutos.compareTo(BigDecimal.ZERO) > 0
                    ? valorProduto.divide(valorProdutos, 10, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal despesaRateada = despesasTotais
                    .multiply(proporcao)
                    .setScale(ESCALA_MOEDA, RoundingMode.HALF_UP);

            BigDecimal custoFinal = valorProduto
                    .add(despesaRateada)
                    .setScale(ESCALA_MOEDA, RoundingMode.HALF_UP);

            BigDecimal quantidade = nz(item.getQuantidade());

            BigDecimal custoPorUnidade = quantidade.compareTo(BigDecimal.ZERO) > 0
                    ? custoFinal.divide(quantidade, ESCALA_CUSTO_KG, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            item.setDespesasRateadas(despesaRateada);
            item.setCustoFinal(custoFinal);
            item.setCustoKg(custoPorUnidade);
        }
    }

    public BigDecimal calcularValorProdutos(List<CompraItem> itens) {
        return itens.stream()
                .map(CompraItem::getValorProduto)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularValorNota(
            BigDecimal valorProdutos,
            BigDecimal frete,
            BigDecimal seguro,
            BigDecimal outrasDespesas,
            BigDecimal desconto
    ) {
        return nz(valorProdutos)
                .add(nz(frete))
                .add(nz(seguro))
                .add(nz(outrasDespesas))
                .subtract(nz(desconto));
    }

    @Transactional
    public Compra salvar(Compra compra, List<CompraItem> itens) {

        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException(
                    "Informe ao menos um item para dar entrada na compra."
            );
        }

        validarOrigemDosItens(compra, itens);

        if (compra.getChaveAcessoNfe() != null && !compra.getChaveAcessoNfe().isBlank()
                && repository.findByChaveAcessoNfe(compra.getChaveAcessoNfe()).isPresent()) {
            throw new IllegalArgumentException(
                    "Esta NF-e já foi lançada antes (mesma chave de acesso)."
            );
        }

        calcularItens(itens, compra.getFrete(), compra.getSeguro(),
                compra.getOutrasDespesas(), compra.getDesconto());

        BigDecimal valorProdutos = calcularValorProdutos(itens)
                .setScale(ESCALA_MOEDA, RoundingMode.HALF_UP);

        BigDecimal valorNota = calcularValorNota(
                valorProdutos, compra.getFrete(), compra.getSeguro(),
                compra.getOutrasDespesas(), compra.getDesconto()
        ).setScale(ESCALA_MOEDA, RoundingMode.HALF_UP);

        compra.setValorProdutos(valorProdutos);
        compra.setValorNota(valorNota);
        compra.setAtivo(true);

        for (CompraItem item : itens) {
            item.setCompra(compra);
        }
        compra.setItens(itens);

        return repository.save(compra);
    }

    private BigDecimal nz(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private void validarOrigemDosItens(
            Compra compra,
            List<CompraItem> itens) {

        boolean compraXml = compra.getChaveAcessoNfe() != null
                && !compra.getChaveAcessoNfe().isBlank();

        for (CompraItem item : itens) {

            if (item.getMateriaPrima() == null) {
                throw new IllegalArgumentException(
                        "Todos os itens precisam ter uma matéria-prima."
                );
            }

            if (compraXml) {

                if (!item.isItemImportadoXml()) {
                    throw new IllegalArgumentException(
                            "A compra XML não pode conter itens manuais."
                    );
                }

                if (item.getCodigoProdutoXml() == null
                        || item.getCodigoProdutoXml().isBlank()) {
                    throw new IllegalArgumentException(
                            "Item sem código original da NF-e."
                    );
                }

                if (item.getDescricaoProdutoXml() == null
                        || item.getDescricaoProdutoXml().isBlank()) {
                    throw new IllegalArgumentException(
                            "Item sem descrição original da NF-e."
                    );
                }

                if (item.getQuantidadeOriginal() == null
                        || item.getFatorConversao() == null
                        || item.getQuantidadeOriginal().signum() <= 0
                        || item.getFatorConversao().signum() <= 0) {
                    throw new IllegalArgumentException(
                            "Quantidade original ou fator de conversão inválido."
                    );
                }

                BigDecimal quantidadeEsperada =
                        item.getQuantidadeOriginal()
                                .multiply(item.getFatorConversao())
                                .setScale(3, RoundingMode.HALF_UP);

                if (item.getQuantidade() == null
                        || item.getQuantidade().compareTo(quantidadeEsperada) != 0) {
                    throw new IllegalArgumentException(
                            "A quantidade em KG foi alterada em relação ao XML."
                    );
                }

                if (item.getValorProdutoXml() == null
                        || item.getValorProduto() == null
                        || item.getValorProduto().compareTo(
                        item.getValorProdutoXml()) != 0) {
                    throw new IllegalArgumentException(
                            "O valor do produto foi alterado em relação ao XML."
                    );
                }

            } else if (item.isItemImportadoXml()) {

                throw new IllegalArgumentException(
                        "Uma compra manual não pode conter itens importados de XML."
                );
            }
        }
    }

}
