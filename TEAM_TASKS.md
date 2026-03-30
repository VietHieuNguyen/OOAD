# Phân chia công việc Đồ án Thiết kế Phần mềm Hướng đối tượng (Hệ thống BookStore)

Tài liệu này dùng để tracking tiến độ dự án, ghi chú rõ ràng các phần đã hoàn thiện và quy hoạch vùng scope code cho 3 thành viên để **đảm bảo làm việc độc lập, không bị conflict code**.

---

## 1. Thành viên 1: Người phụ trách Checkout & Payment (ĐÃ HOÀN THÀNH 99%)
**Nhiệm vụ:** Xử lý luồng chọn địa chỉ, thanh toán đơn hàng và xem lại đơn hàng.
**Design Pattern chính:** Strategy Pattern & Builder Pattern.

**Tiến độ - Các phần ĐÃ LÀM XONG:**
*   **Strategy Pattern (Thanh toán):** Đã tạo interface `PaymentStrategy` và các ConcreteStrategy là `CODStrategy`, `BankTransferStrategy`. Đã hoàn tất Context `PaymentService`. Code rất chuẩn OOP, không dính dáng framework "magic", dễ dàng chém gió với thầy cô.
*   **Builder Pattern (Tạo Đơn Hàng):** Nằm gọn bên trong `Order.java` (`Order.Builder`). Đã xử lý logic chuyển đổi `CartItem` -> `OrderItem` dạng snapshot giá, tính toán `TotalAmount`.
*   **MVC Layer:**
    *   Đã viết `OrderService`, `CartService` (chuẩn bị sườn minimal để test độc lập).
    *   Đã viết `CheckoutController`, `OrderController`.
*   **UI (Templates Front-end):** Đã code đầy đủ giao diện, CSS ở `/checkout`, `/order-success/{id}`, `/orders`, `/orders/{id}`.

*(Lưu ý: Hai bạn còn lại **không thay đổi** file `Order.java`, thư mục `service/payment` và các templates `/orders` để tránh conflict tiến độ, đã setup luồng khá chặt chẽ rồi).*

### ĐÃ CẬP NHẬT THÊM (30/03/2026):
*   **Admin CRUD - Quản lý Sách (Manage Books):** Hoàn thành toàn bộ CRUD cho sách trên Admin panel.
    *   `AdminBookController.java` — Controller đầy đủ (List, Create, Edit, Save, Delete) với JavaDoc.
    *   `BookService.java` — Viết lại hoàn chỉnh với `save()`, `deleteById()`, `updateStock()`, `searchByTitle()`, `isbnExists()`.
    *   `BookRepository.java` — Thêm `existsByIsbn()`, `findByTitleContainingIgnoreCase()`, `findByCategoryId()`.
    *   Templates: `admin/books.html` (danh sách + search), `admin/book-form.html` (form thêm/sửa 2 cột giống mockup).
*   **Admin CRUD - Quản lý Danh mục (Manage Categories):** Hoàn thành toàn bộ CRUD cho danh mục.
    *   `CategoryService.java` — Service CRUD + ràng buộc xóa (không cho xóa Category còn sách bên trong).
    *   `AdminCategoryController.java` — Controller đầy đủ (List, Detail, Save, Delete) với RedirectAttributes flash messages.
    *   Templates:
        *   `admin/categories.html` — Danh sách danh mục + **Bootstrap 5 Modal** thêm/sửa (styled theo admin theme).
        *   `admin/category-detail.html` — **Trang chi tiết danh mục**: hiển thị thông tin Category (editable inline) + bảng sách thuộc danh mục đó, click vào sách → chuyển sang trang Edit Book.
*   **Sidebar Admin:** Cập nhật thêm 2 menu: "Inventory" → `/admin/books`, "Categories" → `/admin/categories`.
*   **CSS:** Thêm ~500 dòng CSS cho toàn bộ trang Books, Categories, Book Form, Category Detail, Modal.
*   **Sửa lỗi Checkout:** Fix lỗi `SpelParseException` trên `checkout.html` (đẩy logic tính toán `subTotal` về Java Controller thay vì dùng SpEL Lambda phức tạp trong HTML). Fix lỗi `BigDecimal * Integer` trong `CheckoutController`.

---

## 2. Thành viên 2: Người phụ trách Cart & Tính Giá (ĐÃ HOÀN THÀNH 95%)
**Nhiệm vụ:** Hoàn thiện luồng thêm vào giỏ hàng, áp dụng giảm giá, và giao diện các trang sản phẩm.
**Design Pattern chính:** Decorator Pattern.

**Tiến độ - Các phần ĐÃ LÀM XONG:**
*   **Decorator Pattern (Tính giá giỏ hàng):** Đã tạo interface `CartPricer` (Component), class `BaseCartPricer` (Concrete Component), abstract class `CartPricerDecorator` (Base Decorator), và 2 Concrete Decorator: `GiftWrapDecorator` (+20,000₫/item), `VoucherDiscountDecorator` (giảm % với cap tối đa). Package: `pattern/decorator/`.
*   **Entity Voucher (Bảng mã giảm giá):** Tạo entity `Voucher` với các trường `code`, `discountPercentage`, `maxDiscountAmount`, `expirationDate`, `usageLimit`, `usedCount`, `isActive`. Có method `isValid()` kiểm tra hợp lệ.
*   **Entity Wishlist (Bảng yêu thích):** Tạo entity `Wishlist` liên kết `Customer ↔ Book` với unique constraint, lưu `addedAt`. Hiển thị trên trang Profile.
*   **Cập nhật Entity:**
    *   `Cart.java`: Thêm `appliedVoucher` (@ManyToOne → Voucher), `giftWrap` (Boolean).
    *   `Book.java`: Thêm `thumbnail` (String) cho ảnh bìa sách.
    *   `Category.java`: Thêm `imageUrl` (String) cho ảnh danh mục.
*   **MVC Layer (tuân thủ nghiêm ngặt Controller → Service → Repository):**
    *   `BookService`, `CartService` (mở rộng: `addCartItem`, `removeCartItem`, `updateQuantity`, `applyVoucher`, `removeVoucher`, `toggleGiftWrap`, `calculatePriceBreakdown`), `WishlistService`.
    *   `CollectionController` (GET /collections), `BookDetailController` (GET /books/{id}), `CartController` (GET /cart, POST /cart/add, /cart/remove, /cart/update, /cart/apply-voucher, /cart/remove-voucher, /cart/toggle-gift-wrap).
    *   Cập nhật `ProfileController` (lấy wishlist từ DB), `HomeController` (inject books + categories).
*   **UI (Templates Front-end):**
    *   `collections.html`: Trang danh sách sách + sidebar filter + phân trang + sort.
    *   `book-detail.html`: Trang chi tiết sách + Add to Cart + Add to Wishlist.
    *   `cart.html`: Trang giỏ hàng + voucher input + gift wrap toggle + price breakdown (hiển thị Decorator).
    *   `profile.html`: Cập nhật section Wishlist lấy dữ liệu thực từ DB, có nút xóa và add to cart.
    *   Tất cả icon sử dụng SVG (heart, gift, trash, cart) thay vì emoji.
*   **CSS:** +780 dòng CSS cho Collections, Book Detail, Cart, Wishlist theo design system vintage scholarly.
*   **Security:** Thêm `/collections`, `/books/**`, `/.well-known/**` vào permitAll.
*   **Login Flow:** Sửa redirect sau login → về trang chủ `/` thay vì `/profile`.

*(Lưu ý: Bạn **không thay đổi** file `Order.java`, thư mục `service/payment`, và templates `/orders`, `/checkout` để tránh conflict. Các entity mới đã set `ddl-auto=update` để tự tạo bảng.)*

---

## 3. Thành viên 3: Người phụ trách Notification & Stock Observer (CẦN LÀM)
**Nhiệm vụ:** Xử lý gửi thông báo khi trạng thái đơn hàng thay đổi VÀ khi tồn kho (stock) thay đổi. Xử lý luồng Admin duyệt đơn.
**Design Pattern chính:** Observer Pattern.

### Tình hình hiện tại (Chuẩn bị sẵn cho bạn):

Thành viên 1 đã code sẵn **toàn bộ hệ thống Admin CRUD (Sách + Danh mục)** và đặt sẵn **các hook point (điểm chèn code)** trong Service layer để bạn chỉ cần gắn Observer vào mà **KHÔNG CẦN sửa Controller hay Template**.

#### Các điểm chèn Observer đã chuẩn bị sẵn:

**File: `BookService.java`**
```java
// Điểm chèn 1: Sau khi lưu sách (thêm/sửa) thành công
public Book save(Book book) {
    Book saved = bookRepository.save(book);
    // TODO [Observer]: Gọi notifyObservers(saved) tại đây
    return saved;
}

// Điểm chèn 2: Sau khi cập nhật số lượng tồn kho
public void updateStock(String bookId, int newStock) {
    book.setStockQuantity(newStock);
    bookRepository.save(book);
    // TODO [Observer]: Gọi notifyObservers(book) tại đây khi kết nối Observer Pattern
}
```

#### Quy hoạch scope công việc CHI TIẾT:

**A. Observer Pattern - Stock Change (Thay đổi tồn kho):**
1. Tạo interface `StockObserver` trong package `pattern/observer/`.
2. Tạo `StockSubject` (interface Subject) với `addObserver()`, `removeObserver()`, `notifyObservers()`.
3. Tạo các Concrete Observers:
   - `LowStockAlertObserver` — Log cảnh báo khi stock < 5 (VD: `System.out.println` hoặc `Logger.warn`).
   - `OutOfStockObserver` — Log khi stock = 0.
   - (Tùy chọn) `EmailStockAlertObserver` — Giả lập gửi email (chỉ cần print ra console).
4. Gắn Observer vào `BookService.updateStock()` và `BookService.save()` — tại các dòng `TODO` đã đánh dấu sẵn.

**B. Observer Pattern - Order Status (Trạng thái đơn hàng):**
1. Tạo interface `OrderObserver` trong package `pattern/observer/`.
2. Tạo `OrderStatusSubject`.
3. Tạo các Concrete Observers:
   - `EmailNotifierObserver` — Log thông báo khi đơn hàng chuyển trạng thái (PENDING → CONFIRMED → SHIPPED → DELIVERED).
   - `SMSNotifierObserver` — Log SMS giả lập.
   - `InAppNotificationObserver` — Lưu thông báo vào DB (nếu muốn nâng cao).
4. Gắn Observer vào `OrderService` khi admin duyệt/chuyển trạng thái đơn hàng.

**C. Admin Order Management (Quản lý đơn hàng Admin):**
1. Tạo `AdminOrderController` — Controller CRUD cho đơn hàng admin (`/admin/orders`).
2. Tạo template `admin/orders.html` — Danh sách đơn hàng.
3. Tạo template `admin/order-detail.html` — Chi tiết đơn + nút chuyển trạng thái.
4. **LƯU Ý:** Giao diện admin đã có sẵn layout, sidebar, CSS design system (cream/green tones, Cormorant Garamond serif font, Manrope sans-serif). Bạn chỉ cần `layout:decorate="~{admin/layout}"` và dùng các class CSS đã có (`dashboard-panel`, `inventory-table`, `btn-publish`, `flash-msg`, `panel-kicker`...). Tham khảo file `admin/books.html` hoặc `admin/categories.html` để copy cấu trúc HTML.

### Danh sách file KHÔNG ĐƯỢC SỬA (tránh conflict):

| File | Thuộc về |
|------|----------|
| `Order.java`, `OrderItem.java` | Thành viên 1 |
| `service/payment/*` | Thành viên 1 |
| `CheckoutController.java`, `OrderController.java` | Thành viên 1 |
| `templates/client/checkout.html`, `order-success.html`, `orders.html`, `order-detail.html` | Thành viên 1 |
| `pattern/decorator/*` | Thành viên 2 |
| `CartController.java`, `CollectionController.java`, `BookDetailController.java` | Thành viên 2 |
| `templates/client/collections.html`, `book-detail.html`, `cart.html` | Thành viên 2 |

### Danh sách file BẠN CÓ THỂ SỬA:

| File | Lý do |
|------|-------|
| `BookService.java` | Chèn `notifyObservers()` vào các dòng TODO đã đánh dấu |
| `OrderService.java` | Thêm logic chuyển trạng thái đơn hàng + gọi Observer |
| `admin/fragments/sidebar.html` | Thêm link "Orders" vào sidebar admin |
| `admin.css` | Thêm CSS cho các trang admin orders |

### Danh sách file BẠN TẠO MỚI:

| File | Mô tả |
|------|-------|
| `pattern/observer/StockObserver.java` | Interface Observer cho tồn kho |
| `pattern/observer/StockSubject.java` | Interface Subject cho tồn kho |
| `pattern/observer/LowStockAlertObserver.java` | Concrete Observer: cảnh báo sắp hết hàng |
| `pattern/observer/OutOfStockObserver.java` | Concrete Observer: hết hàng |
| `pattern/observer/OrderObserver.java` | Interface Observer cho đơn hàng |
| `pattern/observer/OrderStatusSubject.java` | Interface Subject cho đơn hàng |
| `pattern/observer/EmailNotifierObserver.java` | Concrete Observer: thông báo email |
| `pattern/observer/SMSNotifierObserver.java` | Concrete Observer: thông báo SMS |
| `controller/AdminOrderController.java` | Controller quản lý đơn hàng admin |
| `templates/admin/orders.html` | Danh sách đơn hàng admin |
| `templates/admin/order-detail.html` | Chi tiết đơn hàng + chuyển trạng thái |
