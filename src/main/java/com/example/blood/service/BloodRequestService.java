package com.example.blood.service;

import com.example.blood.model.BloodRequest;
import com.example.blood.model.Status;
import com.example.blood.repo.*;

import java.time.Instant;
import java.util.Set;

public class BloodRequestService {
    private final HospitalRepository hospitals;
    private final BloodRequestRepository requests;
    private final AdminRepository admins;
    private final BloodInventoryRepository inventory;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private static final Set<String> VALID_TYPES = Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    public BloodRequestService(HospitalRepository hospitals, BloodRequestRepository requests, AdminRepository admins,
                               BloodInventoryRepository inventory, EmailService emailService, NotificationService notificationService) {
        this.hospitals = hospitals;
        this.requests = requests;
        this.admins = admins;
        this.inventory = inventory;
        this.emailService = emailService;
        this.notificationService = notificationService;
    }

    public boolean createBloodRequest(int hospitalId, String bloodType, int quantity) {
        if (!hospitals.existsById(hospitalId)) return false;
        if (quantity <= 0) return false;
        if (bloodType == null || !VALID_TYPES.contains(bloodType)) return false;
        if (requests.existsPending(hospitalId, bloodType)) return false;
        BloodRequest req = new BloodRequest();
        req.setHospitalId(hospitalId);
        req.setBloodType(bloodType);
        req.setQuantity(quantity);
        req.setStatus(Status.PENDING);
        req.setCreatedAt(Instant.now());
        return requests.save(req) != null;
    }

    public boolean approveRequest(int requestId, int adminId) {
        if (!admins.existsById(adminId)) return false;
        BloodRequest r = requests.findById(requestId).orElse(null);
        if (r == null || r.getStatus() != Status.PENDING) return false;
        requests.updateStatus(requestId, Status.APPROVED, adminId, null);
        return true;
    }

    public boolean rejectRequest(int requestId, String reason) {
        if (reason == null || reason.isBlank()) return false;
        BloodRequest r = requests.findById(requestId).orElse(null);
        if (r == null || r.getStatus() != Status.PENDING) return false;
        requests.updateStatus(requestId, Status.REJECTED, null, reason);
        return true;
    }

    public boolean fulfillRequest(int requestId) {
        BloodRequest r = requests.findById(requestId).orElse(null);
        if (r == null || r.getStatus() != Status.APPROVED) return false;
        int available = inventory.getAvailable(r.getBloodType());
        if (available < r.getQuantity()) throw new IllegalStateException("Insufficient stock");
        inventory.deduct(r.getBloodType(), r.getQuantity());
        requests.updateStatus(requestId, Status.COMPLETED, r.getApprovedBy(), null);
        return true;
    }

    public boolean notifyHospital(int requestId) {
        BloodRequest r = requests.findById(requestId).orElse(null);
        if (r == null) return false;
        Status s = r.getStatus();
        if (!(s == Status.APPROVED || s == Status.REJECTED || s == Status.COMPLETED)) return false;
        String to = hospitals.findEmailById(r.getHospitalId()).orElse(null);
        if (to == null || to.isBlank()) return false;
        String subject = "Blood Request " + s;
        String body = "Request #" + r.getId() + " for " + r.getBloodType() + " (" + r.getQuantity() + " units) is " + s
                + (s == Status.REJECTED ? (". Reason: " + r.getReason()) : "");
        boolean e = emailService.send(to, subject, body);
        boolean n = notificationService.pushToHospital(r.getHospitalId(), "Request " + r.getId() + " is " + s);
        return e && n;
    }
}