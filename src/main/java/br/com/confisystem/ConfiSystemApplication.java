package br.com.confisystem;

import br.com.confisystem.util.Icone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ConfiSystemApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {

        try {
            springContext = new SpringApplicationBuilder(ConfiSystemApplication.class)
                    .web(WebApplicationType.NONE)
                    .run(getParameters().getRaw().toArray(new String[0]));
        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Nao foi possivel iniciar o ConfiSystem.\n\n"
                            + "Provavel causa: sem conexao com o banco de dados.\n"
                            + "Verifique se a VPN esta conectada e se o arquivo\n"
                            + "C:\\ConfiSystem\\config.properties esta correto.\n\n"
                            + "Detalhe: " + rootMessage(e),
                    "ConfiSystem - Erro",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private static String rootMessage(Throwable t) {
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return String.valueOf(t.getMessage());
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/br/com/confisystem/view/login.fxml")
        );

        loader.setControllerFactory(springContext::getBean);

        Parent root = loader.load();

        Icone.aplicar(stage);
        stage.setTitle("ConfiSystem - Login");
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}