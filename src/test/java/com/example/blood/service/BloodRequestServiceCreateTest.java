package com.example.blood.service;

import com.example.blood.model.BloodRequest;
import com.example.blood.model.Status;
import com.example.blood.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BloodRequestServiceCreateTest {
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
    void create_ok() {
        when(hospitals.existsById(1)).thenReturn(true);
        when(requests.existsPending(1, "O+")).thenReturn(false);
        when(requests.save(any(BloodRequest.class))).thenAnswer(i -> i.getArgument(0));
        assertTrue(svc.createBloodRequest(1, "O+", 3));
        verify(requests).save(argThat(r -> r.getStatus() == Status.PENDING && r.getQuantity() == 3));
    }

    @Test
    void create_hospitalNotFound_false() {
        when(hospitals.existsById(9)).thenReturn(false);
        assertFalse(svc.createBloodRequest(9, "A+", 2));
        verify(requests, never()).save(any());
    }

    @Test
    void create_invalidQuantity_false() {
        when(hospitals.existsById(1)).thenReturn(true);
        assertFalse(svc.createBloodRequest(1, "A+", 0));
    }

    @Test
    void create_invalidBloodType_false() {
        when(hospitals.existsById(1)).thenReturn(true);
        assertFalse(svc.createBloodRequest(1, "X1", 1));
    }

    @Test
    void create_duplicatePending_false() {
        when(hospitals.existsById(1)).thenReturn(true);
        when(requests.existsPending(1, "A+")).thenReturn(true);
        assertFalse(svc.createBloodRequest(1, "A+", 1));
    }

    @Test
    void create_bloodType_null_false() {
        when(hospitals.existsById(1)).thenReturn(true);
        assertFalse(svc.createBloodRequest(1, null, 1));
        verify(requests, never()).save(any());
    }

    @Test
    void create_bloodType_blank_false() {
        when(hospitals.existsById(1)).thenReturn(true);
        assertFalse(svc.createBloodRequest(1, " ", 1));
        verify(requests, never()).save(any());
    }

    @Test
    void create_hospital_not_found_false() {
        when(hospitals.existsById(999)).thenReturn(false);
        assertFalse(svc.createBloodRequest(999, "O+", 2));
        verify(requests, never()).save(any());
    }

}
