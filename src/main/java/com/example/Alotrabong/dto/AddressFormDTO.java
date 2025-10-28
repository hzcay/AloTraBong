package com.example.Alotrabong.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Dữ liệu form do user submit từ trang /user/addresses (thêm / sửa).
 * Đây KHÔNG PHẢI entity JPA. Chỉ là payload từ HTML <form>.
 */
@Getter
@Setter
public class AddressFormDTO {
    private Integer id;          // null khi tạo mới, !=null khi update
    private String fullName;
    private String phone;
    private String line;         // address_line
    private String district;
    private String city;
    private Boolean isDefault;   // checkbox "đặt làm mặc định"
}
