package cz.vse.si.predikceobleceni.controller;

import cz.vse.si.predikceobleceni.model.obleceni.CastTela;
import cz.vse.si.predikceobleceni.model.obleceni.Formalni;
import cz.vse.si.predikceobleceni.model.obleceni.Obleceni;
import cz.vse.si.predikceobleceni.model.obleceni.Vrstva;
import cz.vse.si.predikceobleceni.utils.Persistence;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.ArrayList;

public class UpravitObleceniController {
    int currentId = Integer.MIN_VALUE;
    CastTela currentCastTela;
    Vrstva currentVrstva;
    @FXML
    private TextField nazev;
    @FXML
    private ComboBox castTela;
    @FXML
    private ComboBox vrstva;
    @FXML
    private Spinner<Integer> minimalniTeplota;
    @FXML
    private Spinner<Integer> maximalniTeplota;
    @FXML
    private ComboBox formalnost;
    @FXML
    private ListView<Obleceni> obleceniListView;
    @FXML
    private Button ulozitButton;
    @FXML
    private Label appendArea;

    private void nacistListView() {
        ArrayList<Obleceni> obleceni = Persistence.getInstance().getAllObleceni();

        ObservableList<Obleceni> obleceniObservableList = FXCollections.observableArrayList(obleceni);
        obleceniListView.setItems(obleceniObservableList);
    }

    private Formalni getFormalni() {
        String vybranaFormalnost = formalnost.getSelectionModel().getSelectedItem().toString();
        switch (vybranaFormalnost) {
            case "neformální":
                return Formalni.MALO;
            case "středně":
                return Formalni.STREDNE;
            case "formální":
                return Formalni.HODNE;
            default:
                return null;
        }
    }

    private Vrstva getVrstva(String vybranaVrstva) {
        //String vybranaVrstva = vrstva.getSelectionModel().getSelectedItem().toString();
        switch (vybranaVrstva) {
            case "první":
                return Vrstva.PRVNI;
            case "druhá":
                return Vrstva.DRUHA;
            case "třetí":
                return Vrstva.TRETI;
            default:
                return null;
        }
    }

    private CastTela getCastTela(String vybranaCastTela) {
        switch (vybranaCastTela) {
            case "hlava":
                return CastTela.HLAVA;
            case "tělo":
                return CastTela.TELO;
            case "nohy":
                return CastTela.NOHY;
            case "boty":
                return CastTela.BOTY;
            default:
                return null;
        }
    }

    public void zavriOkno() {
        Stage stage = (Stage) nazev.getScene().getWindow();
        stage.close();
    }

    public void zpracujKliknutiMysi(MouseEvent mouseEvent) {
        if (obleceniListView.equals(mouseEvent.getSource())) {
            appendArea.setText("");
            Obleceni obleceni = null;
            try {
                obleceni = obleceniListView.getSelectionModel().getSelectedItem();

                currentId = obleceni.getId();

                nazev.setText(obleceni.getNazev());

                currentCastTela = obleceni.getCastTela();
                castTela.setValue(determineCastTela(currentCastTela));

                currentVrstva = obleceni.getVrstva();
                vrstva.setValue(determineVrstva(currentVrstva));

                minimalniTeplota.getValueFactory().setValue(obleceni.getMinimalniTeplota());
                maximalniTeplota.getValueFactory().setValue(obleceni.getMaximalniTeplota());

                formalnost.setValue(determineFormalnost(obleceni.getFormalni()));
            } catch (NullPointerException e) {
                ulozitButton.setDisable(true);
                return;
            }
        }
        ulozitButton.setDisable(false);
    }

    private String determineCastTela(CastTela castTela) {
        switch (castTela) {
            case HLAVA:
                return "hlava";
            case TELO:
                return "tělo";
            case NOHY:
                return "nohy";
            case BOTY:
                return "boty";
        }

        return null;
    }

    private String determineFormalnost(Formalni formalni) {
        switch (formalni) {
            case MALO:
                return "neformální";
            case STREDNE:
                return "středně";
            case HODNE:
                return "formální";
        }

        return null;
    }

    private String determineVrstva(Vrstva vrstva) {
        switch (vrstva) {
            case PRVNI:
                return "první";
            case DRUHA:
                return "druhá";
            case TRETI:
                return "třetí";
        }

        return null;
    }

    public void upravObleceni() {
        appendArea.setText("");
        if (existujePrazdnaHodnota()) {
            appendArea.setText("Chybějící hodnoty");
            return;
        }

        CastTela castTelaKZapisu = getCastTela(castTela.getValue().toString());
        Vrstva vrstvaKZapisu = getVrstva(vrstva.getValue().toString());

        if (spatnaVrstva(castTelaKZapisu, vrstvaKZapisu)) {
            appendArea.setText("Daný kus oblečení může být pouze první vrstvou");
            return;
        }
        if (minimalniTeplota.getValue() > maximalniTeplota.getValue()) {
            appendArea.setText("Problémy v teplotách");
            return;
        }

        Obleceni obleceniKUlozeni = new Obleceni(nazev.getText(), vrstvaKZapisu, castTelaKZapisu, minimalniTeplota.getValue(), maximalniTeplota.getValue(), getFormalni());
        obleceniKUlozeni.setId(currentId);

        Persistence.getInstance().pridejObleceni(obleceniKUlozeni);

        nacistListView();
    }

    private boolean spatnaVrstva(CastTela castTelaKZapisu, Vrstva vrstvaKZapisu) {
        return (castTelaKZapisu == CastTela.HLAVA && vrstvaKZapisu != Vrstva.PRVNI) || (castTelaKZapisu == CastTela.BOTY && vrstvaKZapisu != Vrstva.PRVNI);
    }

    private boolean existujePrazdnaHodnota() {
        return nazev.getText().equals("") || this.castTela.getSelectionModel().getSelectedItem() == null || vrstva.getSelectionModel().getSelectedItem() == null || formalnost.getSelectionModel().getSelectedItem() == null;
    }
}
