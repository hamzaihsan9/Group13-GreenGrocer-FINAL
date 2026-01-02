# CMPE 343 – Course Project #3 (Fall 2025–2026)
## Group 13 – Local Greengrocer Application

This repository contains a desktop application developed for **CMPE 343 Course Project #3**.
The application simulates a **local greengrocer system** and is implemented using **JavaFX**
for the graphical user interface and **MySQL (JDBC)** for database operations.

The system welcomes the user with a **Login** interface and routes authenticated users to
**role-based interfaces**:
- Customer
- Carrier
- Owner

---

## Technologies Used
- Java: 21
- JavaFX: 21.0.1
- Database: MySQL
- Database Access: JDBC (mysql-connector-j 8.2.0)
- Build Tool: Maven
- UI Design: SceneBuilder (.fxml)
- Documentation: JavaDoc
- Version Control: Git & GitHub

---

## Application Summary
- Initial window size: 960 × 540 pixels
- Window position: Centered on the screen
- Resizable: Yes
- Main class: com.group13.greengrocer.Main
- Initial scene: Login screen (Login.fxml)
- Stage title format: Group13 GreenGrocer

---

## Repository / Project Structure

GROUP13/
├─ sql/
│  └─ schema.sql
├─ src/main/
│  ├─ java/com/group13/greengrocer/
│  │  ├─ controllers/
│  │  ├─ database/
│  │  ├─ models/
│  │  ├─ utils/
│  │  └─ Main.java
│  └─ resources/
│     ├─ css/
│     └─ fxml/
├─ target/
│  ├─ classes/
│  │  ├─ com/group13/greengrocer/
│  │  ├─ css/
│  │  └─ fxml/
│  └─ maven-status/maven-compiler-plugin/
│     ├─ createdFiles.lst
│     └─ inputFiles.lst
├─ mvnw
├─ mvnw.cmd
├─ pom.xml
└─ README.md


---

## User Interfaces (FXML Files)
FXML files are located under src/main/resources/fxml/:
- Login.fxml
- Register.fxml
- Profile.fxml
- Customer.fxml
- Cart.fxml
- Carrier.fxml
- Owner.fxml

Each interface is controlled by a corresponding controller class located in the controllers package.

---

## Database Setup

### Schema and Sample Data
Before running the application, execute the SQL script:
sql/schema.sql

This script:
- Creates the greengrocer database
- Creates all required tables
- Inserts sample data for testing

### Database Connection
All database operations are centralized in the DatabaseAdapter class:
src/main/java/com/group13/greengrocer/database/DatabaseAdapter.java

Local development configuration:
- URL: jdbc:mysql://localhost:3306/greengrocer
- Username: root
- Password: Darkshadow_007

Note: Database credentials can be modified in DatabaseAdapter.java if required by the course environment.

---

## How to Run the Project

### Option 1 — Using Maven (Recommended)
From the project root directory:
mvn clean javafx:run

### Option 2 — Using an IDE
1. Open the project as a Maven project (IntelliJ IDEA or Eclipse).
2. Select JDK 21.
3. Run the main class:
   com.group13.greengrocer.Main

---

## Default Test Users
Owner: admin / admin
Carrier: ahmi / 1234
Customer: ahmadzubi / admin

---

## Functional Features

Customer:
- User authentication and registration
- Profile update (name, email, phone, address, password)
- Viewing available fruits and vegetables
- Products retrieved from the database and sorted by name
- Adding products to a shopping cart with kilogram-based quantities
- Creating orders and selecting a delivery time
- Viewing order history and delivery status
- Cancelling orders (product stock is restored)
- Sending messages to the owner
- Rating carriers after order completion

Carrier:
- Viewing available orders
- Assigning orders to self
- Viewing current and completed deliveries
- Completing deliveries by updating order status and delivery time

Owner:
- Adding, updating, and deleting products
- Managing product prices, stock, and threshold values
- Managing discount coupons
- Viewing all orders in the system
- Reading and replying to customer messages
- Viewing carrier ratings
- Viewing system reports such as total revenue and total order count

---

## Database Design Notes
- Product images are stored in the database as **BLOB** data (`products.image` – **LONGBLOB**).
- Invoice data is stored in the database as **BLOB** data (`orders.invoice` – **LONGBLOB**).
- Orders are stored using a normalized structure:
  - `orders`
  - `order_items` (linked by `order_items.order_id`, with `ON DELETE CASCADE`)
- Order creation is executed inside a **database transaction** to ensure consistency (order insert + item inserts + stock updates are committed together).
- Product stock is decreased during order creation and restored during order cancellation.


---

## Design and Implementation Notes
- The application follows object-oriented design principles.
- Controllers, models, and database logic are clearly separated.
- All database operations are centralized in a database-adapter class.
- Multiple event handlers are implemented across different user interfaces.
- Input validations are applied to prevent logical and runtime errors.

