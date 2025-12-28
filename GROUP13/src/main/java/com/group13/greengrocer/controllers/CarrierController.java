package com.group13.greengrocer.controllers;

import com.group13.greengrocer.Main;
import com.group13.greengrocer.database.DatabaseAdapter;
import com.group13.greengrocer.models.*;
import com.group13.greengrocer.utils.*;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class CarrierController {

    @FXML private Label welcomeLabel;
    @FXML private Label ratingLabel;
    
    @FXML private TableView<Order> availableOrdersTable;
    @FXML private TableColumn<Order, Integer> availIdCol;
    @FXML private TableColumn<Order, String> availCustomerCol;
    @FXML private TableColumn<Order, String> availTimeCol;
    @FXML private TableColumn<Order, Double> availTotalCol;

    @FXML private TableView<Order> myOrdersTable;
    @FXML private TableColumn<Order, Integer> myIdCol;
    @FXML private TableColumn<Order, String> myCustomerCol;
    @FXML private TableColumn<Order, String> myStatusCol;
    @FXML private TableColumn<Order, Double> myTotalCol;

    @FXML private Label avgRatingLabel;
    @FXML private ListView<CarrierRating> ratingsList;

    private SessionManager session;
    private DatabaseAdapter dbAdapter;

    @FXML
    public void initialize() {
        session = SessionManager.getInstance();
        dbAdapter = DatabaseAdapter.getInstance();

        User user = session.getCurrentUser();
        welcomeLabel.setText("Welcome, " + user.getFullName());

        setupTables();
        loadData();
    }

    private void setupTables() {
        // Available orders table
        availIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        availCustomerCol.setCellValueFactory(data -> {
            User customer = data.getValue().getCustomer();
            return new javafx.beans.property.SimpleStringProperty(
                customer != null ? customer.getFullName() : "Unknown");
        });
        availTimeCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getFormattedOrderTime()));
        availTotalCol.setCellValueFactory(new PropertyValueFactory<>("total"));

        // My orders table
        myIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        myCustomerCol.setCellValueFactory(data -> {
            User customer = data.getValue().getCustomer();
            return new javafx.beans.property.SimpleStringProperty(
                customer != null ? customer.getFullName() : "Unknown");
        });
        myStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        myTotalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    private void loadData() {
        loadAvailableOrders();
        loadMyOrders();
        loadRatings();
        updateRatingDisplay();
    }

    private void loadAvailableOrders() {
        List<Order> orders = dbAdapter.getAvailableOrders();
        availableOrdersTable.setItems(FXCollections.observableArrayList(orders));
    }

    private void loadMyOrders() {
        List<Order> orders = dbAdapter.getOrdersByCarrier(session.getCurrentUser().getId());
        myOrdersTable.setItems(FXCollections.observableArrayList(orders));
    }

    private void loadRatings() {
        List<CarrierRating> ratings = dbAdapter.getRatingsForCarrier(session.getCurrentUser().getId());
        ratingsList.setItems(FXCollections.observableArrayList(ratings));
        
        ratingsList.setCellFactory(lv -> new ListCell<CarrierRating>() {
            @Override
            protected void updateItem(CarrierRating rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText(null);
                } else {
                    String text = rating.getStarDisplay() + " - Order #" + rating.getOrderId();
                    if (rating.getComment() != null && !rating.getComment().isEmpty()) {
                        text += "\n\"" + rating.getComment() + "\"";
                    }
                    setText(text);
                }
            }
        });
    }

    private void updateRatingDisplay() {
        double avgRating = dbAdapter.getAverageRating(session.getCurrentUser().getId());
        if (avgRating > 0) {
            ratingLabel.setText(String.format("Rating: %.1f ★", avgRating));
            avgRatingLabel.setText(String.format("Average Rating: %.1f ★", avgRating));
        } else {
            ratingLabel.setText("Rating: N/A");
            avgRatingLabel.setText("Average Rating: No ratings yet");
        }
    }

    @FXML
    private void acceptOrder(ActionEvent event) {
        Order selected = availableOrdersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("No Selection", "Please select an order to accept");
            return;
        }

        if (AlertUtils.showConfirmation("Accept Order", "Accept this order for delivery?")) {
            if (dbAdapter.assignCarrier(selected.getId(), session.getCurrentUser().getId())) {
                AlertUtils.showInfo("Accepted", "Order accepted successfully");
                loadData();
            } else {
                AlertUtils.showError("Error", "Failed to accept order");
            }
        }
    }

    @FXML
    private void completeDelivery(ActionEvent event) {
        Order selected = myOrdersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("No Selection", "Please select an order");
            return;
        }

        if ("delivered".equals(selected.getStatus())) {
            AlertUtils.showWarning("Already Delivered", "This order is already delivered");
            return;
        }

        if (AlertUtils.showConfirmation("Complete Delivery", "Mark this order as delivered?")) {
            if (dbAdapter.completeOrder(selected.getId())) {
                AlertUtils.showInfo("Completed", "Delivery marked as complete");
                loadData();
            } else {
                AlertUtils.showError("Error", "Failed to complete delivery");
            }
        }
    }

    @FXML
    private void viewAvailableOrderDetails(ActionEvent event) {
        Order selected = availableOrdersTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showOrderDetails(selected);
        } else {
            AlertUtils.showWarning("No Selection", "Please select an order");
        }
    }

    @FXML
    private void viewMyOrderDetails(ActionEvent event) {
        Order selected = myOrdersTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showOrderDetails(selected);
        } else {
            AlertUtils.showWarning("No Selection", "Please select an order");
        }
    }

    private void showOrderDetails(Order order) {
        StringBuilder details = new StringBuilder();
        details.append("Order #").append(order.getId()).append("\n");
        details.append("Customer: ").append(order.getCustomer() != null ? order.getCustomer().getFullName() : "Unknown").append("\n");
        details.append("Status: ").append(order.getStatus()).append("\n\n");
        details.append("Items:\n");
        
        for (OrderItem item : order.getItems()) {
            details.append(String.format("- %s: %.2f kg = ₺%.2f\n",
                item.getProductName(), item.getQuantity(), item.getSubtotal()));
        }
        
        details.append(String.format("\nTotal: ₺%.2f", order.getTotal()));

        AlertUtils.showInfo("Order Details", details.toString());
    }

    @FXML
    private void openProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Profile.fxml"));
            Parent root = loader.load();
            
            Stage profileStage = new Stage();
            profileStage.setTitle("Profile");
            profileStage.initModality(Modality.APPLICATION_MODAL);
            profileStage.setScene(new Scene(root, 400, 500));
            profileStage.showAndWait();
        } catch (Exception e) {
            AlertUtils.showError("Error", "Could not open profile");
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
