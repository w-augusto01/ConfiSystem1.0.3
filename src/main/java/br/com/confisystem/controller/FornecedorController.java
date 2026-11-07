package br.com.confisystem.controller;

import br.com.confisystem.entity.Fornecedor;
import br.com.confisystem.service.FornecedorService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    private Fornecedor fornecedorSelecionado = null;

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtCnpj;

    @FXML
    private TextField txtTelefone;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtEndereco;

    @FXML
    private TextField txtPesquisar;

    @FXML
    private ComboBox<String> cbTipo;

    @FXML
    private CheckBox chkMostrarAtivos;

    @FXML
    private Button btnSalvar;

    @FXML
    private Button btnLimpar;

    @FXML
    private TableView<Fornecedor> tabelaFornecedores;

    @FXML
    private TableColumn<Fornecedor, Integer> colId;

    @FXML
    private TableColumn<Fornecedor, String> colNome;

    @FXML
    private TableColumn<Fornecedor, String> colCnpj;

    @FXML
    private TableColumn<Fornecedor, String> colTelefone;

    @FXML
    private TableColumn<Fornecedor, String> colTipo;

    @FXML
    private TableColumn<Fornecedor, Boolean> colAtivo;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @FXML
    private void salvar(ActionEvent event) {

        if (!validarCampos()) {
            return;
        }

        if (fornecedorSelecionado == null) {

            Fornecedor fornecedor = new Fornecedor();

            fornecedor.setNome(txtNome.getText().trim());
            fornecedor.setCnpj(txtCnpj.getText().trim());
            fornecedor.setTelefone(txtTelefone.getText().trim());
            fornecedor.setEmail(txtEmail.getText().trim());
            fornecedor.setEndereco(txtEndereco.getText().trim());
            fornecedor.setTipo(cbTipo.getValue());

            fornecedorService.salvar(fornecedor);

            mostrarInformacao("Fornecedor cadastrado com sucesso!");

        } else {

            fornecedorSelecionado.setNome(txtNome.getText().trim());
            fornecedorSelecionado.setCnpj(txtCnpj.getText().trim());
            fornecedorSelecionado.setTelefone(txtTelefone.getText().trim());
            fornecedorSelecionado.setEmail(txtEmail.getText().trim());
            fornecedorSelecionado.setEndereco(txtEndereco.getText().trim());
            fornecedorSelecionado.setTipo(cbTipo.getValue());

            fornecedorService.atualizar(fornecedorSelecionado);

            mostrarInformacao("Fornecedor atualizado com sucesso!");
        }

        carregarTabela();
        limpar(null);
    }

    private void carregarTabela() {

        String texto = txtPesquisar.getText();

        if (texto == null) {
            texto = "";
        }

        texto = texto.trim();

        if (texto.isEmpty()) {

            if (chkMostrarAtivos.isSelected()) {
                tabelaFornecedores.setItems(
                        FXCollections.observableArrayList(fornecedorService.listarAtivos())
                );
            } else {
                tabelaFornecedores.setItems(
                        FXCollections.observableArrayList(fornecedorService.listar())
                );
            }

        } else {

            tabelaFornecedores.setItems(
                    FXCollections.observableArrayList(
                            fornecedorService.pesquisar(texto, chkMostrarAtivos.isSelected())
                    )
            );

        }
    }

    @FXML
    private void selecionarFornecedor() {

        fornecedorSelecionado = tabelaFornecedores.getSelectionModel().getSelectedItem();

        if (fornecedorSelecionado == null) {
            return;
        }

        txtNome.setText(fornecedorSelecionado.getNome());
        txtCnpj.setText(fornecedorSelecionado.getCnpj());
        txtTelefone.setText(fornecedorSelecionado.getTelefone());
        txtEmail.setText(fornecedorSelecionado.getEmail());
        txtEndereco.setText(fornecedorSelecionado.getEndereco());

        cbTipo.setValue(fornecedorSelecionado.getTipo());

    }

    @FXML
    private void limpar(ActionEvent event) {

        txtNome.clear();
        txtCnpj.clear();
        txtTelefone.clear();
        txtEmail.clear();
        txtEndereco.clear();

        cbTipo.getSelectionModel().clearSelection();

        fornecedorSelecionado = null;

        tabelaFornecedores.getSelectionModel().clearSelection();

        txtPesquisar.clear();

        carregarTabela();
    }

    @FXML
    private void desativarFornecedor(ActionEvent event) {

        if (fornecedorSelecionado == null) {
            mostrarErro("Selecione um fornecedor para desativar.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText(null);
        alert.setContentText("Deseja realmente desativar este fornecedor?");

        ButtonType resposta = alert.showAndWait().orElse(ButtonType.CANCEL);

        if (resposta == ButtonType.OK) {

            fornecedorService.desativar(fornecedorSelecionado.getId());

            mostrarInformacao("Fornecedor desativado com sucesso!");

            fornecedorSelecionado = null;

            carregarTabela();

            limpar(null);
        }
    }

    private boolean validarCampos() {

        if (txtNome.getText().trim().isEmpty()) {
            mostrarErro("O nome do fornecedor é obrigatório.");
            txtNome.requestFocus();
            return false;
        }

        if (txtCnpj.getText().trim().isEmpty()) {
            mostrarErro("O CNPJ é obrigatório.");
            txtCnpj.requestFocus();
            return false;
        }

        if (txtTelefone.getText().trim().isEmpty()) {
            mostrarErro("O telefone é obrigatório.");
            txtTelefone.requestFocus();
            return false;
        }

        if (cbTipo.getValue() == null) {
            mostrarErro("Selecione o tipo do fornecedor.");
            cbTipo.requestFocus();
            return false;
        }

        return true;
    }

    private void mostrarErro(String mensagem) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void mostrarInformacao(String mensagem) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {

        cbTipo.getItems().addAll("Fornecedor", "Terceiro");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCnpj.setCellValueFactory(new PropertyValueFactory<>("cnpj"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colAtivo.setCellValueFactory(new PropertyValueFactory<>("ativo"));

        colAtivo.setCellFactory(coluna -> new TableCell<>() {

            @Override
            protected void updateItem(Boolean ativo, boolean empty) {

                super.updateItem(ativo, empty);

                if (empty || ativo == null) {
                    setText(null);
                } else {
                    setText(ativo ? "Ativo" : "Desativado");
                }
            }
        });

        carregarTabela();

        tabelaFornecedores.setRowFactory(tv -> {

            TableRow<Fornecedor> row = new TableRow<>();

            row.setOnMouseClicked(event -> {

                if (event.getClickCount() == 1) {

                    if (row.isEmpty()) {

                        tabelaFornecedores.getSelectionModel().clearSelection();
                        limpar(null);

                    } else if (row.getItem() == fornecedorSelecionado) {

                        tabelaFornecedores.getSelectionModel().clearSelection();
                        limpar(null);

                    }

                }

            });

            return row;

        });

        txtPesquisar.textProperty().addListener((obs, oldValue, newValue) -> carregarTabela());

        chkMostrarAtivos.selectedProperty().addListener((obs, oldValue, newValue) -> carregarTabela());
    }

}
