package br.com.confisystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "trato")
public class Trato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "piquete_id")
    private Piquete piquete;

    @ManyToOne
    @JoinColumn(name = "formulacao_racao_id")
    private FormulacaoRacao formulacaoRacao;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "quantidade_kg", nullable = false)
    private BigDecimal quantidadeKg;

    @Column(nullable = false)
    private String racao;

    private String observacao;

    public Trato() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Piquete getPiquete() {
        return piquete;
    }

    public void setPiquete(Piquete piquete) {
        this.piquete = piquete;
    }

    public FormulacaoRacao getFormulacaoRacao() {
        return formulacaoRacao;
    }

    public void setFormulacaoRacao(FormulacaoRacao formulacaoRacao) {
        this.formulacaoRacao = formulacaoRacao;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public BigDecimal getQuantidadeKg() {
        return quantidadeKg;
    }

    public void setQuantidadeKg(BigDecimal quantidadeKg) {
        this.quantidadeKg = quantidadeKg;
    }

    public String getRacao() {
        return racao;
    }

    public void setRacao(String racao) {
        this.racao = racao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

}
