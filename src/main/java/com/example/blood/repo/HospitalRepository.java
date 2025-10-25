package com.example.blood.repo;

import java.util.Optional;

public interface HospitalRepository {
    boolean existsById(int hospitalId);

    Optional<String> findEmailById(int hospitalId);
}