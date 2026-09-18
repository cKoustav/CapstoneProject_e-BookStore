# 📚 eBookStore eCommerce Application

A full-stack eCommerce Bookstore application built with **Java 17+**, **Spring Boot 3**, **RESTful APIs**, **File-based JSON Persistence** (no external database required), and a **Modern Responsive HTML5/CSS3/Vanilla JS Frontend**.

---

## 🌟 Key Features

1. **User Authentication & Profiles**:
   - User Registration & Login with SHA-256 password hashing.
   - Session authentication using Bearer tokens.
2. **Book Catalog & Discovery**:
   - Browse catalog with book details, covers, prices, and live stock tracking.
   - Filter by categories (Programming, Architecture, Sci-Fi, Fiction, etc.).
   - Instant real-time search by title, author, category, or ISBN.
3. **Shopping Cart & Checkout**:
   - Add/remove items with quantity controls.
   - Capture detailed delivery address (Street, City, State, Zip, Country).
   - Mock Payment Gateway supporting Credit/Debit Card, UPI, and Net Banking.
   - Option to simulate bank payment decline/failure for validation testing.
4. **Order Management & 48-Hour Cancellation Rule**:
   - Real-time order placement with automatic stock deduction and transaction tracking.
   - View complete order history and detailed breakdowns.
   - **Order cancellation strictly permitted within 48 hours** from order placement time.
   - Automatic restock and refund status update upon cancellation.
5. **Pure File-based Storage**:
   - All data (Users, Books, Orders) is persisted in local JSON files in the `data/` directory (`users.json`, `books.json`, `orders.json`) with thread-safe file read/write locks.

---

## 🛠️ Technology Stack

- **Backend**: Java 17+, Spring Boot 3.3.4 (Spring Web, Spring Validation, Jackson JSR-310)
- **Persistence**: File Repository (`data/*.json` with Jackson ObjectMapper & ReentrantReadWriteLock)
- **Frontend**: HTML5, Modern CSS3 (CSS Variables, Flexbox, CSS Grid), Vanilla JavaScript (ES6+ Fetch API)
- **Testing**: JUnit 5, Spring Boot Test

---

## 🚀 How to Run the Application

### Prerequisites
- JDK 17 or newer
- Apache Maven 3.8+

### Steps:
1. **Build and test the project**:
   ```bash
   mvn clean test
   ```
2. **Run the Spring Boot application**:
   ```bash
   mvn spring-boot:run
   ```
3. **Open the Web Browser**:
   Navigate to [http://localhost:8080](http://localhost:8080)

---

## 📡 REST API Documentation

### 1. Authentication Endpoints (`/api/auth`)
- `POST /api/auth/register` — Register a new account (`{ username, email, password, fullName, phone }`)
- `POST /api/auth/login` — Sign in (`{ identifier, password }`)
- `GET /api/auth/me` — Get profile for authenticated token (`Header: Authorization: Bearer <token>`)
- `POST /api/auth/logout` — Invalidate session token

### 2. Books Catalog Endpoints (`/api/books`)
- `GET /api/books` — Retrieve all books (Query params: `?category=Programming&search=Clean`)
- `GET /api/books/{id}` — Get single book by ID
- `GET /api/books/categories` — Get list of distinct book categories

### 3. Orders Endpoints (`/api/orders`)
- `POST /api/orders` — Place order with delivery address & mock payment payload
- `GET /api/orders` — List current user's orders
- `GET /api/orders/{id}` — Get specific order details
- `POST /api/orders/{id}/cancel` — Cancel order within 48 hours (`{ reason: "..." }`)

---

## 🔒 48-Hour Cancellation Guarantee Logic

When an order is created, the system calculates `cancellationDeadline = orderDate + 48 hours`.
- If `now <= cancellationDeadline`, cancellation is processed:
  - Order status is marked as `CANCELLED`.
  - Ordered items are restored to the bookstore inventory (`stockQuantity` refunded).
  - Mock payment transaction is marked as `REFUNDED`.
- If `now > cancellationDeadline`, cancellation is rejected with `IllegalStateException` / 409 Conflict explaining the expiration window.
