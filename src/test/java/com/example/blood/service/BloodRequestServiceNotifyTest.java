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

class BloodRequestServiceNotifyTest {
    @Mock HospitalRepository hospitals;
    @Mock BloodRequestRepository requests;
    @Mock AdminRepository admins;
    @Mock BloodInventoryRepository inventory;
    @Mock EmailService email;
    @Mock NotificationService notif;
    @InjectMocks BloodRequestService svc;

    @BeforeEach
    void init() { MockitoAnnotations.openMocks(this); }

    @Test
    void notify_completed_ok() {
        BloodRequest r = new BloodRequest(400, 5, "O-", 2, Status.COMPLETED, null, Instant.now(), 10);
        when(requests.findById(400)).thenReturn(Optional.of(r));
        when(hospitals.findEmailById(5)).thenReturn(Optional.of("h@hos.com"));
        when(email.send(anyString(), anyString(), anyString())).thenReturn(true);
        when(notif.pushToHospital(eq(5), anyString())).thenReturn(true);

        assertTrue(svc.notifyHospital(400));

        verify(requests).findById(400);
        verify(hospitals).findEmailById(5);
        verify(email).send(
                eq("h@hos.com"),
                eq("Blood Request COMPLETED"),
                argThat(b -> b.contains("Request #400") && b.contains("COMPLETED"))
        );
        verify(notif).pushToHospital(eq(5), argThat(m -> m.contains("Request 400") && m.contains("COMPLETED")));
        verifyNoMoreInteractions(requests);
    }

    @Test
    void notify_missingEmail_false() {
        BloodRequest r = new BloodRequest(401, 5, "O-", 2, Status.APPROVED, null, Instant.now(), 10);
        when(requests.findById(401)).thenReturn(Optional.of(r));
        when(hospitals.findEmailById(5)).thenReturn(Optional.empty());

        assertFalse(svc.notifyHospital(401));

        verify(requests).findById(401);
        verify(hospitals).findEmailById(5);
        verify(email, never()).send(anyString(), anyString(), anyString());
        verify(notif, never()).pushToHospital(anyInt(), anyString());
        verifyNoMoreInteractions(requests);
    }

    @Test
    void notify_invalidStatus_false() {
        BloodRequest r = new BloodRequest(402, 5, "O-", 2, Status.PENDING, null, Instant.now(), null);
        when(requests.findById(402)).thenReturn(Optional.of(r));

        assertFalse(svc.notifyHospital(402));

        verify(requests).findById(402);
        verify(hospitals, never()).findEmailById(anyInt());
        verify(email, never()).send(anyString(), anyString(), anyString());
        verify(notif, never()).pushToHospital(anyInt(), anyString());
        verifyNoMoreInteractions(requests);
    }

    @Test
    void notify_notFound_false() {
        when(requests.findById(499)).thenReturn(Optional.empty());

        assertFalse(svc.notifyHospital(499));

        verify(requests).findById(499);
        verify(hospitals, never()).findEmailById(anyInt());
        verify(email, never()).send(anyString(), anyString(), anyString());
        verify(notif, never()).pushToHospital(anyInt(), anyString());
        verifyNoMoreInteractions(requests);
    }

    @Test
    void notify_partial_emailOk_notifFail_false() {
        BloodRequest r = new BloodRequest(410, 7, "A+", 1, Status.APPROVED, null, Instant.now(), 33);
        when(requests.findById(410)).thenReturn(Optional.of(r));
        when(hospitals.findEmailById(7)).thenReturn(Optional.of("hos7@mail.com"));
        when(email.send(anyString(), anyString(), anyString())).thenReturn(true);
        when(notif.pushToHospital(eq(7), anyString())).thenReturn(false);

        assertFalse(svc.notifyHospital(410));

        verify(requests).findById(410);
        verify(hospitals).findEmailById(7);
        verify(email).send(
                eq("hos7@mail.com"),
                eq("Blood Request APPROVED"),
                argThat(b -> b.contains("Request #410") && b.contains("APPROVED"))
        );
        verify(notif).pushToHospital(eq(7), argThat(m -> m.contains("Request 410") && m.contains("APPROVED")));
        verifyNoMoreInteractions(requests);
    }

    @Test
    void notify_partial_emailFail_notifOk_false() {
        BloodRequest r = new BloodRequest(411, 8, "B-", 2, Status.REJECTED, null, Instant.now(), 44);
        when(requests.findById(411)).thenReturn(Optional.of(r));
        when(hospitals.findEmailById(8)).thenReturn(Optional.of("hos8@mail.com"));
        when(email.send(anyString(), anyString(), anyString())).thenReturn(false);
        when(notif.pushToHospital(eq(8), anyString())).thenReturn(true);

        assertFalse(svc.notifyHospital(411));

        verify(requests).findById(411);
        verify(hospitals).findEmailById(8);
        verify(email).send(
                eq("hos8@mail.com"),
                eq("Blood Request REJECTED"),
                argThat(b -> b.contains("Request #411") && b.contains("REJECTED"))
        );
        verify(notif).pushToHospital(eq(8), argThat(m -> m.contains("Request 411") && m.contains("REJECTED")));
        verifyNoMoreInteractions(requests);
    }

    @Test
    void notify_both_fail_false() {
        BloodRequest r = new BloodRequest(412, 9, "AB+", 3, Status.COMPLETED, null, Instant.now(), 55);
        when(requests.findById(412)).thenReturn(Optional.of(r));
        when(hospitals.findEmailById(9)).thenReturn(Optional.of("hos9@mail.com"));
        when(email.send(anyString(), anyString(), anyString())).thenReturn(false);
        when(notif.pushToHospital(eq(9), anyString())).thenReturn(false);

        assertFalse(svc.notifyHospital(412));

        verify(requests).findById(412);
        verify(hospitals).findEmailById(9);
        verify(email).send(
                eq("hos9@mail.com"),
                eq("Blood Request COMPLETED"),
                argThat(b -> b.contains("Request #412") && b.contains("COMPLETED"))
        );
        verify(notif).pushToHospital(eq(9), argThat(m -> m.contains("Request 412") && m.contains("COMPLETED")));
        verifyNoMoreInteractions(requests);
    }
}
