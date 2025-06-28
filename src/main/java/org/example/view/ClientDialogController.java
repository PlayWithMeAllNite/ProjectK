package org.example.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.model.Client;

public class ClientDialogController {
    @FXML private TextField phoneField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private Button okButton;
    @FXML private Button cancelButton;

    private Client client;
    private boolean okClicked = false;

    public void setClient(Client client) {
        this.client = client;
        if (client != null) {
            phoneField.setText(client.getPhone());
            fullNameField.setText(client.getFullName());
            emailField.setText(client.getEmail());
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            if (client == null) {
                client = new Client(
                    phoneField.getText(),
                    fullNameField.getText(),
                    emailField.getText()
                );
            } else {
                client.setPhone(phoneField.getText());
                client.setFullName(fullNameField.getText());
                client.setEmail(emailField.getText());
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
        if (phoneField.getText() == null || phoneField.getText().trim().isEmpty()) {
            errorMessage += "Телефон обязателен!\n";
        }
        if (fullNameField.getText() == null || fullNameField.getText().trim().isEmpty()) {
            errorMessage += "ФИО обязательно!\n";
        }
        // Можно добавить email-валидацию
        if (!errorMessage.isEmpty()) {
            // Можно добавить Label для ошибок
            return false;
        }
        return true;
    }

    public Client getClient() {
        return client;
    }
} 