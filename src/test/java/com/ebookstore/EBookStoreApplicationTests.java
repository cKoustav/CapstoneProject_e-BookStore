package com.ebookstore;

import com.ebookstore.dto.CancelOrderRequest;
import com.ebookstore.dto.CreateOrderRequest;
import com.ebookstore.dto.MockPaymentRequest;
import com.ebookstore.dto.OrderItemRequest;
import com.ebookstore.dto.RegisterRequest;
import com.ebookstore.model.Address;
import com.ebookstore.model.Book;
import com.ebookstore.model.Order;
import com.ebookstore.model.OrderStatus;
import com.ebookstore.model.User;
import com.ebookstore.repository.BookRepository;
import com.ebookstore.repository.OrderRepository;
import com.ebookstore.repository.UserRepository;
import com.ebookstore.service.AuthService;
import com.ebookstore.service.BookService;
import com.ebookstore.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EBookStoreApplicationTests {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthService authService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setup() {
        bookService.seedInitialBooks();
    }

    @Test
    void testUserRegistrationAndLogin() {
        String testUser = "testuser_" + System.currentTimeMillis();
        String testEmail = testUser + "@example.com";

        RegisterRequest regReq = new RegisterRequest(testUser, testEmail, "secret123", "Test User", "1234567890");
        var regResponse = authService.register(regReq);
        assertNotNull(regResponse.getToken());
        assertEquals(testUser, regResponse.getUser().getUsername());

        // Verify lookup by token
        var userOpt = authService.getUserByToken(regResponse.getToken());
        assertTrue(userOpt.isPresent());
        assertEquals(testEmail, userOpt.get().getEmail());
    }

    @Test
    void testBookCatalogAndSearch() {
        List<Book> books = bookService.getAllBooks();
        assertFalse(books.isEmpty(), "Books catalog should not be empty");

        List<Book> searchResults = bookService.searchBooks("Clean Code");
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.get(0).getTitle().contains("Clean Code"));
    }

    @Test
    void testOrderPlacementAndStockDeduction() {
        // Register buyer
        String testUser = "buyer_" + System.currentTimeMillis();
        String testEmail = testUser + "@example.com";
        var reg = authService.register(new RegisterRequest(testUser, testEmail, "pass1234", "Buyer Name", "9876543210"));
        User user = authService.getUserByToken(reg.getToken()).orElseThrow();

        Book book = bookService.getAllBooks().get(0);
        int initialStock = book.getStockQuantity();

        Address address = new Address("456 Elm St", "Austin", "TX", "78701", "USA");
        MockPaymentRequest payment = new MockPaymentRequest("CARD", "Buyer Name", "4111222233334444", "12/28", "123");
        CreateOrderRequest orderReq = new CreateOrderRequest(
                List.of(new OrderItemRequest(book.getId(), 2)),
                address,
                payment
        );

        Order order = orderService.createOrder(user, orderReq);
        assertNotNull(order.getId());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals(2, order.getItems().get(0).getQuantity());

        // Verify stock deducted
        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertEquals(initialStock - 2, updatedBook.getStockQuantity());
    }

    @Test
    void testOrderCancellationWithin48Hours() {
        // Register buyer
        String testUser = "canceller_" + System.currentTimeMillis();
        String testEmail = testUser + "@example.com";
        var reg = authService.register(new RegisterRequest(testUser, testEmail, "pass1234", "Cancel User", "9876543210"));
        User user = authService.getUserByToken(reg.getToken()).orElseThrow();

        Book book = bookService.getAllBooks().get(0);
        int initialStock = book.getStockQuantity();

        Address address = new Address("789 Pine Rd", "Seattle", "WA", "98101", "USA");
        MockPaymentRequest payment = new MockPaymentRequest("UPI", "Cancel User", "", "", "");
        payment.setUpiId("cancel@upi");

        CreateOrderRequest orderReq = new CreateOrderRequest(
                List.of(new OrderItemRequest(book.getId(), 1)),
                address,
                payment
        );

        Order order = orderService.createOrder(user, orderReq);
        assertEquals(initialStock - 1, bookRepository.findById(book.getId()).get().getStockQuantity());

        // Cancel within 48h
        Order cancelled = orderService.cancelOrder(order.getId(), user.getId(), new CancelOrderRequest("Changed my mind"));
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        assertEquals("REFUNDED", cancelled.getPaymentDetails().getPaymentStatus());

        // Verify stock restored
        assertEquals(initialStock, bookRepository.findById(book.getId()).get().getStockQuantity());
    }

    @Test
    void testOrderCancellationAfter48HoursFails() {
        // Register buyer
        String testUser = "expired_" + System.currentTimeMillis();
        String testEmail = testUser + "@example.com";
        var reg = authService.register(new RegisterRequest(testUser, testEmail, "pass1234", "Expired User", "9876543210"));
        User user = authService.getUserByToken(reg.getToken()).orElseThrow();

        Book book = bookService.getAllBooks().get(0);

        Address address = new Address("101 High St", "Denver", "CO", "80201", "USA");
        MockPaymentRequest payment = new MockPaymentRequest("CARD", "Expired User", "4111222233334444", "12/28", "123");

        CreateOrderRequest orderReq = new CreateOrderRequest(
                List.of(new OrderItemRequest(book.getId(), 1)),
                address,
                payment
        );

        Order order = orderService.createOrder(user, orderReq);

        // Manually tamper deadline to 3 days ago in file to simulate 48+ hours elapsed
        order.setOrderDate(LocalDateTime.now().minusHours(50));
        order.setCancellationDeadline(LocalDateTime.now().minusHours(2));
        orderRepository.save(order);

        // Attempting to cancel must throw IllegalStateException
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            orderService.cancelOrder(order.getId(), user.getId(), new CancelOrderRequest("Late cancel"));
        });

        assertTrue(ex.getMessage().contains("48-hour cancellation window has expired"));
    }
}
