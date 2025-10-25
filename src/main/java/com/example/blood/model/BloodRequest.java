package com.example.blood.model;

import java.time.Instant;

public class BloodRequest {
    private int id;
    private int hospitalId;
    private String bloodType;
    private int quantity;
    private Status status;
    private String reason;
    private Instant createdAt;
    private Integer approvedBy;

    public BloodRequest() {
    }

    public BloodRequest(int id, int hospitalId, String bloodType, int quantity, Status status, String reason, Instant createdAt, Integer approvedBy) {
        this.id = id;
        this.hospitalId = hospitalId;
        this.bloodType = bloodType;
        this.quantity = quantity;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
        this.approvedBy = approvedBy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(int hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }
}