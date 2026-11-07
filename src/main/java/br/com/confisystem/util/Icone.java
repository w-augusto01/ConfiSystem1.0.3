package br.com.confisystem.util;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public final class Icone {

    private static final String CAMINHO = "/br/com/confisystem/view/logo.png";

    private static Image imagem;

    private Icone() {
    }

    public static void aplicar(Stage stage) {
        if (stage == null) {
            return;
        }
        try {
            if (imagem == null) {
                InputStream in = Icone.class.getResourceAsStream(CAMINHO);
                if (in == null) {
                    return;
                }
                imagem = new Image(in);
            }
            if (stage.getIcons().isEmpty()) {
                stage.getIcons().add(imagem);
            }
        } catch (Exception e) {
        }
    }
}
