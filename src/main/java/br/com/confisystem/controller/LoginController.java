package br.com.confisystem.controller;

import br.com.confisystem.security.Perfil;
import br.com.confisystem.security.Sessao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    private final ApplicationContext applicationContext;
    private final Sessao sessao;

    @FXML private ComboBox<Perfil> usuarioCombo;
    @FXML private PasswordField senhaField;
    @FXML private VBox senhaBox;
    @FXML private Label dicaLabel;
    @FXML private Label erroLabel;
    @FXML private Button entrarButton;

    public LoginController(ApplicationContext applicationContext, Sessao sessao) {
        this.applicationContext = applicationContext;
        this.sessao = sessao;
    }

    @FXML
    private void initialize() {

        usuarioCombo.getItems().setAll(Perfil.values());
        usuarioCombo.getSelectionModel().select(Perfil.OPERADOR);

        usuarioCombo.valueProperty().addListener((obs, antigo, novo) -> atualizarCampoSenha());
        senhaField.textProperty().addListener((obs, antigo, novo) -> limparErro());

        atualizarCampoSenha();
    }

    private void atualizarCampoSenha() {

        Perfil perfil = usuarioCombo.getValue();
        boolean exigeSenha = perfil != null && perfil.exigeSenha();

        senhaBox.setVisible(exigeSenha);
        senhaBox.setManaged(exigeSenha);
        senhaField.clear();
        limparErro();

        dicaLabel.setText(exigeSenha
                ? "Acesso completo a todos os módulos do sistema."
                : "Acesso a Ordem de Produção e Confinamento. Não exige senha.");

        if (exigeSenha) {
            senhaField.requestFocus();
        }
    }

    @FXML
    private void entrar() {

        Perfil perfil = usuarioCombo.getValue();

        if (perfil == null) {
            mostrarErro("Selecione um usuário.");
            return;
        }

        if (!sessao.entrar(perfil, senhaField.getText())) {
            mostrarErro("Senha incorreta. Tente novamente.");
            senhaField.clear();
            senhaField.requestFocus();
            return;
        }

        abrirMenu();
    }

    private void abrirMenu() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/br/com/confisystem/view/menu.fxml")
            );
            loader.setControllerFactory(applicationContext::getBean);

            Parent root = loader.load();

            Stage stage = (Stage) entrarButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("ConfiSystem - Confinamento de Bovinos");

            if (root instanceof Region r && r.getPrefWidth() > 0 && r.getPrefHeight() > 0) {
                stage.setMinWidth(r.getPrefWidth());
                stage.setMinHeight(r.getPrefHeight());
            }

            stage.setResizable(true);
            stage.setMaximized(true);

        } catch (Exception e) {
            sessao.sair();
            mostrarErro("Não foi possível abrir o sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarErro(String mensagem) {
        erroLabel.setText(mensagem);
        erroLabel.setVisible(true);
        erroLabel.setManaged(true);
    }

    private void limparErro() {
        erroLabel.setVisible(false);
        erroLabel.setManaged(false);
    }
}
