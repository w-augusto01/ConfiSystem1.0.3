package br.com.confisystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TratoHistoricoLinha {

    private LocalDate data;
    private String racao;
    private BigDecimal quantidadeKg;
    private Long diasAteProximoTrato;
    private BigDecimal consumoMedioKgDia;

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getRacao() {
        return racao;
    }

    public void setRacao(String racao) {
        this.racao = racao;
    }

    public BigDecimal getQuantidadeKg() {
        return quantidadeKg;
    }

    public void setQuantidadeKg(BigDecimal quantidadeKg) {
        this.quantidadeKg = quantidadeKg;
    }

    public Long getDiasAteProximoTrato() {
        return diasAteProximoTrato;
    }

    public void setDiasAteProximoTrato(Long diasAteProximoTrato) {
        this.diasAteProximoTrato = diasAteProximoTrato;
    }

    public BigDecimal getConsumoMedioKgDia() {
        return consumoMedioKgDia;
    }

    public void setConsumoMedioKgDia(BigDecimal consumoMedioKgDia) {
        this.consumoMedioKgDia = consumoMedioKgDia;
    }

    public String getSituacao() {
        return diasAteProximoTrato != null ? diasAteProximoTrato + " dias" : "Em andamento";
    }

}
