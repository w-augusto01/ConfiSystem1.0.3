package br.com.confisystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "piquete")
public class Piquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "quantidade_animais", nullable = false)
    private Integer quantidadeAnimais;

    @Column(nullable = false)
    private boolean ativo = true;

    public Piquete() {
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

    public Integer getQuantidadeAnimais() {
        return quantidadeAnimais;
    }

    public void setQuantidadeAnimais(Integer quantidadeAnimais) {
        this.quantidadeAnimais = quantidadeAnimais;
    }

    public boolean isAtivo() {
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
