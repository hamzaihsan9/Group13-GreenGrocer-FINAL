package com.group13.greengrocer.controllers;

import com.group13.greengrocer.Main;
import com.group13.greengrocer.database.DatabaseAdapter;
import com.group13.greengrocer.models.User;
import com.group13.greengrocer.utils.AlertUtils;
import com.group13.greengrocer.utils.ValidationUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private Label errorLabel;

    private DatabaseAdapter dbAdapter;

    @FXML
    public void initialize() {
        dbAdapter = DatabaseAdapter.getInstance();
        errorLabel.setText("");
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        // Validation
        if (!ValidationUtils.isValidUsername(username)) {
            errorLabel.setText(ValidationUtils.getUsernameError());
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            errorLabel.setText(ValidationUtils.getPasswordError());
            return;
        }
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            return;
        }
        if (!ValidationUtils.isNotEmpty(fullName)) {
            errorLabel.setText("Please enter your full name");
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("customer");
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);

        try {
            if (dbAdapter.registerUser(user)) {
                AlertUtils.showInfo("Success", "Registration successful! Please login.");
                handleBackToLogin(event);
            } else {
                errorLabel.setText("Registration failed. Username may already exist.");
            }
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = Main.getPrimaryStage();
            Scene scene = new Scene(root, Main.DEFAULT_WIDTH, Main.DEFAULT_HEIGHT);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("GreenGrocer - Login");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Could not return to login");
        }
    }
}
