package com.bankapp.bankingapp.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Application custom properties configuration
 * Properties defined here will be available through injection
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Admin admin = new Admin();
    private Otp otp = new Otp();

    public static class Admin {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Otp {
        private int expirationMinutes = 10;
        private int maxAttempts = 5;

        public int getExpirationMinutes() {
            return expirationMinutes;
        }

        public void setExpirationMinutes(int expirationMinutes) {
            this.expirationMinutes = expirationMinutes;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public Otp getOtp() {
        return otp;
    }

    public void setOtp(Otp otp) {
        this.otp = otp;
    }
}
