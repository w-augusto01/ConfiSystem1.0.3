package br.com.confisystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "formulacao_item")
public class FormulacaoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "formulacao_id")
    private FormulacaoRacao formulacao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "materia_prima_id")
    private MateriaPrima materiaPrima;

    @Column(nullable = false)
    private BigDecimal percentual;

    @Column(name = "quantidade_kg", nullable = false)
    private BigDecimal quantidadeKg;

    @Column(name = "custo_unitario_kg")
    private BigDecimal custoUnitarioKg;

    @Column(name = "custo_total")
    private BigDecimal custoTotal;

    public FormulacaoItem() {
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

    public MateriaPrima getMateriaPrima() {
        return materiaPrima;
    }

    public void setMateriaPrima(MateriaPrima materiaPrima) {
        this.materiaPrima = materiaPrima;
    }

    public String getNomeMateriaPrima() {
        return materiaPrima != null ? materiaPrima.getNome() : "";
    }

    public BigDecimal getPercentual() {
        return percentual;
    }

    public void setPercentual(BigDecimal percentual) {
        this.percentual = percentual;
    }

    public BigDecimal getQuantidadeKg() {
        return quantidadeKg;
    }

    public void setQuantidadeKg(BigDecimal quantidadeKg) {
        this.quantidadeKg = quantidadeKg;
    }

    public BigDecimal getCustoUnitarioKg() {
        return custoUnitarioKg;
    }

    public void setCustoUnitarioKg(BigDecimal custoUnitarioKg) {
        this.custoUnitarioKg = custoUnitarioKg;
    }

    public BigDecimal getCustoTotal() {
        return custoTotal;
    }

    public void setCustoTotal(BigDecimal custoTotal) {
        this.custoTotal = custoTotal;
    }

}
