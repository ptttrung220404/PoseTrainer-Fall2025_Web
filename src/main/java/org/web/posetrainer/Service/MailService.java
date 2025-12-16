package org.web.posetrainer.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailService {
    private final JavaMailSender mailSender;
    private final String fromEmail;

    public MailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendHtmlMail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("PoseTrainer System <" + fromEmail + ">");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Send mail failed to {}", to, e);
            throw new RuntimeException(e);
        }
    }
//    public void sendAccountStatusMail(String toEmail, boolean active) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper =
//                    new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setFrom("PoseTrainer System <" + fromEmail + ">");
//            helper.setTo(toEmail);
//
//            String subject;
//            String htmlContent;
//
//            if (active) {
//                subject = "✅ Tài khoản của bạn đã được mở khóa";
//
//                htmlContent = """
//                        <!DOCTYPE html>
//                        <html lang="vi">
//                        <body style="font-family:Arial,sans-serif;background:#f4f6f8;padding:40px;">
//                            <div style="max-width:600px;margin:auto;background:#fff;
//                                        border-radius:8px;overflow:hidden;
//                                        box-shadow:0 4px 10px rgba(0,0,0,0.1);">
//
//                                <div style="background:#27ae60;color:#fff;padding:20px;text-align:center;">
//                                    <h2 style="margin:0;">TÀI KHOẢN ĐÃ ĐƯỢC MỞ KHÓA</h2>
//                                </div>
//
//                                <div style="padding:30px;color:#333;">
//                                    <p>Xin chào,</p>
//
//                                    <p>
//                                        Tài khoản của bạn trên hệ thống
//                                        <b>PoseTrainer System</b> đã được
//                                        <b style="color:#27ae60;">MỞ KHÓA</b>.
//                                    </p>
//
//                                    <p>
//                                        Bạn có thể đăng nhập và sử dụng các chức năng
//                                        của hệ thống bình thường.
//                                    </p>
//
//                                    <p style="margin-top:30px;">
//                                        Trân trọng,<br>
//                                        <b>PoseTrainer System</b>
//                                    </p>
//                                </div>
//
//                                <div style="background:#f1f1f1;padding:12px;
//                                            text-align:center;font-size:12px;color:#777;">
//                                    © 2025 PoseTrainer System
//                                </div>
//                            </div>
//                        </body>
//                        </html>
//                        """;
//
//            } else {
//                subject = "🔒 Tài khoản của bạn đã bị khóa";
//
//                htmlContent = """
//                        <!DOCTYPE html>
//                        <html lang="vi">
//                        <body style="font-family:Arial,sans-serif;background:#f4f6f8;padding:40px;">
//                            <div style="max-width:600px;margin:auto;background:#fff;
//                                        border-radius:8px;overflow:hidden;
//                                        box-shadow:0 4px 10px rgba(0,0,0,0.1);">
//
//                                <div style="background:#e74c3c;color:#fff;padding:20px;text-align:center;">
//                                    <h2 style="margin:0;">TÀI KHOẢN BỊ KHÓA</h2>
//                                </div>
//
//                                <div style="padding:30px;color:#333;">
//                                    <p>Xin chào,</p>
//
//                                    <p>
//                                        Tài khoản của bạn trên hệ thống
//                                        <b>PoseTrainer System</b> đã bị
//                                        <b style="color:#e74c3c;">KHÓA</b>
//                                        bởi quản trị viên.
//                                    </p>
//
//                                    <div style="background:#fff3f3;
//                                                border-left:4px solid #e74c3c;
//                                                padding:15px;margin:20px 0;">
//                                        <p style="margin:0;">
//                                            Trong thời gian này, bạn sẽ không thể đăng nhập
//                                            hoặc sử dụng các chức năng của hệ thống.
//                                        </p>
//                                    </div>
//
//                                    <p>
//                                        Nếu bạn cho rằng đây là nhầm lẫn,
//                                        vui lòng liên hệ quản trị viên để được hỗ trợ.
//                                    </p>
//
//                                    <p style="margin-top:30px;">
//                                        Trân trọng,<br>
//                                        <b>Admin System</b>
//                                    </p>
//                                </div>
//
//                                <div style="background:#f1f1f1;padding:12px;
//                                            text-align:center;font-size:12px;color:#777;">
//                                    © 2025 PoseTrainer System
//                                </div>
//                            </div>
//                        </body>
//                        </html>
//                        """;
//            }
//
//            helper.setSubject(subject);
//            helper.setText(htmlContent, true); // true = HTML
//
//            mailSender.send(message);
//
//        } catch (Exception e) {
//            // Không throw để tránh fail API
//            e.printStackTrace();
//        }
//    }
//
//    public void sendPostVisibilityMail(
//            String toEmail,
//            String postTitle,
//            boolean isVisible
//    ) {
//        String subject = isVisible
//                ? "Bài viết của bạn đã được mở hiển thị"
//                : "Bài viết của bạn đã bị ẩn";
//
//        String statusText = isVisible ? "ĐÃ ĐƯỢC MỞ HIỂN THỊ" : "ĐÃ BỊ ẨN";
//        String statusColor = isVisible ? "#16a34a" : "#dc2626";
//
//        String html = """
//        <div style="font-family:Arial,Helvetica,sans-serif;
//                    max-width:600px;
//                    margin:auto;
//                    border:1px solid #e5e7eb;
//                    border-radius:8px;
//                    overflow:hidden">
//
//            <div style="background:#0f172a;
//                        color:white;
//                        padding:16px;
//                        font-size:18px;
//                        font-weight:bold">
//                PoseTrainer Community
//            </div>
//
//            <div style="padding:20px; color:#111827">
//                <p>Xin chào,</p>
//
//                <p>Bài viết <strong>%s</strong> của bạn <span style="color:%s; font-weight:bold">%s</span>.</p>
//
//                %s
//
//                <p style="margin-top:24px">
//                    Nếu bạn cho rằng đây là nhầm lẫn, vui lòng liên hệ quản trị viên để được hỗ trợ.
//                </p>
//
//                <p style="margin-top:24px">
//                    Trân trọng,<br/>
//                    <strong>PoseTrainer Admin</strong>
//                </p>
//            </div>
//        </div>
//        """.formatted(
//                postTitle,
//                statusColor,
//                statusText,
//                (!isVisible )
//                        ? "<div style='margin-top:16px;padding:12px;background:#fef2f2;border-left:4px solid #dc2626'>" +
//                        "<strong>Lý do:</strong> Vi phạm tiêu chuẩn cộng đồng </div>"
//                        : ""
//        );
//
//        sendHtmlMail(toEmail, subject, html);
//    }
//

}
