package br.com.confisystem.dto;

import java.time.LocalDate;

public class MovimentacaoLinha {

    private LocalDate data;
    private String movimento;
    private Integer variacao;
    private String observacao;

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getMovimento() { return movimento; }
    public void setMovimento(String movimento) { this.movimento = movimento; }

    public Integer getVariacao() { return variacao; }
    public void setVariacao(Integer variacao) { this.variacao = variacao; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
