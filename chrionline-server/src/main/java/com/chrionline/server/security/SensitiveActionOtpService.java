package com.chrionline.server.security;

import com.chrionline.common.AppConstants;
import com.chrionline.server.model.OtpCode;
import com.chrionline.server.repository.OtpDAO;
import com.chrionline.server.service.EmailService;

import java.util.Date;

public class SensitiveActionOtpService {

    private static final SensitiveActionOtpService INSTANCE = new SensitiveActionOtpService();
    private static final String SENSITIVE_ACTION_TYPE = "ACTION_SENSIBLE";

    private final OtpDAO otpDAO = new OtpDAO();
    private final EmailService emailService = EmailService.getInstance();

    private SensitiveActionOtpService() {
    }

    public static SensitiveActionOtpService getInstance() {
        return INSTANCE;
    }

    public boolean verifyOrSend(String email, String code, String actionLabel) {
        if (code == null || code.isBlank()) {
            send(email, actionLabel);
            return false;
        }
        OtpCode otp = otpDAO.findValid(email, code.trim(), SENSITIVE_ACTION_TYPE);
        if (otp == null) {
            return false;
        }
        otpDAO.markAsUsed(otp.getId());
        return true;
    }

    private void send(String email, String actionLabel) {
        String code = emailService.generateOtp();
        Date expireAt = new Date(System.currentTimeMillis() + AppConstants.SENSITIVE_ACTION_OTP_TTL_MILLIS);
        otpDAO.save(new OtpCode(email, code, SENSITIVE_ACTION_TYPE, expireAt));
        emailService.sendTextEmail(
                email,
                "ChriOnline - Confirmation action sensible",
                """
                Bonjour,

                Une action sensible a ete demandee : %s

                Votre code OTP est : %s

                Ce code est valable pendant 5 minutes.

                Si vous n'etes pas a l'origine de cette action, ignorez cet email et changez votre mot de passe.

                L'equipe ChriOnline
                """.formatted(actionLabel, code)
        );
    }
}
