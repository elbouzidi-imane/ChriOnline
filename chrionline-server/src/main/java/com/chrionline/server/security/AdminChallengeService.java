package com.chrionline.server.security;

import com.chrionline.common.RsaSignatureUtils;
import com.chrionline.server.model.User;
import com.chrionline.server.repository.UserDAO;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class AdminChallengeService {

    private static final AdminChallengeService INSTANCE = new AdminChallengeService();
    private static final int CHALLENGE_BYTES = 32;
    private static final long CHALLENGE_TTL_SECONDS = 30;

    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentMap<String, PendingChallenge> challenges = new ConcurrentHashMap<>();
    private final UserDAO userDAO = new UserDAO();

    private AdminChallengeService() {
    }

    public static AdminChallengeService getInstance() {
        return INSTANCE;
    }

    public ChallengeResponse createChallenge(String adminEmail) {
        purgeExpired();
        User admin = userDAO.findAdminByEmailWithPublicKey(adminEmail);
        if (admin == null) {
            return ChallengeResponse.error("Admin introuvable ou inactif.");
        }
        if (admin.getClePubliqueRsa() == null || admin.getClePubliqueRsa().isBlank()) {
            return ChallengeResponse.error("Cle publique RSA admin absente en base.");
        }

        byte[] random = new byte[CHALLENGE_BYTES];
        secureRandom.nextBytes(random);
        String challenge = Base64.getEncoder().encodeToString(random);
        String challengeId = UUID.randomUUID().toString();
        challenges.put(challengeId, new PendingChallenge(
                challengeId,
                challenge,
                admin.getEmail(),
                Instant.now().plusSeconds(CHALLENGE_TTL_SECONDS)
        ));
        System.out.println("[ADMIN] Challenge envoye : " + challenge);
        return ChallengeResponse.ok(challengeId, challenge);
    }

    public VerificationResult verify(String challengeId, String signatureBase64) {
        purgeExpired();
        if (challengeId == null || challengeId.isBlank() || signatureBase64 == null || signatureBase64.isBlank()) {
            return VerificationResult.rejected("Challenge ou signature manquant.");
        }

        PendingChallenge pending = challenges.remove(challengeId.trim());
        if (pending == null) {
            return VerificationResult.rejected("Challenge invalide ou expire.");
        }
        if (pending.expiresAt().isBefore(Instant.now())) {
            return VerificationResult.rejected("Challenge expire.");
        }

        User admin = userDAO.findAdminByEmailWithPublicKey(pending.adminEmail());
        if (admin == null || admin.getClePubliqueRsa() == null || admin.getClePubliqueRsa().isBlank()) {
            return VerificationResult.rejected("Cle publique admin indisponible.");
        }

        try {
            System.out.println("[ADMIN] Signature recue du client");
            System.out.println("[ADMIN] Verification signature...");
            boolean valid = RsaSignatureUtils.verify(
                    pending.challenge(),
                    Base64.getDecoder().decode(signatureBase64.trim()),
                    RsaSignatureUtils.publicKeyFromBase64(admin.getClePubliqueRsa())
            );
            if (!valid) {
                System.out.println("[ADMIN] Signature invalide - acces refuse");
                return VerificationResult.rejected("Signature RSA invalide.");
            }
            System.out.println("[ADMIN] Acces admin autorise");
            admin.setMotDePasse(null);
            admin.setClePubliqueRsa(null);
            return VerificationResult.accepted(admin);
        } catch (Exception e) {
            return VerificationResult.rejected("Verification RSA impossible : " + e.getMessage());
        }
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        challenges.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private record PendingChallenge(String challengeId, String challenge, String adminEmail, Instant expiresAt) {
    }

    public record ChallengeResponse(boolean accepted, String challengeId, String challenge, String reason) {
        public static ChallengeResponse ok(String challengeId, String challenge) {
            return new ChallengeResponse(true, challengeId, challenge, "");
        }

        public static ChallengeResponse error(String reason) {
            return new ChallengeResponse(false, "", "", reason);
        }
    }

    public record VerificationResult(boolean accepted, User admin, String reason) {
        public static VerificationResult accepted(User admin) {
            return new VerificationResult(true, admin, "");
        }

        public static VerificationResult rejected(String reason) {
            return new VerificationResult(false, null, reason);
        }
    }
}
