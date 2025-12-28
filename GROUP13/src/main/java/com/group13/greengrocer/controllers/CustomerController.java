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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerController {

    @FXML private Label welcomeLabel;
    @FXML private TabPane tabPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ListView<Product> productList;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdCol;
    @FXML private TableColumn<Order, String> orderDateCol;
    @FXML private TableColumn<Order, String> orderStatusCol;
    @FXML private TableColumn<Order, Double> orderTotalCol;
    @FXML private ListView<Message> messageList;
    @FXML private TextArea newMessageArea;

    private SessionManager session;
    private DatabaseAdapter dbAdapter;
    private List<Product> allProducts;

    @FXML
    public void initialize() {
        session = SessionManager.getInstance();
        dbAdapter = DatabaseAdapter.getInstance();

        User user = session.getCurrentUser();
        welcomeLabel.setText("Welcome, " + user.getFullName());

        setupTypeFilter();
        setupProductList();
        setupOrdersTable();
        loadProducts();
        loadOrders();
        loadMessages();

        searchField.textProperty().addListener((obs, old, newVal) -> filterProducts());
        typeFilter.setOnAction(e -> filterProducts());
    }

    private void setupTypeFilter() {
        typeFilter.setItems(FXCollections.observableArrayList("All", "vegetable", "fruit"));
        typeFilter.setValue("All");
    }

    private void setupProductList() {
        productList.setCellFactory(lv -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    ImageView img = new ImageView();
                    img.setFitWidth(60);
                    img.setFitHeight(60);
                    if (product.getImage() != null) {
                        img.setImage(new Image(new ByteArrayInputStream(product.getImage())));
                    }
                    
                    VBox info = new VBox(5);
                    Label nameLabel = new Label(product.getName());
                    nameLabel.setStyle("-fx-font-weight: bold;");
                    Label priceLabel = new Label(String.format("₺%.2f/kg", product.getEffectivePrice()));
                    Label stockLabel = new Label(String.format("Stock: %.1f kg", product.getStock()));
                    info.getChildren().addAll(nameLabel, priceLabel, stockLabel);
                    
                    Button addBtn = new Button("Add to Cart");
                    addBtn.setOnAction(e -> addToCart(product));
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    box.getChildren().addAll(img, info, spacer, addBtn);
                    setGraphic(box);
                }
            }
        });
    }

    private void setupOrdersTable() {
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderDateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getFormattedOrderTime()));
        orderStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        orderTotalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    private void loadProducts() {
        try {
            allProducts = dbAdapter.getAllProducts();
            productList.setItems(FXCollections.observableArrayList(allProducts));
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to load products");
        }
    }

    private void filterProducts() {
        String search = searchField.getText().toLowerCase();
        String type = typeFilter.getValue();
        
        List<Product> filtered = allProducts.stream()
            .filter(p -> p.getName().toLowerCase().contains(search))
            .filter(p -> "All".equals(type) || p.getType().equals(type))
            .collect(Collectors.toList());
        
        productList.setItems(FXCollections.observableArrayList(filtered));
    }

    private void addToCart(Product product) {
        if (product.getStock() <= 0) {
            AlertUtils.showWarning("Out of Stock", "This product is out of stock");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("1.0");
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Enter quantity (kg) for " + product.getName());
        dialog.setContentText("Quantity:");

        dialog.showAndWait().ifPresent(quantityStr -> {
            try {
                double quantity = Double.parseDouble(quantityStr);
                if (quantity <= 0) {
                    AlertUtils.showWarning("Invalid", "Quantity must be positive");
                    return;
                }
                if (quantity > product.getStock()) {
                    AlertUtils.showWarning("Invalid", "Not enough stock");
                    return;
                }

                OrderItem item = new OrderItem(product, quantity);
                session.addToCart(item);
                AlertUtils.showInfo("Added", product.getName() + " added to cart");
            } catch (NumberFormatException e) {
                AlertUtils.showError("Error", "Invalid quantity");
            }
        });
    }

    private void loadOrders() {
        try {
            List<Order> orders = dbAdapter.getOrdersByCustomer(session.getCurrentUser().getId());
            ordersTable.setItems(FXCollections.observableArrayList(orders));
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to load orders");
        }
    }

    private void loadMessages() {
        try {
            List<Message> messages = dbAdapter.getMessagesForUser(session.getCurrentUser().getId());
            messageList.setItems(FXCollections.observableArrayList(messages));
            
            messageList.setCellFactory(lv -> new ListCell<Message>() {
                @Override
                protected void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || msg == null) {
                        setText(null);
                    } else {
                        String prefix = msg.getSenderId() == session.getCurrentUser().getId() ? "You: " : "Owner: ";
                        setText(msg.getFormattedTime() + " - " + prefix + msg.getContent());
                    }
                }
            });
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to load messages");
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
        details.append("Date: ").append(selected.getFormattedOrderTime()).append("\n");
        details.append("Status: ").append(selected.getStatus()).append("\n\n");
        details.append("Items:\n");
        
        for (OrderItem item : selected.getItems()) {
            details.append(String.format("- %s: %.2f kg @ ₺%.2f = ₺%.2f\n",
                item.getProductName(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal()));
        }
        
        details.append(String.format("\nSubtotal: ₺%.2f", selected.getSubtotal()));
        details.append(String.format("\nVAT: ₺%.2f", selected.getVatAmount()));
        details.append(String.format("\nTotal: ₺%.2f", selected.getTotal()));

        AlertUtils.showInfo("Order Details", details.toString());
    }

    @FXML
    private void cancelOrder(ActionEvent event) {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("No Selection", "Please select an order");
            return;
        }

        if (!selected.canCancel()) {
            AlertUtils.showWarning("Cannot Cancel", "This order cannot be cancelled");
            return;
        }

        if (AlertUtils.showConfirmation("Cancel Order", "Are you sure you want to cancel this order?")) {
            if (dbAdapter.cancelOrder(selected.getId())) {
                AlertUtils.showInfo("Cancelled", "Order has been cancelled");
                loadOrders();
            } else {
                AlertUtils.showError("Error", "Failed to cancel order");
            }
        }
    }

    @FXML
    private void rateCarrier(ActionEvent event) {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("No Selection", "Please select an order");
            return;
        }

        if (!"delivered".equals(selected.getStatus())) {
            AlertUtils.showWarning("Cannot Rate", "You can only rate delivered orders");
            return;
        }

        if (dbAdapter.hasRatedOrder(selected.getId())) {
            AlertUtils.showWarning("Already Rated", "You have already rated this order");
            return;
        }

        Dialog<CarrierRating> dialog = new Dialog<>();
        dialog.setTitle("Rate Carrier");
        
        ComboBox<Integer> ratingBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        ratingBox.setValue(5);
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Optional comment...");
        
        VBox content = new VBox(10,
            new Label("Rating:"), ratingBox,
            new Label("Comment:"), commentArea
        );
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                CarrierRating rating = new CarrierRating();
                rating.setCarrierId(selected.getCarrierId());
                rating.setCustomerId(session.getCurrentUser().getId());
                rating.setOrderId(selected.getId());
                rating.setRating(ratingBox.getValue());
                rating.setComment(commentArea.getText());
                return rating;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(rating -> {
            if (dbAdapter.addCarrierRating(rating)) {
                AlertUtils.showInfo("Thank You", "Rating submitted successfully");
            } else {
                AlertUtils.showError("Error", "Failed to submit rating");
            }
        });
    }

    @FXML
    private void sendMessage(ActionEvent event) {
        String content = newMessageArea.getText().trim();
        if (content.isEmpty()) {
            AlertUtils.showWarning("Empty Message", "Please enter a message");
            return;
        }

        User owner = dbAdapter.getOwner();
        if (owner == null) {
            AlertUtils.showError("Error", "Could not find owner");
            return;
        }

        Message msg = new Message(session.getCurrentUser().getId(), owner.getId(), content);
        if (dbAdapter.sendMessage(msg)) {
            newMessageArea.clear();
            loadMessages();
        } else {
            AlertUtils.showError("Error", "Failed to send message");
        }
    }

    @FXML
    private void openCart(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Cart.fxml"));
            Parent root = loader.load();
            
            Stage cartStage = new Stage();
            cartStage.setTitle("Shopping Cart");
            cartStage.initModality(Modality.APPLICATION_MODAL);
            cartStage.setScene(new Scene(root, 600, 500));
            cartStage.showAndWait();
            
            loadOrders();
            loadProducts();
        } catch (Exception e) {
            AlertUtils.showError("Error", "Could not open cart: " + e.getMessage());
        }
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
