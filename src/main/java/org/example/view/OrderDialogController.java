package org.example.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class OrderDialogController {
    @FXML private ComboBox<Client> clientComboBox;
    @FXML private ComboBox<ProductType> productTypeComboBox;
    @FXML private ComboBox<Order.Status> statusComboBox;
    @FXML private DatePicker orderDatePicker;
    @FXML private TextField totalWeightField;
    @FXML private TextField laborCostField;
    @FXML private TextField priceField;
    @FXML private Button okButton;
    @FXML private Button cancelButton;
    
    // Материалы
    @FXML private ComboBox<Material> materialComboBox;
    @FXML private TextField materialWeightField;
    @FXML private Button addMaterialBtn;
    @FXML private TableView<OrderMaterial> materialsTable;
    @FXML private TableColumn<OrderMaterial, String> materialNameColumn;
    @FXML private TableColumn<OrderMaterial, BigDecimal> materialWeightColumn;
    @FXML private TableColumn<OrderMaterial, BigDecimal> materialCostColumn;
    @FXML private TableColumn<OrderMaterial, BigDecimal> materialTotalColumn;
    @FXML private TableColumn<OrderMaterial, Void> materialActionColumn;

    private Order order;
    private boolean okClicked = false;
    private ObservableList<OrderMaterial> orderMaterials;

    public void setOrder(Order order) {
        this.order = order;
        if (order != null) {
            clientComboBox.setValue(order.getClient());
            productTypeComboBox.setValue(order.getProductType());
            statusComboBox.setValue(order.getStatus());
            orderDatePicker.setValue(order.getOrderDate());
            totalWeightField.setText(order.getTotalWeight().toString());
            laborCostField.setText(order.getProductType().getLaborCost().toString());
            priceField.setText(order.getPrice().toString());
            
            // Загружаем материалы заказа
            orderMaterials.clear();
            orderMaterials.addAll(order.getMaterials());
        } else {
            orderDatePicker.setValue(LocalDate.now());
            statusComboBox.setValue(Order.Status.IN_PROCESS);
            orderMaterials.clear();
        }
        updateCalculations();
    }

    public void setClients(List<Client> clients) {
        clientComboBox.getItems().addAll(clients);
    }

    public void setProductTypes(List<ProductType> productTypes) {
        productTypeComboBox.getItems().addAll(productTypes);
    }

    public void setMaterials(List<Material> materials) {
        materialComboBox.getItems().addAll(materials);
    }

    public void setStatuses() {
        statusComboBox.getItems().addAll(Order.Status.values());
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void initialize() {
        // Инициализация таблицы материалов
        orderMaterials = FXCollections.observableArrayList();
        materialsTable.setItems(orderMaterials);
        
        materialNameColumn.setCellValueFactory(new PropertyValueFactory<>("materialName"));
        materialWeightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));
        materialCostColumn.setCellValueFactory(new PropertyValueFactory<>("costPerGram"));
        materialTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        
        // Кнопка удаления материала
        materialActionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Удалить");
            {
                deleteButton.setOnAction(event -> {
                    OrderMaterial material = getTableView().getItems().get(getIndex());
                    orderMaterials.remove(material);
                    updateCalculations();
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
        
        // Обработчики изменений для автоматического расчета
        productTypeComboBox.setOnAction(event -> updateCalculations());
        orderMaterials.addListener((javafx.collections.ListChangeListener<OrderMaterial>) change -> updateCalculations());
    }

    @FXML
    private void handleAddMaterial() {
        Material selectedMaterial = materialComboBox.getValue();
        String weightText = materialWeightField.getText();
        
        if (selectedMaterial == null) {
            showAlert("Ошибка", "Выберите материал");
            return;
        }
        
        if (weightText == null || weightText.trim().isEmpty()) {
            showAlert("Ошибка", "Введите вес материала");
            return;
        }
        
        try {
            BigDecimal weight = new BigDecimal(weightText);
            if (weight.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert("Ошибка", "Вес должен быть больше нуля");
                return;
            }
            
            // Создаем временный заказ для OrderMaterial
            Order tempOrder = order != null ? order : new Order(
                clientComboBox.getValue(), 
                productTypeComboBox.getValue()
            );
            
            OrderMaterial orderMaterial = new OrderMaterial(tempOrder, selectedMaterial, weight);
            orderMaterials.add(orderMaterial);
            
            // Очищаем поля
            materialComboBox.setValue(null);
            materialWeightField.clear();
            
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректный вес (число)");
        }
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            if (order == null) {
                order = new Order(
                    clientComboBox.getValue(),
                    productTypeComboBox.getValue()
                );
            }
            order.setClient(clientComboBox.getValue());
            order.setProductType(productTypeComboBox.getValue());
            order.setStatus(statusComboBox.getValue());
            order.setOrderDate(orderDatePicker.getValue());
            
            // Устанавливаем материалы
            order.getMaterials().clear();
            order.getMaterials().addAll(orderMaterials);
            
            // Устанавливаем рассчитанные значения
            order.setTotalWeight(new BigDecimal(totalWeightField.getText()));
            order.setPrice(new BigDecimal(priceField.getText()));
            
            okClicked = true;
            ((Stage) okButton.getScene().getWindow()).close();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private void updateCalculations() {
        // Рассчитываем общий вес
        BigDecimal totalWeight = orderMaterials.stream()
            .map(OrderMaterial::getWeight)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalWeightField.setText(totalWeight.toString());
        
        // Рассчитываем стоимость материалов
        BigDecimal materialsCost = orderMaterials.stream()
            .map(OrderMaterial::getTotalCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Рассчитываем стоимость работы
        BigDecimal laborCost = BigDecimal.ZERO;
        if (productTypeComboBox.getValue() != null) {
            laborCost = productTypeComboBox.getValue().getLaborCost();
        }
        laborCostField.setText(laborCost.toString());
        
        // Рассчитываем общую стоимость
        BigDecimal totalPrice = materialsCost.add(laborCost);
        priceField.setText(totalPrice.toString());
    }

    private boolean isInputValid() {
        String errorMessage = "";
        if (clientComboBox.getValue() == null) {
            errorMessage += "Выберите клиента!\n";
        }
        if (productTypeComboBox.getValue() == null) {
            errorMessage += "Выберите тип изделия!\n";
        }
        if (statusComboBox.getValue() == null) {
            errorMessage += "Выберите статус!\n";
        }
        if (orderDatePicker.getValue() == null) {
            errorMessage += "Выберите дату!\n";
        }
        if (orderMaterials.isEmpty()) {
            errorMessage += "Добавьте хотя бы один материал!\n";
        }
        if (!errorMessage.isEmpty()) {
            showAlert("Ошибка валидации", errorMessage);
            return false;
        }
        return true;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public Order getOrder() {
        return order;
    }
} 