package com.example.blood.service;

import com.example.blood.model.BloodRequest;
import com.example.blood.model.Status;
import com.example.blood.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BloodRequestServiceRejectTest {
    @Mock HospitalRepository hospitals;
    @Mock BloodRequestRepository requests;
    @Mock AdminRepository admins;
    @Mock BloodInventoryRepository inventory;
    @Mock EmailService email;
    @Mock NotificationService notification;
    @InjectMocks BloodRequestService svc;

    @BeforeEach
    void init() { MockitoAnnotations.openMocks(this); }

    @Test
    void reject_pending_reasonOK_true() {
        BloodRequest r = new BloodRequest(201, 1, "O+", 2, Status.PENDING, null, Instant.now(), null);
        when(requests.findById(201)).thenReturn(Optional.of(r));

        assertTrue(svc.rejectRequest(201, "duplicated"));
        verify(requests).updateStatus(201, Status.REJECTED, null, "duplicated");
    }

    @Test
    void reject_reason_blank_false() {
        assertFalse(svc.rejectRequest(202, " "));
        verify(requests, never()).updateStatus(anyInt(), any(), any(), any());
    }

    @Test
    void reject_reason_null_false() {
        assertFalse(svc.rejectRequest(203, null));
        verify(requests, never()).updateStatus(anyInt(), any(), any(), any());
    }

    @Test
    void reject_notFound_false() {
        when(requests.findById(204)).thenReturn(Optional.empty());

        assertFalse(svc.rejectRequest(204, "why"));
        verify(requests, never()).updateStatus(anyInt(), any(), any(), any());
    }

    @Test
    void reject_status_notPending_false() {
        BloodRequest r = new BloodRequest(205, 1, "A+", 1, Status.APPROVED, null, Instant.now(), 9);
        when(requests.findById(205)).thenReturn(Optional.of(r));

        assertFalse(svc.rejectRequest(205, "reason"));
        verify(requests, never()).updateStatus(anyInt(), any(), any(), any());
    }
}
