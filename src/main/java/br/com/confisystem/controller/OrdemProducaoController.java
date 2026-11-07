package br.com.confisystem.controller;

import br.com.confisystem.util.Icone;

import br.com.confisystem.entity.FormulacaoRacao;
import br.com.confisystem.entity.MateriaPrima;
import br.com.confisystem.entity.OrdemProducao;
import br.com.confisystem.entity.OrdemProducaoItem;
import br.com.confisystem.service.FormulacaoService;
import br.com.confisystem.service.OrdemProducaoService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.print.Printer;
import javafx.print.Paper;
import javafx.print.PageOrientation;
import javafx.print.PageLayout;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.transform.Scale;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class OrdemProducaoController implements Initializable {

    @FXML
    private ComboBox<FormulacaoRacao> cbFormula;

    @FXML
    private TextField txtQuantidadeKg;

    @FXML
    private Button btnGerarOrdem;

    @FXML
    private Label lblPreviaBatidas;

    @FXML
    private TableView<OrdemProducao> tabelaOrdens;

    @FXML
    private TableColumn<OrdemProducao, Integer> colOrdemId;

    @FXML
    private TableColumn<OrdemProducao, String> colOrdemFormula;

    @FXML
    private TableColumn<OrdemProducao, BigDecimal> colOrdemQuantidade;

    @FXML
    private TableColumn<OrdemProducao, Integer> colOrdemBatidas;

    @FXML
    private TableColumn<OrdemProducao, LocalDate> colOrdemDataCriacao;

    @FXML
    private TableColumn<OrdemProducao, String> colOrdemStatus;

    @FXML
    private TableColumn<OrdemProducao, BigDecimal> colOrdemCusto;

    @FXML
    private Button btnVerImprimir;

    @FXML
    private Button btnFinalizar;

    @FXML
    private Button btnCancelarOrdem;

    private final OrdemProducaoService ordemProducaoService;
    private final FormulacaoService formulacaoService;

    private final DecimalFormat formatoKg;
    private final DecimalFormat formatoMoeda;
    private final DecimalFormat formatoPercentual;
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public OrdemProducaoController(OrdemProducaoService ordemProducaoService, FormulacaoService formulacaoService) {
        this.ordemProducaoService = ordemProducaoService;
        this.formulacaoService = formulacaoService;

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("pt", "BR"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');

        formatoKg = new DecimalFormat("#,##0.###", simbolos);
        formatoMoeda = new DecimalFormat("#,##0.00", simbolos);
        formatoPercentual = new DecimalFormat("#,##0.0'%'", simbolos);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        cbFormula.getItems().setAll(formulacaoService.listarFormulas());

        configurarTabelaOrdens();
        carregarOrdens();

        txtQuantidadeKg.textProperty().addListener((obs, oldV, newV) -> atualizarPreviaBatidas());
    }

    @FXML
    private void gerarOrdem() {

        if (cbFormula.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Selecione a ração (fórmula).");
            cbFormula.requestFocus();
            return;
        }

        BigDecimal quantidadeKg;

        try {
            quantidadeKg = parseValor(txtQuantidadeKg.getText());
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Quantidade inválida.");
            txtQuantidadeKg.requestFocus();
            return;
        }

        try {

            OrdemProducao ordem = ordemProducaoService.gerarOrdem(cbFormula.getValue().getId(), quantidadeKg);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Ordem Gerada",
                    "Ordem #" + ordem.getId() + " gerada como PENDENTE — " +
                            ordem.getNumeroBatidas() + " batida(s) de " +
                            formatoKg.format(ordem.getPesoBatidaKg()) + " Kg cada.");

            cbFormula.getSelectionModel().clearSelection();
            txtQuantidadeKg.clear();
            lblPreviaBatidas.setText("");

            carregarOrdens();

            try {
                abrirFolhaImpressao(ordem.getId());
            } catch (RuntimeException e) {
                e.printStackTrace();
                mostrarAlerta(Alert.AlertType.WARNING, "Ordem gerada",
                        "A ordem #" + ordem.getId() + " foi gerada normalmente, mas não foi possível abrir a " +
                                "folha de impressão automaticamente. Selecione a ordem na lista e use " +
                                "\"Ver / Imprimir Ordem\". Detalhe: " + e.getMessage());
            }

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao gerar ordem",
                    "Não foi possível gerar a ordem de produção: " + e.getMessage());
        }
    }

    @FXML
    private void verImprimir() {

        OrdemProducao selecionada = tabelaOrdens.getSelectionModel().getSelectedItem();

        if (selecionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atenção", "Selecione uma ordem na lista.");
            return;
        }

        try {
            abrirFolhaImpressao(selecionada.getId());
        } catch (RuntimeException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao abrir folha",
                    "Não foi possível abrir a folha de impressão: " + e.getMessage());
        }
    }

    @FXML
    private void finalizarOrdem() {

        OrdemProducao selecionada = tabelaOrdens.getSelectionModel().getSelectedItem();

        if (selecionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atenção", "Selecione uma ordem na lista.");
            return;
        }

        if (!selecionada.isPendente()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Ordem já finalizada",
                    "Essa ordem já foi finalizada anteriormente.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Finalizar esta ordem vai consumir o estoque das matérias-primas e liberar " +
                        formatoKg.format(selecionada.getQuantidadeTotalKg()) + " Kg de \"" +
                        selecionada.getNomeFormula() + "\" no estoque de ração pronta. Confirmar?");
        confirmacao.setTitle("Confirmar Finalização");
        confirmacao.setHeaderText(null);

        confirmacao.showAndWait().ifPresent(botao -> {

            if (botao == ButtonType.OK) {

                try {

                    ordemProducaoService.finalizarOrdem(selecionada.getId());

                    mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                            "Ordem finalizada! O estoque foi atualizado e a ração já pode ser usada no Confinamento.");

                    carregarOrdens();

                } catch (IllegalArgumentException e) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Não foi possível finalizar", e.getMessage());
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    mostrarAlerta(Alert.AlertType.ERROR, "Erro ao finalizar",
                            "Não foi possível finalizar a ordem: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void cancelarOrdem() {

        OrdemProducao selecionada = tabelaOrdens.getSelectionModel().getSelectedItem();

        if (selecionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atenção", "Selecione uma ordem na lista.");
            return;
        }

        if (!selecionada.isPendente()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Não é possível cancelar",
                    "Só é possível cancelar ordens PENDENTES. Essa ordem já está " + selecionada.getStatus() +
                            " — se ela já foi finalizada, o estoque já foi movimentado e cancelar aqui não desfaz isso.");
            return;
        }

        TextInputDialog dialogMotivo = new TextInputDialog();
        dialogMotivo.setTitle("Cancelar Ordem #" + selecionada.getId());
        dialogMotivo.setHeaderText("Cancelar a ordem #" + selecionada.getId() + " (" + selecionada.getNomeFormula() +
                " — " + formatoKg.format(selecionada.getQuantidadeTotalKg()) + " Kg)?");
        dialogMotivo.setContentText("Motivo do cancelamento:");

        dialogMotivo.showAndWait().ifPresent(motivo -> {

            if (motivo.trim().isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Atenção", "Informe o motivo do cancelamento.");
                return;
            }

            try {

                ordemProducaoService.cancelarOrdem(selecionada.getId(), motivo);

                mostrarAlerta(Alert.AlertType.INFORMATION, "Ordem cancelada",
                        "A ordem #" + selecionada.getId() + " foi cancelada.");

                carregarOrdens();

            } catch (IllegalArgumentException e) {
                mostrarAlerta(Alert.AlertType.WARNING, "Não foi possível cancelar", e.getMessage());
            } catch (RuntimeException e) {
                e.printStackTrace();
                mostrarAlerta(Alert.AlertType.ERROR, "Erro ao cancelar",
                        "Não foi possível cancelar a ordem: " + e.getMessage());
            }
        });
    }

    private void atualizarPreviaBatidas() {

        try {

            BigDecimal quantidadeKg = parseValor(txtQuantidadeKg.getText());

            if (quantidadeKg.compareTo(BigDecimal.ZERO) <= 0) {
                lblPreviaBatidas.setText("");
                return;
            }

            int numeroBatidas = quantidadeKg
                    .divide(BigDecimal.valueOf(1000), 0, RoundingMode.CEILING)
                    .intValue();

            if (numeroBatidas < 1) {
                numeroBatidas = 1;
            }

            BigDecimal pesoBatida = quantidadeKg.divide(BigDecimal.valueOf(numeroBatidas), 3, RoundingMode.HALF_UP);

            lblPreviaBatidas.setText(
                    formatoKg.format(quantidadeKg) + " Kg → " + numeroBatidas + " batida(s) de " +
                            formatoKg.format(pesoBatida) + " Kg cada"
            );

        } catch (IllegalArgumentException e) {
            lblPreviaBatidas.setText("");
        }
    }

    private void carregarOrdens() {
        tabelaOrdens.getItems().setAll(ordemProducaoService.listarOrdensPendentes());
    }

    private void abrirFolhaImpressao(Integer ordemId) {

        OrdemProducao ordem = ordemProducaoService.buscarPorId(ordemId);
        List<OrdemProducaoItem> itens = ordemProducaoService.itensDaOrdem(ordemId);

        VBox documento = construirDocumentoImpressao(ordem, itens);

        ScrollPane scrollDocumento = new ScrollPane(documento);
        scrollDocumento.setFitToWidth(false);
        scrollDocumento.setStyle("-fx-background: #E2E8F0; -fx-border-color: transparent;");

        Button btnImprimir = new Button("Imprimir");
        btnImprimir.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 600; " +
                "-fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");

        Button btnFechar = new Button("Fechar");
        btnFechar.setStyle("-fx-background-color: #CBD5E1; -fx-text-fill: #1E293B; -fx-font-weight: 600; " +
                "-fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");

        HBox botoes = new HBox(10, btnImprimir, btnFechar);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        botoes.setStyle("-fx-padding: 12 20;");

        VBox root = new VBox(botoes, scrollDocumento);
        root.setStyle("-fx-background-color: #E2E8F0;");
        VBox.setVgrow(scrollDocumento, Priority.ALWAYS);

        Stage stage = new Stage();
        Icone.aplicar(stage);
        stage.setTitle("Ordem de Produção #" + String.format("%06d", ordem.getId()));
        stage.setScene(new Scene(root, 1180, 820));

        btnImprimir.setOnAction(e -> imprimir(documento, stage));
        btnFechar.setOnAction(e -> stage.close());

        stage.show();
    }

    private static final double LARGURA_DOCUMENTO = 1120;
    private static final double COL_ITEM = 55;
    private static final double COL_MATERIA = 240;
    private static final double COL_KG_TOTAL = 110;
    private static final double COL_PERCENTUAL = 110;
    private static final double COL_BATIDA = 62;
    private static final double COL_PESO_FINAL = 130;

    private VBox construirDocumentoImpressao(OrdemProducao ordem, List<OrdemProducaoItem> itens) {

        VBox documento = new VBox(16);
        documento.setPrefWidth(LARGURA_DOCUMENTO);
        documento.setMinWidth(LARGURA_DOCUMENTO);
        documento.setMaxWidth(LARGURA_DOCUMENTO);
        documento.setPadding(new Insets(30));
        documento.setStyle("-fx-background-color: white;");

        documento.getChildren().addAll(
                construirCabecalhoDocumento(ordem),
                construirLinhaInfo(ordem),
                construirObservacoes(ordem),
                construirTabelaDocumento(ordem, itens),
                construirAssinaturas(),
                construirRodapeDocumento()
        );

        return documento;
    }

    private HBox construirCabecalhoDocumento(OrdemProducao ordem) {

        Label logo = new Label("CONFISYSTEM");
        logo.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");

        Label logoSub = new Label("G E S T Ã O   D E   C O N F I N A M E N T O");
        logoSub.setStyle("-fx-font-size: 8px; -fx-font-weight: 700; -fx-text-fill: #475569;");

        VBox logoBox = new VBox(2, logo, logoSub);

        Label titulo = new Label("ORDEM DE PRODUÇÃO");
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");

        HBox tituloBox = new HBox(titulo);
        tituloBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(tituloBox, Priority.ALWAYS);

        GridPane numeroData = new GridPane();
        numeroData.setStyle("-fx-border-color: #1E293B; -fx-border-width: 1.4;");
        numeroData.getColumnConstraints().addAll(new ColumnConstraints(130), new ColumnConstraints(150));

        numeroData.add(celulaCompacta("Nº DA ORDEM", false, true), 0, 0);
        numeroData.add(celulaCompacta(String.format("%06d", ordem.getId()), true, false), 1, 0);
        numeroData.add(celulaCompacta("DATA EMISSÃO", false, true), 0, 1);
        numeroData.add(celulaCompacta(ordem.getDataCriacao().format(formatoData), true, false), 1, 1);

        HBox cabecalho = new HBox(20, logoBox, tituloBox, numeroData);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        return cabecalho;
    }

    private Label celulaCompacta(String texto, boolean valor, boolean bordaDireita) {

        Label label = new Label(texto);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setAlignment(valor ? Pos.CENTER : Pos.CENTER_LEFT);
        label.setPadding(new Insets(8, 12, 8, 12));
        label.setStyle(
                "-fx-background-color: white; -fx-text-fill: #0F172A; " +
                        (valor ? "-fx-font-size: 16px; -fx-font-weight: 800; " : "-fx-font-size: 11px; -fx-font-weight: 700; ") +
                        "-fx-border-color: #1E293B; -fx-border-width: 0 " + (bordaDireita ? "1" : "0") + " 1 0;"
        );

        return label;
    }

    private GridPane construirLinhaInfo(OrdemProducao ordem) {

        GridPane grid = new GridPane();
        grid.setStyle("-fx-border-color: #1E293B; -fx-border-width: 1.4;");

        double[] percentuais = {34, 20, 22, 24};

        for (double p : percentuais) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(p);
            grid.getColumnConstraints().add(c);
        }

        grid.add(caixaCampo("PRODUTO / FÓRMULA", ordem.getNomeFormula(), false), 0, 0);
        grid.add(caixaCampo("DESTINO / SETOR", "CONFINAMENTO", true), 1, 0);
        grid.add(caixaCampo("QUANTIDADE TOTAL (KG)", formatoKg.format(ordem.getQuantidadeTotalKg()), true), 2, 0);
        grid.add(caixaCampo("PREVISÃO DE PRODUÇÃO", "__ / __ / ____", true), 3, 0);

        return grid;
    }

    private VBox caixaCampo(String rotulo, String valor, boolean bordaEsquerda) {

        Label lblRotulo = new Label(rotulo);
        lblRotulo.setStyle("-fx-font-size: 10px; -fx-font-weight: 800; -fx-text-fill: #1E293B;");

        Label lblValor = new Label(valor == null || valor.isBlank() ? "-" : valor);
        lblValor.setWrapText(true);
        lblValor.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #0F172A;");

        VBox caixa = new VBox(8, lblRotulo, lblValor);
        caixa.setPadding(new Insets(8, 12, 12, 12));
        caixa.setStyle("-fx-border-color: #1E293B; -fx-border-width: 0 0 0 " + (bordaEsquerda ? "1" : "0") + ";");

        return caixa;
    }

    private VBox construirObservacoes(OrdemProducao ordem) {

        String observacao = ordem.getFormulacao() != null ? ordem.getFormulacao().getObservacao() : null;

        Label lblRotulo = new Label("OBSERVAÇÕES");
        lblRotulo.setStyle("-fx-font-size: 10px; -fx-font-weight: 800; -fx-text-fill: #1E293B;");

        Label lblValor = new Label(observacao == null || observacao.isBlank() ? "-" : observacao);
        lblValor.setWrapText(true);
        lblValor.setStyle("-fx-font-size: 13px; -fx-text-fill: #0F172A;");

        VBox caixa = new VBox(8, lblRotulo, lblValor);
        caixa.setMinHeight(64);
        caixa.setPadding(new Insets(8, 12, 14, 12));
        caixa.setStyle("-fx-border-color: #1E293B; -fx-border-width: 1.4;");

        return caixa;
    }

    private GridPane construirTabelaDocumento(OrdemProducao ordem, List<OrdemProducaoItem> itens) {

        int numeroBatidas = Math.max(ordem.getNumeroBatidas(), 1);
        int totalColunas = 4 + numeroBatidas + 1;

        GridPane grid = new GridPane();
        grid.setStyle("-fx-border-color: #1E293B; -fx-border-width: 1.4;");

        grid.getColumnConstraints().add(new ColumnConstraints(COL_ITEM));
        grid.getColumnConstraints().add(new ColumnConstraints(COL_MATERIA));
        grid.getColumnConstraints().add(new ColumnConstraints(COL_KG_TOTAL));
        grid.getColumnConstraints().add(new ColumnConstraints(COL_PERCENTUAL));
        for (int b = 0; b < numeroBatidas; b++) {
            grid.getColumnConstraints().add(new ColumnConstraints(COL_BATIDA));
        }
        grid.getColumnConstraints().add(new ColumnConstraints(COL_PESO_FINAL));

        int linha = 0;
        int coluna = 0;

        grid.add(celulaCabecalhoDoc("ITEM", COL_ITEM), coluna++, linha);
        grid.add(celulaCabecalhoDoc("MATÉRIA-PRIMA", COL_MATERIA), coluna++, linha);
        grid.add(celulaCabecalhoDoc("KG\n(TOTAL)", COL_KG_TOTAL), coluna++, linha);
        grid.add(celulaCabecalhoDoc("% NA\nFÓRMULA", COL_PERCENTUAL), coluna++, linha);

        for (int b = 1; b <= numeroBatidas; b++) {
            grid.add(celulaCabecalhoDoc(String.valueOf(b), COL_BATIDA), coluna++, linha);
        }

        grid.add(celulaCabecalhoDoc("PESO FINAL\n(KG)", COL_PESO_FINAL), coluna, linha);

        linha++;

        Map<String, List<OrdemProducaoItem>> porCategoria = itens.stream()
                .collect(Collectors.groupingBy(
                        this::categoriaDoItem,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        BigDecimal pesoAcumulado = BigDecimal.ZERO;
        BigDecimal pesoBatidaTotal = ordem.getPesoBatidaKg();
        int numeroItem = 1;

        for (List<OrdemProducaoItem> grupo : porCategoria.values()) {

            Label categoria = new Label(categoriaDoItem(grupo.get(0)).toUpperCase());
            categoria.setMaxWidth(Double.MAX_VALUE);
            categoria.setPadding(new Insets(3, 10, 3, 10));
            categoria.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #334155; " +
                    "-fx-font-size: 10px; -fx-font-weight: 800; " +
                    "-fx-border-color: #CBD5E1; -fx-border-width: 0 0 1 0;");
            grid.add(categoria, 0, linha, totalColunas, 1);

            linha++;

            for (OrdemProducaoItem item : grupo) {

                BigDecimal pesoBatida = ordemProducaoService.pesoPorBatida(item, ordem);

                BigDecimal percentual = pesoBatidaTotal != null && pesoBatidaTotal.signum() > 0
                        ? pesoBatida.multiply(BigDecimal.valueOf(100)).divide(pesoBatidaTotal, 4, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                pesoAcumulado = pesoAcumulado.add(pesoBatida);

                coluna = 0;
                grid.add(celulaTextoDoc(String.format("%02d", numeroItem++), COL_ITEM, Pos.CENTER, false), coluna++, linha);
                grid.add(celulaTextoDoc(item.getNomeMateriaPrima(), COL_MATERIA, Pos.CENTER_LEFT, false), coluna++, linha);
                grid.add(celulaTextoDoc(formatoKg.format(pesoBatida), COL_KG_TOTAL, Pos.CENTER, false), coluna++, linha);
                grid.add(celulaTextoDoc(formatoPercentual.format(percentual), COL_PERCENTUAL, Pos.CENTER, false), coluna++, linha);

                for (int b = 0; b < numeroBatidas; b++) {
                    grid.add(celulaMarcacaoDoc(COL_BATIDA), coluna++, linha);
                }

                grid.add(celulaTextoDoc(formatoKg.format(pesoAcumulado), COL_PESO_FINAL, Pos.CENTER, true), coluna, linha);

                linha++;
            }
        }


        grid.add(celulaTotalDoc("TOTAL", COL_ITEM + COL_MATERIA, Pos.CENTER_RIGHT), 0, linha, 2, 1);
        coluna = 2;
        grid.add(celulaTotalDoc(formatoKg.format(pesoBatidaTotal), COL_KG_TOTAL, Pos.CENTER), coluna++, linha);
        grid.add(celulaTotalDoc("100,0%", COL_PERCENTUAL, Pos.CENTER), coluna++, linha);

        for (int b = 0; b < numeroBatidas; b++) {
            grid.add(celulaTotalDoc("-", COL_BATIDA, Pos.CENTER), coluna++, linha);
        }

        grid.add(celulaTotalDoc(formatoKg.format(pesoBatidaTotal), COL_PESO_FINAL, Pos.CENTER), coluna, linha);

        return grid;
    }

    private HBox construirAssinaturas() {

        VBox b1 = blocoAssinatura("RESPONSÁVEL PELA PRODUÇÃO");
        VBox b2 = blocoAssinatura("CONFERIDO POR");
        VBox b3 = blocoAssinatura("APROVADO POR");

        HBox.setHgrow(b1, Priority.ALWAYS);
        HBox.setHgrow(b2, Priority.ALWAYS);
        HBox.setHgrow(b3, Priority.ALWAYS);

        b1.setMaxWidth(Double.MAX_VALUE);
        b2.setMaxWidth(Double.MAX_VALUE);
        b3.setMaxWidth(Double.MAX_VALUE);

        HBox linha = new HBox(18, b1, b2, b3);
        linha.setPadding(new Insets(6, 0, 0, 0));

        return linha;
    }

    private VBox blocoAssinatura(String titulo) {

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 10px; -fx-font-weight: 800; -fx-text-fill: #1E293B;");

        Region espaco = new Region();
        espaco.setPrefHeight(46);

        Region linhaAssinatura = new Region();
        linhaAssinatura.setPrefHeight(1);
        linhaAssinatura.setMinHeight(1);
        linhaAssinatura.setMaxHeight(1);
        linhaAssinatura.setStyle("-fx-background-color: #1E293B;");

        Label lblNome = new Label("NOME");
        lblNome.setStyle("-fx-font-size: 9px; -fx-font-weight: 700; -fx-text-fill: #475569;");

        Label lblAssinatura = new Label("ASSINATURA");
        lblAssinatura.setStyle("-fx-font-size: 9px; -fx-font-weight: 700; -fx-text-fill: #475569;");

        Region flex = new Region();
        HBox.setHgrow(flex, Priority.ALWAYS);

        HBox legenda = new HBox(lblNome, flex, lblAssinatura);

        VBox caixa = new VBox(4, lblTitulo, espaco, linhaAssinatura, legenda);
        caixa.setPadding(new Insets(8, 14, 10, 14));
        caixa.setStyle("-fx-border-color: #1E293B; -fx-border-width: 1.2;");

        return caixa;
    }

    private HBox construirRodapeDocumento() {

        Region linha = new Region();
        linha.setPrefHeight(1);
        linha.setMinHeight(1);
        linha.setMaxHeight(1);
        linha.setStyle("-fx-background-color: #1E293B;");
        HBox.setHgrow(linha, Priority.ALWAYS);

        Label texto = new Label("CONFISYSTEM   |   GESTÃO DE CONFINAMENTO");
        texto.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #334155;");

        HBox rodape = new HBox(14, linha, texto);
        rodape.setAlignment(Pos.CENTER);
        rodape.setPadding(new Insets(10, 0, 0, 0));

        return rodape;
    }

    private String categoriaDoItem(OrdemProducaoItem item) {

        MateriaPrima mp = item.getMateriaPrima();

        if (mp == null || mp.getCategoria() == null) {
            return "Outros";
        }

        return mp.getCategoria().getNome();
    }

    private Label celulaCabecalhoDoc(String texto, double largura) {

        Label label = new Label(texto);
        label.setPrefWidth(largura);
        label.setMinHeight(42);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setPadding(new Insets(6));
        label.setStyle("-fx-background-color: white; -fx-text-fill: #0F172A; " +
                "-fx-font-size: 11px; -fx-font-weight: 800; " +
                "-fx-border-color: #1E293B; -fx-border-width: 0 1 1.4 0;");

        return label;
    }

    private Label celulaTextoDoc(String texto, double largura, Pos alinhamento, boolean negrito) {

        Label label = new Label(texto);
        label.setPrefWidth(largura);
        label.setMinHeight(30);
        label.setAlignment(alinhamento);
        label.setPadding(new Insets(5, 10, 5, 10));
        label.setStyle("-fx-background-color: white; -fx-text-fill: #0F172A; -fx-font-size: 12px; " +
                (negrito ? "-fx-font-weight: 800; " : "-fx-font-weight: 500; ") +
                "-fx-border-color: #94A3B8; -fx-border-width: 0 1 1 0;");

        return label;
    }

    private Label celulaMarcacaoDoc(double largura) {

        Label label = new Label("");
        label.setPrefWidth(largura);
        label.setMinHeight(30);
        label.setStyle("-fx-background-color: white; -fx-border-color: #94A3B8; -fx-border-width: 0 1 1 0;");

        return label;
    }

    private Label celulaTotalDoc(String texto, double largura, Pos alinhamento) {

        Label label = new Label(texto);
        label.setPrefWidth(largura);
        label.setMinHeight(34);
        label.setAlignment(alinhamento);
        label.setPadding(new Insets(6, 12, 6, 12));
        label.setStyle("-fx-background-color: white; -fx-text-fill: #0F172A; " +
                "-fx-font-size: 14px; -fx-font-weight: 800; " +
                "-fx-border-color: #1E293B; -fx-border-width: 1.4 1 0 0;");

        return label;
    }

    private void imprimir(Node conteudo, Stage janela) {

        PrinterJob job = PrinterJob.createPrinterJob();

        if (job == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Impressão",
                    "Nenhuma impressora foi encontrada. Verifique se há uma impressora instalada/configurada.");
            return;
        }

        Printer impressora = job.getPrinter();
        PageLayout layoutInicial = impressora.createPageLayout(
                Paper.A4, PageOrientation.LANDSCAPE, Printer.MarginType.DEFAULT);
        job.getJobSettings().setPageLayout(layoutInicial);

        boolean prosseguir = job.showPrintDialog(janela);

        if (!prosseguir) {
            return;
        }

        PageLayout layout = job.getJobSettings().getPageLayout();

        double larguraConteudo = conteudo.getBoundsInParent().getWidth();
        double alturaConteudo = conteudo.getBoundsInParent().getHeight();

        double escala = Math.min(
                layout.getPrintableWidth() / larguraConteudo,
                layout.getPrintableHeight() / alturaConteudo
        );

        Scale transformacao = new Scale(escala, escala, 0, 0);
        conteudo.getTransforms().add(transformacao);

        boolean sucesso;

        try {
            sucesso = job.printPage(layout, conteudo);
        } finally {
            conteudo.getTransforms().remove(transformacao);
        }

        if (sucesso) {
            job.endJob();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Impressão", "Não foi possível concluir a impressão.");
        }
    }

    private void configurarTabelaOrdens() {

        colOrdemId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOrdemFormula.setCellValueFactory(new PropertyValueFactory<>("nomeFormula"));
        colOrdemQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidadeTotalKg"));
        colOrdemBatidas.setCellValueFactory(new PropertyValueFactory<>("numeroBatidas"));
        colOrdemDataCriacao.setCellValueFactory(new PropertyValueFactory<>("dataCriacao"));
        colOrdemStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colOrdemCusto.setCellValueFactory(new PropertyValueFactory<>("custoTotal"));

        colOrdemQuantidade.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : formatoKg.format(valor) + " Kg");
            }
        });

        colOrdemDataCriacao.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : valor.format(formatoData));
            }
        });

        colOrdemStatus.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(String valor, boolean empty) {
                super.updateItem(valor, empty);

                getStyleClass().removeAll("status-pendente", "status-finalizada", "status-cancelada");

                if (empty || valor == null) {
                    setText(null);
                    return;
                }

                setText(valor);

                if (OrdemProducao.STATUS_PENDENTE.equals(valor)) {
                    getStyleClass().add("status-pendente");
                } else if (OrdemProducao.STATUS_CANCELADA.equals(valor)) {
                    getStyleClass().add("status-cancelada");
                } else {
                    getStyleClass().add("status-finalizada");
                }
            }
        });

        colOrdemCusto.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? "-" : "R$ " + formatoMoeda.format(valor));
            }
        });

        tabelaOrdens.setRowFactory(tv -> {

            TableRow<OrdemProducao> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (row.isEmpty()) {
                    tabelaOrdens.getSelectionModel().clearSelection();
                }
            });

            return row;
        });
    }

    private BigDecimal parseValor(String texto) {

        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe um valor.");
        }

        String valor = texto.trim();

        if (valor.contains(",")) {
            valor = valor.replace(".", "").replace(",", ".");
        }

        return new BigDecimal(valor);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

}
