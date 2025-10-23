package com.example.Alotrabong.entity;

enum RoleCode { ADMIN, BRANCH_MANAGER, SHIPPER, USER }


enum MediaType { image, video }


enum PaymentMethod { COD, VNPAY, MOMO }


enum PaymentStatus { UNPAID, PAID, REFUNDED } // 0,1,2


enum OrderStatus { NEW, CONFIRMED, SHIPPING, DELIVERED, CANCELED, REFUNDED } // 0..5


enum ShipmentStatus { ASSIGNED, IN_TRANSIT, DELIVERED, CANCELED } // 0..3


enum PromoType { PERCENT, AMOUNT, COMBO, FREESHIP }


enum DiscountType { PERCENT, AMOUNT }


enum NotificationType { ORDER_STATUS, PROMO }


enum OtpPurpose { SIGNUP, RESET_PWD }