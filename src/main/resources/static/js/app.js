/**
 * eBookStore Application Frontend Script
 * REST API client & State Management
 */

const API_BASE = '/api';

// Application State
const state = {
    user: null,
    token: localStorage.getItem('ebookstore_token') || null,
    books: [],
    categories: [],
    selectedCategory: 'all',
    cart: JSON.parse(localStorage.getItem('ebookstore_cart') || '[]'),
    orders: [],
    activeView: 'catalog',
    currentCancellingOrderId: null
};

// ==========================================
// Initialization
// ==========================================
document.addEventListener('DOMContentLoaded', async () => {
    initTheme();
    initAuth();
    await loadCategories();
    await loadBooks();
    updateCartUI();
});

// ==========================================
// Theme (Light / Dark Mode)
// ==========================================
function initTheme() {
    updateThemeToggleIcon(document.documentElement.getAttribute('data-theme') || 'light');
}

function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme') || 'light';
    const next = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('ebookstore_theme', next);
    updateThemeToggleIcon(next);
}

function updateThemeToggleIcon(theme) {
    const btn = document.getElementById('theme-toggle-btn');
    if (!btn) return;
    btn.textContent = theme === 'dark' ? '☀️' : '🌙';
}

// ==========================================
// Notification / Toast
// ==========================================
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '❌';

    toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(20px)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// ==========================================
// Authentication
// ==========================================
async function initAuth() {
    if (!state.token) {
        renderAuthUI(null);
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/auth/me`, {
            headers: { 'Authorization': `Bearer ${state.token}` }
        });
        const data = await res.json();
        if (data.success && data.data) {
            state.user = data.data;
            renderAuthUI(state.user);
        } else {
            // Token expired or invalid
            handleLogout(false);
        }
    } catch (err) {
        console.error('Auth verification failed:', err);
        renderAuthUI(null);
    }
}

function renderAuthUI(user) {
    const guestControls = document.getElementById('auth-guest-controls');
    const userControls = document.getElementById('auth-user-controls');
    const navOrders = document.getElementById('nav-orders');

    if (user) {
        guestControls.style.display = 'none';
        userControls.style.display = 'flex';
        navOrders.style.display = 'inline-flex';

        document.getElementById('nav-user-avatar').innerText = user.fullName ? user.fullName.charAt(0).toUpperCase() : 'U';
        document.getElementById('nav-user-name').innerText = user.fullName || user.username;
        document.getElementById('nav-user-email').innerText = user.email;
    } else {
        guestControls.style.display = 'flex';
        userControls.style.display = 'none';
        navOrders.style.display = 'none';
    }
}

function openAuthModal(tab = 'login') {
    document.getElementById('modal-auth').style.display = 'flex';
    switchAuthTab(tab);
}

function closeAuthModal() {
    document.getElementById('modal-auth').style.display = 'none';
}

function switchAuthTab(tab) {
    const tabLogin = document.getElementById('tab-login');
    const tabReg = document.getElementById('tab-register');
    const formLogin = document.getElementById('form-login');
    const formReg = document.getElementById('form-register');

    if (tab === 'login') {
        tabLogin.classList.add('active');
        tabReg.classList.remove('active');
        formLogin.style.display = 'block';
        formReg.style.display = 'none';
    } else {
        tabLogin.classList.remove('active');
        tabReg.classList.add('active');
        formLogin.style.display = 'none';
        formReg.style.display = 'block';
    }
}

async function handleLoginSubmit(e) {
    e.preventDefault();
    const identifier = document.getElementById('login-identifier').value.trim();
    const password = document.getElementById('login-password').value;

    const btn = document.getElementById('btn-login-submit');
    btn.disabled = true;
    btn.innerText = 'Signing in...';

    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ identifier, password })
        });
        const data = await res.json();

        if (data.success && data.data) {
            state.token = data.data.token;
            state.user = data.data.user;
            localStorage.setItem('ebookstore_token', state.token);
            renderAuthUI(state.user);
            closeAuthModal();
            showToast(`Welcome back, ${state.user.fullName}!`, 'success');
            
            // If user has orders view selected, load them
            if (state.activeView === 'orders') {
                loadMyOrders();
            }
        } else {
            showToast(data.message || 'Login failed', 'error');
        }
    } catch (err) {
        showToast('Network error during login', 'error');
    } finally {
        btn.disabled = false;
        btn.innerText = 'Sign In';
    }
}

async function handleRegisterSubmit(e) {
    e.preventDefault();
    const fullName = document.getElementById('reg-fullname').value.trim();
    const username = document.getElementById('reg-username').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const phone = document.getElementById('reg-phone').value.trim();
    const password = document.getElementById('reg-password').value;

    const btn = document.getElementById('btn-reg-submit');
    btn.disabled = true;
    btn.innerText = 'Creating account...';

    try {
        const res = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fullName, username, email, phone, password })
        });
        const data = await res.json();

        if (data.success && data.data) {
            state.token = data.data.token;
            state.user = data.data.user;
            localStorage.setItem('ebookstore_token', state.token);
            renderAuthUI(state.user);
            closeAuthModal();
            showToast(`Account created! Welcome, ${state.user.fullName}!`, 'success');
        } else {
            showToast(data.message || 'Registration failed', 'error');
        }
    } catch (err) {
        showToast('Network error during registration', 'error');
    } finally {
        btn.disabled = false;
        btn.innerText = 'Create Account';
    }
}

async function handleLogout(notify = true) {
    if (state.token) {
        try {
            await fetch(`${API_BASE}/auth/logout`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${state.token}` }
            });
        } catch (e) {
            // Ignore error on logout
        }
    }
    state.user = null;
    state.token = null;
    localStorage.removeItem('ebookstore_token');
    renderAuthUI(null);
    navigateTo('catalog');
    if (notify) {
        showToast('You have been signed out', 'info');
    }
}

// ==========================================
// Navigation & Views
// ==========================================
function navigateTo(viewName) {
    state.activeView = viewName;
    const navCatalog = document.getElementById('nav-catalog');
    const navOrders = document.getElementById('nav-orders');
    const viewCatalog = document.getElementById('view-catalog');
    const viewOrders = document.getElementById('view-orders');
    const heroBanner = document.getElementById('hero-banner');

    if (viewName === 'catalog') {
        navCatalog.classList.add('active');
        if (navOrders) navOrders.classList.remove('active');
        viewCatalog.classList.add('active');
        viewOrders.style.display = 'none';
        heroBanner.style.display = 'block';
    } else if (viewName === 'orders') {
        if (!state.user) {
            showToast('Please sign in to view your orders', 'info');
            openAuthModal('login');
            return;
        }
        navCatalog.classList.remove('active');
        if (navOrders) navOrders.classList.add('active');
        viewCatalog.classList.remove('active');
        viewOrders.style.display = 'block';
        heroBanner.style.display = 'none';
        loadMyOrders();
    }
}

function scrollToBooks() {
    document.getElementById('view-catalog').scrollIntoView({ behavior: 'smooth' });
}

function openSpecialOffer() {
    showToast('Enjoy free shipping on all orders and a 48h cancellation refund guarantee!', 'info');
}

// ==========================================
// Book Catalog & Filtering
// ==========================================
async function loadCategories() {
    try {
        const res = await fetch(`${API_BASE}/books/categories`);
        const data = await res.json();
        if (data.success && Array.isArray(data.data)) {
            state.categories = data.data;
            renderCategories();
        }
    } catch (e) {
        console.error('Failed to load categories', e);
    }
}

function renderCategories() {
    const container = document.getElementById('category-pills');
    let html = `<button class="pill ${state.selectedCategory === 'all' ? 'active' : ''}" onclick="filterByCategory('all', this)">All Categories</button>`;
    state.categories.forEach(cat => {
        html += `<button class="pill ${state.selectedCategory === cat ? 'active' : ''}" onclick="filterByCategory('${cat}', this)">${cat}</button>`;
    });
    container.innerHTML = html;
}

async function loadBooks(category = '', search = '') {
    const grid = document.getElementById('books-grid');
    grid.innerHTML = `
        <div class="loading-state">
            <div class="spinner"></div>
            <p>Loading book catalog...</p>
        </div>
    `;

    try {
        let url = `${API_BASE}/books`;
        const params = new URLSearchParams();
        if (category && category !== 'all') params.append('category', category);
        if (search) params.append('search', search);
        if (params.toString()) url += `?${params.toString()}`;

        const res = await fetch(url);
        const data = await res.json();
        if (data.success) {
            state.books = data.data;
            renderBooks(state.books);
        } else {
            grid.innerHTML = `<div class="empty-state"><h3>Unable to load books</h3><p>${data.message}</p></div>`;
        }
    } catch (err) {
        grid.innerHTML = `<div class="empty-state"><h3>Connection Error</h3><p>Could not fetch books from server.</p></div>`;
    }
}

function renderBooks(books) {
    const grid = document.getElementById('books-grid');
    const countLabel = document.getElementById('catalog-count-label');

    if (!books || books.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <h3>📖 No Books Found</h3>
                <p>Try refining your search terms or browse another category.</p>
            </div>
        `;
        countLabel.innerText = '0 books found';
        return;
    }

    countLabel.innerText = `Showing ${books.length} book${books.length > 1 ? 's' : ''}`;

    grid.innerHTML = books.map(book => `
        <div class="book-card">
            <div class="book-cover-container" onclick="openBookDetails('${book.id}')">
                <img src="${book.coverImageUrl || 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=400'}" alt="${book.title}" class="book-cover-img" loading="lazy" />
                <span class="book-category-tag">${book.category}</span>
            </div>
            <div class="book-info">
                <h3 class="book-title" onclick="openBookDetails('${book.id}')">${book.title}</h3>
                <p class="book-author">by ${book.author}</p>
                <p class="book-desc-short">${book.description}</p>
                
                <div class="book-footer">
                    <div>
                        <div class="book-price">$${book.price.toFixed(2)}</div>
                        <div class="stock-indicator ${book.stockQuantity <= 0 ? 'out-of-stock' : ''}">
                            ${book.stockQuantity > 0 ? `In Stock (${book.stockQuantity})` : 'Out of Stock'}
                        </div>
                    </div>
                    <button class="btn btn-primary btn-sm" 
                            ${book.stockQuantity <= 0 ? 'disabled' : ''} 
                            onclick="addToCart('${book.id}')">
                        ${book.stockQuantity <= 0 ? 'Sold Out' : '+ Add to Cart'}
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

function filterByCategory(cat, btnElement) {
    state.selectedCategory = cat;
    document.querySelectorAll('.category-pills .pill').forEach(b => b.classList.remove('active'));
    if (btnElement) btnElement.classList.add('active');
    loadBooks(cat, document.getElementById('global-search-input').value.trim());
}

let searchTimeout;
function handleGlobalSearch(e) {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        executeSearch();
    }, 350);
}

function executeSearch() {
    const q = document.getElementById('global-search-input').value.trim();
    loadBooks(state.selectedCategory, q);
}

function openBookDetails(bookId) {
    const book = state.books.find(b => b.id === bookId);
    if (!book) return;

    const modalBody = document.getElementById('book-detail-body');
    modalBody.innerHTML = `
        <div>
            <img src="${book.coverImageUrl}" alt="${book.title}" class="detail-cover-img" />
        </div>
        <div class="detail-info">
            <span class="book-category-tag" style="position:static; display:inline-block; margin-bottom:0.75rem; width:fit-content;">${book.category}</span>
            <h2 class="detail-title">${book.title}</h2>
            <p class="detail-author">Author: <strong>${book.author}</strong> | ISBN: ${book.isbn}</p>
            <div class="detail-price">$${book.price.toFixed(2)}</div>
            <p class="detail-desc">${book.description}</p>
            
            <div style="margin-top:auto; padding-top:1.5rem; display:flex; align-items:center; gap:1rem;">
                <button class="btn btn-primary btn-lg" ${book.stockQuantity <= 0 ? 'disabled' : ''} onclick="addToCart('${book.id}'); closeBookModal();">
                    🛒 ${book.stockQuantity > 0 ? 'Add to Cart' : 'Currently Unavailable'}
                </button>
                <span class="stock-indicator ${book.stockQuantity <= 0 ? 'out-of-stock' : ''}">
                    ${book.stockQuantity > 0 ? `In Stock (${book.stockQuantity} copies)` : 'Out of Stock'}
                </span>
            </div>
        </div>
    `;

    document.getElementById('modal-book-detail').style.display = 'flex';
}

function closeBookModal() {
    document.getElementById('modal-book-detail').style.display = 'none';
}

// ==========================================
// Cart Management & Checkout
// ==========================================
function addToCart(bookId) {
    const book = state.books.find(b => b.id === bookId);
    if (!book) return;

    const existing = state.cart.find(item => item.bookId === bookId);
    if (existing) {
        if (existing.quantity + 1 > book.stockQuantity) {
            showToast(`Sorry, only ${book.stockQuantity} copies in stock.`, 'error');
            return;
        }
        existing.quantity += 1;
    } else {
        state.cart.push({
            bookId: book.id,
            title: book.title,
            price: book.price,
            quantity: 1,
            maxStock: book.stockQuantity
        });
    }

    saveCart();
    updateCartUI();
    showToast(`Added "${book.title}" to cart!`, 'success');
}

function updateCartQuantity(bookId, delta) {
    const item = state.cart.find(i => i.bookId === bookId);
    if (!item) return;

    item.quantity += delta;
    if (item.quantity <= 0) {
        state.cart = state.cart.filter(i => i.bookId !== bookId);
    } else if (item.quantity > item.maxStock) {
        item.quantity = item.maxStock;
        showToast(`Reached maximum available stock (${item.maxStock})`, 'info');
    }

    saveCart();
    updateCartUI();
    renderCartModalItems();
}

function saveCart() {
    localStorage.setItem('ebookstore_cart', JSON.stringify(state.cart));
}

function updateCartUI() {
    const count = state.cart.reduce((acc, item) => acc + item.quantity, 0);
    document.getElementById('cart-badge').innerText = count;
}

function openCartModal() {
    document.getElementById('modal-cart').style.display = 'flex';
    renderCartModalItems();
}

function closeCartModal() {
    document.getElementById('modal-cart').style.display = 'none';
}

function renderCartModalItems() {
    const list = document.getElementById('cart-items-list');
    const countSpan = document.getElementById('cart-items-count');
    const subtotalVal = document.getElementById('cart-subtotal-val');
    const totalVal = document.getElementById('cart-total-val');

    countSpan.innerText = state.cart.reduce((a, b) => a + b.quantity, 0);

    if (state.cart.length === 0) {
        list.innerHTML = `
            <div class="empty-state" style="padding:1.5rem 0.5rem;">
                <p>Your shopping cart is empty.</p>
                <button class="btn btn-outline-primary btn-sm mt-2" onclick="closeCartModal(); navigateTo('catalog');">Browse Catalog</button>
            </div>
        `;
        subtotalVal.innerText = '$0.00';
        totalVal.innerText = '$0.00';
        document.getElementById('btn-place-order').disabled = true;
        return;
    }

    document.getElementById('btn-place-order').disabled = false;

    let subtotal = 0;
    list.innerHTML = state.cart.map(item => {
        const itemTotal = item.price * item.quantity;
        subtotal += itemTotal;
        return `
            <div class="cart-item-row">
                <div class="cart-item-details">
                    <div class="cart-item-title">${item.title}</div>
                    <div class="cart-item-price">$${item.price.toFixed(2)} each</div>
                </div>
                <div class="qty-control">
                    <button class="qty-btn" onclick="updateCartQuantity('${item.bookId}', -1)">-</button>
                    <span class="qty-display">${item.quantity}</span>
                    <button class="qty-btn" onclick="updateCartQuantity('${item.bookId}', 1)">+</button>
                </div>
                <div style="font-weight:700; font-size:0.95rem; min-width:60px; text-align:right;">
                    $${itemTotal.toFixed(2)}
                </div>
            </div>
        `;
    }).join('');

    subtotalVal.innerText = `$${subtotal.toFixed(2)}`;
    totalVal.innerText = `$${subtotal.toFixed(2)}`;
}

function selectPaymentMethod(method, element) {
    document.querySelectorAll('.payment-method-selector .radio-card').forEach(c => c.classList.remove('active'));
    element.classList.add('active');
    element.querySelector('input').checked = true;

    const cardFields = document.getElementById('card-payment-fields');
    const upiFields = document.getElementById('upi-payment-fields');

    if (method === 'UPI') {
        cardFields.style.display = 'none';
        upiFields.style.display = 'block';
    } else {
        cardFields.style.display = 'block';
        upiFields.style.display = 'none';
    }
}

async function handlePlaceOrder(e) {
    e.preventDefault();

    if (!state.user || !state.token) {
        showToast('Please sign in or register to place your order.', 'info');
        closeCartModal();
        openAuthModal('login');
        return;
    }

    if (state.cart.length === 0) {
        showToast('Your cart is empty', 'error');
        return;
    }

    const street = document.getElementById('addr-street').value.trim();
    const city = document.getElementById('addr-city').value.trim();
    const stateVal = document.getElementById('addr-state').value.trim();
    const zipCode = document.getElementById('addr-zip').value.trim();
    const country = document.getElementById('addr-country').value.trim();

    const selectedPayRadio = document.querySelector('input[name="pay_method"]:checked');
    const paymentMethod = selectedPayRadio ? selectedPayRadio.value : 'CARD';
    const simulateFailure = document.getElementById('simulate-fail-checkbox').checked;

    const payment = {
        paymentMethod: paymentMethod,
        cardHolderName: document.getElementById('card-holder').value.trim() || 'Valued Customer',
        cardNumber: document.getElementById('card-number').value.replace(/\s+/g, '') || '4111222233334444',
        expiryDate: document.getElementById('card-expiry').value.trim() || '12/28',
        cvv: document.getElementById('card-cvv').value.trim() || '123',
        upiId: document.getElementById('upi-id').value.trim(),
        simulateFailure: simulateFailure
    };

    const orderPayload = {
        items: state.cart.map(i => ({ bookId: i.bookId, quantity: i.quantity })),
        deliveryAddress: {
            street,
            city,
            state: stateVal,
            zipCode,
            country
        },
        payment: payment
    };

    const btn = document.getElementById('btn-place-order');
    btn.disabled = true;
    btn.innerText = 'Processing Mock Payment...';

    try {
        const res = await fetch(`${API_BASE}/orders`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${state.token}`
            },
            body: JSON.stringify(orderPayload)
        });

        const data = await res.json();
        if (data.success && data.data) {
            showToast('🎉 Order placed successfully! Check your orders.', 'success');
            state.cart = [];
            saveCart();
            updateCartUI();
            closeCartModal();
            // Refresh book catalog to reflect new stock counts
            await loadBooks(state.selectedCategory);
            // Navigate to orders view
            navigateTo('orders');
        } else {
            showToast(data.message || 'Payment or Order placement failed', 'error');
        }
    } catch (err) {
        showToast('Network error while placing order', 'error');
    } finally {
        btn.disabled = false;
        btn.innerText = 'Pay & Place Order';
    }
}

// ==========================================
// Order Management & 48h Cancellation
// ==========================================
async function loadMyOrders() {
    if (!state.user || !state.token) return;

    const container = document.getElementById('orders-list-container');
    container.innerHTML = `
        <div class="loading-state">
            <div class="spinner"></div>
            <p>Fetching your order history...</p>
        </div>
    `;

    try {
        const res = await fetch(`${API_BASE}/orders`, {
            headers: { 'Authorization': `Bearer ${state.token}` }
        });
        const data = await res.json();

        if (data.success) {
            state.orders = data.data;
            renderOrders(state.orders);
        } else {
            container.innerHTML = `<div class="empty-state"><h3>Failed to load orders</h3><p>${data.message}</p></div>`;
        }
    } catch (err) {
        container.innerHTML = `<div class="empty-state"><h3>Error</h3><p>Could not load order history.</p></div>`;
    }
}

function renderOrders(orders) {
    const container = document.getElementById('orders-list-container');

    if (!orders || orders.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <h3>📦 No Orders Placed Yet</h3>
                <p>Browse our catalog and pick your favorite books!</p>
                <button class="btn btn-primary mt-3" onclick="navigateTo('catalog')">Explore Catalog</button>
            </div>
        `;
        return;
    }

    container.innerHTML = orders.map(order => {
        const orderTime = new Date(order.orderDate);
        const deadlineTime = new Date(order.cancellationDeadline);
        const now = new Date();
        const isCancellable = (order.status === 'CONFIRMED' || order.status === 'PROCESSING') && now < deadlineTime;

        // Calculate hours remaining
        let timeRemainingText = '';
        if (isCancellable) {
            const diffMs = deadlineTime - now;
            const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
            const diffMins = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
            timeRemainingText = `⏳ Cancellation Window: ${diffHours}h ${diffMins}m remaining`;
        } else if (order.status === 'CANCELLED') {
            timeRemainingText = `❌ Cancelled on ${new Date(order.cancelledAt || order.orderDate).toLocaleDateString()}`;
        } else {
            timeRemainingText = `🔒 48-Hour cancellation period has ended`;
        }

        const addr = order.deliveryAddress;
        const addrStr = `${addr.street}, ${addr.city}, ${addr.state} ${addr.zipCode}, ${addr.country}`;
        const pay = order.paymentDetails;

        return `
            <div class="order-card">
                <div class="order-header">
                    <div class="order-header-info">
                        <div class="order-meta-item">
                            <span class="meta-label">Order Reference</span>
                            <span class="meta-value">${order.id}</span>
                        </div>
                        <div class="order-meta-item">
                            <span class="meta-label">Date Placed</span>
                            <span class="meta-value">${orderTime.toLocaleDateString()} ${orderTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                        </div>
                        <div class="order-meta-item">
                            <span class="meta-label">Total Amount</span>
                            <span class="meta-value text-success">$${order.totalAmount.toFixed(2)}</span>
                        </div>
                    </div>
                    <div>
                        <span class="order-status-badge status-${order.status.toLowerCase()}">${order.status}</span>
                    </div>
                </div>

                <div class="order-body">
                    <div>
                        <table class="order-items-table">
                            <thead>
                                <tr>
                                    <th>Book</th>
                                    <th>Qty</th>
                                    <th>Unit Price</th>
                                    <th>Subtotal</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${order.items.map(item => `
                                    <tr>
                                        <td>
                                            <strong>${item.bookTitle}</strong><br />
                                            <span class="text-muted" style="font-size:0.8rem;">by ${item.bookAuthor}</span>
                                        </td>
                                        <td>${item.quantity}</td>
                                        <td>$${item.unitPrice.toFixed(2)}</td>
                                        <td><strong>$${item.subtotal.toFixed(2)}</strong></td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>

                    <div class="order-delivery-box">
                        <h4 style="font-size:0.85rem; text-transform:uppercase; color:var(--text-secondary); margin-bottom:0.5rem;">📍 Delivery Info</h4>
                        <p style="font-size:0.875rem; margin-bottom:1rem;">${addrStr}</p>
                        
                        <h4 style="font-size:0.85rem; text-transform:uppercase; color:var(--text-secondary); margin-bottom:0.5rem;">💳 Mock Payment</h4>
                        <p style="font-size:0.875rem;">
                            Method: <strong>${pay.paymentMethod}</strong><br />
                            Account: <code>${pay.maskedCardNumber}</code><br />
                            Status: <strong class="${pay.paymentStatus === 'REFUNDED' ? 'text-danger' : 'text-success'}">${pay.paymentStatus}</strong><br />
                            Txn ID: <code style="font-size:0.75rem;">${pay.transactionId}</code>
                        </p>
                        ${order.cancellationReason ? `<p style="margin-top:0.75rem; font-size:0.8rem; color:var(--danger);"><strong>Cancellation Note:</strong> ${order.cancellationReason}</p>` : ''}
                    </div>
                </div>

                <div class="order-footer">
                    <div class="cancellation-countdown ${!isCancellable ? 'expired' : ''}">
                        ${timeRemainingText}
                    </div>
                    <div>
                        ${isCancellable ? `
                            <button class="btn btn-outline-danger btn-sm" onclick="openCancelModal('${order.id}')">
                                🚫 Cancel Order (48h Policy)
                            </button>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function openCancelModal(orderId) {
    state.currentCancellingOrderId = orderId;
    document.getElementById('cancel-modal-order-id').innerText = '#' + orderId;
    document.getElementById('modal-cancel-order').style.display = 'flex';
}

function closeCancelModal() {
    state.currentCancellingOrderId = null;
    document.getElementById('modal-cancel-order').style.display = 'none';
}

async function confirmCancelOrder() {
    if (!state.currentCancellingOrderId || !state.token) return;

    const reason = document.getElementById('cancel-reason-select').value;
    const btn = document.getElementById('btn-confirm-cancel');
    btn.disabled = true;
    btn.innerText = 'Cancelling...';

    try {
        const res = await fetch(`${API_BASE}/orders/${state.currentCancellingOrderId}/cancel`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${state.token}`
            },
            body: JSON.stringify({ reason })
        });

        const data = await res.json();
        if (data.success) {
            showToast('Order cancelled successfully! Stock updated and refund issued.', 'success');
            closeCancelModal();
            await loadMyOrders();
            // Also refresh books in catalog so restocked items are visible
            await loadBooks(state.selectedCategory);
        } else {
            showToast(data.message || 'Failed to cancel order', 'error');
        }
    } catch (err) {
        showToast('Network error while cancelling order', 'error');
    } finally {
        btn.disabled = false;
        btn.innerText = 'Confirm Cancellation';
    }
}
