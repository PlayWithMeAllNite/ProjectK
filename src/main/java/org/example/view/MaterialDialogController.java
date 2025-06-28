package org.example.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.model.Material;

import java.math.BigDecimal;

public class MaterialDialogController {
    @FXML private TextField nameField;
    @FXML private TextField costField;
    @FXML private Button okButton;
    @FXML private Button cancelButton;

    private Material material;
    private boolean okClicked = false;

    public void setMaterial(Material material) {
        this.material = material;
        if (material != null) {
            nameField.setText(material.getName());
            costField.setText(material.getCostPerGram().toString());
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            if (material == null) {
                material = new Material(nameField.getText(), new BigDecimal(costField.getText()));
            } else {
                material.setName(nameField.getText());
                material.setCostPerGram(new BigDecimal(costField.getText()));
            }
            okClicked = true;
            ((Stage) okButton.getScene().getWindow()).close();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private boolean isInputValid() {
        String errorMessage = "";
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage += "Название обязательно!\n";
        }
        try {
            new BigDecimal(costField.getText());
        } catch (Exception e) {
            errorMessage += "Цена должна быть числом!\n";
        }
        if (!errorMessage.isEmpty()) {
            // Можно добавить Label для ошибок
            return false;
        }
        return true;
    }

    public Material getMaterial() {
        return material;
    }
} 