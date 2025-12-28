package com.group13.greengrocer.utils;

public class ValidationUtils {

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 4;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return true; // Optional field
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return true; // Optional field
        return phone.matches("^[0-9+\\-\\s()]{7,20}$");
    }

    public static boolean isValidPositiveDouble(String text) {
        try {
            double value = Double.parseDouble(text);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidNonNegativeDouble(String text) {
        try {
            double value = Double.parseDouble(text);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPositiveInteger(String text) {
        try {
            int value = Integer.parseInt(text);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String getUsernameError() {
        return "Username must be 3-20 characters (letters, numbers, underscore)";
    }

    public static String getPasswordError() {
        return "Password must be at least 4 characters";
    }

    public static String getEmailError() {
        return "Please enter a valid email address";
    }

    public static String getPhoneError() {
        return "Please enter a valid phone number";
    }
}
