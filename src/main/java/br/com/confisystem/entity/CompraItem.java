package br.com.confisystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "compra_item")
public class CompraItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "compra_id")
    private Compra compra;

    @ManyToOne(optional = false)
    @JoinColumn(name = "materia_prima_id")
    private MateriaPrima materiaPrima;

    @Column(nullable = false)
    private BigDecimal quantidade;

    @Column(name = "valor_produto", nullable = false)
    private BigDecimal valorProduto;

    @Column(name = "despesas_rateadas")
    private BigDecimal despesasRateadas = BigDecimal.ZERO;

    @Column(name = "custo_final")
    private BigDecimal custoFinal = BigDecimal.ZERO;

    @Column(name = "custo_kg")
    private BigDecimal custoKg = BigDecimal.ZERO;

    @Column(name = "codigo_produto_xml")
    private String codigoProdutoXml;

    @Column(name = "descricao_produto_xml")
    private String descricaoProdutoXml;

    @Column(name = "item_importado_xml", nullable = false)
    private boolean itemImportadoXml = false;

    @Column(name = "quantidade_original")
    private BigDecimal quantidadeOriginal;

    @Column(name = "unidade_original")
    private String unidadeOriginal;

    @Column(name = "fator_conversao")
    private BigDecimal fatorConversao;

    @Column(name = "valor_produto_xml", precision = 15, scale = 2)
    private BigDecimal valorProdutoXml;

    public BigDecimal getValorProdutoXml() {
        return valorProdutoXml;
    }

    public void setValorProdutoXml(BigDecimal valorProdutoXml) {
        this.valorProdutoXml = valorProdutoXml;
    }

    public CompraItem() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
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

    public String getUnidadeMateriaPrima() {
        return materiaPrima != null ? materiaPrima.getUnidade() : "";
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorProduto() {
        return valorProduto;
    }

    public void setValorProduto(BigDecimal valorProduto) {
        this.valorProduto = valorProduto;
    }

    public BigDecimal getDespesasRateadas() {
        return despesasRateadas;
    }

    public void setDespesasRateadas(BigDecimal despesasRateadas) {
        this.despesasRateadas = despesasRateadas;
    }

    public BigDecimal getCustoFinal() {
        return custoFinal;
    }

    public void setCustoFinal(BigDecimal custoFinal) {
        this.custoFinal = custoFinal;
    }

    public BigDecimal getCustoKg() {
        return custoKg;
    }

    public void setCustoKg(BigDecimal custoKg) {
        this.custoKg = custoKg;
    }

    public BigDecimal getQuantidadeOriginal() {
        return quantidadeOriginal;
    }

    public void setQuantidadeOriginal(BigDecimal quantidadeOriginal) {
        this.quantidadeOriginal = quantidadeOriginal;
    }

    public String getUnidadeOriginal() {
        return unidadeOriginal;
    }

    public void setUnidadeOriginal(String unidadeOriginal) {
        this.unidadeOriginal = unidadeOriginal;
    }

    public BigDecimal getFatorConversao() {
        return fatorConversao;
    }

    public void setFatorConversao(BigDecimal fatorConversao) {
        this.fatorConversao = fatorConversao;
    }

    public String getCodigoProdutoXml() {
        return codigoProdutoXml;
    }

    public void setCodigoProdutoXml(String codigoProdutoXml) {
        this.codigoProdutoXml = codigoProdutoXml;
    }

    public String getDescricaoProdutoXml() {
        return descricaoProdutoXml;
    }

    public void setDescricaoProdutoXml(String descricaoProdutoXml) {
        this.descricaoProdutoXml = descricaoProdutoXml;
    }

    public boolean isItemImportadoXml() {
        return itemImportadoXml;
    }

    public void setItemImportadoXml(boolean itemImportadoXml) {
        this.itemImportadoXml = itemImportadoXml;
    }

    public String getOrigemNota() {
        if (quantidadeOriginal == null || unidadeOriginal == null) {
            return "-";
        }
        String qtd = quantidadeOriginal.stripTrailingZeros().toPlainString();
        String fator = fatorConversao == null ? "?" : fatorConversao.stripTrailingZeros().toPlainString();
        return qtd + " " + unidadeOriginal + " (x" + fator + ")";
    }
}
