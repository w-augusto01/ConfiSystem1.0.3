package br.com.confisystem.controller;

import br.com.confisystem.dto.MovimentacaoLinha;
import br.com.confisystem.dto.PiqueteResumo;
import br.com.confisystem.dto.TratoHistoricoLinha;
import br.com.confisystem.entity.Piquete;
import br.com.confisystem.service.PiqueteService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Component
@Scope("prototype")
public class PiqueteDetalheController {

    @FXML
    private Label lblTituloPiquete;

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtQuantidadeAnimais;

    @FXML
    private Button btnSalvar;

    @FXML
    private Button btnDesativar;

    @FXML
    private Label lblRacaoAtual;

    @FXML
    private Label lblInicioDieta;

    @FXML
    private Label lblUltimoTrato;

    @FXML
    private Label lblCicloTitulo;

    @FXML
    private Label lblCicloRacao;

    @FXML
    private Label lblCicloPeriodo;

    @FXML
    private Label lblCicloConsumo;

    @FXML
    private TableView<TratoHistoricoLinha> tabelaHistorico;

    @FXML
    private TableColumn<TratoHistoricoLinha, LocalDate> colData;

    @FXML
    private TableColumn<TratoHistoricoLinha, String> colRacao;

    @FXML
    private TableColumn<TratoHistoricoLinha, BigDecimal> colQuantidade;

    @FXML
    private TableColumn<TratoHistoricoLinha, String> colSituacao;

    @FXML
    private TableColumn<TratoHistoricoLinha, BigDecimal> colConsumoMedio;

    @FXML
    private TableView<MovimentacaoLinha> tabelaMovimentacoes;

    @FXML
    private TableColumn<MovimentacaoLinha, LocalDate> colMovData;

    @FXML
    private TableColumn<MovimentacaoLinha, String> colMovMovimento;

    @FXML
    private TableColumn<MovimentacaoLinha, Integer> colMovVariacao;

    @FXML
    private TableColumn<MovimentacaoLinha, String> colMovObservacao;

    private final PiqueteService piqueteService;

    private final DecimalFormat formatoKg;
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Integer piqueteId;
    private Runnable aoAtualizar;

    public PiqueteDetalheController(PiqueteService piqueteService) {
        this.piqueteService = piqueteService;

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("pt", "BR"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');

        formatoKg = new DecimalFormat("#,##0.##", simbolos);
    }

    @FXML
    public void initialize() {

        colData.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("data"));
        colRacao.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("racao"));
        colQuantidade.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantidadeKg"));
        colSituacao.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("situacao"));
        colConsumoMedio.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("consumoMedioKgDia"));

        colData.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : valor.format(formatoData));
            }
        });

        colQuantidade.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : formatoKg.format(valor) + " Kg");
            }
        });

        colConsumoMedio.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? "—" : formatoKg.format(valor) + " Kg/Dia");
            }
        });

        colMovData.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("data"));
        colMovMovimento.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("movimento"));
        colMovVariacao.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("variacao"));
        colMovObservacao.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("observacao"));

        colMovData.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : valor.format(formatoData));
            }
        });

        colMovVariacao.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(Integer valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : (valor > 0 ? "+" + valor : String.valueOf(valor)));
            }
        });

        tabelaMovimentacoes.setRowFactory(tv -> {

            TableRow<MovimentacaoLinha> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (row.isEmpty()) {
                    tabelaMovimentacoes.getSelectionModel().clearSelection();
                }
            });

            return row;
        });

        tabelaHistorico.setRowFactory(tv -> {

            TableRow<TratoHistoricoLinha> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (row.isEmpty()) {
                    tabelaHistorico.getSelectionModel().clearSelection();
                }
            });

            return row;
        });

    }

    public void carregar(Integer piqueteId, Runnable aoAtualizar) {

        this.piqueteId = piqueteId;
        this.aoAtualizar = aoAtualizar;

        recarregarDados();
    }

    private void recarregarDados() {

        Piquete piquete = piqueteService.buscarPorId(piqueteId);

        lblTituloPiquete.setText(piquete.getNome());

        txtNome.setText(piquete.getNome());
        txtQuantidadeAnimais.setText(String.valueOf(piquete.getQuantidadeAnimais()));

        PiqueteResumo resumo = piqueteService.calcularResumo(piquete);

        lblRacaoAtual.setText(resumo.getRacaoAtual() != null ? resumo.getRacaoAtual() : "-");
        lblInicioDieta.setText(resumo.getInicioDietaAtual() != null
                ? resumo.getInicioDietaAtual().format(formatoData) : "-");
        lblUltimoTrato.setText(resumo.getUltimoTratoKg() != null
                ? formatoKg.format(resumo.getUltimoTratoKg()) + " Kg" : "-");

        if (resumo.isTemCicloCompleto()) {
            lblCicloRacao.setText("Ração: " + resumo.getUltimoCicloRacao());
            lblCicloPeriodo.setText("Período: " +
                    resumo.getUltimoCicloInicio().format(formatoData) + " → " +
                    resumo.getUltimoCicloFim().format(formatoData));
            lblCicloConsumo.setText("Consumo Médio: " +
                    formatoKg.format(resumo.getUltimoCicloConsumoMedioKgDia()) + " Kg/Dia");
        } else {
            lblCicloRacao.setText("Aguardando o próximo trato para fechar o primeiro ciclo.");
            lblCicloPeriodo.setText("");
            lblCicloConsumo.setText("");
        }

        tabelaHistorico.getItems().setAll(piqueteService.historico(piqueteId));
        tabelaMovimentacoes.getItems().setAll(piqueteService.historicoMovimentacoes(piqueteId));
    }

    @FXML
    private void salvar(ActionEvent event) {

        if (txtNome.getText() == null || txtNome.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Informe o nome do piquete.");
            return;
        }

        Integer quantidade;

        try {
            quantidade = Integer.parseInt(txtQuantidadeAnimais.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Quantidade de animais inválida.");
            return;
        }

        try {

            piqueteService.atualizarPiquete(piqueteId, txtNome.getText(), quantidade);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Piquete atualizado com sucesso.");

            recarregarDados();

            if (aoAtualizar != null) {
                aoAtualizar.run();
            }

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", e.getMessage());
        }
    }

    @FXML
    private void desativar(ActionEvent event) {

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja realmente desativar este piquete? Ele deixará de aparecer no painel de Confinamento.");
        confirmacao.setTitle("Confirmação");
        confirmacao.setHeaderText(null);

        Optional<ButtonType> resposta = confirmacao.showAndWait();

        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {

            piqueteService.desativarPiquete(piqueteId);

            if (aoAtualizar != null) {
                aoAtualizar.run();
            }

            ((Stage) btnDesativar.getScene().getWindow()).close();
        }
    }

    @FXML
    private void fechar(ActionEvent event) {
        ((Stage) btnSalvar.getScene().getWindow()).close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

}
