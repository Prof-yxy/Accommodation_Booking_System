package com.camping.dto;

import java.util.List;

/**
 * 预订创建 DTO
 */
public class BookingCreateDTO {
    private Long userId;
    private Long typeId;
    private String checkIn;
    private String checkOut;
    private List<EquipSelectDTO> equipments;
    private Integer quantity;
    private String guestName;
    private String guestPhone;

    public BookingCreateDTO() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }

    public List<EquipSelectDTO> getEquipments() {
        return equipments;
    }

    public void setEquipments(List<EquipSelectDTO> equipments) {
        this.equipments = equipments;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }
}
