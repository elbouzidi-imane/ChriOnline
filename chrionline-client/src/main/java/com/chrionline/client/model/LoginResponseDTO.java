package com.chrionline.client.model;

public class LoginResponseDTO {
    private boolean requiresOtp;
    private String pendingToken;
    private String message;
    private UserDTO user;

    public boolean isRequiresOtp() {
        return requiresOtp;
    }

    public String getPendingToken() {
        return pendingToken;
    }

    public String getMessage() {
        return message;
    }

    public UserDTO getUser() {
        return user;
    }
}
