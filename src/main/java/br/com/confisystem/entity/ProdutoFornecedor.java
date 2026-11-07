package br.com.confisystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produto_fornecedor",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_produto_fornecedor",
                columnNames = {"fornecedor_id", "codigo_produto"}))
public class ProdutoFornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Column(name = "codigo_produto", nullable = false)
    private String codigoProduto;

    @Column(name = "descricao_produto")
    private String descricaoProduto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "materia_prima_id")
    private MateriaPrima materiaPrima;

    @Column(name = "unidade_nota", nullable = false)
    private String unidadeNota;

    @Column(name = "fator_para_kg", nullable = false)
    private BigDecimal fatorParaKg;

    public ProdutoFornecedor() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public String getCodigoProduto() {
        return codigoProduto;
    }

    public void setCodigoProduto(String codigoProduto) {
        this.codigoProduto = codigoProduto;
    }

    public String getDescricaoProduto() {
        return descricaoProduto;
    }

    public void setDescricaoProduto(String descricaoProduto) {
        this.descricaoProduto = descricaoProduto;
    }

    public MateriaPrima getMateriaPrima() {
        return materiaPrima;
    }

    public void setMateriaPrima(MateriaPrima materiaPrima) {
        this.materiaPrima = materiaPrima;
    }

    public String getUnidadeNota() {
        return unidadeNota;
    }

    public void setUnidadeNota(String unidadeNota) {
        this.unidadeNota = unidadeNota;
    }

    public BigDecimal getFatorParaKg() {
        return fatorParaKg;
    }

    public void setFatorParaKg(BigDecimal fatorParaKg) {
        this.fatorParaKg = fatorParaKg;
    }
}
