# Báo cáo các thay đổi (Changelog Refactor)

Dưới đây là toàn bộ danh sách những gì tôi đã sửa chữa và refactor trong dự án `BookStore` của bạn để đồng bộ mã nguồn với Class Diagram:

## 1. Đồng bộ tên biến Identifier (khóa chính)
Đã đổi tên thuộc tính `id` mặc định thành tên cụ thể để đúng với mô tả trên sơ đồ lớp:
* `Book.java`: `private String id;` 👉 đổi thành `private String bookId;`
* `Category.java`: `private String id;` 👉 đổi thành `private String categoryId;`
* `Voucher.java`: `private String id;` 👉 đổi thành `private String voucherId;`

## 2. Cấu trúc lại Address (Xóa CustomerAddress)
Theo đúng class diagram, class quản lý địa chỉ là `Address`.
* **Đã xoá**: `CustomerAddress.java` và `CustomerAddressRepository.java`.
* **Đã tạo mới**: `Address.java` và `AddressRepository.java`.
* **Đã cập nhật reference**:
  * Các Controller liên quan như `ProfileController.java` và `CheckoutController.java` giờ đây đã query/thao tác qua `AddressRepository` thay cho `CustomerAddressRepository`.
  * Trong `Customer.java`, list quan hệ 1-N đã được cập nhật thành `Set<Address> addresses`.

## 3. Chỉnh sửa lỗi Naming Conflict (Trùng lặp tên cấu trúc) trong Customer
* **Lý do sửa**: Trong Entity `Customer.java` có một trường lưu địa chỉ chữ viết là `private String address;`. Tuy nhiên do ta mới bổ sung class `Address`, tên biến `address` (cho Java Lombok generate method `setAddress()`) bị đụng độ thẳng với tên class `Address`, gây lỗi build chết toàn bộ hệ thống.
* **Đã sửa**: Đổi property `private String address;` trong `Customer.java` thành `private String addressLine;`.
* **Các file bị ảnh hưởng đã được sửa theo**: Báo cho `CheckoutController.java` và `UserService.java` gọi hàm `customer.getAddressLine()` thay vì hàm cũ.

## 4. Cập nhật Enum ShippingStatus
* Đã gỡ bỏ trạng thái `RETURNED` ra khỏi tệp `ShippingStatus.java` để file enum này ăn khớp hoàn toàn với Class Diagram cập nhật trước đó.

## 5. Sửa lỗi build Maven với Lombok
* **Lý do sửa**: Lỗi xung đột ở `Customer` khiến Java 21 ngưng cấp quyền annotation processor cho Lombok, làm hệ thống báo lỗi không tìm thấy `getId()`, `getCategoryId()` và hàng tá getter/setter khác dù file không sai.
* **Đã sửa**: Tại `pom.xml`, tôi đã thêm thẻ cấu hình ép buộc `maven-compiler-plugin` phải chạy song song `annotationProcessorPaths` của Lombok. Việc bổ sung này giải quyết dứt điểm các lỗi không cho build. 

---
👉 **Khuyến nghị:** Bạn có thể mở lịch trình commit (ví dụ dùng Git trên VS Code hoặc IntelliJ) ở nhánh `develop` hiện tại để xem file-diff chi tiết trực quan nhất! Tất cả code hiện đều đã compile thành công (`mvn clean compile` báo SUCCESS) và chạy Spring Boot ổn định.
