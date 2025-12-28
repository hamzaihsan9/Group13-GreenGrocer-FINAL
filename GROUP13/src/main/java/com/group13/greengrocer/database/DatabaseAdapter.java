package com.group13.greengrocer.database;

import com.group13.greengrocer.models.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAdapter {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/greengrocer";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Darkshadow_007";

    private static DatabaseAdapter instance;

    private DatabaseAdapter() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
        }
    }

    public static synchronized DatabaseAdapter getInstance() {
        if (instance == null) {
            instance = new DatabaseAdapter();
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // ========== USER OPERATIONS ==========

    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Auth error: " + e.getMessage());
        }
        return null;
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, role, full_name, email, phone, address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getPhone());
            stmt.setString(7, user.getAddress());
            
            if (stmt.executeUpdate() > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Register error: " + e.getMessage());
        }
        return false;
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, address = ?, password = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getAddress());
            stmt.setString(5, user.getPassword());
            stmt.setInt(6, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update user error: " + e.getMessage());
        }
        return false;
    }

    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Get user error: " + e.getMessage());
        }
        return null;
    }

    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(extractUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Get users error: " + e.getMessage());
        }
        return users;
    }

    public User getOwner() {
        List<User> owners = getUsersByRole("owner");
        return owners.isEmpty() ? null : owners.get(0);
    }

    private User extractUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("role"),
            rs.getString("full_name"),
            rs.getString("address"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getInt("loyalty_points"),
            rs.getInt("completed_orders")
        );
    }

    // ========== PRODUCT OPERATIONS ==========

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY name";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Get products error: " + e.getMessage());
        }
        return products;
    }

    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractProduct(rs);
            }
        } catch (SQLException e) {
            System.err.println("Get product error: " + e.getMessage());
        }
        return null;
    }

    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (name, type, price, stock, image, threshold) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getType());
            stmt.setDouble(3, product.getPrice());
            stmt.setDouble(4, product.getStock());
            stmt.setBytes(5, product.getImageData());
            stmt.setDouble(6, product.getThreshold());
            
            if (stmt.executeUpdate() > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    product.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Add product error: " + e.getMessage());
        }
        return false;
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, type = ?, price = ?, stock = ?, image = ?, threshold = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getType());
            stmt.setDouble(3, product.getPrice());
            stmt.setDouble(4, product.getStock());
            stmt.setBytes(5, product.getImageData());
            stmt.setDouble(6, product.getThreshold());
            stmt.setInt(7, product.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update product error: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Delete product error: " + e.getMessage());
        }
        return false;
    }

    private Product extractProduct(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("type"),
            rs.getDouble("price"),
            rs.getDouble("stock"),
            rs.getBytes("image"),
            rs.getDouble("threshold")
        );
    }

    // ========== ORDER OPERATIONS ==========

    public boolean createOrder(Order order) {
        String orderSql = "INSERT INTO orders (customer_id, order_time, delivery_time, status, subtotal, vat_amount, total, discount, invoice) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, order.getCustomerId());
            orderStmt.setTimestamp(2, Timestamp.valueOf(order.getOrderTime()));
            orderStmt.setTimestamp(3, order.getDeliveryTime() != null ? Timestamp.valueOf(order.getDeliveryTime()) : null);
            orderStmt.setString(4, order.getStatus());
            orderStmt.setDouble(5, order.getSubtotal());
            orderStmt.setDouble(6, order.getVatAmount());
            orderStmt.setDouble(7, order.getTotal());
            orderStmt.setDouble(8, order.getDiscount());
            orderStmt.setBytes(9, order.getInvoiceData());
            
            if (orderStmt.executeUpdate() == 0) {
                throw new SQLException("Creating order failed");
            }
            
            ResultSet keys = orderStmt.getGeneratedKeys();
            if (keys.next()) {
                order.setId(keys.getInt(1));
            }
            
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            for (OrderItem item : order.getItems()) {
                itemStmt.setInt(1, order.getId());
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setDouble(3, item.getQuantity());
                itemStmt.setDouble(4, item.getUnitPrice());
                itemStmt.setDouble(5, item.getSubtotal());
                itemStmt.addBatch();
                
                // Update stock
                updateProductStock(item.getProductId(), -item.getQuantity(), conn);
            }
            itemStmt.executeBatch();
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Create order error: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {}
            }
        }
        return false;
    }

    private void updateProductStock(int productId, double quantityChange, Connection conn) throws SQLException {
        String sql = "UPDATE products SET stock = stock + ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setDouble(1, quantityChange);
        stmt.setInt(2, productId);
        stmt.executeUpdate();
    }

    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_time DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = extractOrder(rs);
                order.setItems(getOrderItems(order.getId()));
                order.setCustomer(getUserById(order.getCustomerId()));
                if (order.getCarrierId() > 0) {
                    order.setCarrier(getUserById(order.getCarrierId()));
                }
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Get orders error: " + e.getMessage());
        }
        return orders;
    }

    public List<Order> getAvailableOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = 'available' ORDER BY order_time";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Order order = extractOrder(rs);
                order.setItems(getOrderItems(order.getId()));
                order.setCustomer(getUserById(order.getCustomerId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Get available orders error: " + e.getMessage());
        }
        return orders;
    }

    public List<Order> getOrdersByCarrier(int carrierId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE carrier_id = ? ORDER BY order_time DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carrierId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = extractOrder(rs);
                order.setItems(getOrderItems(order.getId()));
                order.setCustomer(getUserById(order.getCustomerId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Get carrier orders error: " + e.getMessage());
        }
        return orders;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_time DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Order order = extractOrder(rs);
                order.setItems(getOrderItems(order.getId()));
                order.setCustomer(getUserById(order.getCustomerId()));
                if (order.getCarrierId() > 0) {
                    order.setCarrier(getUserById(order.getCarrierId()));
                }
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("Get all orders error: " + e.getMessage());
        }
        return orders;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, p.name as product_name FROM order_items oi JOIN products p ON oi.product_id = p.id WHERE oi.order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem(
                    rs.getInt("id"),
                    rs.getInt("order_id"),
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getDouble("quantity"),
                    rs.getDouble("unit_price")
                );
                item.setSubtotal(rs.getDouble("subtotal"));
                item.setProduct(getProductById(item.getProductId()));
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Get order items error: " + e.getMessage());
        }
        return items;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update status error: " + e.getMessage());
        }
        return false;
    }

    public boolean assignCarrier(int orderId, int carrierId) {
        String sql = "UPDATE orders SET carrier_id = ?, status = 'in_progress' WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carrierId);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Assign carrier error: " + e.getMessage());
        }
        return false;
    }

    public boolean completeOrder(int orderId) {
        String sql = "UPDATE orders SET status = 'delivered', actual_delivery_time = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, orderId);
            if (stmt.executeUpdate() > 0) {
                // Update customer completed orders
                String updateSql = "UPDATE users SET completed_orders = completed_orders + 1 WHERE id = (SELECT customer_id FROM orders WHERE id = ?)";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, orderId);
                updateStmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Complete order error: " + e.getMessage());
        }
        return false;
    }

    public boolean cancelOrder(int orderId) {
        try (Connection conn = getConnection()) {
            // Restore stock
            List<OrderItem> items = getOrderItems(orderId);
            for (OrderItem item : items) {
                updateProductStock(item.getProductId(), item.getQuantity(), conn);
            }
            
            // Update status
            String sql = "UPDATE orders SET status = 'cancelled' WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Cancel order error: " + e.getMessage());
        }
        return false;
    }

    private Order extractOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setCustomerId(rs.getInt("customer_id"));
        order.setCarrierId(rs.getInt("carrier_id"));
        
        Timestamp orderTime = rs.getTimestamp("order_time");
        if (orderTime != null) order.setOrderTime(orderTime.toLocalDateTime());
        
        Timestamp deliveryTime = rs.getTimestamp("delivery_time");
        if (deliveryTime != null) order.setDeliveryTime(deliveryTime.toLocalDateTime());
        
        Timestamp actualDelivery = rs.getTimestamp("actual_delivery_time");
        if (actualDelivery != null) order.setActualDeliveryTime(actualDelivery.toLocalDateTime());
        
        order.setStatus(rs.getString("status"));
        order.setSubtotal(rs.getDouble("subtotal"));
        order.setVatAmount(rs.getDouble("vat_amount"));
        order.setTotal(rs.getDouble("total"));
        order.setDiscount(rs.getDouble("discount"));
        order.setInvoiceData(rs.getBytes("invoice"));
        
        return order;
    }

    // ========== MESSAGE OPERATIONS ==========

    public boolean sendMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content, is_read, reply_to_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, message.getSenderId());
            stmt.setInt(2, message.getReceiverId());
            stmt.setString(3, message.getContent());
            stmt.setBoolean(4, false);
            stmt.setObject(5, message.getReplyToId());
            
            if (stmt.executeUpdate() > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    message.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Send message error: " + e.getMessage());
        }
        return false;
    }

    public List<Message> getMessagesForUser(int userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE receiver_id = ? OR sender_id = ? ORDER BY sent_time DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Message msg = extractMessage(rs);
                msg.setSender(getUserById(msg.getSenderId()));
                msg.setReceiver(getUserById(msg.getReceiverId()));
                messages.add(msg);
            }
        } catch (SQLException e) {
            System.err.println("Get messages error: " + e.getMessage());
        }
        return messages;
    }

    private Message extractMessage(ResultSet rs) throws SQLException {
        Message msg = new Message();
        msg.setId(rs.getInt("id"));
        msg.setSenderId(rs.getInt("sender_id"));
        msg.setReceiverId(rs.getInt("receiver_id"));
        msg.setContent(rs.getString("content"));
        msg.setRead(rs.getBoolean("is_read"));
        msg.setReplyToId((Integer) rs.getObject("reply_to_id"));
        
        Timestamp sentTime = rs.getTimestamp("sent_time");
        if (sentTime != null) msg.setSentTime(sentTime.toLocalDateTime());
        
        return msg;
    }

    // ========== COUPON OPERATIONS ==========

    public Coupon getCouponByCode(String code) {
        String sql = "SELECT * FROM coupons WHERE code = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractCoupon(rs);
            }
        } catch (SQLException e) {
            System.err.println("Get coupon error: " + e.getMessage());
        }
        return null;
    }

    public List<Coupon> getAllCoupons() {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT * FROM coupons ORDER BY expiry_date";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                coupons.add(extractCoupon(rs));
            }
        } catch (SQLException e) {
            System.err.println("Get coupons error: " + e.getMessage());
        }
        return coupons;
    }

    public boolean createCoupon(Coupon coupon) {
        String sql = "INSERT INTO coupons (code, discount_value, is_percentage, expiry_date, assigned_user_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, coupon.getCode());
            stmt.setDouble(2, coupon.getDiscountValue());
            stmt.setBoolean(3, coupon.isPercentage());
            stmt.setDate(4, coupon.getExpiryDate() != null ? java.sql.Date.valueOf(coupon.getExpiryDate()) : null);
            stmt.setObject(5, coupon.getAssignedUserId());
            
            if (stmt.executeUpdate() > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    coupon.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Create coupon error: " + e.getMessage());
        }
        return false;
    }

    public boolean markCouponUsed(int couponId) {
        String sql = "UPDATE coupons SET is_used = TRUE WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, couponId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Mark coupon error: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteCoupon(int couponId) {
        String sql = "DELETE FROM coupons WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, couponId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Delete coupon error: " + e.getMessage());
        }
        return false;
    }

    private Coupon extractCoupon(ResultSet rs) throws SQLException {
        Coupon coupon = new Coupon();
        coupon.setId(rs.getInt("id"));
        coupon.setCode(rs.getString("code"));
        coupon.setDiscountValue(rs.getDouble("discount_value"));
        coupon.setPercentage(rs.getBoolean("is_percentage"));
        
        java.sql.Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) coupon.setExpiryDate(expiryDate.toLocalDate());
        
        coupon.setAssignedUserId((Integer) rs.getObject("assigned_user_id"));
        coupon.setUsed(rs.getBoolean("is_used"));
        
        return coupon;
    }

    // ========== RATING OPERATIONS ==========

    public boolean addCarrierRating(CarrierRating rating) {
        String sql = "INSERT INTO carrier_ratings (carrier_id, customer_id, order_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, rating.getCarrierId());
            stmt.setInt(2, rating.getCustomerId());
            stmt.setInt(3, rating.getOrderId());
            stmt.setInt(4, rating.getRating());
            stmt.setString(5, rating.getComment());
            
            if (stmt.executeUpdate() > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    rating.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Add rating error: " + e.getMessage());
        }
        return false;
    }

    public List<CarrierRating> getRatingsForCarrier(int carrierId) {
        List<CarrierRating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM carrier_ratings WHERE carrier_id = ? ORDER BY created_at DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carrierId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CarrierRating rating = extractRating(rs);
                rating.setCustomer(getUserById(rating.getCustomerId()));
                ratings.add(rating);
            }
        } catch (SQLException e) {
            System.err.println("Get ratings error: " + e.getMessage());
        }
        return ratings;
    }

    public double getAverageRating(int carrierId) {
        String sql = "SELECT AVG(rating) as avg_rating FROM carrier_ratings WHERE carrier_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carrierId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            System.err.println("Get avg rating error: " + e.getMessage());
        }
        return 0;
    }

    public boolean hasRatedOrder(int orderId) {
        String sql = "SELECT COUNT(*) FROM carrier_ratings WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Check rating error: " + e.getMessage());
        }
        return false;
    }

    private CarrierRating extractRating(ResultSet rs) throws SQLException {
        CarrierRating rating = new CarrierRating();
        rating.setId(rs.getInt("id"));
        rating.setCarrierId(rs.getInt("carrier_id"));
        rating.setCustomerId(rs.getInt("customer_id"));
        rating.setOrderId(rs.getInt("order_id"));
        rating.setRating(rs.getInt("rating"));
        rating.setComment(rs.getString("comment"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) rating.setCreatedAt(createdAt.toLocalDateTime());
        
        return rating;
    }

    // ========== REPORTING ==========

    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total), 0) as total FROM orders WHERE status = 'delivered'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Get revenue error: " + e.getMessage());
        }
        return 0;
    }

    public int getTotalOrders() {
        String sql = "SELECT COUNT(*) as count FROM orders";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Get order count error: " + e.getMessage());
        }
        return 0;
    }
}
