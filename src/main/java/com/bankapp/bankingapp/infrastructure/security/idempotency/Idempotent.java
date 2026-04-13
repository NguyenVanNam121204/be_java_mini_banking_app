package com.bankapp.bankingapp.infrastructure.security.idempotency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation đánh dấu API yêu cầu kiểm tra tính Idempotent (Chống duplicate request).
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
}
