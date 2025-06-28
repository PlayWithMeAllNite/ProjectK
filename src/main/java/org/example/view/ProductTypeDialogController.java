package org.example.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.model.ProductType;

import java.math.BigDecimal;

public class ProductTypeDialogController {
    @FXML private TextField nameField;
    @FXML private TextField costField;
    @FXML private Button okButton;
    @FXML private Button cancelButton;

    private ProductType productType;
    private boolean okClicked = false;

    public void setProductType(ProductType productType) {
        this.productType = productType;
        if (productType != null) {
            nameField.setText(productType.getName());
            costField.setText(productType.getLaborCost().toString());
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            if (productType == null) {
                productType = new ProductType(nameField.getText(), new BigDecimal(costField.getText()));
            } else {
                productType.setName(nameField.getText());
                productType.setLaborCost(new BigDecimal(costField.getText()));
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

    public ProductType getProductType() {
        return productType;
    }
} 