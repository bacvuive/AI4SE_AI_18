package com.example.blood.service;

public interface NotificationService {
    boolean pushToHospital(int hospitalId, String message);
}