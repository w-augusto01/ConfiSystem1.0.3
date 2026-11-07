package br.com.confisystem.controller;

import br.com.confisystem.dto.TratoHistoricoLinha;
import br.com.confisystem.service.PiqueteService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@Scope("prototype")
public class PiqueteHistoricoController {

    @FXML
    private Label lblTitulo;

    @FXML
    private TableView<TratoHistoricoLinha> tabelaHistorico;

    @FXML
    private TableColumn<TratoHistoricoLinha, LocalDate> colData;

    @FXML
    private TableColumn<TratoHistoricoLinha, String> colRacao;

    @FXML
    private TableColumn<TratoHistoricoLinha, BigDecimal> colQuantidade;

    @FXML
    private TableColumn<TratoHistoricoLinha, String> colSituacao;

    @FXML
    private TableColumn<TratoHistoricoLinha, BigDecimal> colConsumoMedio;

    @FXML
    private Button btnFechar;

    private final PiqueteService piqueteService;

    private final DecimalFormat formatoKg;
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PiqueteHistoricoController(PiqueteService piqueteService) {
        this.piqueteService = piqueteService;

        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("pt", "BR"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');

        formatoKg = new DecimalFormat("#,##0.##", simbolos);
    }

    @FXML
    public void initialize() {

        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colRacao.setCellValueFactory(new PropertyValueFactory<>("racao"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidadeKg"));
        colSituacao.setCellValueFactory(new PropertyValueFactory<>("situacao"));
        colConsumoMedio.setCellValueFactory(new PropertyValueFactory<>("consumoMedioKgDia"));

        colData.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : valor.format(formatoData));
            }
        });

        colQuantidade.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : formatoKg.format(valor) + " Kg");
            }
        });

        colConsumoMedio.setCellFactory(coluna -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? "—" : formatoKg.format(valor) + " Kg/Dia");
            }
        });

        tabelaHistorico.setRowFactory(tv -> {

            TableRow<TratoHistoricoLinha> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (row.isEmpty()) {
                    tabelaHistorico.getSelectionModel().clearSelection();
                }
            });

            return row;
        });

    }

    public void carregar(Integer piqueteId, String nomePiquete) {

        lblTitulo.setText("Histórico de Tratos — " + nomePiquete);

        tabelaHistorico.getItems().setAll(piqueteService.historico(piqueteId));
    }

    @FXML
    private void fechar(ActionEvent event) {
        ((Stage) btnFechar.getScene().getWindow()).close();
    }

}
