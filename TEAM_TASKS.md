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

## 3. Thành viên 3: Người phụ trách Notification & Status Order (CẦN LÀM)
**Nhiệm vụ:** Xử lý gửi thông báo khi đơn hàng và xử lý luồng Admin.
**Design Pattern chính:** Observer Pattern.

**Quy hoạch scope công việc:**
*   **Design Pattern Logic:**
    *   Thiết kế hệ thống Observer lắng nghe thay đổi trạng thái của `Order` (ví dụ từ `PENDING` -> `CONFIRMED` -> `DELIVERED`).
    *   **Subject/Publisher:** `OrderNotifier` hoặc nhúng trực tiếp vào service duyệt đơn.
    *   **Observer/Subscriber:** Xây dựng interface `OrderObserver`, kèm các class implement như `EmailNotifierObserver`, `SMSNotifierObserver`, `InAppNotificationObserver`. (Chỉ cần System.out ra console hoặc log info là đủ báo cáo rồi, không cần tích hợp gửi SMS tốn tiền API thật).
*   **Business Logic & UI/Controller:**
    *   Nếu chưa có ai làm, bạn hãy làm **AdminOrderController** và trang UI quản lý đơn hàng (`/admin/orders.html`).
    *   Trong chức năng "Duyệt đơn", bạn cập nhật biến `status` của Order, và gọi hàm `.notifyObservers()` của pattern. Vừa demo chuẩn pattern vừa hoàn thiện luồng nghiệp vụ.
    *   Hoàn thiện luồng gửi thư Welcome khi đăng kí user (nếu cần).
