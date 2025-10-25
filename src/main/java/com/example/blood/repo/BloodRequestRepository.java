package com.example.blood.repo;
import com.example.blood.model.BloodRequest;
import com.example.blood.model.Status;
import java.util.Optional;
public interface BloodRequestRepository {
  boolean existsPending(int hospitalId, String bloodType);
  BloodRequest save(BloodRequest req);
  Optional<BloodRequest> findById(int requestId);
  void updateStatus(int requestId, Status newStatus, Integer approvedBy, String reason);
}