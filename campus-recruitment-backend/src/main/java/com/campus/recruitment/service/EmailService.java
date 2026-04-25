package com.campus.recruitment.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendMagicLinkEmail(String to, String token) {
        try {
            String verifyUrl = baseUrl + "/auth/verify?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("校招信息汇总 — 登录验证");

            String html = """
                <div style="max-width:480px;margin:0 auto;font-family:'Helvetica Neue',Arial,sans-serif;padding:40px 20px;">
                    <div style="text-align:center;margin-bottom:32px;">
                        <div style="display:inline-block;width:48px;height:48px;background:linear-gradient(135deg,#f59e0b,#f97316);border-radius:12px;line-height:48px;font-size:24px;color:#fff;font-weight:700;">招</div>
                        <h2 style="color:#1a2744;margin:16px 0 4px;font-size:20px;">校招信息汇总</h2>
                        <p style="color:#94a3b8;font-size:14px;">Campus Recruitment</p>
                    </div>
                    <div style="background:#fff;border-radius:12px;padding:32px;border:1px solid #e2e8f0;box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                        <p style="color:#334155;font-size:15px;margin:0 0 24px;">点击下方按钮登录，链接 <strong>15 分钟内</strong>有效：</p>
                        <a href="%s" style="display:inline-block;background:#1a2744;color:#fff;text-decoration:none;padding:12px 32px;border-radius:8px;font-size:16px;font-weight:600;">立即登录</a>
                        <p style="color:#94a3b8;font-size:13px;margin:24px 0 0;">如果按钮无法点击，请复制以下链接到浏览器打开：</p>
                        <p style="color:#0284c7;font-size:13px;word-break:break-all;margin:8px 0 0;">%s</p>
                    </div>
                    <p style="color:#94a3b8;font-size:12px;text-align:center;margin-top:24px;">此邮件由系统自动发送，请勿回复</p>
                </div>
                """.formatted(verifyUrl, verifyUrl);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Magic link email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send magic link email to {}", to, e);
            throw new RuntimeException("发送邮件失败，请稍后重试");
        }
    }
}
