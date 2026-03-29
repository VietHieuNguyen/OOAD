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

## 2. Thành viên 2: Người phụ trách Cart & Tính Giá (CẦN LÀM)
**Nhiệm vụ:** Hoàn thiện luồng thêm vào giỏ hàng và áp dụng giảm giá.
**Design Pattern chính:** Decorator Pattern.

**Quy hoạch scope công việc:**
*   **Design Pattern Logic:** 
    *   Thiết kế hệ thống tính giá đơn hàng lồng nhau (Nested Pricing) cho Giỏ Hàng.
    *   **Thành phần Component:** `PricingService` hoặc interface `CartPricer`.
    *   **Thành phần Decorator:** Viết các lớp Bọc thẻ giảm giá: `VoucherDiscountDecorator` (ví dụ giảm 10%), `VIPCustomerDecorator` (Giảm cứng 50k), `GiftWrapDecorator` (Cộng thêm tiền gói quà).
*   **Business Logic:**
    *   Viết code chính cho `CartService` (Hàm `addCartItem`, `removeCartItem`, `updateQuantity`). Hiện tại `CartService` chỉ mới có sẵn hàm `getOrCreateCart` và `clearCart`, bạn sẽ phát triển nối tiếp vào đây là an toàn nhất.
*   **UI/Controller:**
    *   Viết `CartController` và file `client/cart.html` để hiện giỏ hàng cho user tương tác, có form nhập mã giảm giá. 

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
