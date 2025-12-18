package org.web.posetrainer.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailAsyncService {

    private final MailService mailService;

    public MailAsyncService(MailService mailService) {
        this.mailService = mailService;
    }

    // ================= ACCOUNT =================

    @Async("mailExecutor")
    public void sendAccountStatusMail(String toEmail, boolean active) {
        try {
            String subject = active
                    ? "✅ Tài khoản của bạn đã được mở khóa"
                    : "🔒 Tài khoản của bạn đã bị khóa";

            String html = active
                    ? buildAccountUnlockedMail()
                    : buildAccountLockedMail();

            mailService.sendHtmlMail(toEmail, subject, html);

        } catch (Exception e) {
            log.error("Async sendAccountStatusMail failed", e);
        }
    }

    // ================= POST =================

    @Async("mailExecutor")
    public void sendPostVisibilityMail(
            String toEmail,
            String postTitle,
            boolean isVisible
    ) {
        try {
            String subject = isVisible
                    ? "Bài viết của bạn đã được mở hiển thị"
                    : "Bài viết của bạn đã bị ẩn";

            String html = buildPostVisibilityMail(postTitle, isVisible);

            mailService.sendHtmlMail(toEmail, subject, html);

        } catch (Exception e) {
            log.error("Async sendPostVisibilityMail failed", e);
        }
    }

    // ================= TEMPLATE =================

    private String buildAccountUnlockedMail() {
        return """
        <!DOCTYPE html>
        <html lang="vi">
        <body style="font-family:Arial,sans-serif;background:#f4f6f8;padding:40px;">
            <div style="max-width:600px;margin:auto;background:#fff;
                        border-radius:8px;overflow:hidden;
                        box-shadow:0 4px 10px rgba(0,0,0,0.1);">

                <div style="background:#27ae60;color:#fff;padding:20px;text-align:center;">
                    <h2 style="margin:0;">TÀI KHOẢN ĐÃ ĐƯỢC MỞ KHÓA</h2>
                </div>

                <div style="padding:30px;color:#333;">
                    <p>Xin chào,</p>

                    <p>
                        Tài khoản của bạn trên hệ thống
                        <b>PoseTrainer System</b> đã được
                        <b style="color:#27ae60;">MỞ KHÓA</b>.
                    </p>

                    <p>
                        Bạn có thể đăng nhập và sử dụng hệ thống bình thường.
                    </p>

                    <p style="margin-top:30px;">
                        Trân trọng,<br>
                        <b>PoseTrainer</b>
                    </p>
                </div>

                <div style="background:#f1f1f1;padding:12px;
                            text-align:center;font-size:12px;color:#777;">
                    © 2025 PoseTrainer
                </div>
            </div>
        </body>
        </html>
        """;
    }

    private String buildAccountLockedMail() {
        return """
        <!DOCTYPE html>
        <html lang="vi">
        <body style="font-family:Arial,sans-serif;background:#f4f6f8;padding:40px;">
            <div style="max-width:600px;margin:auto;background:#fff;
                        border-radius:8px;overflow:hidden;
                        box-shadow:0 4px 10px rgba(0,0,0,0.1);">

                <div style="background:#e74c3c;color:#fff;padding:20px;text-align:center;">
                    <h2 style="margin:0;">TÀI KHOẢN BỊ KHÓA</h2>
                </div>

                <div style="padding:30px;color:#333;">
                    <p>Xin chào,</p>

                    <p>
                        Tài khoản của bạn trên hệ thống
                        <b>PoseTrainer System</b> đã bị
                        <b style="color:#e74c3c;">KHÓA</b>
                        bởi quản trị viên.
                    </p>

                    <div style="background:#fff3f3;
                                border-left:4px solid #e74c3c;
                                padding:15px;margin:20px 0;">
                        <p style="margin:0;">
                            Lý do: <b>Vi phạm tiêu chuẩn cộng đồng</b>
                        </p>
                    </div>

                    <p>
                        Nếu bạn cho rằng đây là nhầm lẫn,
                        vui lòng liên hệ quản trị viên.
                    </p>

                    <p style="margin-top:30px;">
                        Trân trọng,<br>
                        <b>PoseTrainer</b>
                    </p>
                </div>

                <div style="background:#f1f1f1;padding:12px;
                            text-align:center;font-size:12px;color:#777;">
                    © 2025 PoseTrainer
                </div>
            </div>
        </body>
        </html>
        """;
    }

    private String buildPostVisibilityMail(String postTitle, boolean isVisible) {
        String statusText = isVisible ? "ĐÃ ĐƯỢC MỞ HIỂN THỊ" : "ĐÃ BỊ ẨN";
        String statusColor = isVisible ? "#16a34a" : "#dc2626";

        return """
        <div style="font-family:Arial,Helvetica,sans-serif;
                    max-width:600px;
                    margin:auto;
                    border:1px solid #e5e7eb;
                    border-radius:8px;
                    overflow:hidden">

            <div style="background:#0f172a;
                        color:white;
                        padding:16px;
                        font-size:18px;
                        font-weight:bold">
                PoseTrainer Community
            </div>

            <div style="padding:20px; color:#111827">
                <p>Xin chào,</p>

                <p>
                    Bài viết <strong>%s</strong> của bạn
                    <span style="color:%s;font-weight:bold">%s</span>.
                </p>

                %s

                <p style="margin-top:24px">
                    Nếu bạn cho rằng đây là nhầm lẫn,
                    vui lòng liên hệ quản trị viên.
                </p>

                <p style="margin-top:24px">
                    Trân trọng,<br/>
                    <strong>PoseTrainer Admin</strong>
                </p>
            </div>
        </div>
        """.formatted(
                postTitle,
                statusColor,
                statusText,
                !isVisible
                        ? "<div style='margin-top:16px;padding:12px;background:#fef2f2;border-left:4px solid #dc2626'>" +
                        "<strong>Lý do:</strong> Vi phạm tiêu chuẩn cộng đồng</div>"
                        : ""
        );
    }
}


