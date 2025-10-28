package com.example.Alotrabong.service;

import java.io.UnsupportedEncodingException;

import com.example.Alotrabong.entity.Order;

public interface VnpayService {
	String createPaymentUrl(Order order) throws UnsupportedEncodingException;
	boolean handleReturn(java.util.Map<String, String> params);
}