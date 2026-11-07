package br.com.confisystem.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "movimentacao_animal")
public class MovimentacaoAnimal {

    public static final String TIPO_ENTRADA = "ENTRADA";
    public static final String TIPO_SAIDA = "SAIDA";
    public static final String TIPO_TRANSFERENCIA = "TRANSFERENCIA";
    public static final String TIPO_AJUSTE = "AJUSTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private String tipo;

    @ManyToOne
    @JoinColumn(name = "piquete_origem_id")
    private Piquete piqueteOrigem;

    @ManyToOne
    @JoinColumn(name = "piquete_destino_id")
    private Piquete piqueteDestino;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(columnDefinition = "text")
    private String observacao;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Piquete getPiqueteOrigem() { return piqueteOrigem; }
    public void setPiqueteOrigem(Piquete piqueteOrigem) { this.piqueteOrigem = piqueteOrigem; }

    public Piquete getPiqueteDestino() { return piqueteDestino; }
    public void setPiqueteDestino(Piquete piqueteDestino) { this.piqueteDestino = piqueteDestino; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
