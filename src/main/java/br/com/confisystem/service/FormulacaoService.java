package br.com.confisystem.service;

import br.com.confisystem.entity.FormulacaoItem;
import br.com.confisystem.entity.FormulacaoRacao;
import br.com.confisystem.entity.MateriaPrima;
import br.com.confisystem.entity.OrdemProducao;
import br.com.confisystem.repository.FormulacaoItemRepository;
import br.com.confisystem.repository.FormulacaoRacaoRepository;
import br.com.confisystem.repository.OrdemProducaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class FormulacaoService {

    private static final BigDecimal LOTE_PADRAO_KG = BigDecimal.valueOf(1000);
    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final BigDecimal TOLERANCIA_KG = new BigDecimal("0.5");

    private static final int ESCALA_KG = 3;
    private static final int ESCALA_MOEDA = 2;

    private final FormulacaoRacaoRepository formulacaoRacaoRepository;
    private final FormulacaoItemRepository formulacaoItemRepository;
    private final EstoqueService estoqueService;
    private final OrdemProducaoRepository ordemProducaoRepository;

    public FormulacaoService(
            FormulacaoRacaoRepository formulacaoRacaoRepository,
            FormulacaoItemRepository formulacaoItemRepository,
            EstoqueService estoqueService,
            OrdemProducaoRepository ordemProducaoRepository
    ) {
        this.formulacaoRacaoRepository = formulacaoRacaoRepository;
        this.formulacaoItemRepository = formulacaoItemRepository;
        this.estoqueService = estoqueService;
        this.ordemProducaoRepository = ordemProducaoRepository;
    }

    public BigDecimal getLotePadraoKg() {
        return LOTE_PADRAO_KG;
    }

    public FormulacaoItem montarItem(MateriaPrima materiaPrima, BigDecimal quantidadeKg) {

        if (quantidadeKg == null || quantidadeKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }

        if (quantidadeKg.compareTo(LOTE_PADRAO_KG) > 0) {
            throw new IllegalArgumentException(
                    "A quantidade não pode ultrapassar o lote de referência de " + LOTE_PADRAO_KG + " Kg."
            );
        }

        BigDecimal percentual = quantidadeKg
                .multiply(CEM)
                .divide(LOTE_PADRAO_KG, ESCALA_KG, RoundingMode.HALF_UP);

        BigDecimal custoUnitarioKg = estoqueService.custoAtualKg(materiaPrima.getId());

        BigDecimal custoTotalItem = custoUnitarioKg != null
                ? custoUnitarioKg.multiply(quantidadeKg).setScale(ESCALA_MOEDA, RoundingMode.HALF_UP)
                : null;

        FormulacaoItem item = new FormulacaoItem();
        item.setMateriaPrima(materiaPrima);
        item.setPercentual(percentual);
        item.setQuantidadeKg(quantidadeKg);
        item.setCustoUnitarioKg(custoUnitarioKg);
        item.setCustoTotal(custoTotalItem);

        return item;
    }

    public BigDecimal somarQuantidade(List<FormulacaoItem> itens) {
        return itens.stream()
                .map(FormulacaoItem::getQuantidadeKg)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal somarPercentual(List<FormulacaoItem> itens) {
        return itens.stream()
                .map(FormulacaoItem::getPercentual)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal somarCusto(List<FormulacaoItem> itens) {
        return itens.stream()
                .map(FormulacaoItem::getCustoTotal)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public FormulacaoRacao salvarFormula(String nome, List<FormulacaoItem> itens, String observacao) {

        String nomeValidado = validarNomeENumeroDeItens(nome, itens);

        formulacaoRacaoRepository.findByNomeIgnoreCase(nomeValidado).ifPresent(existente -> {
            throw new IllegalArgumentException("Já existe uma fórmula cadastrada com esse nome.");
        });

        FormulacaoRacao formulacao = new FormulacaoRacao();
        formulacao.setNome(nomeValidado);
        formulacao.setData(LocalDate.now());
        formulacao.setLoteKg(LOTE_PADRAO_KG);
        formulacao.setObservacao(observacao);
        formulacao.setCustoTotal(somarCusto(itens));

        for (FormulacaoItem item : itens) {
            item.setFormulacao(formulacao);
        }
        formulacao.setItens(itens);

        return formulacaoRacaoRepository.save(formulacao);
    }

    @Transactional
    public FormulacaoRacao atualizarFormula(Integer id, String nome, List<FormulacaoItem> itens, String observacao) {

        FormulacaoRacao formulacao = buscarPorId(id);

        String nomeValidado = validarNomeENumeroDeItens(nome, itens);

        formulacaoRacaoRepository.findByNomeIgnoreCase(nomeValidado)
                .filter(outra -> !outra.getId().equals(id))
                .ifPresent(outra -> {
                    throw new IllegalArgumentException("Já existe outra fórmula cadastrada com esse nome.");
                });

        formulacao.setNome(nomeValidado);
        formulacao.setObservacao(observacao);
        formulacao.setCustoTotal(somarCusto(itens));

        formulacao.getItens().clear();

        for (FormulacaoItem item : itens) {
            FormulacaoItem novoItem = new FormulacaoItem();
            novoItem.setMateriaPrima(item.getMateriaPrima());
            novoItem.setQuantidadeKg(item.getQuantidadeKg());
            novoItem.setPercentual(item.getPercentual());
            novoItem.setCustoUnitarioKg(item.getCustoUnitarioKg());
            novoItem.setCustoTotal(item.getCustoTotal());
            novoItem.setFormulacao(formulacao);
            formulacao.getItens().add(novoItem);
        }

        return formulacaoRacaoRepository.save(formulacao);
    }

    private String validarNomeENumeroDeItens(String nome, List<FormulacaoItem> itens) {

        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o nome da ração.");
        }

        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Adicione ao menos uma matéria-prima à fórmula.");
        }

        BigDecimal somaQuantidade = somarQuantidade(itens);

        if (somaQuantidade.subtract(LOTE_PADRAO_KG).abs().compareTo(TOLERANCIA_KG) > 0) {
            throw new IllegalArgumentException(
                    "A fórmula precisa fechar em " + LOTE_PADRAO_KG + " Kg (atualmente está em " +
                            somaQuantidade + " Kg)."
            );
        }

        return nome.trim();
    }

    public List<FormulacaoRacao> listarFormulas() {
        return formulacaoRacaoRepository.findAllByOrderByNome();
    }

    public boolean estaEmUso(Integer id) {
        return ordemProducaoRepository.existsByFormulacaoId(id);
    }

    @Transactional
    public void excluirFormula(Integer id, String motivoExclusao) {

        FormulacaoRacao formulacao = buscarPorId(id);

        List<OrdemProducao> ordensComEstaFormula = ordemProducaoRepository.findByFormulacaoId(id);

        for (OrdemProducao ordem : ordensComEstaFormula) {
            ordem.setNomeFormulaHistorico(formulacao.getNome());
            ordem.setMotivoExclusaoFormula(motivoExclusao);
            ordem.setFormulacao(null);
        }

        if (!ordensComEstaFormula.isEmpty()) {
            ordemProducaoRepository.saveAll(ordensComEstaFormula);
        }

        formulacaoRacaoRepository.delete(formulacao);
    }

    public FormulacaoRacao buscarPorId(Integer id) {
        return formulacaoRacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fórmula não encontrada."));
    }

    public List<FormulacaoItem> itensDaFormula(Integer formulacaoId) {
        return formulacaoItemRepository.findByFormulacaoIdOrderByPercentualDesc(formulacaoId);
    }

}
