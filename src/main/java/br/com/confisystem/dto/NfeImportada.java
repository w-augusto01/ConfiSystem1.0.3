package br.com.confisystem.dto;

import br.com.confisystem.entity.Fornecedor;
import br.com.confisystem.entity.MateriaPrima;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NfeImportada {

    private String chaveAcesso;
    private String numero;
    private LocalDate dataEmissao;

    private String documentoEmitente; // CNPJ (ou CPF de produtor rural), so' digitos
    private String nomeEmitente;

    private BigDecimal valorProdutos = BigDecimal.ZERO;
    private BigDecimal frete = BigDecimal.ZERO;
    private BigDecimal seguro = BigDecimal.ZERO;
    private BigDecimal outrasDespesas = BigDecimal.ZERO;
    private BigDecimal desconto = BigDecimal.ZERO;
    private BigDecimal valorNota = BigDecimal.ZERO;

    private final List<Item> itens = new ArrayList<>();

    private Fornecedor fornecedorResolvido;

    public Fornecedor getFornecedorResolvido() { return fornecedorResolvido; }
    public void setFornecedorResolvido(Fornecedor fornecedorResolvido) { this.fornecedorResolvido = fornecedorResolvido; }

    public String getChaveAcesso() { return chaveAcesso; }
    public void setChaveAcesso(String chaveAcesso) { this.chaveAcesso = chaveAcesso; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public LocalDate getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDate dataEmissao) { this.dataEmissao = dataEmissao; }

    public String getDocumentoEmitente() { return documentoEmitente; }
    public void setDocumentoEmitente(String documentoEmitente) { this.documentoEmitente = documentoEmitente; }

    public String getNomeEmitente() { return nomeEmitente; }
    public void setNomeEmitente(String nomeEmitente) { this.nomeEmitente = nomeEmitente; }

    public BigDecimal getValorProdutos() { return valorProdutos; }
    public void setValorProdutos(BigDecimal valorProdutos) { this.valorProdutos = valorProdutos; }

    public BigDecimal getFrete() { return frete; }
    public void setFrete(BigDecimal frete) { this.frete = frete; }

    public BigDecimal getSeguro() { return seguro; }
    public void setSeguro(BigDecimal seguro) { this.seguro = seguro; }

    public BigDecimal getOutrasDespesas() { return outrasDespesas; }
    public void setOutrasDespesas(BigDecimal outrasDespesas) { this.outrasDespesas = outrasDespesas; }

    public BigDecimal getDesconto() { return desconto; }
    public void setDesconto(BigDecimal desconto) { this.desconto = desconto; }

    public BigDecimal getValorNota() { return valorNota; }
    public void setValorNota(BigDecimal valorNota) { this.valorNota = valorNota; }

    public List<Item> getItens() { return itens; }

    public static class Item {

        private String codigoProduto;      // cProd
        private String descricao;          // xProd
        private String unidadeNota;        // uCom (ja normalizada, ex.: SC, KG)
        private BigDecimal quantidadeNota; // qCom
        private BigDecimal valorProduto;   // vProd
        private String unidadeTributavel;  // uTrib
        private BigDecimal quantidadeTributavel; // qTrib

        private MateriaPrima materiaPrima;
        private BigDecimal fatorParaKg;    // quantos KG tem 1 unidade da nota
        private String origemFator;        // de onde veio o fator (cadastro, sugestao...)
        private boolean mapeado;           // true = veio do cadastro, nao precisa perguntar

        public String getCodigoProduto() { return codigoProduto; }
        public void setCodigoProduto(String codigoProduto) { this.codigoProduto = codigoProduto; }

        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }

        public String getUnidadeNota() { return unidadeNota; }
        public void setUnidadeNota(String unidadeNota) { this.unidadeNota = unidadeNota; }

        public BigDecimal getQuantidadeNota() { return quantidadeNota; }
        public void setQuantidadeNota(BigDecimal quantidadeNota) { this.quantidadeNota = quantidadeNota; }

        public BigDecimal getValorProduto() { return valorProduto; }
        public void setValorProduto(BigDecimal valorProduto) { this.valorProduto = valorProduto; }

        public String getUnidadeTributavel() { return unidadeTributavel; }
        public void setUnidadeTributavel(String unidadeTributavel) { this.unidadeTributavel = unidadeTributavel; }

        public BigDecimal getQuantidadeTributavel() { return quantidadeTributavel; }
        public void setQuantidadeTributavel(BigDecimal quantidadeTributavel) { this.quantidadeTributavel = quantidadeTributavel; }

        public MateriaPrima getMateriaPrima() { return materiaPrima; }
        public void setMateriaPrima(MateriaPrima materiaPrima) { this.materiaPrima = materiaPrima; }

        public BigDecimal getFatorParaKg() { return fatorParaKg; }
        public void setFatorParaKg(BigDecimal fatorParaKg) { this.fatorParaKg = fatorParaKg; }

        public String getOrigemFator() { return origemFator; }
        public void setOrigemFator(String origemFator) { this.origemFator = origemFator; }

        public boolean isMapeado() { return mapeado; }
        public void setMapeado(boolean mapeado) { this.mapeado = mapeado; }

        public BigDecimal getQuantidadeKg() {
            if (quantidadeNota == null || fatorParaKg == null) {
                return null;
            }
            return quantidadeNota.multiply(fatorParaKg).setScale(3, RoundingMode.HALF_UP);
        }
    }
}
