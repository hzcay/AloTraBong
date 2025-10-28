package com.example.Alotrabong.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO địa chỉ hiển thị ra trang checkout.
 * Đây không phải entity, chỉ data sạch cho UI.
 */
@Data
@Builder
public class AddressDTO {
    private Integer id;        // addressId
    private String fullName;   // receiverName
    private String phone;      // phone
    private String line;       // addressLine
    private String ward;       // ward/phường (hiện entity chưa có => tạm null)
    private String district;   // district
    private String city;       // city
    private boolean isDefault; // isDefault
}