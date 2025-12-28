package com.group13.greengrocer.controllers;

import com.group13.greengrocer.Main;
import com.group13.greengrocer.database.DatabaseAdapter;
import com.group13.greengrocer.models.User;
import com.group13.greengrocer.utils.AlertUtils;
import com.group13.greengrocer.utils.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private DatabaseAdapter dbAdapter;

    @FXML
    public void initialize() {
        dbAdapter = DatabaseAdapter.getInstance();
        errorLabel.setText("");
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password");
            return;
        }

        try {
            User user = dbAdapter.authenticateUser(username, password);
            if (user != null) {
                SessionManager.getInstance().setCurrentUser(user);
                navigateToDashboard(user);
            } else {
                errorLabel.setText("Invalid username or password");
            }
        } catch (Exception e) {
            errorLabel.setText("Login error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Register.fxml"));
            Stage stage = Main.getPrimaryStage();
            stage.setScene(new Scene(root, Main.DEFAULT_WIDTH, Main.DEFAULT_HEIGHT));
            stage.setTitle("GreenGrocer - Register");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Could not open registration page");
        }
    }

    private void navigateToDashboard(User user) {
        try {
            String fxml;
            String title;
            
            switch (user.getRole()) {
                case "owner":
                    fxml = "/fxml/Owner.fxml";
                    title = "GreenGrocer - Owner Dashboard";
                    break;
                case "carrier":
                    fxml = "/fxml/Carrier.fxml";
                    title = "GreenGrocer - Carrier Dashboard";
                    break;
                default:
                    fxml = "/fxml/Customer.fxml";
                    title = "GreenGrocer - Customer Dashboard";
            }

            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = Main.getPrimaryStage();
            Scene scene = new Scene(root, Main.DEFAULT_WIDTH, Main.DEFAULT_HEIGHT);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (Exception e) {
            AlertUtils.showError("Error", "Could not load dashboard: " + e.getMessage());
        }
    }
}
