package br.com.confisystem.controller;

import br.com.confisystem.util.Icone;

import br.com.confisystem.dto.PiqueteResumo;
import br.com.confisystem.entity.FormulacaoRacao;
import br.com.confisystem.entity.Piquete;
import br.com.confisystem.service.EstoqueService;
import br.com.confisystem.service.FormulacaoService;
import br.com.confisystem.service.PiqueteService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
@Scope("prototype")
public class ConfinamentoController implements Initializable {

    @FXML
    private HBox kpiContainer;

    @FXML
    private Label lblTotalPiquetes;

    @FXML
    private Label lblTotalAnimais;

    @FXML
    private Label lblTotalUltimoTrato;

    @FXML
    private Label lblConsumoMedioGeral;

    @FXML
    private FlowPane piquetesContainer;

    private final PiqueteService piqueteService;
    private final FormulacaoService formulacaoService;
    private final EstoqueService estoqueService;
    private final ApplicationContext applicationContext;

    private final DecimalFormat formatoKg;
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ConfinamentoController(
            PiqueteService piqueteService,
            FormulacaoService formulacaoService,
            EstoqueService estoqueService,
            ApplicationContext applicationContext
    ) {
        this.piqueteService = piqueteService;
        this.formulacaoService = formulacaoService;
        this.estoqueService = estoqueService;
        this.applicationContext = applicationContext;

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("pt", "BR"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');

        formatoKg = new DecimalFormat("#,##0.##", simbolos);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        atualizarDashboard();
    }


    private void atualizarDashboard() {

        List<PiqueteResumo> resumos = piqueteService.listarResumos();

        atualizarKpis(resumos);

        piquetesContainer.getChildren().clear();

        for (PiqueteResumo resumo : resumos) {
            piquetesContainer.getChildren().add(construirCardPiquete(resumo));
        }
    }

    private void atualizarKpis(List<PiqueteResumo> resumos) {

        int totalPiquetes = resumos.size();

        int totalAnimais = resumos.stream()
                .mapToInt(r -> r.getQuantidadeAnimais() != null ? r.getQuantidadeAnimais() : 0)
                .sum();

        BigDecimal totalUltimoTrato = resumos.stream()
                .map(PiqueteResumo::getUltimoTratoKg)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BigDecimal> consumosValidos = resumos.stream()
                .map(PiqueteResumo::getUltimoCicloConsumoMedioKgDia)
                .filter(v -> v != null)
                .toList();

        lblTotalPiquetes.setText(String.valueOf(totalPiquetes));
        lblTotalAnimais.setText(String.valueOf(totalAnimais));
        lblTotalUltimoTrato.setText(formatoKg.format(totalUltimoTrato) + " Kg");

        if (consumosValidos.isEmpty()) {
            lblConsumoMedioGeral.setText("-");
        } else {
            BigDecimal soma = consumosValidos.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal media = soma.divide(BigDecimal.valueOf(consumosValidos.size()), 2, java.math.RoundingMode.HALF_UP);
            lblConsumoMedioGeral.setText(formatoKg.format(media) + " Kg/Dia");
        }
    }

    private VBox construirCardPiquete(PiqueteResumo resumo) {

        VBox card = new VBox();
        card.getStyleClass().add("piquete-card");
        card.setPrefWidth(280);

        Label nome = new Label(resumo.getNome());
        nome.getStyleClass().add("piquete-card-title");

        VBox campos = new VBox(8);
        campos.getChildren().addAll(
                campo("Quantidade de Animais", resumo.getQuantidadeAnimais() + " Cabeças"),
                campo("Ração Atual", resumo.getRacaoAtual() != null ? resumo.getRacaoAtual() : "-"),
                campo("Início da Dieta Atual", resumo.getInicioDietaAtual() != null
                        ? resumo.getInicioDietaAtual().format(formatoData) : "-"),
                campo("Último Trato", resumo.getUltimoTratoKg() != null
                        ? formatoKg.format(resumo.getUltimoTratoKg()) + " Kg" : "-")
        );

        VBox cicloBox = new VBox();
        cicloBox.getStyleClass().add("ciclo-box");

        if (resumo.isTemCicloCompleto()) {

            Label cicloTitulo = new Label("Último Ciclo");
            cicloTitulo.getStyleClass().add("ciclo-title");

            Label cicloRacao = new Label("Ração: " + resumo.getUltimoCicloRacao());
            Label cicloPeriodo = new Label("Período: " +
                    resumo.getUltimoCicloInicio().format(formatoData) + " → " +
                    resumo.getUltimoCicloFim().format(formatoData));
            Label cicloConsumo = new Label("Consumo Médio: " +
                    formatoKg.format(resumo.getUltimoCicloConsumoMedioKgDia()) + " Kg/Dia");

            cicloBox.getChildren().addAll(cicloTitulo, cicloRacao, cicloPeriodo, cicloConsumo);

        } else {

            Label cicloTitulo = new Label("Último Ciclo");
            cicloTitulo.getStyleClass().add("ciclo-title");

            Label vazio = new Label("Aguardando o próximo trato para fechar o primeiro ciclo.");
            vazio.getStyleClass().add("ciclo-empty");
            vazio.setWrapText(true);

            cicloBox.getChildren().addAll(cicloTitulo, vazio);
        }

        Button btnHistorico = new Button("Histórico");
        btnHistorico.getStyleClass().add("btn-secondary");
        btnHistorico.setOnAction(e -> abrirHistorico(resumo.getPiqueteId(), resumo.getNome()));

        Button btnDetalhes = new Button("Detalhes");
        btnDetalhes.getStyleClass().add("btn-secondary");
        btnDetalhes.setOnAction(e -> abrirDetalhes(resumo.getPiqueteId()));

        HBox botoes = new HBox(10, btnHistorico, btnDetalhes);

        Button btnMovimentar = new Button("Movimentar Animais");
        btnMovimentar.getStyleClass().add("btn-secondary");
        btnMovimentar.setMaxWidth(Double.MAX_VALUE);
        btnMovimentar.setOnAction(e -> abrirDialogoMovimentacao(resumo.getPiqueteId()));

        card.getChildren().addAll(nome, campos, cicloBox, botoes, btnMovimentar);

        return card;
    }

    private VBox campo(String rotulo, String valor) {

        Label lblRotulo = new Label(rotulo);
        lblRotulo.getStyleClass().add("info-label");

        Label lblValor = new Label(valor);
        lblValor.getStyleClass().add("info-value");

        VBox box = new VBox(2, lblRotulo, lblValor);
        return box;
    }


    @FXML
    private void novoPiquete() {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Novo Piquete");
        dialog.setHeaderText(null);

        ButtonType btnCriar = new ButtonType("Criar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCriar, ButtonType.CANCEL);

        TextField txtNome = new TextField();
        txtNome.setPromptText("Ex.: Piquete 07");

        TextField txtQuantidade = new TextField();
        txtQuantidade.setPromptText("Ex.: 150");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 10, 10, 10));

        grid.add(new Label("Nome do Piquete:"), 0, 0);
        grid.add(txtNome, 1, 0);
        grid.add(new Label("Quantidade de Animais:"), 0, 1);
        grid.add(txtQuantidade, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultado = dialog.showAndWait();

        if (resultado.isPresent() && resultado.get() == btnCriar) {

            try {

                Integer quantidade = parseInteiro(txtQuantidade.getText());

                piqueteService.criarPiquete(txtNome.getText(), quantidade);

                atualizarDashboard();

            } catch (IllegalArgumentException e) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validação", e.getMessage());
            }
        }
    }

    @FXML
    private void movimentarAnimais() {
        abrirDialogoMovimentacao(null);
    }


    private void abrirDialogoMovimentacao(Integer piquetePreSelecionadoId) {

        List<Piquete> piquetes = piqueteService.listarAtivos();

        if (piquetes.isEmpty()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Nenhum piquete cadastrado",
                    "Cadastre um piquete antes de movimentar animais.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Movimentar Animais");
        dialog.setHeaderText(null);

        ButtonType btnConfirmar = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirmar, ButtonType.CANCEL);

        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList(
                "Entrada (chegada de animais)",
                "Saída (retirada de animais)",
                "Transferência entre piquetes"
        ));
        cbTipo.setPrefWidth(280);
        cbTipo.getSelectionModel().select(0);

        javafx.util.StringConverter<Piquete> conversorPiquete = new javafx.util.StringConverter<>() {
            @Override
            public String toString(Piquete p) {
                return p == null ? "" : p.getNome() + "  (" + p.getQuantidadeAnimais() + " cab.)";
            }

            @Override
            public Piquete fromString(String texto) {
                return null;
            }
        };

        ComboBox<Piquete> cbOrigem = new ComboBox<>(FXCollections.observableArrayList(piquetes));
        cbOrigem.setPrefWidth(280);
        cbOrigem.setConverter(conversorPiquete);

        ComboBox<Piquete> cbDestino = new ComboBox<>(FXCollections.observableArrayList(piquetes));
        cbDestino.setPrefWidth(280);
        cbDestino.setConverter(conversorPiquete);

        if (piquetePreSelecionadoId != null) {
            piquetes.stream()
                    .filter(p -> p.getId().equals(piquetePreSelecionadoId))
                    .findFirst()
                    .ifPresent(selecionado -> cbOrigem.getSelectionModel().select(selecionado));
        }

        TextField txtQuantidade = new TextField();
        txtQuantidade.setPromptText("Ex.: 20");

        DatePicker dpData = new DatePicker(LocalDate.now());

        TextField txtObservacao = new TextField();
        txtObservacao.setPromptText("Opcional (ex.: lote do fornecedor, motivo da saída)");

        Label lblOrigem = new Label("Piquete:");
        Label lblDestino = new Label("Para o piquete:");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 10, 10, 10));

        grid.add(new Label("Movimentação:"), 0, 0);
        grid.add(cbTipo, 1, 0);
        grid.add(lblOrigem, 0, 1);
        grid.add(cbOrigem, 1, 1);
        grid.add(lblDestino, 0, 2);
        grid.add(cbDestino, 1, 2);
        grid.add(new Label("Quantidade de animais:"), 0, 3);
        grid.add(txtQuantidade, 1, 3);
        grid.add(new Label("Data:"), 0, 4);
        grid.add(dpData, 1, 4);
        grid.add(new Label("Observação:"), 0, 5);
        grid.add(txtObservacao, 1, 5);


        Runnable ajustarCampos = () -> {
            boolean transferencia = cbTipo.getSelectionModel().getSelectedIndex() == 2;
            lblOrigem.setText(transferencia ? "Do piquete:" : "Piquete:");
            lblDestino.setVisible(transferencia);
            lblDestino.setManaged(transferencia);
            cbDestino.setVisible(transferencia);
            cbDestino.setManaged(transferencia);
        };

        cbTipo.setOnAction(e -> {
            ajustarCampos.run();
            javafx.application.Platform.runLater(() -> {
                if (dialog.getDialogPane().getScene() != null
                        && dialog.getDialogPane().getScene().getWindow() != null) {
                    dialog.getDialogPane().getScene().getWindow().sizeToScene();
                }
            });
        });
        ajustarCampos.run();

        dialog.setResizable(true);
        dialog.getDialogPane().setContent(grid);

        String[] mensagemSucesso = new String[1];

        Button botaoConfirmar = (Button) dialog.getDialogPane().lookupButton(btnConfirmar);

        botaoConfirmar.addEventFilter(ActionEvent.ACTION, evento -> {

            try {

                int tipo = cbTipo.getSelectionModel().getSelectedIndex();
                Piquete origem = cbOrigem.getValue();
                Piquete destino = cbDestino.getValue();

                if (origem == null) {
                    throw new IllegalArgumentException(tipo == 2
                            ? "Selecione o piquete de origem."
                            : "Selecione o piquete.");
                }

                if (tipo == 2 && destino == null) {
                    throw new IllegalArgumentException("Selecione o piquete de destino.");
                }

                Integer quantidade = parseInteiro(txtQuantidade.getText());
                LocalDate data = dpData.getValue();
                String observacao = txtObservacao.getText();

                if (tipo == 0) {
                    piqueteService.registrarEntradaAnimais(origem.getId(), quantidade, data, observacao);
                    mensagemSucesso[0] = quantidade + " animal(is) adicionado(s) ao " + origem.getNome() + ".";
                } else if (tipo == 1) {
                    piqueteService.registrarSaidaAnimais(origem.getId(), quantidade, data, observacao);
                    mensagemSucesso[0] = quantidade + " animal(is) retirado(s) do " + origem.getNome() + ".";
                } else {
                    piqueteService.transferirAnimais(origem.getId(), destino.getId(), quantidade, data, observacao);
                    mensagemSucesso[0] = quantidade + " animal(is) transferido(s) de " + origem.getNome() +
                            " para " + destino.getNome() + ".";
                }

            } catch (IllegalArgumentException e) {
                evento.consume(); // mantem o dialogo aberto
                mostrarAlerta(Alert.AlertType.WARNING, "Validação", e.getMessage());
            }
        });

        Optional<ButtonType> resultado = dialog.showAndWait();

        if (resultado.isPresent() && resultado.get() == btnConfirmar && mensagemSucesso[0] != null) {
            atualizarDashboard();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Movimentação registrada", mensagemSucesso[0]);
        }
    }

    @FXML
    private void alimentarPiquete() {

        List<Piquete> piquetes = piqueteService.listarAtivos();

        if (piquetes.isEmpty()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Nenhum piquete cadastrado",
                    "Cadastre um piquete antes de lançar um trato.");
            return;
        }

        List<FormulacaoRacao> formulas = formulacaoService.listarFormulas();

        if (formulas.isEmpty()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Nenhuma ração disponível",
                    "Cadastre uma fórmula em \"Formulação de Ração\" e gere/finalize uma Ordem de Produção antes de alimentar um piquete.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Alimentar Piquete");
        dialog.setHeaderText(null);

        ButtonType btnSalvar = new ButtonType("Lançar Trato", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        ComboBox<Piquete> cbPiquete = new ComboBox<>(FXCollections.observableArrayList(piquetes));
        cbPiquete.setPrefWidth(260);

        DatePicker dpData = new DatePicker(LocalDate.now());

        ComboBox<FormulacaoRacao> cbFormula = new ComboBox<>(FXCollections.observableArrayList(formulas));
        cbFormula.setPrefWidth(260);
        cbFormula.setPromptText("Selecione a ração");

        javafx.util.StringConverter<FormulacaoRacao> conversor = new javafx.util.StringConverter<>() {
            @Override
            public String toString(FormulacaoRacao formula) {
                if (formula == null) {
                    return "";
                }
                BigDecimal estoque = estoqueService.estoqueRacaoProntaKg(formula.getId());
                return formula.getNome() + "  (disponível: " + formatoKg.format(estoque) + " Kg)";
            }

            @Override
            public FormulacaoRacao fromString(String string) {
                return cbFormula.getValue();
            }
        };
        cbFormula.setConverter(conversor);
        cbFormula.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(FormulacaoRacao formula, boolean empty) {
                super.updateItem(formula, empty);
                setText(empty || formula == null ? null : conversor.toString(formula));
            }
        });
        cbFormula.setCellFactory(lista -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(FormulacaoRacao formula, boolean empty) {
                super.updateItem(formula, empty);
                setText(empty || formula == null ? null : conversor.toString(formula));
            }
        });

        TextField txtQuantidadeKg = new TextField();
        txtQuantidadeKg.setPromptText("Ex.: 700");

        TextField txtObservacao = new TextField();
        txtObservacao.setPromptText("Opcional");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 10, 10, 10));

        grid.add(new Label("Piquete:"), 0, 0);
        grid.add(cbPiquete, 1, 0);
        grid.add(new Label("Data:"), 0, 1);
        grid.add(dpData, 1, 1);
        grid.add(new Label("Ração:"), 0, 2);
        grid.add(cbFormula, 1, 2);
        grid.add(new Label("Quantidade (Kg):"), 0, 3);
        grid.add(txtQuantidadeKg, 1, 3);
        grid.add(new Label("Observação:"), 0, 4);
        grid.add(txtObservacao, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultado = dialog.showAndWait();

        if (resultado.isPresent() && resultado.get() == btnSalvar) {

            try {

                if (cbPiquete.getValue() == null) {
                    throw new IllegalArgumentException("Selecione o piquete.");
                }

                if (cbFormula.getValue() == null) {
                    throw new IllegalArgumentException("Selecione a ração.");
                }

                BigDecimal quantidadeKg = parseValor(txtQuantidadeKg.getText());

                piqueteService.registrarTrato(
                        cbPiquete.getValue().getId(),
                        dpData.getValue(),
                        quantidadeKg,
                        cbFormula.getValue().getId(),
                        txtObservacao.getText()
                );

                atualizarDashboard();

            } catch (IllegalArgumentException e) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validação", e.getMessage());
            }
        }
    }

    private void abrirHistorico(Integer piqueteId, String nomePiquete) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/br/com/confisystem/view/piquetehistorico.fxml")
            );
            loader.setControllerFactory(applicationContext::getBean);

            Parent root = loader.load();

            PiqueteHistoricoController controller = loader.getController();
            controller.carregar(piqueteId, nomePiquete);

            Stage stage = new Stage();
            Icone.aplicar(stage);
            stage.setTitle("Histórico de Tratos - " + nomePiquete);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir o histórico: " + e.getMessage());
        }
    }

    private void abrirDetalhes(Integer piqueteId) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/br/com/confisystem/view/piquetedetalhe.fxml")
            );
            loader.setControllerFactory(applicationContext::getBean);

            Parent root = loader.load();

            PiqueteDetalheController controller = loader.getController();
            controller.carregar(piqueteId, this::atualizarDashboard);

            Stage stage = new Stage();
            Icone.aplicar(stage);
            stage.setTitle("Detalhes do Piquete");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir os detalhes: " + e.getMessage());
        }
    }


    private Integer parseInteiro(String texto) {

        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe a quantidade de animais.");
        }

        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Quantidade de animais inválida.");
        }
    }

    private BigDecimal parseValor(String texto) {

        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe a quantidade em Kg.");
        }

        String valor = texto.trim();

        if (valor.contains(",")) {
            valor = valor.replace(".", "").replace(",", ".");
        }

        try {
            BigDecimal resultado = new BigDecimal(valor);
            if (resultado.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
            }
            return resultado;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Quantidade em Kg inválida.");
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

}
