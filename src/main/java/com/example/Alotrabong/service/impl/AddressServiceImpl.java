package com.example.Alotrabong.service.impl;

import com.example.Alotrabong.dto.AddressDTO;
import com.example.Alotrabong.dto.AddressFormDTO;
import com.example.Alotrabong.entity.Address;
import com.example.Alotrabong.entity.User;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.AddressRepository;
import com.example.Alotrabong.repository.UserRepository;
import com.example.Alotrabong.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    // ================== READ ==================

    @Override
    public List<AddressDTO> getAddressesForUser(String userLoginOrId) {
        User u = resolveUser(userLoginOrId);

        List<Address> rows = addressRepository.findByUser_UserId(u.getUserId());

        return rows.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public AddressDTO getDefaultAddressForUser(String userLoginOrId) {
        User u = resolveUser(userLoginOrId);

        // Ưu tiên địa chỉ default trong DB
        List<Address> defs = addressRepository.findByUser_UserIdAndIsDefaultTrue(u.getUserId());
        Address def = defs.stream().findFirst().orElse(null);
        if (def != null) {
            return mapToDTO(def);
        }

        // fallback: nếu user chưa có default thì lấy địa chỉ đầu tiên (nếu có)
        List<Address> all = addressRepository.findByUser_UserId(u.getUserId());
        return all.stream()
                .findFirst()
                .map(this::mapToDTO)
                .orElse(null);
    }

    // ================== WRITE ==================

    /**
     * Tạo địa chỉ mới cho user dựa trên form.
     */
    @Override
    @Transactional
    public AddressDTO createAddress(String userLoginOrId, AddressFormDTO form) {
        User u = resolveUser(userLoginOrId);

        Address a = new Address();
        a.setUser(u);
        a.setReceiverName(form.getFullName());
        a.setPhone(form.getPhone());
        a.setAddressLine(form.getLine());
        a.setDistrict(form.getDistrict());
        a.setCity(form.getCity());
        a.setIsDefault(Boolean.TRUE.equals(form.getIsDefault()));

        Address saved = addressRepository.save(a);

        // Nếu địa chỉ mới được chọn làm mặc định -> bỏ default ở các địa chỉ khác
        if (Boolean.TRUE.equals(saved.getIsDefault())) {
            clearOtherDefaults(u.getUserId(), saved.getAddressId());
        }

        return mapToDTO(saved);
    }

    /**
     * Cập nhật địa chỉ đã có của user.
     * form.id bắt buộc phải != null.
     */
    @Override
    @Transactional
    public AddressDTO updateAddress(String userLoginOrId, AddressFormDTO form) {
        if (form.getId() == null) {
            throw new IllegalArgumentException("Address ID is required for update");
        }

        User u = resolveUser(userLoginOrId);

        Address addr = addressRepository.findById(form.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // chặn user khác cố sửa địa chỉ không thuộc họ
        if (addr.getUser() == null
                || !u.getUserId().equals(addr.getUser().getUserId())) {
            throw new ResourceNotFoundException("Address does not belong to this user");
        }

        addr.setReceiverName(form.getFullName());
        addr.setPhone(form.getPhone());
        addr.setAddressLine(form.getLine());
        addr.setDistrict(form.getDistrict());
        addr.setCity(form.getCity());
        addr.setIsDefault(Boolean.TRUE.equals(form.getIsDefault()));

        Address saved = addressRepository.save(addr);

        // nếu form tick default => bỏ default mấy địa chỉ khác
        if (Boolean.TRUE.equals(saved.getIsDefault())) {
            clearOtherDefaults(u.getUserId(), saved.getAddressId());
        }

        return mapToDTO(saved);
    }

    /**
     * Xoá 1 địa chỉ thuộc user.
     */
    @Override
    @Transactional
    public void deleteAddress(String userLoginOrId, Integer addressId) {
        User u = resolveUser(userLoginOrId);

        Address addr = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // check quyền sở hữu
        if (addr.getUser() == null
                || !u.getUserId().equals(addr.getUser().getUserId())) {
            throw new ResourceNotFoundException("Address does not belong to this user");
        }

        addressRepository.delete(addr);
    }

    /**
     * Đặt 1 địa chỉ làm mặc định cho user (và clear default của các địa chỉ khác).
     */
    @Override
    @Transactional
    public void setDefault(String userLoginOrId, Integer addressId) {
        User u = resolveUser(userLoginOrId);

        Address addr = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (addr.getUser() == null
                || !u.getUserId().equals(addr.getUser().getUserId())) {
            throw new ResourceNotFoundException("Address does not belong to this user");
        }

        // bật default cho địa chỉ này
        addr.setIsDefault(true);
        addressRepository.save(addr);

        // tắt default cho các địa chỉ khác
        clearOtherDefaults(u.getUserId(), addr.getAddressId());
    }

    // ================== helpers ==================

    /**
     * Tìm user từ login/email/sđt HOẶC từ userId (primary key).
     */
    private User resolveUser(String userLoginOrId) {
        return userRepository.findById(userLoginOrId)
                .or(() -> userRepository.findByLogin(userLoginOrId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Convert Address entity -> AddressDTO sạch cho view (checkout, quản lý địa chỉ).
     */
    private AddressDTO mapToDTO(Address a) {
        if (a == null) return null;
        return AddressDTO.builder()
                .id(a.getAddressId())
                .fullName(a.getReceiverName())
                .phone(a.getPhone())
                .line(a.getAddressLine())
                .district(a.getDistrict())
                .city(a.getCity())
                .isDefault(Boolean.TRUE.equals(a.getIsDefault()))
                .build();
    }

    /**
     * Sau khi set một địa chỉ là default, cần bỏ default ở các địa chỉ khác cùng user.
     */
    @Transactional
    protected void clearOtherDefaults(String userId, Integer keepAddressId) {
        List<Address> all = addressRepository.findByUser_UserId(userId);
        for (Address ad : all) {
            if (keepAddressId != null && keepAddressId.equals(ad.getAddressId())) {
                continue; // đừng đụng vào địa chỉ vừa set mặc định
            }
            if (Boolean.TRUE.equals(ad.getIsDefault())) {
                ad.setIsDefault(false);
                addressRepository.save(ad);
            }
        }
    }
}