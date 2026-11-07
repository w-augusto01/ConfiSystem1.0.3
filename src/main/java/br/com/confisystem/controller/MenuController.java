package br.com.confisystem.controller;

import br.com.confisystem.util.Icone;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import br.com.confisystem.security.Modulo;
import br.com.confisystem.security.Sessao;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MenuController {

    private final ApplicationContext applicationContext;
    private final Sessao sessao;

    @FXML private Label usuarioLabel;
    @FXML private Button navFormulacao, navCompras, navMaterias, navFornecedores;
    @FXML private VBox cardFormulacao, cardCompras, cardMaterias, cardFornecedores;

    public MenuController(ApplicationContext applicationContext, Sessao sessao) {
        this.applicationContext = applicationContext;
        this.sessao = sessao;
    }

    @FXML
    private void initialize() {

        usuarioLabel.setText("Usuário: " + sessao.getPerfilAtual());

        aplicarPermissao(Modulo.FORMULACAO, navFormulacao, cardFormulacao);
        aplicarPermissao(Modulo.COMPRAS, navCompras, cardCompras);
        aplicarPermissao(Modulo.MATERIAS_PRIMAS, navMaterias, cardMaterias);
        aplicarPermissao(Modulo.FORNECEDORES, navFornecedores, cardFornecedores);
    }

    private void aplicarPermissao(Modulo modulo, javafx.scene.Node... nos) {
        boolean permitido = sessao.podeAcessar(modulo);
        for (javafx.scene.Node no : nos) {
            no.setVisible(permitido);
            no.setManaged(permitido);
        }
    }

    @FXML
    private void sair() {

        try {
            sessao.sair();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/br/com/confisystem/view/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) usuarioLabel.getScene().getWindow();
            stage.setMaximized(false);
            stage.setMinWidth(0);
            stage.setMinHeight(0);
            stage.setScene(new Scene(root));
            stage.setTitle("ConfiSystem - Login");
            stage.setWidth(520);
            stage.setHeight(560);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirFornecedores() {
        abrirTela(Modulo.FORNECEDORES, "/br/com/confisystem/view/fornecedor.fxml", "Fornecedores");
    }

    @FXML
    private void abrirMateriasPrimas() {
        abrirTela(Modulo.MATERIAS_PRIMAS, "/br/com/confisystem/view/materiaprima.fxml", "Matérias-Primas");
    }

    @FXML
    private void abrirCompras() {
        abrirTela(Modulo.COMPRAS, "/br/com/confisystem/view/compra.fxml", "Compras - Entrada de Insumos");
    }

    @FXML
    private void abrirConfinamento() {
        abrirTela(Modulo.CONFINAMENTO, "/br/com/confisystem/view/confinamento.fxml", "Confinamento");
    }

    @FXML
    private void abrirFormulacao() {
        abrirTela(Modulo.FORMULACAO, "/br/com/confisystem/view/formulacao.fxml", "Formulação de Ração");
    }

    @FXML
    private void abrirOrdemProducao() {
        abrirTela(Modulo.ORDEM_PRODUCAO, "/br/com/confisystem/view/ordemproducao.fxml", "Ordem de Produção");
    }

    private void abrirTela(Modulo modulo, String caminhoFxml, String titulo) {

        if (!sessao.podeAcessar(modulo)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Acesso negado");
            alert.setHeaderText(null);
            alert.setContentText("Seu perfil não tem acesso a esta tela.");
            alert.showAndWait();
            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFxml));

            loader.setControllerFactory(applicationContext::getBean);

            Parent root = loader.load();

            Stage stage = new Stage();
            Icone.aplicar(stage);
            stage.setTitle("ConfiSystem - " + titulo);
            stage.setScene(new Scene(root));

            if (root instanceof Region raiz && raiz.getPrefWidth() > 0 && raiz.getPrefHeight() > 0) {
                stage.setMinWidth(raiz.getPrefWidth());
                stage.setMinHeight(raiz.getPrefHeight());
            }

            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Não foi possível abrir a tela de " + titulo + ": " + e.getMessage());
            alert.showAndWait();

            e.printStackTrace();
        }
    }

}
