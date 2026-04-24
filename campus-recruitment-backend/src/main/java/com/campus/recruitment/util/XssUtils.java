package com.campus.recruitment.util;

import lombok.extern.slf4j.Slf4j;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;

@Slf4j
public class XssUtils {

    private static final AntiSamy ANTI_SAMY;

    static {
        try {
            Policy policy = Policy.getInstance(XssUtils.class.getResourceAsStream("/antisamy.xml"));
            ANTI_SAMY = new AntiSamy(policy);
        } catch (Exception e) {
            log.error("Failed to initialize AntiSamy policy", e);
            throw new RuntimeException("Failed to initialize AntiSamy", e);
        }
    }

    public static String clean(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }
        try {
            CleanResults results = ANTI_SAMY.scan(html);
            return results.getCleanHTML();
        } catch (Exception e) {
            log.warn("XSS cleaning failed, returning empty string", e);
            return "";
        }
    }
}
