package br.com.confisystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "formulacao_racao")
public class FormulacaoRacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "lote_kg", nullable = false)
    private BigDecimal loteKg = BigDecimal.valueOf(1000);

    @Column(name = "custo_total")
    private BigDecimal custoTotal = BigDecimal.ZERO;

    private String observacao;

    @OneToMany(mappedBy = "formulacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormulacaoItem> itens = new ArrayList<>();

    public FormulacaoRacao() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public BigDecimal getLoteKg() {
        return loteKg;
    }

    public void setLoteKg(BigDecimal loteKg) {
        this.loteKg = loteKg;
    }

    public BigDecimal getCustoTotal() {
        return custoTotal;
    }

    public void setCustoTotal(BigDecimal custoTotal) {
        this.custoTotal = custoTotal;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<FormulacaoItem> getItens() {
        return itens;
    }

    public void setItens(List<FormulacaoItem> itens) {
        this.itens = itens;
    }

    @Override
    public String toString() {
        return nome;
    }

}
