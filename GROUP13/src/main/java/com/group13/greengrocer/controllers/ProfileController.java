package com.group13.greengrocer.controllers;

import com.group13.greengrocer.database.DatabaseAdapter;
import com.group13.greengrocer.models.User;
import com.group13.greengrocer.utils.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ProfileController {

    @FXML private Label usernameLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private PasswordField passwordField;
    @FXML private Label loyaltyLabel;
    @FXML private Label ordersLabel;

    private SessionManager session;
    private DatabaseAdapter dbAdapter;

    @FXML
    public void initialize() {
        session = SessionManager.getInstance();
        dbAdapter = DatabaseAdapter.getInstance();
        loadUserData();
    }

    private void loadUserData() {
        User user = session.getCurrentUser();
        usernameLabel.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        phoneField.setText(user.getPhone() != null ? user.getPhone() : "");
        addressField.setText(user.getAddress() != null ? user.getAddress() : "");

        ordersLabel.setText("Completed Orders: " + user.getCompletedOrders());
        double discount = user.getLoyaltyDiscount();
        loyaltyLabel.setText(String.format("Loyalty Discount: %.0f%%", discount));
    }

    @FXML
    private void handleSave(ActionEvent event) {
        User user = session.getCurrentUser();
        user.setFullName(fullNameField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setPhone(phoneField.getText().trim());
        user.setAddress(addressField.getText().trim());

        String newPassword = passwordField.getText();
        if (!newPassword.isEmpty()) {
            if (newPassword.length() < 4) {
                AlertUtils.showWarning("Invalid Password", "Password must be at least 4 characters");
                return;
            }
            user.setPassword(newPassword);
        }

        if (dbAdapter.updateUser(user)) {
            AlertUtils.showInfo("Success", "Profile updated successfully");
            handleCancel(event);
        } else {
            AlertUtils.showError("Error", "Failed to update profile");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) usernameLabel.getScene().getWindow();
        stage.close();
    }
}
