package org.example.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.controller.*;
import org.example.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class MainController {
    
    @FXML private TabPane tabPane;
    @FXML private Label userLabel;
    
    // Клиенты
    @FXML private TableView<Client> clientsTable;
    @FXML private TableColumn<Client, Integer> clientIdColumn;
    @FXML private TableColumn<Client, String> clientPhoneColumn;
    @FXML private TableColumn<Client, String> clientNameColumn;
    @FXML private TableColumn<Client, String> clientEmailColumn;
    @FXML private TableColumn<Client, BigDecimal> clientTotalColumn;
    @FXML private TableColumn<Client, Integer> clientDiscountColumn;
    @FXML private Button addClientBtn, editClientBtn, deleteClientBtn;
    
    // Материалы
    @FXML private TableView<Material> materialsTable;
    @FXML private TableColumn<Material, Integer> materialIdColumn;
    @FXML private TableColumn<Material, String> materialNameColumn;
    @FXML private TableColumn<Material, BigDecimal> materialCostColumn;
    @FXML private Button addMaterialBtn, editMaterialBtn, deleteMaterialBtn;
    
    // Типы изделий
    @FXML private TableView<ProductType> productTypesTable;
    @FXML private TableColumn<ProductType, Integer> productTypeIdColumn;
    @FXML private TableColumn<ProductType, String> productTypeNameColumn;
    @FXML private TableColumn<ProductType, BigDecimal> productTypeCostColumn;
    @FXML private Button addProductTypeBtn, editProductTypeBtn, deleteProductTypeBtn;
    
    // Заказы
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> orderDateColumn;
    @FXML private TableColumn<Order, String> orderStatusColumn;
    @FXML private TableColumn<Order, String> orderClientColumn;
    @FXML private TableColumn<Order, String> orderTypeColumn;
    @FXML private TableColumn<Order, BigDecimal> orderWeightColumn;
    @FXML private TableColumn<Order, BigDecimal> orderPriceColumn;
    @FXML private TableColumn<Order, BigDecimal> orderTotalColumn;
    @FXML private Button addOrderBtn, editOrderBtn, deleteOrderBtn;
    
    // Контроллеры
    private ClientsController clientsController;
    private MaterialsController materialsController;
    private ProductTypesController productTypesController;
    private OrdersController ordersController;
    
    public void initialize() {
        // Инициализация контроллеров
        clientsController = ClientsController.getInstance();
        materialsController = MaterialsController.getInstance();
        productTypesController = ProductTypesController.getInstance();
        ordersController = OrdersController.getInstance();
        
        // Устанавливаем информацию о пользователе
        userLabel.setText("Пользователь системы");
        
        // Инициализируем таблицы
        initializeTables();
        
        // Загружаем данные
        refreshAllData();
    }
    
    private void initializeTables() {
        // Настройка таблицы клиентов
        clientIdColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().getClientId()).asObject());
        clientPhoneColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getPhone()));
        clientNameColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));
        clientEmailColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        clientTotalColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTotalPurchases()));
        clientDiscountColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().getDiscount()).asObject());
        
        // Настройка таблицы материалов
        materialIdColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().getMaterialId()).asObject());
        materialNameColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        materialCostColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getCostPerGram()));
        
        // Настройка таблицы типов изделий
        productTypeIdColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().getTypeId()).asObject());
        productTypeNameColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        productTypeCostColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getLaborCost()));
        
        // Настройка таблицы заказов
        orderIdColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().getOrderId()).asObject());
        orderDateColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getOrderDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        orderStatusColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().toString()));
        orderClientColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getClient().getFullName()));
        orderTypeColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getProductType().getName()));
        orderWeightColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTotalWeight()));
        orderPriceColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrice()));
        orderTotalColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTotalWithDiscount()));
    }
    
    private void refreshAllData() {
        // Загружаем данные из БД
        clientsController.loadClients();
        materialsController.loadMaterials();
        productTypesController.loadProductTypes();
        ordersController.loadOrders();
        
        // Обновляем таблицы
        clientsTable.setItems(FXCollections.observableArrayList(clientsController.getAllClients()));
        materialsTable.setItems(FXCollections.observableArrayList(materialsController.getAllMaterials()));
        productTypesTable.setItems(FXCollections.observableArrayList(productTypesController.getAllProductTypes()));
        ordersTable.setItems(FXCollections.observableArrayList(ordersController.getAllOrders()));
    }
    
    private void refreshClientsData() {
        // Обновляем только данные клиентов
        clientsController.loadClients();
        clientsTable.setItems(FXCollections.observableArrayList(clientsController.getAllClients()));
    }
    
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setTitle("Ювелирная мастерская - Вход в систему");
            stage.setScene(new Scene(root));
            stage.setMaximized(false);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Обработчики для клиентов
    @FXML private void handleAddClient() {
        showClientDialog(null);
    }
    
    @FXML private void handleEditClient() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите клиента для редактирования");
            return;
        }
        showClientDialog(selected);
    }
    
    @FXML private void handleDeleteClient() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите клиента для удаления");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Удаление клиента");
        alert.setContentText("Вы уверены, что хотите удалить клиента " + selected.getFullName() + "?");
        
        if (alert.showAndWait().orElse(null) == ButtonType.OK) {
            if (clientsController.deleteClient(selected.getClientId())) {
                refreshAllData();
                showAlert("Успех", "Клиент успешно удален");
            } else {
                showAlert("Ошибка", "Не удалось удалить клиента");
            }
        }
    }
    
    @FXML private void handleRefreshClients() {
        refreshAllData();
    }
    
    // Обработчики для материалов
    @FXML private void handleAddMaterial() {
        showMaterialDialog(null);
    }
    
    @FXML private void handleEditMaterial() {
        Material selected = materialsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите материал для редактирования");
            return;
        }
        showMaterialDialog(selected);
    }
    
    @FXML private void handleDeleteMaterial() {
        Material selected = materialsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите материал для удаления");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Удаление материала");
        alert.setContentText("Вы уверены, что хотите удалить материал " + selected.getName() + "?");
        
        if (alert.showAndWait().orElse(null) == ButtonType.OK) {
            if (materialsController.deleteMaterial(selected.getMaterialId())) {
                refreshAllData();
                showAlert("Успех", "Материал успешно удален");
            } else {
                showAlert("Ошибка", "Не удалось удалить материал");
            }
        }
    }
    
    @FXML private void handleRefreshMaterials() {
        refreshAllData();
    }
    
    // Обработчики для типов изделий
    @FXML private void handleAddProductType() {
        showProductTypeDialog(null);
    }
    
    @FXML private void handleEditProductType() {
        ProductType selected = productTypesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите тип изделия для редактирования");
            return;
        }
        showProductTypeDialog(selected);
    }
    
    @FXML private void handleDeleteProductType() {
        ProductType selected = productTypesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите тип изделия для удаления");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Удаление типа изделия");
        alert.setContentText("Вы уверены, что хотите удалить тип изделия " + selected.getName() + "?");
        
        if (alert.showAndWait().orElse(null) == ButtonType.OK) {
            if (productTypesController.deleteProductType(selected.getTypeId())) {
                refreshAllData();
                showAlert("Успех", "Тип изделия успешно удален");
            } else {
                showAlert("Ошибка", "Не удалось удалить тип изделия");
            }
        }
    }
    
    @FXML private void handleRefreshProductTypes() {
        refreshAllData();
    }
    
    // Обработчики для заказов
    @FXML private void handleAddOrder() {
        showOrderDialog(null);
    }
    
    @FXML private void handleEditOrder() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите заказ для редактирования");
            return;
        }
        showOrderDialog(selected);
    }
    
    @FXML private void handleDeleteOrder() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Предупреждение", "Выберите заказ для удаления");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Удаление заказа");
        alert.setContentText("Вы уверены, что хотите удалить заказ №" + selected.getOrderId() + "?");
        
        if (alert.showAndWait().orElse(null) == ButtonType.OK) {
            if (ordersController.deleteOrder(selected.getOrderId())) {
                refreshAllData();
                refreshClientsData();
                showAlert("Успех", "Заказ успешно удален");
            } else {
                showAlert("Ошибка", "Не удалось удалить заказ");
            }
        }
    }
    
    @FXML private void handleRefreshOrders() {
        refreshAllData();
    }
    
    // Методы для показа диалогов
    private void showClientDialog(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClientDialog.fxml"));
            Parent page = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(client == null ? "Добавить клиента" : "Редактировать клиента");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(userLabel.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
            ClientDialogController controller = loader.getController();
            controller.setClient(client);
            
            dialogStage.showAndWait();
            
            if (controller.isOkClicked()) {
                Client newClient = controller.getClient();
                boolean success;
                if (client == null) {
                    success = clientsController.addClient(newClient);
                } else {
                    success = clientsController.updateClient(newClient);
                }
                
                if (success) {
                    refreshAllData();
                    showAlert("Успех", client == null ? "Клиент добавлен" : "Клиент обновлен");
                } else {
                    showAlert("Ошибка", "Не удалось сохранить клиента");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showMaterialDialog(Material material) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MaterialDialog.fxml"));
            Parent page = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(material == null ? "Добавить материал" : "Редактировать материал");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(userLabel.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
            MaterialDialogController controller = loader.getController();
            controller.setMaterial(material);
            
            dialogStage.showAndWait();
            
            if (controller.isOkClicked()) {
                Material newMaterial = controller.getMaterial();
                boolean success;
                if (material == null) {
                    success = materialsController.addMaterial(newMaterial);
                } else {
                    success = materialsController.updateMaterial(newMaterial);
                }
                
                if (success) {
                    refreshAllData();
                    showAlert("Успех", material == null ? "Материал добавлен" : "Материал обновлен");
                } else {
                    showAlert("Ошибка", "Не удалось сохранить материал");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showProductTypeDialog(ProductType productType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductTypeDialog.fxml"));
            Parent page = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(productType == null ? "Добавить тип изделия" : "Редактировать тип изделия");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(userLabel.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
            ProductTypeDialogController controller = loader.getController();
            controller.setProductType(productType);
            
            dialogStage.showAndWait();
            
            if (controller.isOkClicked()) {
                ProductType newProductType = controller.getProductType();
                boolean success;
                if (productType == null) {
                    success = productTypesController.addProductType(newProductType);
                } else {
                    success = productTypesController.updateProductType(newProductType);
                }
                
                if (success) {
                    refreshAllData();
                    showAlert("Успех", productType == null ? "Тип изделия добавлен" : "Тип изделия обновлен");
                } else {
                    showAlert("Ошибка", "Не удалось сохранить тип изделия");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showOrderDialog(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OrderDialog.fxml"));
            Parent page = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(order == null ? "Добавить заказ" : "Редактировать заказ");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(userLabel.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
            OrderDialogController controller = loader.getController();
            controller.setOrder(order);
            controller.setClients(clientsController.getAllClients());
            controller.setProductTypes(productTypesController.getAllProductTypes());
            controller.setMaterials(materialsController.getAllMaterials());
            controller.setStatuses();
            
            dialogStage.showAndWait();
            
            if (controller.isOkClicked()) {
                Order newOrder = controller.getOrder();
                boolean success;
                if (order == null) {
                    success = ordersController.addOrder(newOrder);
                } else {
                    success = ordersController.updateOrder(newOrder);
                }
                
                if (success) {
                    refreshAllData();
                    refreshClientsData();
                    showAlert("Успех", order == null ? "Заказ добавлен" : "Заказ обновлен");
                } else {
                    showAlert("Ошибка", "Не удалось сохранить заказ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 