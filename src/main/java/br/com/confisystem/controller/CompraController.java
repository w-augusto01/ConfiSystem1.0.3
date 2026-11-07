package br.com.confisystem.controller;

import br.com.confisystem.dto.NfeImportada;
import br.com.confisystem.entity.Compra;
import br.com.confisystem.entity.CompraItem;
import br.com.confisystem.entity.Fornecedor;
import br.com.confisystem.entity.MateriaPrima;
import br.com.confisystem.service.CompraService;
import br.com.confisystem.service.FornecedorService;
import br.com.confisystem.service.MateriaPrimaService;
import br.com.confisystem.service.NfeImportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
@Scope("prototype")
public class CompraController implements Initializable {

    @FXML
    private ComboBox<Fornecedor> cbFornecedor;

    @FXML
    private TextField txtNumeroNota;

    @FXML
    private DatePicker dpData;

    @FXML
    private TextField txtFrete;

    @FXML
    private TextField txtSeguro;

    @FXML
    private TextField txtOutrasDespesas;

    @FXML
    private TextField txtDesconto;

    @FXML
    private ComboBox<MateriaPrima> cbMateriaPrima;

    @FXML
    private TextField txtQuantidade;

    @FXML
    private TextField txtValorProduto;

    @FXML
    private Button btnAdicionarItem;

    @FXML
    private Button btnRemoverItem;

    @FXML
    private TableView<CompraItem> tabelaItens;

    @FXML
    private TableColumn<CompraItem, String> colItemMateriaPrima;

    @FXML
    private TableColumn<CompraItem, BigDecimal> colItemQuantidade;

    @FXML
    private TableColumn<CompraItem, BigDecimal> colItemValorProduto;

    @FXML
    private TableColumn<CompraItem, BigDecimal> colItemDespesasRateadas;

    @FXML
    private TableColumn<CompraItem, BigDecimal> colItemCustoFinal;

    @FXML
    private TableColumn<CompraItem, BigDecimal> colItemCustoKg;

    @FXML
    private TableColumn<CompraItem, String> colItemOrigem;

    @FXML
    private Label lblValorProdutos;

    @FXML
    private Label lblValorNota;

    @FXML
    private Button btnSalvarCompra;

    @FXML
    private Button btnLimpar;

    @FXML
    private TableView<Compra> tabelaCompras;

    @FXML
    private TableColumn<Compra, Integer> colCompraId;

    @FXML
    private TableColumn<Compra, String> colCompraFornecedor;

    @FXML
    private TableColumn<Compra, String> colCompraNumeroNota;

    @FXML
    private TableColumn<Compra, LocalDate> colCompraData;

    @FXML
    private TableColumn<Compra, BigDecimal> colCompraValorNota;

    @FXML
    private Button btnImportarXml;

    private final FornecedorService fornecedorService;
    private final MateriaPrimaService materiaPrimaService;
    private final CompraService compraService;
    private final NfeImportService nfeImportService;

    private final ObservableList<CompraItem> itens = FXCollections.observableArrayList();

    private String chaveAcessoImportada;

    private final DecimalFormat formatoMoeda;
    private final DecimalFormat formatoQuantidade;
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public CompraController(
            FornecedorService fornecedorService,
            MateriaPrimaService materiaPrimaService,
            CompraService compraService,
            NfeImportService nfeImportService
    ) {
        this.fornecedorService = fornecedorService;
        this.materiaPrimaService = materiaPrimaService;
        this.compraService = compraService;
        this.nfeImportService = nfeImportService;

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("pt", "BR"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');

        formatoMoeda = new DecimalFormat("#,##0.00", simbolos);
        formatoQuantidade = new DecimalFormat("#,##0.###", simbolos);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        carregarFornecedores();
        carregarMateriasPrimas();

        dpData.setValue(LocalDate.now());

        configurarTabelaItens();
        configurarTabelaCompras();

        tabelaItens.setItems(itens);

        carregarHistoricoCompras();
        atualizarTotais();

        txtFrete.textProperty().addListener((obs, oldV, newV) -> recalcularPreview());
        txtSeguro.textProperty().addListener((obs, oldV, newV) -> recalcularPreview());
        txtOutrasDespesas.textProperty().addListener((obs, oldV, newV) -> recalcularPreview());
        txtDesconto.textProperty().addListener((obs, oldV, newV) -> recalcularPreview());
    }

    @FXML
    private void importarXml() {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecione o XML da NF-e");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML da NF-e", "*.xml"));

        File arquivo = chooser.showOpenDialog(janelaAtual());

        if (arquivo == null) {
            return;
        }

        NfeImportada nfe;

        try {
            nfe = nfeImportService.importar(arquivo);
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Não foi possível importar", e.getMessage());
            return;
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao importar",
                    "Ocorreu um erro lendo o arquivo: " + e.getMessage());
            return;
        }

        if (nfe.getFornecedorResolvido() != null) {
            cbFornecedor.getSelectionModel().select(nfe.getFornecedorResolvido());
        } else {
            cbFornecedor.getSelectionModel().clearSelection();
        }

        int resolvidosAutomaticamente = 0;
        int confirmadosAgora = 0;
        int ignorados = 0;

        List<CompraItem> itensImportados = new ArrayList<>();

        for (NfeImportada.Item item : nfe.getItens()) {

            if (item.isMapeado()) {
                resolvidosAutomaticamente++;
            } else {
                boolean confirmado = resolverItemComDialogo(item, nfe.getFornecedorResolvido());
                if (!confirmado) {
                    ignorados++;
                    continue;
                }
                confirmadosAgora++;
            }

            itensImportados.add(paraCompraItem(item));
        }

        if (itensImportados.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Nenhum item importado",
                    "Nenhum item da nota foi adicionado (todos foram pulados na conferência de unidade).");
            return;
        }

        txtNumeroNota.setText(nfe.getNumero());
        dpData.setValue(nfe.getDataEmissao() != null ? nfe.getDataEmissao() : LocalDate.now());
        txtFrete.setText(paraTexto(nfe.getFrete()));
        txtSeguro.setText(paraTexto(nfe.getSeguro()));
        txtOutrasDespesas.setText(paraTexto(nfe.getOutrasDespesas()));
        txtDesconto.setText(paraTexto(nfe.getDesconto()));

        chaveAcessoImportada = nfe.getChaveAcesso();

        itens.setAll(itensImportados);
        recalcularPreview();

        StringBuilder resumo = new StringBuilder();
        resumo.append("Nota nº ").append(nfe.getNumero()).append(" importada.\n\n");
        resumo.append(itensImportados.size()).append(" item(ns) adicionado(s).\n");
        if (resolvidosAutomaticamente > 0) {
            resumo.append("• ").append(resolvidosAutomaticamente).append(" convertido(s) automaticamente (de-para já conhecido).\n");
        }
        if (confirmadosAgora > 0) {
            resumo.append("• ").append(confirmadosAgora).append(" confirmado(s) agora nesta importação.\n");
        }
        if (ignorados > 0) {
            resumo.append("• ").append(ignorados).append(" item(ns) pulado(s) (não confirmados).\n");
        }
        if (nfe.getFornecedorResolvido() == null) {
            resumo.append("\nAtenção: não encontrei um fornecedor cadastrado com o CNPJ da nota. Selecione o fornecedor manualmente.");
        }

        BigDecimal valorNotaXml = nfe.getValorNota();
        if (valorNotaXml != null && valorNotaXml.signum() > 0) {
            BigDecimal valorNotaTela = compraService.calcularValorNota(
                    compraService.calcularValorProdutos(itens),
                    parseValorOuZero(txtFrete.getText()),
                    parseValorOuZero(txtSeguro.getText()),
                    parseValorOuZero(txtOutrasDespesas.getText()),
                    parseValorOuZero(txtDesconto.getText()));
            if (valorNotaTela.subtract(valorNotaXml).abs().compareTo(new BigDecimal("0.05")) > 0) {
                resumo.append("\n\nAtenção: o valor total calculado (")
                        .append(formatoMoeda.format(valorNotaTela))
                        .append(") não bate exatamente com o valor da nota no XML (")
                        .append(formatoMoeda.format(valorNotaXml))
                        .append("). Confira antes de salvar.");
            }
        }

        mostrarAlerta(Alert.AlertType.INFORMATION, "Importação concluída", resumo.toString());

        cbFornecedor.requestFocus();
    }


    private CompraItem paraCompraItem(NfeImportada.Item item) {

        CompraItem compraItem = new CompraItem();

        compraItem.setMateriaPrima(item.getMateriaPrima());


        compraItem.setQuantidade(item.getQuantidadeKg());


        compraItem.setValorProduto(item.getValorProduto());


        compraItem.setCodigoProdutoXml(item.getCodigoProduto());
        compraItem.setDescricaoProdutoXml(item.getDescricao());
        compraItem.setValorProdutoXml(item.getValorProduto());
        compraItem.setItemImportadoXml(true);


        compraItem.setQuantidadeOriginal(item.getQuantidadeNota());
        compraItem.setUnidadeOriginal(item.getUnidadeNota());
        compraItem.setFatorConversao(item.getFatorParaKg());

        return compraItem;
    }


    private boolean resolverItemComDialogo(NfeImportada.Item item, Fornecedor fornecedor) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Conferir item da nota");
        dialog.setHeaderText("Não sei converter este item automaticamente. Confirme abaixo:");

        ButtonType btnConfirmar = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnPular = new ButtonType("Pular item", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirmar, btnPular);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15, 10, 10, 10));

        Label lblProduto = new Label(item.getDescricao() + "  (código " + item.getCodigoProduto() + ")");
        lblProduto.setWrapText(true);
        lblProduto.setMaxWidth(360);

        Label lblQtdNota = new Label(paraTexto(item.getQuantidadeNota()) + " " + item.getUnidadeNota()
                + "   •   Valor: R$ " + paraTexto(item.getValorProduto()));

        ComboBox<MateriaPrima> cbMp = new ComboBox<>();
        cbMp.getItems().setAll(materiaPrimaService.listar());
        cbMp.setPromptText("Selecione a matéria-prima");
        cbMp.setPrefWidth(260);

        TextField txtFator = new TextField();
        txtFator.setPromptText("Ex.: 25");
        if (item.getFatorParaKg() != null) {
            txtFator.setText(paraTexto(item.getFatorParaKg()));
        }

        Label lblFatorAjuda = new Label("Quantos KG tem 1 \"" + item.getUnidadeNota() + "\" desse produto?");
        lblFatorAjuda.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");

        CheckBox chkLembrar = new CheckBox("Lembrar esta conversão para as próximas notas deste fornecedor");
        chkLembrar.setSelected(fornecedor != null);
        chkLembrar.setDisable(fornecedor == null);
        if (fornecedor == null) {
            chkLembrar.setTooltip(new Tooltip("Só é possível memorizar depois que o fornecedor da nota estiver cadastrado."));
        }

        int linha = 0;
        grid.add(lblProduto, 0, linha++, 2, 1);
        grid.add(lblQtdNota, 0, linha++, 2, 1);
        grid.add(new Label("Matéria-prima:"), 0, linha);
        grid.add(cbMp, 1, linha++);
        grid.add(new Label("Fator p/ KG:"), 0, linha);
        grid.add(txtFator, 1, linha++);
        grid.add(lblFatorAjuda, 0, linha++, 2, 1);
        grid.add(chkLembrar, 0, linha++, 2, 1);

        if (item.getOrigemFator() != null) {
            Label lblSugestao = new Label("Sugestão baseada em: " + item.getOrigemFator());
            lblSugestao.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");
            grid.add(lblSugestao, 0, linha, 2, 1);
        }

        dialog.getDialogPane().setContent(grid);

        Node botaoConfirmar = dialog.getDialogPane().lookupButton(btnConfirmar);
        botaoConfirmar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {

            if (cbMp.getValue() == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Selecione a matéria-prima do item.");
                event.consume();
                return;
            }

            BigDecimal fator;
            try {
                fator = parseValor(txtFator.getText());
            } catch (IllegalArgumentException e) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Informe um fator de conversão válido.");
                event.consume();
                return;
            }

            if (fator.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validação", "O fator de conversão deve ser maior que zero.");
                event.consume();
            }
        });

        Optional<ButtonType> resultado = dialog.showAndWait();

        if (resultado.isEmpty() || resultado.get() != btnConfirmar) {
            return false;
        }

        item.setMateriaPrima(cbMp.getValue());
        item.setFatorParaKg(parseValor(txtFator.getText()));

        if (chkLembrar.isSelected() && fornecedor != null) {
            nfeImportService.confirmarMapeamento(
                    fornecedor,
                    item.getCodigoProduto(),
                    item.getDescricao(),
                    item.getMateriaPrima(),
                    item.getUnidadeNota(),
                    item.getFatorParaKg()
            );
        }

        return true;
    }

    private String paraTexto(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return formatoQuantidade.format(valor);
    }

    private Window janelaAtual() {
        return btnImportarXml.getScene() != null ? btnImportarXml.getScene().getWindow() : null;
    }

    @FXML
    private void adicionarItem() {

        if (chaveAcessoImportada != null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Compra importada por XML",
                    "Não é possível adicionar itens manualmente "
                            + "a uma compra importada de NF-e."
            );
            return;
        }

        if (cbMateriaPrima.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Selecione a matéria-prima do item.");
            cbMateriaPrima.requestFocus();
            return;
        }

        BigDecimal quantidade;
        BigDecimal valorProduto;

        try {
            quantidade = parseValor(txtQuantidade.getText());
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Quantidade inválida.");
            txtQuantidade.requestFocus();
            return;
        }

        if (quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "A quantidade deve ser maior que zero.");
            txtQuantidade.requestFocus();
            return;
        }

        try {
            valorProduto = parseValor(txtValorProduto.getText());
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Valor do produto inválido.");
            txtValorProduto.requestFocus();
            return;
        }

        if (valorProduto.compareTo(BigDecimal.ZERO) <= 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "O valor do produto deve ser maior que zero.");
            txtValorProduto.requestFocus();
            return;
        }

        CompraItem item = new CompraItem();
        item.setMateriaPrima(cbMateriaPrima.getValue());
        item.setQuantidade(quantidade);
        item.setValorProduto(valorProduto);

        itens.add(item);

        cbMateriaPrima.getSelectionModel().clearSelection();
        txtQuantidade.clear();
        txtValorProduto.clear();
        cbMateriaPrima.requestFocus();

        recalcularPreview();
    }

    @FXML
    private void removerItem() {

        CompraItem selecionado = tabelaItens.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atenção", "Selecione um item da nota para remover.");
            return;
        }

        itens.remove(selecionado);

        recalcularPreview();
    }

    @FXML
    private void salvarCompra() {

        if (cbFornecedor.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Selecione o fornecedor.");
            cbFornecedor.requestFocus();
            return;
        }

        if (txtNumeroNota.getText() == null || txtNumeroNota.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Informe o número da nota.");
            txtNumeroNota.requestFocus();
            return;
        }

        if (dpData.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Informe a data da compra.");
            dpData.requestFocus();
            return;
        }

        if (itens.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Adicione ao menos um item à compra.");
            return;
        }

        BigDecimal frete;
        BigDecimal seguro;
        BigDecimal outrasDespesas;
        BigDecimal desconto;

        try {
            frete = parseValor(txtFrete.getText());
            seguro = parseValor(txtSeguro.getText());
            outrasDespesas = parseValor(txtOutrasDespesas.getText());
            desconto = parseValor(txtDesconto.getText());
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Verifique os valores de frete, seguro, outras despesas e desconto.");
            return;
        }

        Compra compra = new Compra();
        compra.setFornecedor(cbFornecedor.getValue());
        compra.setNumeroNota(txtNumeroNota.getText().trim());
        compra.setData(dpData.getValue());
        compra.setFrete(frete);
        compra.setSeguro(seguro);
        compra.setOutrasDespesas(outrasDespesas);
        compra.setDesconto(desconto);
        compra.setChaveAcessoNfe(chaveAcessoImportada);

        try {

            compraService.salvar(compra, new ArrayList<>(itens));

            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                    "Compra lançada com sucesso! Os insumos já estão disponíveis no estoque.");

            carregarHistoricoCompras();
            limpar();

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", e.getMessage());
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao salvar",
                    "Não foi possível salvar a compra: " + e.getMessage());
        }
    }

    @FXML
    private void limpar() {

        cbFornecedor.getSelectionModel().clearSelection();
        txtNumeroNota.clear();
        dpData.setValue(LocalDate.now());
        txtFrete.clear();
        txtSeguro.clear();
        txtOutrasDespesas.clear();
        txtDesconto.clear();

        cbMateriaPrima.getSelectionModel().clearSelection();
        txtQuantidade.clear();
        txtValorProduto.clear();

        itens.clear();
        chaveAcessoImportada = null;

        atualizarTotais();

        cbFornecedor.requestFocus();
    }

    private void recalcularPreview() {

        BigDecimal frete = parseValorOuZero(txtFrete.getText());
        BigDecimal seguro = parseValorOuZero(txtSeguro.getText());
        BigDecimal outrasDespesas = parseValorOuZero(txtOutrasDespesas.getText());
        BigDecimal desconto = parseValorOuZero(txtDesconto.getText());

        compraService.calcularItens(itens, frete, seguro, outrasDespesas, desconto);

        tabelaItens.refresh();

        atualizarTotais();
    }

    private void atualizarTotais() {

        BigDecimal frete = parseValorOuZero(txtFrete.getText());
        BigDecimal seguro = parseValorOuZero(txtSeguro.getText());
        BigDecimal outrasDespesas = parseValorOuZero(txtOutrasDespesas.getText());
        BigDecimal desconto = parseValorOuZero(txtDesconto.getText());

        BigDecimal valorProdutos = compraService.calcularValorProdutos(itens);
        BigDecimal valorNota = compraService.calcularValorNota(
                valorProdutos, frete, seguro, outrasDespesas, desconto
        );

        lblValorProdutos.setText(formatoMoeda.format(valorProdutos));
        lblValorNota.setText(formatoMoeda.format(valorNota));
    }

    private void carregarFornecedores() {
        cbFornecedor.getItems().setAll(fornecedorService.listarAtivos());
    }

    private void carregarMateriasPrimas() {
        cbMateriaPrima.getItems().setAll(materiaPrimaService.listar());
    }

    private void carregarHistoricoCompras() {
        tabelaCompras.getItems().setAll(compraService.listar());
    }

    private void configurarTabelaItens() {

        colItemMateriaPrima.setCellValueFactory(new PropertyValueFactory<>("nomeMateriaPrima"));
        colItemQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colItemValorProduto.setCellValueFactory(new PropertyValueFactory<>("valorProduto"));
        colItemDespesasRateadas.setCellValueFactory(new PropertyValueFactory<>("despesasRateadas"));
        colItemCustoFinal.setCellValueFactory(new PropertyValueFactory<>("custoFinal"));
        colItemCustoKg.setCellValueFactory(new PropertyValueFactory<>("custoKg"));
        colItemOrigem.setCellValueFactory(new PropertyValueFactory<>("origemNota"));

        colItemQuantidade.setCellFactory(coluna -> celulaFormatada(formatoQuantidade));
        colItemValorProduto.setCellFactory(coluna -> celulaFormatada(formatoMoeda));
        colItemDespesasRateadas.setCellFactory(coluna -> celulaFormatada(formatoMoeda));
        colItemCustoFinal.setCellFactory(coluna -> celulaFormatada(formatoMoeda));
        colItemCustoKg.setCellFactory(coluna -> celulaFormatada(formatoMoeda));

        habilitarLimparSelecaoAoClicarFora(tabelaItens);
    }

    private void configurarTabelaCompras() {

        colCompraId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCompraFornecedor.setCellValueFactory(new PropertyValueFactory<>("nomeFornecedor"));
        colCompraNumeroNota.setCellValueFactory(new PropertyValueFactory<>("numeroNota"));
        colCompraData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colCompraValorNota.setCellValueFactory(new PropertyValueFactory<>("valorNota"));

        colCompraData.setCellFactory(coluna -> new TableCell<Compra, LocalDate>() {
            @Override
            protected void updateItem(LocalDate valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : valor.format(formatoData));
            }
        });

        colCompraValorNota.setCellFactory(coluna -> celulaFormatada(formatoMoeda));

        habilitarLimparSelecaoAoClicarFora(tabelaCompras);
    }

    private <T> void habilitarLimparSelecaoAoClicarFora(TableView<T> tabela) {

        tabela.setRowFactory(tv -> {

            TableRow<T> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (row.isEmpty()) {
                    tabela.getSelectionModel().clearSelection();
                }
            });

            return row;
        });
    }

    private <T> TableCell<T, BigDecimal> celulaFormatada(DecimalFormat formato) {
        return new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : formato.format(valor));
            }
        };
    }

    private BigDecimal parseValor(String texto) {

        if (texto == null || texto.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        String valor = texto.trim();

        if (valor.contains(",")) {
            valor = valor.replace(".", "").replace(",", ".");
        }

        return new BigDecimal(valor);
    }

    private BigDecimal parseValorOuZero(String texto) {
        try {
            return parseValor(texto);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
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
