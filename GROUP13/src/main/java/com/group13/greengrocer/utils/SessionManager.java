package com.group13.greengrocer.utils;

import com.group13.greengrocer.models.Order;
import com.group13.greengrocer.models.OrderItem;
import com.group13.greengrocer.models.User;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private Order currentCart;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null && user.isCustomer()) {
            this.currentCart = new Order(user);
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public Order getCurrentCart() {
        return currentCart;
    }

    public void setCurrentCart(Order cart) {
        this.currentCart = cart;
    }

    public List<OrderItem> getCartItems() {
        if (currentCart != null) {
            return currentCart.getItems();
        }
        return new ArrayList<>();
    }

    public void addToCart(OrderItem item) {
        if (currentCart != null) {
            currentCart.addItem(item);
        }
    }

    public void removeFromCart(OrderItem item) {
        if (currentCart != null) {
            currentCart.removeItem(item);
        }
    }

    public int getCartItemCount() {
        if (currentCart != null) {
            return currentCart.getItems().size();
        }
        return 0;
    }

    public void clearCart() {
        if (currentUser != null && currentUser.isCustomer()) {
            this.currentCart = new Order(currentUser);
        }
    }

    public void clearSession() {
        currentUser = null;
        currentCart = null;
    }

    public void logout() {
        clearSession();
    }

    public boolean isCustomer() {
        return currentUser != null && currentUser.isCustomer();
    }

    public boolean isCarrier() {
        return currentUser != null && currentUser.isCarrier();
    }

    public boolean isOwner() {
        return currentUser != null && currentUser.isOwner();
    }
}
