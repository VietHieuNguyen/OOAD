# BookStore

Dự án web bán sách được nhóm xây dựng theo hướng Spring Boot, ưu tiên cấu trúc rõ ràng, dễ mở rộng và an toàn khi làm việc nhóm.

## Mục tiêu

- Tách rõ phần giao diện, xử lý nghiệp vụ và truy cập dữ liệu.
- Hạn chế đưa secret vào source code.
- Để các thành viên clone về có thể chạy local với ít bước cấu hình nhất.

## Công nghệ dự kiến

- Java 21
- Maven
- Spring Boot
- Spring MVC
- Spring Data JPA
- Spring Security
- MySQL
- Thymeleaf

Lưu ý: repo hiện đang ở giai đoạn khởi tạo cấu hình. `pom.xml` chưa đủ dependency Spring Boot đầy đủ, nên README này tập trung vào cấu trúc và quy trình làm việc nhóm.

## Cấu trúc thư mục chính

```text
BookStore/
|-- src/
|   |-- main/
|   |   |-- java/
|   |   |   `-- com/example/bookstore/
|   |   |       |-- config/
|   |   |       |-- controller/
|   |   |       |-- security/
|   |   |       |-- service/
|   |   |       |-- repository/
|   |   |       |-- entity/
|   |   |       |-- dto/
|   |   |       |-- validator/
|   |   |       |-- exception/
|   |   |       `-- util/
|   |   `-- resources/
|   |       |-- static/
|   |       |-- templates/
|   |       |-- db/migration/
|   |       |-- application.properties
|   |       `-- application-local.properties.example
|   `-- test/
|-- docs/
|   `-- project-structure.md
|-- pom.xml
`-- .gitignore
```

Mô tả nhanh:

- `controller`: nhận request, trả về view/model, không xử lý nghiệp vụ lớn.
- `service`: chứa nghiệp vụ chính của hệ thống.
- `repository`: làm việc với database.
- `entity`: ánh xạ bảng dữ liệu.
- `dto`: dữ liệu vào/ra để tránh bind trực tiếp vào entity.
- `security`: cấu hình đăng nhập, phân quyền, mã hóa mật khẩu.
- `resources/templates`: giao diện server-side.
- `resources/static`: CSS, JS, images.

Chi tiết hơn xem tại [docs/project-structure.md](docs/project-structure.md).

## Sau khi clone về cần làm gì

1. Clone repo về máy.
2. Mở dự án bằng IntelliJ IDEA hoặc VS Code.
3. Kiểm tra máy đã cài Java 21 và Maven.
4. Tạo file local config từ file mẫu.
5. Điền thông tin database của nhóm vào file local.

## Cách tạo file cấu hình local

Dự án dùng 2 file cấu hình:

- `src/main/resources/application.properties`
  Đây là file cấu hình chung, có thể commit lên GitHub.
- `src/main/resources/application-local.properties`
  Đây là file local trên máy mỗi thành viên, dùng để chứa thông tin nhạy cảm như `DB URL`, `username`, `password`. File này đã được thêm vào `.gitignore`.

Cách làm:

1. Copy file `src/main/resources/application-local.properties.example`
2. Đổi tên thành `src/main/resources/application-local.properties`
3. Điền giá trị thật vào:

```properties
spring.datasource.url=...
spring.datasource.username=...
spring.datasource.password=...
```

Không commit file `application-local.properties`.

## Ý nghĩa của dấu `${...}` trong application.properties

Ví dụ:

```properties
server.port=${SERVER_PORT:8080}
spring.datasource.username=${DB_USERNAME:root}
```

Ý nghĩa:

- Nếu biến môi trường tồn tại thì Spring sẽ lấy giá trị đó.
- Nếu không có thì dùng giá trị mặc định sau dấu `:`.

Vì vậy file `application.properties` vẫn an toàn để commit, còn giá trị thật nên đặt trong file local hoặc biến môi trường.

## Quy ước bảo mật khi làm việc nhóm

- Không hardcode key, password, token vào source code.
- Không commit `application-local.properties`.
- Không copy secret vào file `.example`.
- Không chụp màn hình hoặc gửi credential lên nhóm chat công khai.
- Nếu nghi ngờ secret đã lộ, phải đổi password ngay.

## Quy trình làm việc cơ bản

1. Kéo code mới nhất về máy.
2. Tạo hoặc cập nhật `application-local.properties` nếu cần.
3. Code theo đúng package được phân công.
4. Tự kiểm tra lại trước khi commit.
5. Không sửa file local của người khác thành file chung.

## Nguyên tắc code trong dự án

- Controller mỏng, service xử lý nghiệp vụ.
- Không bind trực tiếp form vào entity nếu là thao tác ghi.
- Ưu tiên dùng DTO + validation.
- Tách riêng chức năng `client`, `admin`, `auth`.
- Không dùng `ddl-auto=update` cho môi trường nghiêm túc, ưu tiên migration.

## Tài liệu liên quan

- Cấu trúc đề xuất: [docs/project-structure.md](docs/project-structure.md)
- Cấu hình chung: [src/main/resources/application.properties](src/main/resources/application.properties)
- Cấu hình local mẫu: [src/main/resources/application-local.properties.example](src/main/resources/application-local.properties.example)

## Ghi chú

- Nếu sau này nhóm chuyển `pom.xml` sang Spring Boot đầy đủ, README sẽ được cập nhật thêm phần chạy ứng dụng bằng Maven.
- Nếu credential database đã từng bị chia sẻ bằng plain text, nên đổi lại password trước khi đưa dự án vào môi trường thật.
