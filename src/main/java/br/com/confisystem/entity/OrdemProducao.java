package br.com.confisystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ordem_producao")
public class OrdemProducao {

    public static final String STATUS_PENDENTE = "PENDENTE";
    public static final String STATUS_FINALIZADA = "FINALIZADA";
    public static final String STATUS_CANCELADA = "CANCELADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = true)
    @JoinColumn(name = "formulacao_id")
    private FormulacaoRacao formulacao;

    @Column(name = "nome_formula_historico")
    private String nomeFormulaHistorico;

    @Column(name = "motivo_exclusao_formula", columnDefinition = "text")
    private String motivoExclusaoFormula;

    @Column(name = "motivo_cancelamento", columnDefinition = "text")
    private String motivoCancelamento;

    @Column(name = "data_cancelamento")
    private LocalDate dataCancelamento;

    @Column(name = "quantidade_total_kg", nullable = false)
    private BigDecimal quantidadeTotalKg;

    @Column(name = "numero_batidas", nullable = false)
    private Integer numeroBatidas;

    @Column(name = "peso_batida_kg", nullable = false)
    private BigDecimal pesoBatidaKg;

    @Column(name = "data_criacao", nullable = false)
    private LocalDate dataCriacao;

    @Column(name = "data_finalizacao")
    private LocalDate dataFinalizacao;

    @Column(nullable = false)
    private String status = STATUS_PENDENTE;

    @Column(name = "custo_total")
    private BigDecimal custoTotal;

    @OneToMany(mappedBy = "ordem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdemProducaoItem> itens = new ArrayList<>();

    public OrdemProducao() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public FormulacaoRacao getFormulacao() {
        return formulacao;
    }

    public void setFormulacao(FormulacaoRacao formulacao) {
        this.formulacao = formulacao;
    }

    public String getNomeFormula() {

        if (formulacao != null) {
            return formulacao.getNome();
        }

        return nomeFormulaHistorico != null
                ? nomeFormulaHistorico + " (fórmula excluída)"
                : "Fórmula excluída";
    }

    public String getNomeFormulaHistorico() {
        return nomeFormulaHistorico;
    }

    public void setNomeFormulaHistorico(String nomeFormulaHistorico) {
        this.nomeFormulaHistorico = nomeFormulaHistorico;
    }

    public String getMotivoExclusaoFormula() {
        return motivoExclusaoFormula;
    }

    public void setMotivoExclusaoFormula(String motivoExclusaoFormula) {
        this.motivoExclusaoFormula = motivoExclusaoFormula;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public LocalDate getDataCancelamento() {
        return dataCancelamento;
    }

    public void setDataCancelamento(LocalDate dataCancelamento) {
        this.dataCancelamento = dataCancelamento;
    }

    public BigDecimal getQuantidadeTotalKg() {
        return quantidadeTotalKg;
    }

    public void setQuantidadeTotalKg(BigDecimal quantidadeTotalKg) {
        this.quantidadeTotalKg = quantidadeTotalKg;
    }

    public Integer getNumeroBatidas() {
        return numeroBatidas;
    }

    public void setNumeroBatidas(Integer numeroBatidas) {
        this.numeroBatidas = numeroBatidas;
    }

    public BigDecimal getPesoBatidaKg() {
        return pesoBatidaKg;
    }

    public void setPesoBatidaKg(BigDecimal pesoBatidaKg) {
        this.pesoBatidaKg = pesoBatidaKg;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDate getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDate dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isPendente() {
        return STATUS_PENDENTE.equals(status);
    }

    public boolean isCancelada() {
        return STATUS_CANCELADA.equals(status);
    }

    public BigDecimal getCustoTotal() {
        return custoTotal;
    }

    public void setCustoTotal(BigDecimal custoTotal) {
        this.custoTotal = custoTotal;
    }

    public List<OrdemProducaoItem> getItens() {
        return itens;
    }

    public void setItens(List<OrdemProducaoItem> itens) {
        this.itens = itens;
    }

}
