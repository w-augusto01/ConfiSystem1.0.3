package br.com.confisystem.dto;

import br.com.confisystem.entity.Piquete;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PiqueteResumo {

    private Piquete piquete;

    private String racaoAtual;
    private LocalDate inicioDietaAtual;
    private BigDecimal ultimoTratoKg;

    private String ultimoCicloRacao;
    private LocalDate ultimoCicloInicio;
    private LocalDate ultimoCicloFim;
    private BigDecimal ultimoCicloConsumoMedioKgDia;

    public Integer getPiqueteId() {
        return piquete != null ? piquete.getId() : null;
    }

    public String getNome() {
        return piquete != null ? piquete.getNome() : "";
    }

    public Integer getQuantidadeAnimais() {
        return piquete != null ? piquete.getQuantidadeAnimais() : null;
    }

    public Piquete getPiquete() {
        return piquete;
    }

    public void setPiquete(Piquete piquete) {
        this.piquete = piquete;
    }

    public String getRacaoAtual() {
        return racaoAtual;
    }

    public void setRacaoAtual(String racaoAtual) {
        this.racaoAtual = racaoAtual;
    }

    public LocalDate getInicioDietaAtual() {
        return inicioDietaAtual;
    }

    public void setInicioDietaAtual(LocalDate inicioDietaAtual) {
        this.inicioDietaAtual = inicioDietaAtual;
    }

    public BigDecimal getUltimoTratoKg() {
        return ultimoTratoKg;
    }

    public void setUltimoTratoKg(BigDecimal ultimoTratoKg) {
        this.ultimoTratoKg = ultimoTratoKg;
    }

    public String getUltimoCicloRacao() {
        return ultimoCicloRacao;
    }

    public void setUltimoCicloRacao(String ultimoCicloRacao) {
        this.ultimoCicloRacao = ultimoCicloRacao;
    }

    public LocalDate getUltimoCicloInicio() {
        return ultimoCicloInicio;
    }

    public void setUltimoCicloInicio(LocalDate ultimoCicloInicio) {
        this.ultimoCicloInicio = ultimoCicloInicio;
    }

    public LocalDate getUltimoCicloFim() {
        return ultimoCicloFim;
    }

    public void setUltimoCicloFim(LocalDate ultimoCicloFim) {
        this.ultimoCicloFim = ultimoCicloFim;
    }

    public BigDecimal getUltimoCicloConsumoMedioKgDia() {
        return ultimoCicloConsumoMedioKgDia;
    }

    public void setUltimoCicloConsumoMedioKgDia(BigDecimal ultimoCicloConsumoMedioKgDia) {
        this.ultimoCicloConsumoMedioKgDia = ultimoCicloConsumoMedioKgDia;
    }

    public boolean isTemCicloCompleto() {
        return ultimoCicloConsumoMedioKgDia != null;
    }

}
