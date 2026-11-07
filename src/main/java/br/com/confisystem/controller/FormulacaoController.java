package br.com.confisystem.controller;

import br.com.confisystem.entity.FormulacaoItem;
import br.com.confisystem.entity.FormulacaoRacao;
import br.com.confisystem.entity.MateriaPrima;
import br.com.confisystem.service.FormulacaoService;
import br.com.confisystem.service.MateriaPrimaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

@Component
@Scope("prototype")
public class FormulacaoController implements Initializable {

    @FXML
    private VBox listaFormulasContainer;

    @FXML
    private Label lblTituloFormulario;

    @FXML
    private TextField txtNomeFormula;

    @FXML
    private ComboBox<MateriaPrima> cbMateriaPrima;

    @FXML
    private TextField txtQuantidadeKg;

    @FXML
    private Button btnAdicionarItem;

    @FXML
    private Button btnRemoverItem;

    @FXML
    private TableView<FormulacaoItem> tabelaItens;

    @FXML
    private TableColumn<FormulacaoItem, String> colItemMateriaPrima;

    @FXML
    private TableColumn<FormulacaoItem, BigDecimal> colItemQuantidade;

    @FXML
    private TableColumn<FormulacaoItem, BigDecimal> colItemPercentual;

    @FXML
    private TableColumn<FormulacaoItem, BigDecimal> colItemCustoUnitario;

    @FXML
    private TableColumn<FormulacaoItem, BigDecimal> colItemCusto;

    @FXML
    private Label lblLote;

    @FXML
    private Label lblPercentualTotal;

    @FXML
    private Label lblQuantidadeTotal;

    @FXML
    private Label lblFaltando;

    @FXML
    private Label lblCustoTotal;

    @FXML
    private TextField txtObservacao;

    @FXML
    private Button btnSalvarFormula;

    @FXML
    private Button btnLimpar;

    @FXML
    private Button btnCancelarEdicao;

    @FXML
    private Button btnExcluirFormula;

    private final FormulacaoService formulacaoService;
    private final MateriaPrimaService materiaPrimaService;

    private final ObservableList<FormulacaoItem> itens = FXCollections.observableArrayList();


    private Integer formulaEmEdicaoId;

    private final DecimalFormat formatoKg;
    private final DecimalFormat formatoMoeda;

    public FormulacaoController(FormulacaoService formulacaoService, MateriaPrimaService materiaPrimaService) {
        this.formulacaoService = formulacaoService;
        this.materiaPrimaService = materiaPrimaService;

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("pt", "BR"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');

        formatoKg = new DecimalFormat("#,##0.###", simbolos);
        formatoMoeda = new DecimalFormat("#,##0.00", simbolos);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        cbMateriaPrima.getItems().setAll(materiaPrimaService.listar());

        configurarTabelaItens();

        tabelaItens.setItems(itens);

        lblLote.setText(formatoKg.format(formulacaoService.getLotePadraoKg()) + " Kg");

        carregarListaFormulas();
        atualizarTotais();
    }

    @FXML
    private void adicionarItem() {

        if (cbMateriaPrima.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Selecione a matéria-prima.");
            cbMateriaPrima.requestFocus();
            return;
        }

        boolean jaAdicionada = itens.stream()
                .anyMatch(i -> i.getMateriaPrima().getId().equals(cbMateriaPrima.getValue().getId()));

        if (jaAdicionada) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação",
                    "Essa matéria-prima já foi adicionada. Remova-a antes de adicionar novamente.");
            return;
        }

        BigDecimal quantidadeKg;

        try {
            quantidadeKg = parseValor(txtQuantidadeKg.getText());
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", "Quantidade em Kg inválida.");
            txtQuantidadeKg.requestFocus();
            return;
        }

        BigDecimal somaAtual = formulacaoService.somarQuantidade(itens);
        BigDecimal restante = formulacaoService.getLotePadraoKg().subtract(somaAtual);

        if (quantidadeKg.compareTo(restante) > 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação",
                    "Isso ultrapassaria o lote de " + formatoKg.format(formulacaoService.getLotePadraoKg()) +
                            " Kg. Restam " + formatoKg.format(restante) + " Kg para completar a fórmula.");
            return;
        }

        try {

            FormulacaoItem item = formulacaoService.montarItem(cbMateriaPrima.getValue(), quantidadeKg);
            itens.add(item);

            cbMateriaPrima.getSelectionModel().clearSelection();
            txtQuantidadeKg.clear();
            cbMateriaPrima.requestFocus();

            atualizarTotais();

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", e.getMessage());
        }
    }

    @FXML
    private void removerItem() {

        FormulacaoItem selecionado = tabelaItens.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atenção", "Selecione um item da fórmula para remover.");
            return;
        }

        itens.remove(selecionado);

        atualizarTotais();
    }

    @FXML
    private void salvarFormula() {

        try {

            if (formulaEmEdicaoId != null) {

                formulacaoService.atualizarFormula(formulaEmEdicaoId, txtNomeFormula.getText(),
                        new ArrayList<>(itens), txtObservacao.getText());

                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                        "Fórmula atualizada com sucesso!");

            } else {

                formulacaoService.salvarFormula(txtNomeFormula.getText(), new ArrayList<>(itens), txtObservacao.getText());

                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                        "Fórmula salva com sucesso! Ela já aparece na lista ao lado e pode ser usada em Ordem de Produção.");
            }

            carregarListaFormulas();
            limpar();

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", e.getMessage());
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao salvar",
                    "Não foi possível salvar a fórmula: " + e.getMessage());
        }
    }

    private void carregarFormulaParaEdicao(FormulacaoRacao formula) {

        formulaEmEdicaoId = formula.getId();

        txtNomeFormula.setText(formula.getNome());
        txtObservacao.setText(formula.getObservacao());

        itens.setAll(formulacaoService.itensDaFormula(formula.getId()));

        lblTituloFormulario.setText("Editando fórmula: " + formula.getNome());
        btnSalvarFormula.setText("Salvar Alterações");
        btnCancelarEdicao.setVisible(true);
        btnCancelarEdicao.setManaged(true);
        btnExcluirFormula.setVisible(true);
        btnExcluirFormula.setManaged(true);

        atualizarTotais();
        carregarListaFormulas();
    }

    @FXML
    private void cancelarEdicao() {
        limpar();
    }

    private static final String SENHA_EXCLUSAO_FORMULA_USADA = "banco";

    @FXML
    private void excluirFormula() {

        if (formulaEmEdicaoId == null) {
            return;
        }

        String nome = txtNomeFormula.getText();

        if (!formulacaoService.estaEmUso(formulaEmEdicaoId)) {

            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                    "Excluir a fórmula \"" + nome + "\"? Essa ação não pode ser desfeita.");
            confirmacao.setTitle("Confirmar Exclusão");
            confirmacao.setHeaderText(null);

            confirmacao.showAndWait().ifPresent(botao -> {
                if (botao == ButtonType.OK) {
                    executarExclusaoFormula(nome, null);
                }
            });

            return;
        }

        Alert aviso = new Alert(Alert.AlertType.WARNING,
                "A fórmula \"" + nome + "\" já foi usada em uma ou mais Ordens de Produção.\n\n" +
                        "Essas ordens continuam no histórico normalmente (o nome da fórmula fica registrado " +
                        "nelas), mas a fórmula em si deixa de existir e não poderá mais ser usada em novas " +
                        "ordens.\n\nDeseja realmente excluir?");
        aviso.setTitle("Fórmula já usada — confirme a exclusão");
        aviso.setHeaderText(null);
        aviso.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        aviso.showAndWait().ifPresent(botaoAviso -> {

            if (botaoAviso != ButtonType.YES) {
                return;
            }

            String senha = pedirSenhaExclusao();

            if (senha == null) {
                return; // cancelou o diálogo de senha
            }

            if (!SENHA_EXCLUSAO_FORMULA_USADA.equals(senha)) {
                mostrarAlerta(Alert.AlertType.ERROR, "Senha incorreta",
                        "Senha incorreta. A exclusão foi cancelada.");
                return;
            }

            TextInputDialog dialogMotivo = new TextInputDialog();
            dialogMotivo.setTitle("Motivo da Exclusão");
            dialogMotivo.setHeaderText("Fórmula: " + nome);
            dialogMotivo.setContentText("Motivo da exclusão:");

            dialogMotivo.showAndWait().ifPresent(motivo -> {

                if (motivo.trim().isEmpty()) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Atenção", "Informe o motivo da exclusão.");
                    return;
                }

                executarExclusaoFormula(nome, motivo.trim());
            });
        });
    }

    private void executarExclusaoFormula(String nome, String motivo) {

        try {

            formulacaoService.excluirFormula(formulaEmEdicaoId, motivo);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Fórmula \"" + nome + "\" excluída.");

            limpar();

        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Não foi possível excluir", e.getMessage());
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao excluir",
                    "Não foi possível excluir a fórmula: " + e.getMessage());
        }
    }

    private String pedirSenhaExclusao() {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Confirmação necessária");
        dialog.setHeaderText("Digite a senha para confirmar a exclusão:");

        ButtonType btnConfirmar = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirmar, ButtonType.CANCEL);

        PasswordField campoSenha = new PasswordField();
        campoSenha.setPromptText("Senha");

        VBox conteudo = new VBox(10, campoSenha);
        conteudo.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(conteudo);

        dialog.setResultConverter(botao -> botao == btnConfirmar ? campoSenha.getText() : null);

        return dialog.showAndWait().orElse(null);
    }

    @FXML
    private void limpar() {

        formulaEmEdicaoId = null;

        txtNomeFormula.clear();
        cbMateriaPrima.getSelectionModel().clearSelection();
        txtQuantidadeKg.clear();
        txtObservacao.clear();

        itens.clear();

        lblTituloFormulario.setText("Nova Fórmula");
        btnSalvarFormula.setText("Salvar Fórmula");
        btnCancelarEdicao.setVisible(false);
        btnCancelarEdicao.setManaged(false);
        btnExcluirFormula.setVisible(false);
        btnExcluirFormula.setManaged(false);

        atualizarTotais();
        carregarListaFormulas();

        txtNomeFormula.requestFocus();
    }

    private void atualizarTotais() {

        BigDecimal somaPercentual = formulacaoService.somarPercentual(itens);
        BigDecimal somaQuantidade = formulacaoService.somarQuantidade(itens);
        BigDecimal somaCusto = formulacaoService.somarCusto(itens);
        BigDecimal faltando = formulacaoService.getLotePadraoKg().subtract(somaQuantidade);

        lblPercentualTotal.setText(formatoKg.format(somaPercentual) + " %");
        lblQuantidadeTotal.setText(formatoKg.format(somaQuantidade) + " Kg");
        lblFaltando.setText(formatoKg.format(faltando) + " Kg");
        lblCustoTotal.setText(formatoMoeda.format(somaCusto));
    }

    private void carregarListaFormulas() {

        List<FormulacaoRacao> formulas = formulacaoService.listarFormulas();

        listaFormulasContainer.getChildren().clear();

        if (formulas.isEmpty()) {

            Label vazio = new Label("Nenhuma fórmula cadastrada ainda.");
            vazio.getStyleClass().add("ciclo-empty");
            vazio.setWrapText(true);
            listaFormulasContainer.getChildren().add(vazio);
            return;
        }

        for (FormulacaoRacao formula : formulas) {
            listaFormulasContainer.getChildren().add(construirCardFormula(formula));
        }
    }

    private VBox construirCardFormula(FormulacaoRacao formula) {

        VBox card = new VBox(6);
        card.getStyleClass().add("piquete-card");
        card.getStyleClass().add("piquete-card-clickable");

        boolean emEdicao = formula.getId().equals(formulaEmEdicaoId);

        if (emEdicao) {
            card.getStyleClass().add("piquete-card-selected");
        }

        Label nome = new Label(formula.getNome());
        nome.getStyleClass().add("piquete-card-title");
        nome.setWrapText(true);

        Label lote = new Label("Lote de referência: " + formatoKg.format(formula.getLoteKg()) + " Kg");
        lote.getStyleClass().add("info-value");

        int quantidadeItens = formulacaoService.itensDaFormula(formula.getId()).size();

        Label itensInfo = new Label(quantidadeItens + " matéria(s)-prima(s)");
        itensInfo.getStyleClass().add("info-label");

        Label custo = new Label(formula.getCustoTotal() != null
                ? "Custo estimado: R$ " + formatoMoeda.format(formula.getCustoTotal())
                : "Custo estimado: -");
        custo.getStyleClass().add("info-label");

        Label dica = new Label(emEdicao ? "Editando agora ↓" : "Clique para ver detalhes e editar");
        dica.getStyleClass().add("info-label");

        card.getChildren().addAll(nome, lote, itensInfo, custo, dica);

        card.setOnMouseClicked(event -> carregarFormulaParaEdicao(formula));

        return card;
    }

    private void configurarTabelaItens() {

        colItemMateriaPrima.setCellValueFactory(new PropertyValueFactory<>("nomeMateriaPrima"));
        colItemQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidadeKg"));
        colItemPercentual.setCellValueFactory(new PropertyValueFactory<>("percentual"));
        colItemCustoUnitario.setCellValueFactory(new PropertyValueFactory<>("custoUnitarioKg"));
        colItemCusto.setCellValueFactory(new PropertyValueFactory<>("custoTotal"));

        colItemQuantidade.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : formatoKg.format(valor) + " Kg");
            }
        });

        colItemPercentual.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : formatoKg.format(valor) + " %");
            }
        });

        colItemCustoUnitario.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? "sem compra registrada" : "R$ " + formatoMoeda.format(valor) + "/Kg");
            }
        });

        colItemCusto.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? "sem compra registrada" : "R$ " + formatoMoeda.format(valor));
            }
        });

        tabelaItens.setRowFactory(tv -> {

            TableRow<FormulacaoItem> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (row.isEmpty()) {
                    tabelaItens.getSelectionModel().clearSelection();
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
