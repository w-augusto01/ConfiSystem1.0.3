package br.com.confisystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ordem_producao_item")
public class OrdemProducaoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ordem_id")
    private OrdemProducao ordem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "materia_prima_id")
    private MateriaPrima materiaPrima;

    @Column(name = "quantidade_total_kg", nullable = false)
    private BigDecimal quantidadeTotalKg;

    @Column(name = "custo_unitario_kg")
    private BigDecimal custoUnitarioKg;

    @Column(name = "custo_total")
    private BigDecimal custoTotal;

    public OrdemProducaoItem() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public OrdemProducao getOrdem() {
        return ordem;
    }

    public void setOrdem(OrdemProducao ordem) {
        this.ordem = ordem;
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

    public BigDecimal getQuantidadeTotalKg() {
        return quantidadeTotalKg;
    }

    public void setQuantidadeTotalKg(BigDecimal quantidadeTotalKg) {
        this.quantidadeTotalKg = quantidadeTotalKg;
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
