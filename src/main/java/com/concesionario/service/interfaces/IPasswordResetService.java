package com.concesionario.service.interfaces;

public interface IPasswordResetService {
    void initiatePasswordReset(String email);

    void resetPassword(String token, String newPassword);

    void validateToken(String token);

    boolean emailExists(String email);

    String getCurrentBaseUrl();
}
