package com.example.Alotrabong.entity;

public enum ShipmentStatus {
    ASSIGNED(0),      // 0: Assigned
    IN_TRANSIT(1),    // 1: Đang giao  
    DELIVERED(2),     // 2: Đã giao
    CANCELED(3);      // 3: Hủy

    private final int code;

    ShipmentStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ShipmentStatus fromCode(int code) {
        for (ShipmentStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid shipment status code: " + code);
    }
}
