package com.group13.greengrocer.controllers;

import com.group13.greengrocer.database.DatabaseAdapter;
import com.group13.greengrocer.models.*;
import com.group13.greengrocer.utils.*;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class CartController {

    @FXML private TableView<OrderItem> cartTable;
    @FXML private TableColumn<OrderItem, String> productCol;
    @FXML private TableColumn<OrderItem, Double> quantityCol;
    @FXML private TableColumn<OrderItem, Double> priceCol;
    @FXML private TableColumn<OrderItem, Double> subtotalCol;
    @FXML private TextField couponField;
    @FXML private Label couponLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label discountLabel;
    @FXML private Label vatLabel;
    @FXML private Label totalLabel;

    private SessionManager session;
    private DatabaseAdapter dbAdapter;
    private Coupon appliedCoupon;

    @FXML
    public void initialize() {
        session = SessionManager.getInstance();
        dbAdapter = DatabaseAdapter.getInstance();

        setupTable();
        loadCart();
    }

    private void setupTable() {
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        subtotalCol.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
    }

    private void loadCart() {
        cartTable.setItems(FXCollections.observableArrayList(session.getCartItems()));
        updateTotals();
    }

    private void updateTotals() {
        double subtotal = session.getCartItems().stream()
            .mapToDouble(OrderItem::getSubtotal)
            .sum();

        double couponDiscount = 0;
        if (appliedCoupon != null && appliedCoupon.isValid()) {
            couponDiscount = appliedCoupon.calculateDiscount(subtotal);
        }

        double loyaltyDiscount = session.getCurrentUser().getLoyaltyDiscount() / 100.0 * (subtotal - couponDiscount);
        double totalDiscount = couponDiscount + loyaltyDiscount;
        double afterDiscount = subtotal - totalDiscount;
        double vat = afterDiscount * Order.VAT_RATE;
        double total = afterDiscount + vat;

        subtotalLabel.setText(String.format("Subtotal: ₺%.2f", subtotal));
        discountLabel.setText(String.format("Discount: ₺%.2f", totalDiscount));
        vatLabel.setText(String.format("VAT (18%%): ₺%.2f", vat));
        totalLabel.setText(String.format("Total: ₺%.2f", total));
    }

    @FXML
    private void removeItem(ActionEvent event) {
        OrderItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("No Selection", "Please select an item to remove");
            return;
        }

        session.removeFromCart(selected);
        loadCart();
    }

    @FXML
    private void updateQuantity(ActionEvent event) {
        OrderItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("No Selection", "Please select an item");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getQuantity()));
        dialog.setTitle("Update Quantity");
        dialog.setHeaderText("Enter new quantity (kg):");

        dialog.showAndWait().ifPresent(str -> {
            try {
                double newQty = Double.parseDouble(str);
                if (newQty <= 0) {
                    session.removeFromCart(selected);
                } else {
                    selected.setQuantity(newQty);
                }
                loadCart();
            } catch (NumberFormatException e) {
                AlertUtils.showError("Error", "Invalid quantity");
            }
        });
    }

    @FXML
    private void applyCoupon(ActionEvent event) {
        String code = couponField.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            AlertUtils.showWarning("Empty Code", "Please enter a coupon code");
            return;
        }

        Coupon coupon = dbAdapter.getCouponByCode(code);
        if (coupon == null) {
            couponLabel.setText("Invalid coupon");
            couponLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!coupon.isValid()) {
            couponLabel.setText("Coupon expired or already used");
            couponLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        appliedCoupon = coupon;
        couponLabel.setText("Coupon applied: " + coupon.getDiscountDisplay());
        couponLabel.setStyle("-fx-text-fill: green;");
        updateTotals();
    }

    @FXML
    private void placeOrder(ActionEvent event) {
        if (session.getCartItems().isEmpty()) {
            AlertUtils.showWarning("Empty Cart", "Your cart is empty");
            return;
        }

        double subtotal = session.getCartItems().stream()
            .mapToDouble(OrderItem::getSubtotal)
            .sum();

        if (subtotal < 50) {
            AlertUtils.showWarning("Minimum Order", "Minimum order value is ₺50");
            return;
        }

        if (!AlertUtils.showConfirmation("Confirm Order", "Place this order?")) {
            return;
        }

        try {
            Order order = new Order(session.getCurrentUser());
            order.setOrderTime(LocalDateTime.now());
            order.setStatus(Order.STATUS_AVAILABLE);

            for (OrderItem item : session.getCartItems()) {
                order.addItem(item);
            }

            double couponDiscount = 0;
            if (appliedCoupon != null && appliedCoupon.isValid()) {
                couponDiscount = appliedCoupon.calculateDiscount(order.getSubtotal());
            }

            double loyaltyDiscount = session.getCurrentUser().getLoyaltyDiscount() / 100.0 * (order.getSubtotal() - couponDiscount);
            order.setDiscount(couponDiscount + loyaltyDiscount);
            order.recalculateTotals();

            if (dbAdapter.createOrder(order)) {
                if (appliedCoupon != null) {
                    dbAdapter.markCouponUsed(appliedCoupon.getId());
                }

                AlertUtils.showInfo("Success", "Order placed successfully!\nOrder ID: " + order.getId());
                session.clearCart();
                closeCart(event);
            } else {
                AlertUtils.showError("Error", "Failed to place order");
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to place order: " + e.getMessage());
        }
    }

    @FXML
    private void closeCart(ActionEvent event) {
        Stage stage = (Stage) cartTable.getScene().getWindow();
        stage.close();
    }
}
