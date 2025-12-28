package com.group13.greengrocer.controllers;

import com.group13.greengrocer.Main;
import com.group13.greengrocer.database.DatabaseAdapter;
import com.group13.greengrocer.models.*;
import com.group13.greengrocer.utils.*;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class OwnerController {

    @FXML private Label welcomeLabel;
    
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> prodNameCol;
    @FXML private TableColumn<Product, String> prodTypeCol;
    @FXML private TableColumn<Product, Double> prodPriceCol;
    @FXML private TableColumn<Product, Double> prodStockCol;

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdCol;
    @FXML private TableColumn<Order, String> orderCustomerCol;
    @FXML private TableColumn<Order, String> orderStatusCol;
    @FXML private TableColumn<Order, Double> orderTotalCol;

    @FXML private TableView<Coupon> couponsTable;
    @FXML private TableColumn<Coupon, String> couponCodeCol;
    @FXML private TableColumn<Coupon, String> couponDiscountCol;
    @FXML private TableColumn<Coupon, LocalDate> couponExpiryCol;
    @FXML private TableColumn<Coupon, Boolean> couponUsedCol;

    @FXML private ListView<Message> messagesList;
    @FXML private TextArea replyArea;

    @FXML private Label revenueLabel;
    @FXML private Label ordersCountLabel;

    private SessionManager session;
    private DatabaseAdapter dbAdapter;

    @FXML
    public void initialize() {
        session = SessionManager.getInstance();
        dbAdapter = DatabaseAdapter.getInstance();

        welcomeLabel.setText("Owner Dashboard");

        setupTables();
        loadAllData();
    }

    private void setupTables() {
        // Products table
        prodNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        prodTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        prodPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        prodStockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Orders table
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderCustomerCol.setCellValueFactory(data -> {
            User customer = data.getValue().getCustomer();
            return new javafx.beans.property.SimpleStringProperty(
                customer != null ? customer.getFullName() : "Unknown");
        });
        orderStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        orderTotalCol.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Coupons table
        couponCodeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        couponDiscountCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getDiscountDisplay()));
        couponExpiryCol.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        couponUsedCol.setCellValueFactory(new PropertyValueFactory<>("used"));
    }

    private void loadAllData() {
        loadProducts();
        loadOrders();
        loadCoupons();
        loadMessages();
        loadReports();
    }

    private void loadProducts() {
        List<Product> products = dbAdapter.getAllProducts();
        productsTable.setItems(FXCollections.observableArrayList(products));
    }

    private void loadOrders() {
        List<Order> orders = dbAdapter.getAllOrders();
        ordersTable.setItems(FXCollections.observableArrayList(orders));
    }

    private void loadCoupons() {
        List<Coupon> coupons = dbAdapter.getAllCoupons();
        couponsTable.setItems(FXCollections.observableArrayList(coupons));
    }

    private void loadMessages() {
        List<Message> messages = dbAdapter.getMessagesForUser(session.getCurrentUser().getId());
        messagesList.setItems(FXCollections.observableArrayList(messages));
        
        messagesList.setCellFactory(lv -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                } else {
                    String sender = msg.getSender() != null ? msg.getSender().getFullName() : "Unknown";
                    setText(msg.getFormattedTime() + " - " + sender + ": " + msg.getContent());
                }
            }
        });
    }

    private void loadReports() {
        double revenue = dbAdapter.getTotalRevenue();
        int orderCount = dbAdapter.getTotalOrders();
        
        revenueLabel.setText(String.format("Total Revenue: ₺%.2f", revenue));
        ordersCountLabel.setText("Total Orders: " + orderCount);
    }

    @FXML
    private void addProduct(ActionEvent event) {
        showProductDialog(null);
    }

    @FXML
    private void editProduct(ActionEvent event) {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("No Selection", "Please select a product");
            return;
        }
        showProductDialog(selected);
    }

    private void showProductDialog(Product existing) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Product" : "Edit Product");

        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("vegetable", "fruit"));
        typeBox.setValue(existing != null ? existing.getType() : "vegetable");
        TextField priceField = new TextField(existing != null ? String.valueOf(existing.getPrice()) : "");
        TextField stockField = new TextField(existing != null ? String.valueOf(existing.getStock()) : "");
        TextField thresholdField = new TextField(existing != null ? String.valueOf(existing.getThreshold()) : "5");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeBox, 1, 1);
        grid.add(new Label("Price (₺/kg):"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Stock (kg):"), 0, 3);
        grid.add(stockField, 1, 3);
        grid.add(new Label("Threshold (kg):"), 0, 4);
        grid.add(thresholdField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    Product product = existing != null ? existing : new Product();
                    product.setName(nameField.getText().trim());
                    product.setType(typeBox.getValue());
                    product.setPrice(Double.parseDouble(priceField.getText()));
                    product.setStock(Double.parseDouble(stockField.getText()));
                    product.setThreshold(Double.parseDouble(thresholdField.getText()));
                    return product;
                } catch (NumberFormatException e) {
                    AlertUtils.showError("Error", "Invalid number format");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(product -> {
            boolean success;
            if (existing == null) {
                success = dbAdapter.addProduct(product);
            } else {
                success = dbAdapter.updateProduct(product);
            }

            if (success) {
                AlertUtils.showInfo("Success", "Product saved successfully");
                loadProducts();
            } else {
                AlertUtils.showError("Error", "Failed to save product");
            }
        });
    }

    @FXML
    private void deleteProduct(ActionEvent event) {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("No Selection", "Please select a product");
            return;
        }

        if (AlertUtils.showConfirmation("Delete Product", "Delete " + selected.getName() + "?")) {
            if (dbAdapter.deleteProduct(selected.getId())) {
                AlertUtils.showInfo("Deleted", "Product deleted");
                loadProducts();
            } else {
                AlertUtils.showError("Error", "Failed to delete product");
            }
        }
    }

    @FXML
    private void viewOrderDetails(ActionEvent event) {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("No Selection", "Please select an order");
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Order #").append(selected.getId()).append("\n");
        details.append("Customer: ").append(selected.getCustomer() != null ? selected.getCustomer().getFullName() : "Unknown").append("\n");
        details.append("Status: ").append(selected.getStatus()).append("\n\n");
        details.append("Items:\n");
        
        for (OrderItem item : selected.getItems()) {
            details.append(String.format("- %s: %.2f kg = ₺%.2f\n",
                item.getProductName(), item.getQuantity(), item.getSubtotal()));
        }
        
        details.append(String.format("\nTotal: ₺%.2f", selected.getTotal()));

        AlertUtils.showInfo("Order Details", details.toString());
    }

    @FXML
    private void createCoupon(ActionEvent event) {
        Dialog<Coupon> dialog = new Dialog<>();
        dialog.setTitle("Create Coupon");

        TextField codeField = new TextField();
        codeField.setPromptText("COUPON_CODE");
        TextField discountField = new TextField();
        discountField.setPromptText("10");
        CheckBox percentageCheck = new CheckBox("Percentage discount");
        DatePicker expiryPicker = new DatePicker();
        expiryPicker.setValue(LocalDate.now().plusMonths(1));

        VBox content = new VBox(10,
            new Label("Code:"), codeField,
            new Label("Discount Value:"), discountField,
            percentageCheck,
            new Label("Expiry Date:"), expiryPicker
        );
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    Coupon coupon = new Coupon();
                    coupon.setCode(codeField.getText().trim().toUpperCase());
                    coupon.setDiscountValue(Double.parseDouble(discountField.getText()));
                    coupon.setPercentage(percentageCheck.isSelected());
                    coupon.setExpiryDate(expiryPicker.getValue());
                    return coupon;
                } catch (NumberFormatException e) {
                    AlertUtils.showError("Error", "Invalid discount value");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(coupon -> {
            if (dbAdapter.createCoupon(coupon)) {
                AlertUtils.showInfo("Success", "Coupon created");
                loadCoupons();
            } else {
                AlertUtils.showError("Error", "Failed to create coupon");
            }
        });
    }

    @FXML
    private void deleteCoupon(ActionEvent event) {
        Coupon selected = couponsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("No Selection", "Please select a coupon");
            return;
        }

        if (AlertUtils.showConfirmation("Delete Coupon", "Delete coupon " + selected.getCode() + "?")) {
            if (dbAdapter.deleteCoupon(selected.getId())) {
                AlertUtils.showInfo("Deleted", "Coupon deleted");
                loadCoupons();
            } else {
                AlertUtils.showError("Error", "Failed to delete coupon");
            }
        }
    }

    @FXML
    private void sendReply(ActionEvent event) {
        Message selectedMsg = messagesList.getSelectionModel().getSelectedItem();
        String reply = replyArea.getText().trim();

        if (reply.isEmpty()) {
            AlertUtils.showWarning("Empty Message", "Please enter a reply");
            return;
        }

        int receiverId;
        if (selectedMsg != null) {
            receiverId = selectedMsg.getSenderId();
        } else {
            AlertUtils.showWarning("No Selection", "Please select a message to reply to");
            return;
        }

        Message msg = new Message(session.getCurrentUser().getId(), receiverId, reply);
        if (dbAdapter.sendMessage(msg)) {
            replyArea.clear();
            loadMessages();
            AlertUtils.showInfo("Sent", "Reply sent");
        } else {
            AlertUtils.showError("Error", "Failed to send reply");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        session.clearSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = Main.getPrimaryStage();
            Scene scene = new Scene(root, Main.DEFAULT_WIDTH, Main.DEFAULT_HEIGHT);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("GreenGrocer - Login");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Could not logout");
        }
    }
}
