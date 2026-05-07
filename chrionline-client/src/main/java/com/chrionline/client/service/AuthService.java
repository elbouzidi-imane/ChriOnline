package com.chrionline.client.service;

import com.chrionline.client.model.AdminChallengeDTO;
import com.chrionline.client.model.LoginCaptchaDTO;
import com.chrionline.client.model.LoginResponseDTO;
import com.chrionline.client.model.UserDTO;
import com.chrionline.client.network.UDPListener;
import com.chrionline.client.network.TCPClient;
import com.chrionline.client.util.JsonUtils;
import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.chrionline.common.RsaSignatureUtils;
import com.google.gson.JsonObject;

public class AuthService {
    private final TCPClient tcp = TCPClient.getInstance();

    public LoginResponseDTO login(String email, String mdp) throws Exception {
        return login(email, mdp, "", "");
    }

    public LoginResponseDTO login(String email, String mdp, String captchaId, String captchaAnswer) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("email", email);
        payload.addProperty("password", mdp);
        payload.addProperty("captchaId", captchaId == null ? "" : captchaId);
        payload.addProperty("captchaAnswer", captchaAnswer == null ? "" : captchaAnswer);

        return JsonUtils.GSON.fromJson(send(Protocol.LOGIN, payload.toString()).getPayload(), LoginResponseDTO.class);
    }

    public LoginCaptchaDTO getLoginCaptcha(String email) throws Exception {
        return JsonUtils.GSON.fromJson(send(Protocol.GET_LOGIN_CAPTCHA, email).getPayload(), LoginCaptchaDTO.class);
    }

    public UserDTO verifyLoginOtp(String pendingToken, String code) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("pendingToken", pendingToken);
        payload.addProperty("code", code);
        UserDTO user = JsonUtils.GSON.fromJson(send(Protocol.VERIFY_LOGIN_OTP, payload.toString()).getPayload(), UserDTO.class);
        int udpPort = UDPListener.getBoundPort();
        if (udpPort > 0) {
            send(Protocol.REGISTER_UDP_PORT, String.valueOf(udpPort));
        }
        return user;
    }

    public String resendLoginOtp(String pendingToken) throws Exception {
        return send(Protocol.RESEND_LOGIN_OTP, pendingToken).getPayload();
    }

    public AdminChallengeDTO requestAdminChallenge(String adminEmail) throws Exception {
        Message response = send(Protocol.ADMIN_CHALLENGE_REQUEST, adminEmail);
        return JsonUtils.GSON.fromJson(response.getPayload(), AdminChallengeDTO.class);
    }

    public UserDTO verifyAdminChallenge(String challengeId, String challenge, String privateKeyBase64) throws Exception {
        String signatureBase64 = RsaSignatureUtils.signBase64(challenge, privateKeyBase64);
        JsonObject payload = new JsonObject();
        payload.addProperty("challengeId", challengeId);
        payload.addProperty("signature", signatureBase64);
        UserDTO user = JsonUtils.GSON.fromJson(send(Protocol.ADMIN_CHALLENGE_VERIFY, payload.toString()).getPayload(), UserDTO.class);
        int udpPort = UDPListener.getBoundPort();
        if (udpPort > 0) {
            send(Protocol.REGISTER_UDP_PORT, String.valueOf(udpPort));
        }
        return user;
    }

    public String register(String nom, String prenom, String email, String mdp, String tel, String adresse, String dateNaissance) throws Exception {
        String payload = String.join("|", nom, prenom, email, mdp, tel, adresse, dateNaissance == null ? "" : dateNaissance);
        return send(Protocol.REGISTER, payload).getPayload();
    }

    public String verifyEmail(String email, String code) throws Exception {
        return send(Protocol.VERIFY_EMAIL, email + "|" + code).getPayload();
    }

    public String resendOtp(String email, String type) throws Exception {
        return send(Protocol.RESEND_OTP, email + "|" + type).getPayload();
    }

    public String forgotPassword(String email) throws Exception {
        return send(Protocol.FORGOT_PASSWORD, email).getPayload();
    }

    public String verifyResetOtp(String email, String code) throws Exception {
        return send(Protocol.VERIFY_RESET_OTP, email + "|" + code).getPayload();
    }

    public String resetPassword(String email, String newPassword) throws Exception {
        return send(Protocol.RESET_PASSWORD, email + "|" + newPassword).getPayload();
    }

    public String updateProfile(int userId, String nom, String prenom, String telephone, String adresse, String dateNaissance) throws Exception {
        String payload = String.join("|",
                String.valueOf(userId),
                nom == null ? "" : nom,
                prenom == null ? "" : prenom,
                telephone == null ? "" : telephone,
                adresse == null ? "" : adresse,
                dateNaissance == null ? "" : dateNaissance
        );
        return send(Protocol.UPDATE_PROFILE, payload).getPayload();
    }

    public String updateNotificationPreference(int userId, boolean enabled) throws Exception {
        return send(Protocol.UPDATE_NOTIFICATION_PREFERENCE, userId + "|" + enabled).getPayload();
    }

    public String deactivateAccount(int userId) throws Exception {
        return send(Protocol.DEACTIVATE_ACCOUNT, String.valueOf(userId)).getPayload();
    }

    public String deleteAccount(int userId) throws Exception {
        return send(Protocol.DELETE_ACCOUNT, String.valueOf(userId)).getPayload();
    }

    public void logout() throws Exception {
        tcp.send(new Message(Protocol.LOGOUT, ""));
    }

    private Message send(String type, String payload) throws Exception {
        Message response = tcp.send(new Message(type, payload));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        return response;
    }
}
