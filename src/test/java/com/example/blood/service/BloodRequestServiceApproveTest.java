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

class BloodRequestServiceApproveTest {
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
    void approve_pending_adminOK_true() {
        when(admins.existsById(10)).thenReturn(true);
        BloodRequest r = new BloodRequest(101, 1, "O+", 2, Status.PENDING, null, Instant.now(), null);
        when(requests.findById(101)).thenReturn(Optional.of(r));

        assertTrue(svc.approveRequest(101, 10));

        // verify đầy đủ các interaction với requests
        verify(requests).findById(101);
        verify(requests).updateStatus(101, Status.APPROVED, 10, null);
        verifyNoMoreInteractions(requests);
    }

    @Test
    void approve_notFound_false() {
        when(admins.existsById(10)).thenReturn(true);
        when(requests.findById(999)).thenReturn(Optional.empty());

        assertFalse(svc.approveRequest(999, 10));

        verify(requests).findById(999);
        verify(requests, never()).updateStatus(anyInt(), any(), any(), any());
        verifyNoMoreInteractions(requests);
    }

    @Test
    void approve_status_notPending_false() {
        when(admins.existsById(10)).thenReturn(true);
        BloodRequest r = new BloodRequest(102, 1, "A+", 1, Status.REJECTED, "dup", Instant.now(), null);
        when(requests.findById(102)).thenReturn(Optional.of(r));

        assertFalse(svc.approveRequest(102, 10));

        verify(requests).findById(102);
        verify(requests, never()).updateStatus(anyInt(), any(), any(), any());
        verifyNoMoreInteractions(requests);
    }

    @Test
    void approve_admin_invalid_false() {
        // Khi admin không hợp lệ, service trả về sớm và không đụng tới requests
        when(admins.existsById(-1)).thenReturn(false);

        assertFalse(svc.approveRequest(103, -1));

        verifyNoInteractions(requests);
    }
}
