package br.com.confisystem.service;

import br.com.confisystem.dto.MovimentacaoLinha;
import br.com.confisystem.dto.PiqueteResumo;
import br.com.confisystem.dto.TratoHistoricoLinha;
import br.com.confisystem.entity.FormulacaoRacao;
import br.com.confisystem.entity.MovimentacaoAnimal;
import br.com.confisystem.entity.Piquete;
import br.com.confisystem.entity.Trato;
import br.com.confisystem.repository.MovimentacaoAnimalRepository;
import br.com.confisystem.repository.PiqueteRepository;
import br.com.confisystem.repository.TratoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class PiqueteService {

    private static final int ESCALA_KG = 2;

    private final PiqueteRepository piqueteRepository;
    private final TratoRepository tratoRepository;
    private final FormulacaoService formulacaoService;
    private final EstoqueService estoqueService;
    private final MovimentacaoAnimalRepository movimentacaoAnimalRepository;

    public PiqueteService(
            PiqueteRepository piqueteRepository,
            TratoRepository tratoRepository,
            FormulacaoService formulacaoService,
            EstoqueService estoqueService,
            MovimentacaoAnimalRepository movimentacaoAnimalRepository
    ) {
        this.movimentacaoAnimalRepository = movimentacaoAnimalRepository;
        this.piqueteRepository = piqueteRepository;
        this.tratoRepository = tratoRepository;
        this.formulacaoService = formulacaoService;
        this.estoqueService = estoqueService;
    }

    public List<Piquete> listarAtivos() {
        return piqueteRepository.findByAtivoTrueOrderByNome();
    }

    public Piquete buscarPorId(Integer id) {
        return piqueteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Piquete não encontrado."));
    }

    @Transactional
    public Piquete criarPiquete(String nome, Integer quantidadeAnimais) {

        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do piquete.");
        }

        if (quantidadeAnimais == null || quantidadeAnimais < 0) {
            throw new IllegalArgumentException("Informe uma quantidade de animais válida.");
        }

        Piquete piquete = new Piquete();
        piquete.setNome(nome.trim());
        piquete.setQuantidadeAnimais(quantidadeAnimais);
        piquete.setAtivo(true);

        return piqueteRepository.save(piquete);
    }

    @Transactional
    public Piquete atualizarPiquete(Integer id, String nome, Integer quantidadeAnimais) {

        Piquete piquete = buscarPorId(id);

        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do piquete.");
        }

        if (quantidadeAnimais == null || quantidadeAnimais < 0) {
            throw new IllegalArgumentException("Informe uma quantidade de animais válida.");
        }

        int diferenca = quantidadeAnimais - piquete.getQuantidadeAnimais();

        piquete.setNome(nome.trim());
        piquete.setQuantidadeAnimais(quantidadeAnimais);

        Piquete salvo = piqueteRepository.save(piquete);

        if (diferenca != 0) {
            registrarMovimentacao(MovimentacaoAnimal.TIPO_AJUSTE, LocalDate.now(), null, salvo, diferenca,
                    "Correção manual da quantidade em Detalhes do Piquete");
        }

        return salvo;
    }

    @Transactional
    public void desativarPiquete(Integer id) {
        Piquete piquete = buscarPorId(id);
        piquete.setAtivo(false);
        piqueteRepository.save(piquete);
    }

    @Transactional
    public Piquete registrarEntradaAnimais(Integer piqueteId, Integer quantidade, LocalDate data, String observacao) {

        validarMovimentacao(quantidade, data);

        Piquete piquete = buscarAtivo(piqueteId);

        piquete.setQuantidadeAnimais(piquete.getQuantidadeAnimais() + quantidade);
        Piquete salvo = piqueteRepository.save(piquete);

        registrarMovimentacao(MovimentacaoAnimal.TIPO_ENTRADA, data, null, salvo, quantidade, observacao);

        return salvo;
    }

    @Transactional
    public Piquete registrarSaidaAnimais(Integer piqueteId, Integer quantidade, LocalDate data, String observacao) {

        validarMovimentacao(quantidade, data);

        Piquete piquete = buscarAtivo(piqueteId);

        validarSaldo(piquete, quantidade);

        piquete.setQuantidadeAnimais(piquete.getQuantidadeAnimais() - quantidade);
        Piquete salvo = piqueteRepository.save(piquete);

        registrarMovimentacao(MovimentacaoAnimal.TIPO_SAIDA, data, salvo, null, quantidade, observacao);

        return salvo;
    }

    @Transactional
    public void transferirAnimais(Integer origemId, Integer destinoId, Integer quantidade,
                                  LocalDate data, String observacao) {

        validarMovimentacao(quantidade, data);

        if (origemId == null || destinoId == null) {
            throw new IllegalArgumentException("Selecione o piquete de origem e o de destino.");
        }

        if (origemId.equals(destinoId)) {
            throw new IllegalArgumentException("O piquete de origem e o de destino devem ser diferentes.");
        }

        Piquete origem = buscarAtivo(origemId);
        Piquete destino = buscarAtivo(destinoId);

        validarSaldo(origem, quantidade);

        origem.setQuantidadeAnimais(origem.getQuantidadeAnimais() - quantidade);
        destino.setQuantidadeAnimais(destino.getQuantidadeAnimais() + quantidade);

        piqueteRepository.save(origem);
        piqueteRepository.save(destino);

        registrarMovimentacao(MovimentacaoAnimal.TIPO_TRANSFERENCIA, data, origem, destino, quantidade, observacao);
    }

    public List<MovimentacaoLinha> historicoMovimentacoes(Integer piqueteId) {

        List<MovimentacaoLinha> linhas = new ArrayList<>();

        for (MovimentacaoAnimal m : movimentacaoAnimalRepository.listarPorPiquete(piqueteId)) {

            MovimentacaoLinha linha = new MovimentacaoLinha();
            linha.setData(m.getData());
            linha.setObservacao(m.getObservacao());

            switch (m.getTipo()) {
                case MovimentacaoAnimal.TIPO_ENTRADA -> {
                    linha.setMovimento("Entrada de animais");
                    linha.setVariacao(m.getQuantidade());
                }
                case MovimentacaoAnimal.TIPO_SAIDA -> {
                    linha.setMovimento("Saída de animais");
                    linha.setVariacao(-m.getQuantidade());
                }
                case MovimentacaoAnimal.TIPO_TRANSFERENCIA -> {
                    boolean recebeu = m.getPiqueteDestino() != null && m.getPiqueteDestino().getId().equals(piqueteId);
                    if (recebeu) {
                        linha.setMovimento("Transferência recebida de " + nomeOuTraco(m.getPiqueteOrigem()));
                        linha.setVariacao(m.getQuantidade());
                    } else {
                        linha.setMovimento("Transferência enviada para " + nomeOuTraco(m.getPiqueteDestino()));
                        linha.setVariacao(-m.getQuantidade());
                    }
                }
                default -> {
                    linha.setMovimento("Correção manual");
                    linha.setVariacao(m.getQuantidade());
                }
            }

            linhas.add(linha);
        }

        return linhas;
    }

    private String nomeOuTraco(Piquete piquete) {
        return piquete != null ? piquete.getNome() : "-";
    }

    private void validarMovimentacao(Integer quantidade, LocalDate data) {

        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade de animais deve ser maior que zero.");
        }

        if (data == null) {
            throw new IllegalArgumentException("Informe a data da movimentação.");
        }
    }

    private Piquete buscarAtivo(Integer id) {

        if (id == null) {
            throw new IllegalArgumentException("Selecione o piquete.");
        }

        Piquete piquete = buscarPorId(id);

        if (!piquete.isAtivo()) {
            throw new IllegalArgumentException("O piquete \"" + piquete.getNome() + "\" está desativado.");
        }

        return piquete;
    }

    private void validarSaldo(Piquete piquete, Integer quantidade) {

        if (quantidade > piquete.getQuantidadeAnimais()) {
            throw new IllegalArgumentException(
                    "O piquete \"" + piquete.getNome() + "\" tem apenas " + piquete.getQuantidadeAnimais() +
                            " animal(is); não é possível retirar " + quantidade + "."
            );
        }
    }

    private void registrarMovimentacao(String tipo, LocalDate data, Piquete origem, Piquete destino,
                                       Integer quantidade, String observacao) {

        MovimentacaoAnimal movimentacao = new MovimentacaoAnimal();
        movimentacao.setTipo(tipo);
        movimentacao.setData(data);
        movimentacao.setPiqueteOrigem(origem);
        movimentacao.setPiqueteDestino(destino);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setObservacao(observacao == null || observacao.isBlank() ? null : observacao.trim());

        movimentacaoAnimalRepository.save(movimentacao);
    }

    public List<String> listarRacoesUtilizadas() {
        return tratoRepository.listarRacoesUtilizadas();
    }

    @Transactional
    public Trato registrarTrato(Integer piqueteId, LocalDate data, BigDecimal quantidadeKg, Integer formulaId, String observacao) {

        Piquete piquete = buscarPorId(piqueteId);

        if (data == null) {
            throw new IllegalArgumentException("Informe a data do trato.");
        }

        if (quantidadeKg == null || quantidadeKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Informe uma quantidade de ração maior que zero.");
        }

        if (formulaId == null) {
            throw new IllegalArgumentException("Selecione a ração (fórmula) que será usada.");
        }

        FormulacaoRacao formula = formulacaoService.buscarPorId(formulaId);

        BigDecimal estoqueRacao = estoqueService.estoqueRacaoProntaKg(formulaId);

        if (estoqueRacao.compareTo(quantidadeKg) < 0) {
            throw new IllegalArgumentException(
                    "Estoque de ração \"" + formula.getNome() + "\" insuficiente: disponível " +
                            estoqueRacao + " Kg. Gere e finalize uma Ordem de Produção antes de usar essa ração."
            );
        }

        Optional<Trato> ultimo = tratoRepository.findTopByPiqueteIdOrderByDataDesc(piqueteId);

        if (ultimo.isPresent() && !data.isAfter(ultimo.get().getData())) {
            throw new IllegalArgumentException(
                    "A data do trato deve ser posterior à data do último trato lançado (" +
                            ultimo.get().getData() + ")."
            );
        }

        Trato trato = new Trato();
        trato.setPiquete(piquete);
        trato.setData(data);
        trato.setQuantidadeKg(quantidadeKg);
        trato.setFormulacaoRacao(formula);
        trato.setRacao(formula.getNome());
        trato.setObservacao(observacao);

        return tratoRepository.save(trato);
    }

    public List<PiqueteResumo> listarResumos() {
        return listarAtivos().stream()
                .map(this::calcularResumo)
                .toList();
    }

    public PiqueteResumo calcularResumo(Piquete piquete) {

        PiqueteResumo resumo = new PiqueteResumo();
        resumo.setPiquete(piquete);

        List<Trato> ultimosDois = tratoRepository.findTop2ByPiqueteIdOrderByDataDesc(piquete.getId());

        if (ultimosDois.isEmpty()) {
            return resumo;
        }

        Trato ultimo = ultimosDois.get(0);
        resumo.setRacaoAtual(ultimo.getRacao());
        resumo.setInicioDietaAtual(ultimo.getData());
        resumo.setUltimoTratoKg(ultimo.getQuantidadeKg());

        if (ultimosDois.size() < 2) {
            return resumo;
        }

        Trato anterior = ultimosDois.get(1);

        long dias = ChronoUnit.DAYS.between(anterior.getData(), ultimo.getData());

        resumo.setUltimoCicloRacao(anterior.getRacao());
        resumo.setUltimoCicloInicio(anterior.getData());
        resumo.setUltimoCicloFim(ultimo.getData());

        if (dias > 0) {
            BigDecimal consumoMedio = anterior.getQuantidadeKg()
                    .divide(BigDecimal.valueOf(dias), ESCALA_KG, RoundingMode.HALF_UP);
            resumo.setUltimoCicloConsumoMedioKgDia(consumoMedio);
        }

        return resumo;
    }

    public List<TratoHistoricoLinha> historico(Integer piqueteId) {

        List<Trato> tratosAsc = new ArrayList<>(tratoRepository.findByPiqueteIdOrderByDataDesc(piqueteId));
        Collections.reverse(tratosAsc); // agora esta' do mais antigo para o mais novo

        List<TratoHistoricoLinha> linhas = new ArrayList<>();

        for (int i = 0; i < tratosAsc.size(); i++) {

            Trato atual = tratosAsc.get(i);

            TratoHistoricoLinha linha = new TratoHistoricoLinha();
            linha.setData(atual.getData());
            linha.setRacao(atual.getRacao());
            linha.setQuantidadeKg(atual.getQuantidadeKg());

            boolean existeProximo = i + 1 < tratosAsc.size();

            if (existeProximo) {

                Trato proximo = tratosAsc.get(i + 1);
                long dias = ChronoUnit.DAYS.between(atual.getData(), proximo.getData());

                linha.setDiasAteProximoTrato(dias);

                if (dias > 0) {
                    BigDecimal consumoMedio = atual.getQuantidadeKg()
                            .divide(BigDecimal.valueOf(dias), ESCALA_KG, RoundingMode.HALF_UP);
                    linha.setConsumoMedioKgDia(consumoMedio);
                }
            }

            linhas.add(linha);
        }

        Collections.reverse(linhas); // devolve do mais recente para o mais antigo

        return linhas;
    }

}
