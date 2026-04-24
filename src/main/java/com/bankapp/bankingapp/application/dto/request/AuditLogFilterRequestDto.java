package com.bankapp.bankingapp.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO chứa các tiêu chí lọc cho Audit Log.
 * Được dùng để truyền dữ liệu từ Controller → Service một cách gọn gàng.
 * 
 * Tham số lọc:
 *   - username: Tìm kiếm gần đúng theo tên người dùng
 *   - actionGroup: Nhóm hành động (ALL | AUTH | TRANSACTION | ADMIN)
 *   - date: Ngày thực hiện theo định dạng YYYY-MM-DD
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogFilterRequestDto {

    /**
     * Tìm kiếm gần đúng (LIKE) theo username.
     * null hoặc rỗng = không lọc theo username.
     */
    private String username;

    /**
     * Nhóm hành động để lọc.
     * Các giá trị hợp lệ: ALL, AUTH, TRANSACTION, ADMIN
     * null hoặc "ALL" = hiển thị tất cả loại hành động.
     */
    private String actionGroup;

    /**
     * Ngày thực hiện hành động theo định dạng YYYY-MM-DD.
     * null hoặc rỗng = không lọc theo ngày.
     */
    private String date;
}
