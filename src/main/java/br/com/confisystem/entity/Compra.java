package br.com.confisystem.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compra")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Column(name = "numero_nota")
    private String numeroNota;

    @Column(name = "chave_acesso_nfe", unique = true)
    private String chaveAcessoNfe;

    @Column(nullable = false)
    private LocalDate data;

    private BigDecimal frete = BigDecimal.ZERO;

    private BigDecimal seguro = BigDecimal.ZERO;

    @Column(name = "outras_despesas")
    private BigDecimal outrasDespesas = BigDecimal.ZERO;

    private BigDecimal desconto = BigDecimal.ZERO;

    @Column(name = "valor_produtos")
    private BigDecimal valorProdutos = BigDecimal.ZERO;

    @Column(name = "valor_nota")
    private BigDecimal valorNota = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean ativo = true;

    // Dono do relacionamento e' CompraItem.compra. O cascade ALL faz com
    // que salvar/atualizar a Compra propague para os itens automaticamente
    // (substitui o antigo CompraItemDAO.salvar() chamado item a item).
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompraItem> itens = new ArrayList<>();

    public Compra() {
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

    // Getter derivado, usado na tabela de historico de compras
    public String getNomeFornecedor() {
        return fornecedor != null ? fornecedor.getNome() : "";
    }

    public String getNumeroNota() {
        return numeroNota;
    }

    public void setNumeroNota(String numeroNota) {
        this.numeroNota = numeroNota;
    }

    public String getChaveAcessoNfe() {
        return chaveAcessoNfe;
    }

    public void setChaveAcessoNfe(String chaveAcessoNfe) {
        this.chaveAcessoNfe = chaveAcessoNfe;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public BigDecimal getFrete() {
        return frete;
    }

    public void setFrete(BigDecimal frete) {
        this.frete = frete;
    }

    public BigDecimal getSeguro() {
        return seguro;
    }

    public void setSeguro(BigDecimal seguro) {
        this.seguro = seguro;
    }

    public BigDecimal getOutrasDespesas() {
        return outrasDespesas;
    }

    public void setOutrasDespesas(BigDecimal outrasDespesas) {
        this.outrasDespesas = outrasDespesas;
    }

    public BigDecimal getDesconto() {
        return desconto;
    }

    public void setDesconto(BigDecimal desconto) {
        this.desconto = desconto;
    }

    public BigDecimal getValorProdutos() {
        return valorProdutos;
    }

    public void setValorProdutos(BigDecimal valorProdutos) {
        this.valorProdutos = valorProdutos;
    }

    public BigDecimal getValorNota() {
        return valorNota;
    }

    public void setValorNota(BigDecimal valorNota) {
        this.valorNota = valorNota;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public List<CompraItem> getItens() {
        return itens;
    }

    public void setItens(List<CompraItem> itens) {
        this.itens = itens;
    }
}
