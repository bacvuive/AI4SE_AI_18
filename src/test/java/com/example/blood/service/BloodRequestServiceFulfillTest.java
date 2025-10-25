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

class BloodRequestServiceFulfillTest {
    @Mock
    HospitalRepository hospitals;
    @Mock
    BloodRequestRepository requests;
    @Mock
    AdminRepository admins;
    @Mock
    BloodInventoryRepository inventory;
    @Mock
    EmailService email;
    @Mock
    NotificationService notif;
    @InjectMocks
    BloodRequestService svc;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void fulfill_ok_inventoryEnough() {
        BloodRequest r = new BloodRequest(300, 1, "B+", 5, Status.APPROVED, null, Instant.now(), 11);
        when(requests.findById(300)).thenReturn(Optional.of(r));
        when(inventory.getAvailable("B+")).thenReturn(8);
        assertTrue(svc.fulfillRequest(300));
        verify(inventory).deduct("B+", 5);
        verify(requests).updateStatus(300, Status.COMPLETED, 11, null);
    }

    @Test
    void fulfill_insufficientStock_throw() {
        BloodRequest r = new BloodRequest(301, 1, "B+", 5, Status.APPROVED, null, Instant.now(), 11);
        when(requests.findById(301)).thenReturn(Optional.of(r));
        when(inventory.getAvailable("B+")).thenReturn(3);
        assertThrows(IllegalStateException.class, () -> svc.fulfillRequest(301));
        verify(inventory, never()).deduct(anyString(), anyInt());
        verify(requests, never()).updateStatus(anyInt(), any(), any(), any());
    }

    @Test
    void fulfill_notApproved_false() {
        BloodRequest r = new BloodRequest(302, 1, "B+", 5, Status.REJECTED, "no", Instant.now(), null);
        when(requests.findById(302)).thenReturn(Optional.of(r));
        assertFalse(svc.fulfillRequest(302));
    }

    @Test
    void fulfill_notFound_false() {
        when(requests.findById(303)).thenReturn(Optional.empty());
        assertFalse(svc.fulfillRequest(303));
    }

    @Test
    void fulfill_not_found_false() {
        when(requests.findById(707)).thenReturn(java.util.Optional.empty());
        assertFalse(svc.fulfillRequest(707));
        verify(requests).findById(707);
        verify(inventory, never()).deduct(anyString(), anyInt());
        verify(requests, never()).updateStatus(anyInt(), any(), any(), any());
        verifyNoMoreInteractions(requests);
    }

    @Test
    void fulfill_stock_boundary_eq_true() {
        BloodRequest r = new BloodRequest(708, 1, "B+", 3, Status.APPROVED, null, java.time.Instant.now(), 5);
        when(requests.findById(708)).thenReturn(java.util.Optional.of(r));
        when(inventory.getAvailable("B+")).thenReturn(3);

        assertTrue(svc.fulfillRequest(708));
        verify(requests).findById(708);
        verify(inventory).getAvailable("B+");
        verify(inventory).deduct("B+", 3);
        verify(requests).updateStatus(708, Status.COMPLETED, 5, null);
    }

}
