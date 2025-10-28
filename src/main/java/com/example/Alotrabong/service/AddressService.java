package com.example.Alotrabong.service;

import java.util.List;

import com.example.Alotrabong.dto.AddressDTO;
import com.example.Alotrabong.dto.AddressFormDTO;

public interface AddressService {
    List<AddressDTO> getAddressesForUser(String userLoginOrId);
    AddressDTO getDefaultAddressForUser(String userLoginOrId);
    AddressDTO createAddress(String userLoginOrId, AddressFormDTO form);
    AddressDTO updateAddress(String userLoginOrId, AddressFormDTO form);
    void deleteAddress(String userLoginOrId, Integer addressId);
    void setDefault(String userLoginOrId, Integer addressId);
}
