package com.campus.recruitment.util;

import lombok.extern.slf4j.Slf4j;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;

@Slf4j
public class XssUtils {

    private static AntiSamy antiSamy;

    static {
        try {
            Policy policy = Policy.getInstance(XssUtils.class.getResourceAsStream("/antisamy.xml"));
            antiSamy = new AntiSamy(policy);
        } catch (Exception e) {
            log.warn("Failed to initialize AntiSamy policy, XSS cleaning will be bypassed", e);
            antiSamy = null;
        }
    }

    public static String clean(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }
        if (antiSamy == null) {
            return html;
        }
        try {
            CleanResults results = antiSamy.scan(html);
            return results.getCleanHTML();
        } catch (Exception e) {
            log.warn("XSS cleaning failed, returning original content", e);
            return html;
        }
    }
}
