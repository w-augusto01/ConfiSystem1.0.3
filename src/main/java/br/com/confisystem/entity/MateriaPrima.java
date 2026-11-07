package br.com.confisystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "materia_prima")
public class MateriaPrima {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id")
    private CategoriaMateriaPrima categoria;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String unidade;

    @Column(name = "estoque_minimo")
    private Double estoqueMinimo;

    @Column(nullable = false)
    private boolean ativo = true;

    public MateriaPrima() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CategoriaMateriaPrima getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaMateriaPrima categoria) {
        this.categoria = categoria;
    }

    public String getNome() {
        return nome;
    }

    public String getNomeCategoria() {
        return categoria != null ? categoria.getNome() : "";
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public Double getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(Double estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return nome;
    }
}
