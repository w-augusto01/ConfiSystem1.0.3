package br.com.confisystem.controller;

import br.com.confisystem.entity.CategoriaMateriaPrima;
import br.com.confisystem.entity.CompraItem;
import br.com.confisystem.entity.FormulacaoRacao;
import br.com.confisystem.repository.CompraItemRepository;
import br.com.confisystem.service.FormulacaoService;
import br.com.confisystem.entity.MateriaPrima;
import br.com.confisystem.service.CategoriaMateriaPrimaService;
import br.com.confisystem.service.EstoqueService;
import br.com.confisystem.service.MateriaPrimaService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class MateriaPrimaController implements Initializable {

    @FXML
    private TextField txtPesquisar;

    @FXML
    private CheckBox chkMostrarAtivos;

    @FXML
    private VBox categoriasContainer;

    @FXML
    private VBox racoesContainer;

    private final CategoriaMateriaPrimaService categoriaService;
    private final MateriaPrimaService materiaPrimaService;
    private final EstoqueService estoqueService;
    private final CompraItemRepository compraItemRepository;
    private final FormulacaoService formulacaoService;

    private final DecimalFormat formatoKg;
    private final java.text.NumberFormat formatoMoeda = java.text.NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final java.time.format.DateTimeFormatter FORMATO_DATA =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MateriaPrimaController(
            CategoriaMateriaPrimaService categoriaService,
            MateriaPrimaService materiaPrimaService,
            EstoqueService estoqueService,
            CompraItemRepository compraItemRepository,
            FormulacaoService formulacaoService
    ) {
        this.categoriaService = categoriaService;
        this.materiaPrimaService = materiaPrimaService;
        this.estoqueService = estoqueService;
        this.compraItemRepository = compraItemRepository;
        this.formulacaoService = formulacaoService;

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("pt", "BR"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');

        formatoKg = new DecimalFormat("#,##0.###", simbolos);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        carregarTabela();

        txtPesquisar.textProperty().addListener((obs, oldV, newV) -> carregarTabela());
        chkMostrarAtivos.selectedProperty().addListener((obs, oldV, newV) -> carregarTabela());

    }

    @FXML
    private void novaMateriaPrima() {
        abrirCadastro(null);
    }

    private void abrirCadastro(MateriaPrima existente) {

        boolean edicao = existente != null;

        TextField txtNome = new TextField(edicao ? existente.getNome() : "");
        txtNome.setPromptText("Digite o nome da matéria-prima");

        ComboBox<CategoriaMateriaPrima> cbCategoria = new ComboBox<>();
        cbCategoria.getItems().setAll(categoriaService.listar());
        cbCategoria.setPrefWidth(260);
        if (edicao) {
            cbCategoria.setValue(existente.getCategoria());
        }

        ComboBox<String> cbUnidade = new ComboBox<>();
        cbUnidade.getItems().addAll("KG", "LITRO");
        cbUnidade.setPrefWidth(260);
        cbUnidade.setValue(edicao ? existente.getUnidade() : "KG");

        Label erro = new Label();
        erro.getStyleClass().add("login-error");
        erro.setVisible(false);
        erro.setManaged(false);

        GridPane grade = new GridPane();
        grade.setHgap(15);
        grade.setVgap(14);
        grade.setPadding(new Insets(10, 10, 4, 10));

        Label lblNome = new Label("Nome:");
        Label lblCategoria = new Label("Categoria:");
        Label lblUnidade = new Label("Unidade:");
        lblNome.getStyleClass().add("form-label");
        lblCategoria.getStyleClass().add("form-label");
        lblUnidade.getStyleClass().add("form-label");

        grade.add(lblNome, 0, 0);
        grade.add(txtNome, 1, 0);
        grade.add(lblCategoria, 0, 1);
        grade.add(cbCategoria, 1, 1);
        grade.add(lblUnidade, 0, 2);
        grade.add(cbUnidade, 1, 2);
        grade.add(erro, 0, 3, 2, 1);

        ButtonType btSalvar = new ButtonType(edicao ? "Atualizar" : "Salvar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btDesativar = new ButtonType("Desativar", ButtonBar.ButtonData.LEFT);
        ButtonType btCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle(edicao ? "Editar matéria-prima" : "Cadastrar matéria-prima");
        dialogo.setHeaderText(null);
        dialogo.getDialogPane().setContent(grade);
        dialogo.getDialogPane().getStylesheets().add(
                getClass().getResource("/br/com/confisystem/view/styles.css").toExternalForm());
        dialogo.getDialogPane().getButtonTypes().add(btSalvar);
        if (edicao && existente.isAtivo()) {
            dialogo.getDialogPane().getButtonTypes().add(btDesativar);
        }
        dialogo.getDialogPane().getButtonTypes().add(btCancelar);

        Button botaoSalvar = (Button) dialogo.getDialogPane().lookupButton(btSalvar);
        botaoSalvar.getStyleClass().add("btn-primary");

        botaoSalvar.addEventFilter(javafx.event.ActionEvent.ACTION, evento -> {

            String mensagem = null;

            if (txtNome.getText().trim().isEmpty()) {
                mensagem = "Informe o nome da matéria-prima.";
            } else if (cbCategoria.getValue() == null) {
                mensagem = "Selecione uma categoria.";
            } else if (cbUnidade.getValue() == null) {
                mensagem = "Selecione uma unidade.";
            }

            if (mensagem != null) {
                erro.setText(mensagem);
                erro.setVisible(true);
                erro.setManaged(true);
                evento.consume();
                dialogo.getDialogPane().getScene().getWindow().sizeToScene();
            }
        });

        Optional<ButtonType> resposta = dialogo.showAndWait();

        if (resposta.isEmpty()) {
            return;
        }

        if (resposta.get() == btSalvar) {

            MateriaPrima mp = edicao ? existente : new MateriaPrima();
            mp.setNome(txtNome.getText().trim());
            mp.setCategoria(cbCategoria.getValue());
            mp.setUnidade(cbUnidade.getValue());

            if (edicao) {
                materiaPrimaService.atualizar(mp);
            } else {
                mp.setEstoqueMinimo(0.0);
                materiaPrimaService.salvar(mp);
            }

            carregarTabela();

        } else if (resposta.get() == btDesativar) {

            Optional<ButtonType> confirma = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Deseja realmente desativar esta matéria-prima?"
            ).showAndWait();

            if (confirma.isPresent() && confirma.get() == ButtonType.OK) {
                materiaPrimaService.desativar(existente.getId());
                carregarTabela();
            }
        }
    }

    private void carregarTabela() {

        String texto = txtPesquisar.getText() != null ? txtPesquisar.getText().trim() : "";
        boolean somenteAtivos = chkMostrarAtivos.isSelected();

        List<MateriaPrima> materiasPrimas = materiaPrimaService.pesquisar(texto, somenteAtivos);

        Map<String, List<MateriaPrima>> porCategoria = materiasPrimas.stream()
                .collect(Collectors.groupingBy(
                        mp -> mp.getCategoria() != null ? mp.getCategoria().getNome() : "Sem Categoria",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<Integer, BigDecimal> estoques = new java.util.HashMap<>();
        Map<Integer, BigDecimal> custos = new java.util.HashMap<>();
        for (MateriaPrima mp : materiasPrimas) {
            estoques.put(mp.getId(), estoqueService.estoqueMateriaPrimaKg(mp.getId()));
            BigDecimal custo = estoqueService.custoAtualKg(mp.getId());
            if (custo != null) {
                custos.put(mp.getId(), custo);
            }
        }

        categoriasContainer.getChildren().clear();

        porCategoria.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entrada -> categoriasContainer.getChildren()
                        .add(construirBlocoCategoria(entrada.getKey(), entrada.getValue(), estoques, custos)));

        if (materiasPrimas.isEmpty()) {

            Label vazio = new Label("Nenhuma matéria-prima encontrada.");
            vazio.getStyleClass().add("ciclo-empty");
            categoriasContainer.getChildren().add(vazio);
        }

        carregarRacoes(texto);

    }

    private void carregarRacoes(String filtroNome) {

        racoesContainer.getChildren().clear();

        record LinhaRacao(FormulacaoRacao formula, BigDecimal saldo, BigDecimal custoKg) {
            BigDecimal valorTotal() {
                return custoKg == null ? null : saldo.max(BigDecimal.ZERO).multiply(custoKg);
            }
        }

        List<LinhaRacao> linhas = new java.util.ArrayList<>();

        for (FormulacaoRacao formula : formulacaoService.listarFormulas()) {

            if (!filtroNome.isEmpty() && !formula.getNome().toLowerCase().contains(filtroNome.toLowerCase())) {
                continue;
            }

            BigDecimal custoKg = estoqueService.custoMedioRacaoKg(formula.getId());

            if (custoKg == null) {
                continue;
            }

            linhas.add(new LinhaRacao(formula, estoqueService.estoqueRacaoProntaKg(formula.getId()), custoKg));
        }

        VBox bloco = new VBox();
        bloco.getStyleClass().add("categoria-block");

        Label titulo = new Label("Rações produzidas  ");
        titulo.getStyleClass().add("categoria-title");
        Label contagem = new Label("(" + linhas.size() + ")");
        contagem.getStyleClass().add("categoria-count");
        javafx.scene.layout.HBox cabecalho = new javafx.scene.layout.HBox(titulo, contagem);

        TableView<LinhaRacao> tabela = new TableView<>();
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setPrefHeight(Math.min(420, 46 + Math.max(linhas.size(), 3) * 28));
        tabela.getItems().setAll(linhas);
        tabela.setPlaceholder(new Label("Nenhuma ração produzida ainda."));

        TableColumn<LinhaRacao, String> colNome = new TableColumn<>("Ração");
        colNome.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().formula().getNome()));
        colNome.setMinWidth(130);

        TableColumn<LinhaRacao, LinhaRacao> colSaldo = new TableColumn<>("Saldo");
        colSaldo.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
        colSaldo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LinhaRacao l, boolean empty) {
                super.updateItem(l, empty);
                if (empty || l == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(formatoKg.format(l.saldo()) + " kg");
                setStyle(l.saldo().signum() <= 0 ? "-fx-text-fill: #DC2626; -fx-font-weight: 600;" : "");
            }
        });

        TableColumn<LinhaRacao, LinhaRacao> colCustoKg = new TableColumn<>("Custo/kg");
        colCustoKg.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
        colCustoKg.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LinhaRacao l, boolean empty) {
                super.updateItem(l, empty);
                setText(empty || l == null ? null : formatoMoeda.format(l.custoKg()));
            }
        });

        TableColumn<LinhaRacao, LinhaRacao> colTotal = new TableColumn<>("Custo total");
        colTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LinhaRacao l, boolean empty) {
                super.updateItem(l, empty);
                setText(empty || l == null ? null : formatoMoeda.format(l.valorTotal()));
            }
        });

        tabela.getColumns().addAll(colNome, colSaldo, colCustoKg, colTotal);

        Label rodape = new Label("Saldo = produzido nas Ordens de Produção finalizadas − usado nos tratos. "
                + "Custo/kg = custo médio das ordens finalizadas.");
        rodape.getStyleClass().add("login-hint");
        rodape.setWrapText(true);

        bloco.getChildren().addAll(cabecalho, tabela, rodape);
        racoesContainer.getChildren().add(bloco);
    }

    private void abrirDetalhes(MateriaPrima mp, BigDecimal estoque, BigDecimal custoKg) {

        List<CompraItem> entradas = compraItemRepository.findByMateriaPrimaIdOrderByCompraDataDesc(mp.getId());

        VBox conteudo = new VBox(14);
        conteudo.setPadding(new Insets(6));
        conteudo.setPrefWidth(860);

        Label resumoEstoque = new Label("Estoque atual: " + formatoKg.format(estoque) + " " + mp.getUnidade().toLowerCase()
                + "   |   Valor/kg: " + (custoKg == null ? "-" : formatoMoeda.format(custoKg))
                + "   |   Valor total: " + (custoKg == null ? "-" : formatoMoeda.format(estoque.max(BigDecimal.ZERO).multiply(custoKg))));
        resumoEstoque.getStyleClass().add("form-label");

        conteudo.getChildren().add(resumoEstoque);

        if (entradas.isEmpty()) {

            Label vazio = new Label("Nenhuma entrada registrada para esta matéria-prima.");
            vazio.getStyleClass().add("ciclo-empty");
            conteudo.getChildren().add(vazio);

        } else {

            // ----- Ultima entrada em destaque -----
            CompraItem ultima = entradas.get(0);

            Label tituloUltima = new Label("ÚLTIMA ENTRADA");
            tituloUltima.getStyleClass().add("section-label");

            GridPane grade = new GridPane();
            grade.setHgap(30);
            grade.setVgap(4);
            grade.addRow(0, rotulo("Data"), rotulo("Nota fiscal"), rotulo("Fornecedor"),
                    rotulo("Quantidade"), rotulo("Valor"), rotulo("Custo/kg"));
            grade.addRow(1,
                    new Label(ultima.getCompra().getData().format(FORMATO_DATA)),
                    new Label(textoNota(ultima)),
                    new Label(ultima.getCompra().getNomeFornecedor()),
                    new Label(formatoKg.format(ultima.getQuantidade()) + " " + mp.getUnidade().toLowerCase()),
                    new Label(formatoMoeda.format(ultima.getCustoFinal() != null && ultima.getCustoFinal().signum() > 0
                            ? ultima.getCustoFinal() : ultima.getValorProduto())),
                    new Label(ultima.getCustoKg() == null ? "-" : formatoMoeda.format(ultima.getCustoKg())));

            VBox destaque = new VBox(6, tituloUltima, grade);
            destaque.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 8; -fx-padding: 12 16;");

            // ----- Historico completo -----
            Label tituloHistorico = new Label("HISTÓRICO DE ENTRADAS (" + entradas.size() + ")");
            tituloHistorico.getStyleClass().add("section-label");

            TableView<CompraItem> tabela = new TableView<>();
            tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tabela.setPrefHeight(Math.min(340, 46 + entradas.size() * 28));
            tabela.getItems().setAll(entradas);

            tabela.getColumns().add(colunaTexto("Data", 90, i -> i.getCompra().getData().format(FORMATO_DATA)));
            tabela.getColumns().add(colunaTexto("NF", 80, this::textoNota));
            tabela.getColumns().add(colunaTexto("Fornecedor", 170, i -> i.getCompra().getNomeFornecedor()));
            tabela.getColumns().add(colunaTexto("Quantidade", 100,
                    i -> formatoKg.format(i.getQuantidade()) + " " + mp.getUnidade().toLowerCase()));
            tabela.getColumns().add(colunaTexto("Origem na nota", 120, CompraItem::getOrigemNota));
            tabela.getColumns().add(colunaTexto("Valor produtos", 105, i -> formatoMoeda.format(i.getValorProduto())));
            tabela.getColumns().add(colunaTexto("Custo final", 105,
                    i -> i.getCustoFinal() == null ? "-" : formatoMoeda.format(i.getCustoFinal())));
            tabela.getColumns().add(colunaTexto("Custo/kg", 85,
                    i -> i.getCustoKg() == null ? "-" : formatoMoeda.format(i.getCustoKg())));

            conteudo.getChildren().addAll(destaque, tituloHistorico, tabela);
        }

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Detalhes - " + mp.getNome());
        dialogo.setHeaderText(mp.getNome() + "  (" + mp.getNomeCategoria() + ")");
        dialogo.getDialogPane().setContent(conteudo);
        dialogo.getDialogPane().getStylesheets().add(
                getClass().getResource("/br/com/confisystem/view/styles.css").toExternalForm());
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialogo.showAndWait();
    }

    private String textoNota(CompraItem item) {
        String nf = item.getCompra().getNumeroNota();
        return nf == null || nf.isBlank() ? "-" : nf;
    }

    private Label rotulo(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-text-fill: #64748B; -fx-font-size: 11px;");
        return l;
    }

    private TableColumn<CompraItem, String> colunaTexto(String titulo, double largura,
                                                       java.util.function.Function<CompraItem, String> extrator) {
        TableColumn<CompraItem, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(extrator.apply(c.getValue())));
        col.setMinWidth(largura);
        return col;
    }

    private VBox construirBlocoCategoria(String nomeCategoria, List<MateriaPrima> itens,
                                         Map<Integer, BigDecimal> estoques,
                                         Map<Integer, BigDecimal> custos) {

        VBox bloco = new VBox();
        bloco.getStyleClass().add("categoria-block");

        Label titulo = new Label(nomeCategoria + "  ");
        titulo.getStyleClass().add("categoria-title");

        Label contagem = new Label("(" + itens.size() + ")");
        contagem.getStyleClass().add("categoria-count");

        javafx.scene.layout.HBox cabecalho = new javafx.scene.layout.HBox(titulo, contagem);

        TableView<MateriaPrima> tabela = new TableView<>();
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setPrefHeight(Math.min(260, 46 + itens.size() * 28));
        tabela.getItems().setAll(itens);

        TableColumn<MateriaPrima, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setMinWidth(150);

        TableColumn<MateriaPrima, String> colUnidade = new TableColumn<>("Unidade");
        colUnidade.setCellValueFactory(new PropertyValueFactory<>("unidade"));
        colUnidade.setMinWidth(70);

        TableColumn<MateriaPrima, MateriaPrima> colEstoque = new TableColumn<>("Estoque disponível");
        colEstoque.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
        colEstoque.setMinWidth(130);
        colEstoque.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(MateriaPrima mp, boolean empty) {
                super.updateItem(mp, empty);

                if (empty || mp == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                BigDecimal qtd = estoques.getOrDefault(mp.getId(), BigDecimal.ZERO);
                setText(formatoKg.format(qtd) + " " + mp.getUnidade().toLowerCase());
                setStyle(qtd.signum() <= 0 ? "-fx-text-fill: #DC2626; -fx-font-weight: 600;" : "");
            }
        });

        TableColumn<MateriaPrima, MateriaPrima> colCustoKg = new TableColumn<>("Valor/kg");
        colCustoKg.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
        colCustoKg.setMinWidth(90);
        colCustoKg.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(MateriaPrima mp, boolean empty) {
                super.updateItem(mp, empty);
                BigDecimal custo = empty || mp == null ? null : custos.get(mp.getId());
                setText(empty || mp == null ? null : (custo == null ? "-" : formatoMoeda.format(custo)));
            }
        });

        TableColumn<MateriaPrima, MateriaPrima> colValorTotal = new TableColumn<>("Valor total");
        colValorTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
        colValorTotal.setMinWidth(110);
        colValorTotal.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(MateriaPrima mp, boolean empty) {
                super.updateItem(mp, empty);

                if (empty || mp == null) {
                    setText(null);
                    return;
                }

                BigDecimal custo = custos.get(mp.getId());
                BigDecimal qtd = estoques.getOrDefault(mp.getId(), BigDecimal.ZERO);
                setText(custo == null ? "-" : formatoMoeda.format(qtd.max(BigDecimal.ZERO).multiply(custo)));
            }
        });

        TableColumn<MateriaPrima, MateriaPrima> colDetalhes = new TableColumn<>("");
        colDetalhes.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue()));
        colDetalhes.setMinWidth(90);
        colDetalhes.setSortable(false);
        colDetalhes.setCellFactory(coluna -> new TableCell<>() {

            private final Button botao = new Button("Detalhes");

            {
                botao.getStyleClass().add("btn-secondary");
                botao.setStyle("-fx-padding: 3 10; -fx-font-size: 12px;");
                // Nao deixa o clique no botao "vazar" para a linha (que abriria a edicao).
                botao.setOnMouseClicked(javafx.event.Event::consume);
            }

            @Override
            protected void updateItem(MateriaPrima mp, boolean empty) {
                super.updateItem(mp, empty);

                if (empty || mp == null) {
                    setGraphic(null);
                    return;
                }

                botao.setOnAction(e -> abrirDetalhes(mp,
                        estoques.getOrDefault(mp.getId(), BigDecimal.ZERO), custos.get(mp.getId())));
                setGraphic(botao);
            }
        });

        tabela.getColumns().addAll(colNome, colUnidade, colEstoque, colCustoKg, colValorTotal, colDetalhes);

        tabela.setRowFactory(tv -> {

            TableRow<MateriaPrima> row = new TableRow<>();

            row.setOnMouseClicked(event -> {

                if (row.isEmpty()) {
                    tabela.getSelectionModel().clearSelection();
                    return;
                }

                abrirCadastro(row.getItem());
            });

            return row;
        });

        bloco.getChildren().addAll(cabecalho, tabela);

        return bloco;
    }

}
