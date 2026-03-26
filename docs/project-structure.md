# Recommended Structure For A Spring Boot BookStore

Use classic MVC, but do not stop at only `controller - service - repository`.
For a bookstore project that will later need auth, admin pages, order flow, and payment integration,
the safer layout is `MVC + Service Layer + Repository + Security + DTO`.

## Folder Tree

```text
src/
  main/
    java/
      com/example/bookstore/
        BookStoreApplication.java
        config/
          WebConfig.java
          JpaConfig.java
          ModelMapperConfig.java
        security/
          SecurityConfig.java
          CustomUserDetailsService.java
          PasswordConfig.java
          AuthEntryPoint.java
        controller/
          client/
            HomeController.java
            BookController.java
            CartController.java
            OrderController.java
          admin/
            AdminBookController.java
            AdminOrderController.java
            AdminUserController.java
          auth/
            AuthController.java
        service/
          BookService.java
          CartService.java
          OrderService.java
          UserService.java
          impl/
            BookServiceImpl.java
            CartServiceImpl.java
            OrderServiceImpl.java
            UserServiceImpl.java
        repository/
          BookRepository.java
          CategoryRepository.java
          OrderRepository.java
          UserRepository.java
        entity/
          Book.java
          Category.java
          Cart.java
          CartItem.java
          Order.java
          OrderItem.java
          User.java
          Role.java
        dto/
          request/
            LoginRequest.java
            RegisterRequest.java
            CheckoutRequest.java
            BookCreateRequest.java
          response/
            BookResponse.java
            CartResponse.java
            OrderResponse.java
            UserResponse.java
        mapper/
          BookMapper.java
          OrderMapper.java
          UserMapper.java
        validator/
          PasswordMatches.java
          UniqueEmail.java
        exception/
          GlobalExceptionHandler.java
          ResourceNotFoundException.java
          BusinessException.java
        common/
          constant/
            SecurityConstants.java
            AppConstants.java
          enum/
            OrderStatus.java
            PaymentMethod.java
            UserRole.java
        util/
          SlugUtils.java
          FileUploadUtils.java
          PriceUtils.java
    resources/
      static/
        css/
        js/
        images/
      templates/
        client/
        admin/
        auth/
        fragments/
      db/
        migration/
      messages/
      application.properties
      application-local.properties.example
  test/
    java/
      com/example/bookstore/
        controller/
        service/
        repository/
        security/
```

## Why This Layout

- `controller`: only receives request, validates input, and returns view/model. Do not place business logic here.
- `service`: contains business rules such as stock checks, cart updates, order creation, and permission checks.
- `repository`: only handles database access.
- `dto`: prevents binding directly from web form into entity and reduces mass-assignment risk.
- `security`: isolate login, authorization, password encoding, session rules, and exception handling.
- `validator` and `exception`: keep validation and failure handling centralized.
- `db/migration`: prefer Flyway or Liquibase instead of relying on `ddl-auto=update`.

## Security Notes

- Keep secrets out of source code. Use environment variables or ignored local property files.
- For local development, create `src/main/resources/application-local.properties` from the example file and do not commit it.
- Set `spring.jpa.open-in-view=false` to avoid lazy-loading leaks into the view layer.
- Use DTO + Bean Validation for all write actions such as login, register, checkout, and admin CRUD.
- Separate `admin` controllers/views from `client` controllers/views early to avoid authorization sprawl.
- Hash passwords with BCrypt/Argon2 through Spring Security, never store raw passwords.
- Use HTTPS in production and set secure cookies there.
- Rotate the current DB password before production if it has ever been shared in plain text.
