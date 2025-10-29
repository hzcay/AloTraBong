package com.example.Alotrabong.dto;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Form cập nhật hồ sơ – chỉ những trường user được phép chỉnh.
 */
public class UserProfileFormDTO implements Serializable {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 120, message = "Họ tên tối đa 120 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 190, message = "Email tối đa 190 ký tự")
    private String email;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    private String address;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "0[0-9]{9,10}", message = "SĐT phải bắt đầu bằng 0 và gồm 10–11 số")
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    public UserProfileFormDTO() {}

    public UserProfileFormDTO(String fullName, String email, String address, String phone) {
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.phone = phone;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
