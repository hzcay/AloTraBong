package com.example.Alotrabong.service;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.example.Alotrabong.entity.Order;

public interface VnpayService {
	String createPaymentUrl(Order order) throws UnsupportedEncodingException;
	boolean handleReturn(java.util.Map<String, String> params);
	boolean validateReturnData(Map<String, String> params);
    String getPaymentStatus(Map<String, String> params);
}