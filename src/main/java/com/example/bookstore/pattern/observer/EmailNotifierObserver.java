package com.example.bookstore.pattern.observer;

import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.enums.OrderStatus;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern — Concrete Observer: Gửi Email thật khi đơn hàng đổi trạng
 * thái.
 *
 * <p>
 * Sử dụng {@link JavaMailSender} (Spring Mail) để gửi email HTML thật
 * tới khách hàng qua SMTP (Gmail). Email được gửi <b>bất đồng bộ</b>
 * ({@code @Async})
 * để không làm treo request của Admin khi duyệt đơn.
 * </p>
 *
 * <p>
 * <b>Thiết kế OCP:</b> Class này được đánh dấu {@code @Component} —
 * Spring Boot tự động đăng ký vào {@code List<OrderObserver>} trong
 * {@code OrderService}
 * mà không cần sửa bất kỳ file nào.
 * </p>
 *
 * <p>
 * <b>Lưu ý @Async:</b> Phương thức {@link #update} gọi {@link #sendEmailAsync}
 * — một method {@code @Async} riêng biệt. Spring AOP chỉ proxy được khi gọi
 * qua bean reference, nhưng vì {@code sendEmailAsync} là public method trên
 * cùng bean
 * và được gọi internally, ta cần đảm bảo proxy hoạt động bằng cách
 * Spring sẽ wrap class này thành proxy khi có {@code @Async}.
 * </p>
 */
@Component
public class EmailNotifierObserver implements OrderObserver {

  private static final Logger log = LoggerFactory.getLogger(EmailNotifierObserver.class);

  private final JavaMailSender mailSender;

  public EmailNotifierObserver(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /**
   * Được gọi khi trạng thái đơn hàng thay đổi.
   * Thu thập dữ liệu cần thiết rồi ủy quyền cho {@link #sendEmailAsync} chạy
   * ngầm.
   *
   * <p>
   * Email chỉ được gửi với các trạng thái có ý nghĩa với khách hàng:
   * {@code CONFIRMED}, {@code COMPLETED}, {@code CANCELLED}.
   * Bỏ qua trạng thái {@code PENDING}.
   * </p>
   */
  @Override
  public void update(Order order, OrderStatus newStatus) {
    if (newStatus == OrderStatus.PENDING) {
      return;
    }

    // Safety net: bọc toàn bộ logic email trong try-catch
    // Lý do: @Async gọi nội bộ cùng class (self-invocation) sẽ KHÔNG chạy async
    // → nếu email fail, exception sẽ lan ra updateStatus() → crash Admin UI
    // → catch ở đây đảm bảo lỗi email KHÔNG BAO GIỜ ảnh hưởng đến luồng đơn hàng
    try {
      // Pull data từ Order (kiểu Pull của Observer Pattern)
      String toEmail = order.getCustomer().getEmail();
      String toName = order.getCustomer().getFullName();
      String orderId = order.getOrderId();
      String orderShort = orderId.substring(0, Math.min(8, orderId.length())).toUpperCase();
      String totalAmount = order.getTotalAmount().toPlainString();
      String subject = buildSubject(newStatus);
      String htmlBody = buildHtmlBody(toName, orderShort, totalAmount, newStatus);

      // Gửi email
      sendEmailAsync(toEmail, toName, subject, htmlBody, orderShort);
    } catch (Exception e) {
      log.error("[EMAIL] Lỗi khi chuẩn bị/gửi email cho đơn hàng #{}: {}",
          order.getOrderId(), e.getMessage(), e);
    }
  }

  /**
   * Gửi email HTML thật qua SMTP. Chạy trên thread riêng nhờ {@code @Async}.
   * Nếu gửi thất bại, chỉ ghi log ERROR — không ném exception làm crash luồng
   * chính.
   */
  @Async
  public void sendEmailAsync(String toEmail, String toName, String subject,
      String htmlBody, String orderShort) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(htmlBody, true); // true = HTML content

      mailSender.send(message);
      log.info("[EMAIL ✓] Đã gửi email thật tới {} <{}> | Đơn #{} | Tiêu đề: {}",
          toName, toEmail, orderShort, subject);

    } catch (Exception e) {
      // Catch tất cả: MessagingException, MailAuthenticationException
      // (RuntimeException),
      // MailSendException, v.v. — KHÔNG cho exception nào leak ra ngoài
      log.error("[EMAIL ✗] Gửi email THẤT BẠI tới {} <{}> | Đơn #{} | Lỗi: {}",
          toName, toEmail, orderShort, e.getMessage());
    }
  }

  // ======================== BUILD EMAIL CONTENT ========================

  private String buildSubject(OrderStatus status) {
    return switch (status) {
      case CONFIRMED -> "X-Books — Đơn hàng của bạn đã được xác nhận!";
      case COMPLETED -> "X-Books — Đơn hàng đã hoàn thành!";
      case CANCELLED -> "X-Books — Đơn hàng đã bị hủy";
      default -> "X-Books — Cập nhật đơn hàng";
    };
  }

  private String buildHtmlBody(String customerName, String orderShort,
      String totalAmount, OrderStatus status) {
    String statusVi = getStatusVietnamese(status);
    String statusColor = getStatusColor(status);
    String statusEmoji = getStatusEmoji(status);
    String extraMsg = getExtraMessage(status);

    return """
        <!DOCTYPE html>
        <html>
        <head><meta charset="UTF-8"></head>
        <body style="margin:0;padding:0;background:#f4f4f7;font-family:'Segoe UI',Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f7;padding:40px 0;">
            <tr><td align="center">
              <table width="560" cellpadding="0" cellspacing="0"
                     style="background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.08);">

                <!-- Header -->
                <tr>
                  <td style="background:linear-gradient(135deg,#1a1a2e 0%%,#16213e 100%%);padding:32px 40px;text-align:center;">
                    <h1 style="margin:0;color:#e0c97f;font-size:24px;font-weight:700;letter-spacing:1px;">
                      X-BOOKS
                    </h1>
                    <p style="margin:8px 0 0;color:#a0a0b0;font-size:13px;">Hệ thống thông báo đơn hàng</p>
                  </td>
                </tr>

                <!-- Body -->
                <tr>
                  <td style="padding:36px 40px;">
                    <p style="margin:0 0 20px;font-size:16px;color:#333;">
                      Xin chào <strong>%s</strong>,
                    </p>

                    <!-- Status badge -->
                    <div style="text-align:center;margin:24px 0;">
                      <span style="display:inline-block;padding:12px 28px;border-radius:8px;
                                   background:%s;color:#fff;font-size:18px;font-weight:700;">
                        %s %s
                      </span>
                    </div>

                    <!-- Order info -->
                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="margin:24px 0;border:1px solid #e8e8ec;border-radius:8px;overflow:hidden;">
                      <tr style="background:#f8f8fb;">
                        <td style="padding:14px 20px;font-size:13px;color:#666;border-bottom:1px solid #e8e8ec;">
                          Mã đơn hàng
                        </td>
                        <td style="padding:14px 20px;font-size:14px;font-weight:600;color:#1a1a2e;
                                   border-bottom:1px solid #e8e8ec;text-align:right;">
                          #%s
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:14px 20px;font-size:13px;color:#666;">Tổng tiền</td>
                        <td style="padding:14px 20px;font-size:14px;font-weight:600;color:#e0c97f;text-align:right;">
                          %s VND
                        </td>
                      </tr>
                    </table>

                    <p style="margin:20px 0 0;font-size:14px;color:#555;line-height:1.6;">
                      %s
                    </p>
                  </td>
                </tr>

                <!-- Footer -->
                <tr>
                  <td style="background:#f8f8fb;padding:20px 40px;text-align:center;
                             border-top:1px solid #e8e8ec;">
                    <p style="margin:0;font-size:12px;color:#999;">
                      Đây là email tự động từ hệ thống X-Books. Vui lòng không trả lời email này.
                    </p>
                    <p style="margin:8px 0 0;font-size:11px;color:#bbb;">
                      © 2026 X-Books — Nhà sách trực tuyến
                    </p>
                  </td>
                </tr>

              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """
        .formatted(customerName, statusColor, statusEmoji, statusVi,
            orderShort, totalAmount, extraMsg);
  }

  private String getStatusVietnamese(OrderStatus status) {
    return switch (status) {
      case CONFIRMED -> "ĐÃ XÁC NHẬN";
      case COMPLETED -> "ĐÃ HOÀN THÀNH";
      case CANCELLED -> "ĐÃ HỦY";
      default -> "CẬP NHẬT";
    };
  }

  private String getStatusColor(OrderStatus status) {
    return switch (status) {
      case CONFIRMED -> "#2563eb";
      case COMPLETED -> "#16a34a";
      case CANCELLED -> "#dc2626";
      default -> "#6b7280";
    };
  }

  private String getStatusEmoji(OrderStatus status) {
    return switch (status) {
      case CONFIRMED -> "Confirmed";
      case COMPLETED -> "Completed";
      case CANCELLED -> "Cancelled";
      default -> "Pending";
    };
  }

  private String getExtraMessage(OrderStatus status) {
    return switch (status) {
      case CONFIRMED -> "Đơn hàng của bạn đã được xác nhận và đang được xử lý. "
          + "Chúng tôi sẽ thông báo khi đơn hàng hoàn thành.";
      case COMPLETED -> "Đơn hàng đã được giao thành công. "
          + "Cảm ơn bạn đã mua sắm tại X-Books! Hẹn gặp lại bạn.";
      case CANCELLED -> "Đơn hàng của bạn đã bị hủy. "
          + "Nếu bạn có thắc mắc, vui lòng liên hệ bộ phận CSKH.";
      default -> "Đơn hàng của bạn có cập nhật mới. Vui lòng kiểm tra chi tiết trên website.";
    };
  }
}
