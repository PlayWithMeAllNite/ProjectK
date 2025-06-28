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
import org.example.database.DataInitializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class MainController {
    
    // Статический экземпляр для доступа из других классов
    private static MainController instance;
    public static MainController getInstance() {
        return instance;
    }
    
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
    
    // Пользователи
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> userIdColumn;
    @FXML private TableColumn<User, String> userUsernameColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private Button addUserBtn, editUserBtn, deleteUserBtn;
    
    // Табы для ролевой авторизации
    @FXML private Tab clientsTab, materialsTab, productTypesTab, ordersTab, usersTab;
    
    // Контроллеры
    private ClientsController clientsController;
    private MaterialsController materialsController;
    private ProductTypesController productTypesController;
    private OrdersController ordersController;
    private UsersController usersController;
    
    // Инициализатор данных
    private DataInitializer dataInitializer;
    
    // Текущий пользователь
    private User currentUser;
    
    public void initialize() {
        // Устанавливаем статический экземпляр
        instance = this;
        
        // Инициализация контроллеров
        clientsController = ClientsController.getInstance();
        materialsController = MaterialsController.getInstance();
        productTypesController = ProductTypesController.getInstance();
        ordersController = OrdersController.getInstance();
        usersController = UsersController.getInstance();
        
        // Инициализация инициализатора данных
        dataInitializer = DataInitializer.getInstance();
        
        // Устанавливаем таблицы в контроллеры
        clientsController.setTableView(clientsTable);
        materialsController.setTableView(materialsTable);
        productTypesController.setTableView(productTypesTable);
        ordersController.setTableView(ordersTable);
        usersController.setTableView(usersTable);
        
        // Инициализируем таблицы
        initializeTables();
        
        // Загружаем данные
        refreshAllData();
    }
    
    /**
     * Устанавливает текущего пользователя и применяет ролевую авторизацию
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            userLabel.setText("Пользователь: " + user.getUsername() + " (" + user.getRole().getRoleName() + ")");
        } else {
            userLabel.setText("Пользователь системы");
        }
        applyPermissions();
    }
    
    /**
     * Применяет ролевую авторизацию на основе роли текущего пользователя
     * Вызывается сразу после входа пользователя
     */
    public void applyPermissions() {
        if (currentUser == null) {
            // Если пользователь не авторизован, показываем только заказы
            disableAllTabs();
            ordersTab.setDisable(false);
            return;
        }
        
        String roleName = currentUser.getRole().getRoleName();
        
        // Сначала отключаем все табы
        disableAllTabs();
        
        // Включаем табы в зависимости от роли
        switch (roleName) {
            case "ADMIN":
                // Администратор видит все
                clientsTab.setDisable(false);
                materialsTab.setDisable(false);
                productTypesTab.setDisable(false);
                ordersTab.setDisable(false);
                usersTab.setDisable(false);
                break;
            case "MANAGER":
                // Менеджер видит клиентов и заказы
                clientsTab.setDisable(false);
                ordersTab.setDisable(false);
                break;
            case "MASTER":
                // Мастер видит только заказы
                ordersTab.setDisable(false);
                break;
            default:
                // По умолчанию показываем только заказы
                ordersTab.setDisable(false);
                break;
        }
        
        // Переходим на первую активную вкладку
        selectFirstActiveTab();
    }
    
    /**
     * Отключает все табы
     */
    private void disableAllTabs() {
        clientsTab.setDisable(true);
        materialsTab.setDisable(true);
        productTypesTab.setDisable(true);
        ordersTab.setDisable(true);
        usersTab.setDisable(true);
    }
    
    /**
     * Выбирает первую активную вкладку
     */
    private void selectFirstActiveTab() {
        for (Tab tab : tabPane.getTabs()) {
            if (!tab.isDisable()) {
                tabPane.getSelectionModel().select(tab);
                break;
            }
        }
    }
    
    private void initializeTables() {
        // Инициализация таблицы клиентов
        clientIdColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getClientId()).asObject());
        clientPhoneColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPhone()));
        clientNameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));
        clientEmailColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));
        clientTotalColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalPurchases()));
        clientDiscountColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getDiscount()).asObject());
        
        // Инициализация таблицы материалов
        materialIdColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getMaterialId()).asObject());
        materialNameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        materialCostColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCostPerGram()));
        
        // Инициализация таблицы типов изделий
        productTypeIdColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getTypeId()).asObject());
        productTypeNameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        productTypeCostColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getLaborCost()));
        
        // Инициализация таблицы заказов
        orderIdColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getOrderId()).asObject());
        orderDateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getOrderDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        orderStatusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().toString()));
        orderClientColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getClient().getFullName()));
        orderTypeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductType().getName()));
        orderWeightColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalWeight()));
        orderPriceColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPrice()));
        orderTotalColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTotalWithDiscount()));
        
        // Инициализация таблицы пользователей
        userIdColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getUserId()).asObject());
        userUsernameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        userRoleColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole().getRoleName()));
    }
    
    private void refreshAllData() {
        try {
            // Загружаем все данные через DataInitializer
            dataInitializer.initializeAllData();
            
            // Обновляем отображение таблиц
            clientsController.updateTableView();
            materialsController.updateTableView();
            productTypesController.updateTableView();
            ordersController.updateTableView();
            usersController.loadAllUsers();
            
        } catch (Exception e) {
            showError("Ошибка загрузки данных", e.getMessage());
        }
    }
    
    private void refreshClientsData() {
        // Обновляем только данные клиентов через контроллер
        clientsController.refreshClients();
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
        Client selected = clientsController.getSelectedClient();
        if (selected == null) {
            clientsController.showWarning("Предупреждение", "Выберите клиента для редактирования");
            return;
        }
        showClientDialog(selected);
    }
    
    @FXML private void handleDeleteClient() {
        Client selected = clientsController.getSelectedClient();
        if (selected == null) {
            clientsController.showWarning("Предупреждение", "Выберите клиента для удаления");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Удаление клиента");
        alert.setContentText("Вы уверены, что хотите удалить клиента " + selected.getFullName() + "?");
        
        if (alert.showAndWait().orElse(null) == ButtonType.OK) {
            if (clientsController.deleteClient(selected.getClientId())) {
                // Данные уже обновлены в контроллере
            }
        }
    }
    
    // Обработчики для материалов
    @FXML private void handleAddMaterial() {
        showMaterialDialog(null);
    }
    
    @FXML private void handleEditMaterial() {
        Material selected = materialsController.getSelectedMaterial();
        if (selected == null) {
            materialsController.showWarning("Предупреждение", "Выберите материал для редактирования");
            return;
        }
        showMaterialDialog(selected);
    }
    
    @FXML private void handleDeleteMaterial() {
        Material selected = materialsController.getSelectedMaterial();
        if (selected == null) {
            materialsController.showWarning("Предупреждение", "Выберите материал для удаления");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение");
        alert.setHeaderText("Удаление материала");
        alert.setContentText("Вы уверены, что хотите удалить материал " + selected.getName() + "?");
        
        if (alert.showAndWait().orElse(null) == ButtonType.OK) {
            if (materialsController.deleteMaterial(selected.getMaterialId())) {
                // Данные уже обновлены в контроллере
            }
        }
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
            controller.setClients(clientsController.getClients());
            controller.setProductTypes(productTypesController.getProductTypes());
            controller.setMaterials(materialsController.getMaterials());
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
    
    // Методы для работы с пользователями
    @FXML
    private void handleAddUser() {
        showUserDialog(null);
    }
    
    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            showUserDialog(selectedUser);
        } else {
            showAlert("Предупреждение", "Пожалуйста, выберите пользователя для редактирования");
        }
    }
    
    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтверждение удаления");
            alert.setHeaderText("Удалить пользователя?");
            alert.setContentText("Вы уверены, что хотите удалить пользователя '" + selectedUser.getUsername() + "'?");
            
            if (alert.showAndWait().orElse(null) == ButtonType.OK) {
                boolean success = usersController.deleteUser(selectedUser.getUserId());
                if (success) {
                    refreshAllData();
                    showAlert("Успех", "Пользователь удален");
                } else {
                    showAlert("Ошибка", "Не удалось удалить пользователя");
                }
            }
        } else {
            showAlert("Предупреждение", "Пожалуйста, выберите пользователя для удаления");
        }
    }
    
    private void showUserDialog(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserDialog.fxml"));
            Parent page = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(user == null ? "Добавить пользователя" : "Редактировать пользователя");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(userLabel.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
            UserDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setUser(user);
            
            dialogStage.showAndWait();
            
            if (controller.isOkClicked()) {
                User newUser = controller.getUser();
                boolean success;
                if (user == null) {
                    success = usersController.addUser(newUser);
                } else {
                    success = usersController.updateUser(newUser);
                }
                
                if (success) {
                    refreshAllData();
                    showAlert("Успех", user == null ? "Пользователь добавлен" : "Пользователь обновлен");
                } else {
                    showAlert("Ошибка", "Не удалось сохранить пользователя");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить диалог пользователя");
        }
    }
    
    // Вспомогательные методы для отображения диалогов
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 