package br.com.confisystem.service;

import br.com.confisystem.entity.FormulacaoItem;
import br.com.confisystem.entity.FormulacaoRacao;
import br.com.confisystem.entity.OrdemProducao;
import br.com.confisystem.entity.OrdemProducaoItem;
import br.com.confisystem.repository.OrdemProducaoItemRepository;
import br.com.confisystem.repository.OrdemProducaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrdemProducaoService {

    private static final BigDecimal LOTE_MAXIMO_BATIDA_KG = BigDecimal.valueOf(1000);
    private static final int ESCALA_KG = 3;
    private static final int ESCALA_MOEDA = 2;

    private final OrdemProducaoRepository ordemProducaoRepository;
    private final OrdemProducaoItemRepository ordemProducaoItemRepository;
    private final FormulacaoService formulacaoService;
    private final EstoqueService estoqueService;

    public OrdemProducaoService(
            OrdemProducaoRepository ordemProducaoRepository,
            OrdemProducaoItemRepository ordemProducaoItemRepository,
            FormulacaoService formulacaoService,
            EstoqueService estoqueService
    ) {
        this.ordemProducaoRepository = ordemProducaoRepository;
        this.ordemProducaoItemRepository = ordemProducaoItemRepository;
        this.formulacaoService = formulacaoService;
        this.estoqueService = estoqueService;
    }

    @Transactional
    public OrdemProducao gerarOrdem(Integer formulaId, BigDecimal quantidadeTotalKg) {

        if (quantidadeTotalKg == null || quantidadeTotalKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Informe uma quantidade a produzir maior que zero.");
        }

        FormulacaoRacao formula = formulacaoService.buscarPorId(formulaId);
        List<FormulacaoItem> itensFormula = formulacaoService.itensDaFormula(formulaId);

        if (itensFormula.isEmpty()) {
            throw new IllegalArgumentException("Essa fórmula não possui matérias-primas cadastradas.");
        }

        int numeroBatidas = quantidadeTotalKg
                .divide(LOTE_MAXIMO_BATIDA_KG, 0, RoundingMode.CEILING)
                .intValue();

        if (numeroBatidas < 1) {
            numeroBatidas = 1;
        }

        BigDecimal pesoBatidaKg = quantidadeTotalKg
                .divide(BigDecimal.valueOf(numeroBatidas), ESCALA_KG, RoundingMode.HALF_UP);

        BigDecimal fatorEscala = quantidadeTotalKg
                .divide(formula.getLoteKg(), 10, RoundingMode.HALF_UP);

        OrdemProducao ordem = new OrdemProducao();
        ordem.setFormulacao(formula);
        ordem.setNomeFormulaHistorico(formula.getNome());
        ordem.setQuantidadeTotalKg(quantidadeTotalKg);
        ordem.setNumeroBatidas(numeroBatidas);
        ordem.setPesoBatidaKg(pesoBatidaKg);
        ordem.setDataCriacao(LocalDate.now());
        ordem.setStatus(OrdemProducao.STATUS_PENDENTE);

        List<OrdemProducaoItem> itensOrdem = new ArrayList<>();

        for (FormulacaoItem itemFormula : itensFormula) {

            BigDecimal quantidadeTotalItem = itemFormula.getQuantidadeKg()
                    .multiply(fatorEscala)
                    .setScale(ESCALA_KG, RoundingMode.HALF_UP);

            OrdemProducaoItem itemOrdem = new OrdemProducaoItem();
            itemOrdem.setOrdem(ordem);
            itemOrdem.setMateriaPrima(itemFormula.getMateriaPrima());
            itemOrdem.setQuantidadeTotalKg(quantidadeTotalItem);

            itensOrdem.add(itemOrdem);
        }

        ordem.setItens(itensOrdem);

        return ordemProducaoRepository.save(ordem);
    }

    public BigDecimal pesoPorBatida(OrdemProducaoItem item, OrdemProducao ordem) {
        return item.getQuantidadeTotalKg()
                .divide(BigDecimal.valueOf(ordem.getNumeroBatidas()), ESCALA_KG, RoundingMode.HALF_UP);
    }

    @Transactional
    public OrdemProducao finalizarOrdem(Integer ordemId) {

        OrdemProducao ordem = buscarPorId(ordemId);

        if (!ordem.isPendente()) {
            throw new IllegalArgumentException("Essa ordem já foi finalizada.");
        }

        for (OrdemProducaoItem item : ordem.getItens()) {

            BigDecimal estoqueAtual = estoqueService.estoqueMateriaPrimaKg(item.getMateriaPrima().getId());

            if (estoqueAtual.compareTo(item.getQuantidadeTotalKg()) < 0) {
                throw new IllegalArgumentException(
                        "Estoque insuficiente de " + item.getMateriaPrima().getNome() +
                                ": disponível " + estoqueAtual + " Kg, necessário " +
                                item.getQuantidadeTotalKg() + " Kg."
                );
            }
        }

        BigDecimal custoTotalOrdem = BigDecimal.ZERO;

        for (OrdemProducaoItem item : ordem.getItens()) {

            BigDecimal custoUnitario = estoqueService.custoAtualKg(item.getMateriaPrima().getId());

            item.setCustoUnitarioKg(custoUnitario);

            if (custoUnitario != null) {
                BigDecimal custoItem = custoUnitario.multiply(item.getQuantidadeTotalKg())
                        .setScale(ESCALA_MOEDA, RoundingMode.HALF_UP);
                item.setCustoTotal(custoItem);
                custoTotalOrdem = custoTotalOrdem.add(custoItem);
            }
        }

        ordem.setCustoTotal(custoTotalOrdem);
        ordem.setStatus(OrdemProducao.STATUS_FINALIZADA);
        ordem.setDataFinalizacao(LocalDate.now());

        return ordemProducaoRepository.save(ordem);
    }

    @Transactional
    public OrdemProducao cancelarOrdem(Integer ordemId, String motivo) {

        OrdemProducao ordem = buscarPorId(ordemId);

        if (!ordem.isPendente()) {
            throw new IllegalArgumentException(
                    "Só é possível cancelar ordens PENDENTES. Essa ordem já está " + ordem.getStatus() +
                            " e não pode mais ser cancelada."
            );
        }

        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o motivo do cancelamento.");
        }

        ordem.setStatus(OrdemProducao.STATUS_CANCELADA);
        ordem.setMotivoCancelamento(motivo.trim());
        ordem.setDataCancelamento(LocalDate.now());

        return ordemProducaoRepository.save(ordem);
    }

    public OrdemProducao buscarPorId(Integer id) {
        return ordemProducaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ordem de produção não encontrada."));
    }

    public List<OrdemProducao> listarOrdens() {
        return ordemProducaoRepository.findAllByOrderByDataCriacaoDesc();
    }

    public List<OrdemProducao> listarOrdensPendentes() {
        return ordemProducaoRepository.findByStatusOrderByDataCriacaoDesc(OrdemProducao.STATUS_PENDENTE);
    }

    public List<OrdemProducaoItem> itensDaOrdem(Integer ordemId) {
        return ordemProducaoItemRepository.findByOrdemIdOrderByQuantidadeTotalKgDesc(ordemId);
    }

}
