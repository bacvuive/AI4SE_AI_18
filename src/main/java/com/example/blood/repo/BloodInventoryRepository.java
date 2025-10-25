package com.example.blood.repo;

public interface BloodInventoryRepository {
    int getAvailable(String bloodType);

    void deduct(String bloodType, int qty);
}