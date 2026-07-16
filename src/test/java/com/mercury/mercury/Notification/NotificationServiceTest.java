package com.mercury.mercury.Notification;

import com.mercury.mercury.User.service.AuthenticatedUserService; // 💡 Added import
import com.mercury.mercury.notification.domain.Enum.NotificationStatus;
import com.mercury.mercury.notification.domain.Enum.NotificationType;
import com.mercury.mercury.notification.domain.NotificationEntity;
import com.mercury.mercury.notification.dto.NotificationResponseDTO;
import com.mercury.mercury.notification.repository.NotificationRepository;
import com.mercury.mercury.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepo;

    @Mock
    private AuthenticatedUserService authenticatedUserService; // 💡 Injected missing security dependency mock

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        // 💡 Lenient stubbing handles mutual execution setups gracefully across distinct tests
        lenient().when(authenticatedUserService.getCurrentUserId()).thenReturn(1L);
        lenient().when(authenticatedUserService.getCurrentUsername()).thenReturn("test_user");
    }

    @Test
    @DisplayName("Test 1: Verify Trade Created Notification generation template constraints")
    void testCreateTradeNotification() {
        notificationService.createTradeNotification(101L);

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepo, times(1)).save(captor.capture());

        NotificationEntity saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals(101L, saved.getTradeId());
        assertEquals(NotificationType.TRADE_CREATED, saved.getType());
        assertEquals("Trade Created Successfully", saved.getTitle());
        assertEquals(NotificationStatus.PENDING, saved.getStatus());
    }

    @Test
    @DisplayName("Test 2: Verify Trade Approved Notification generation template constraints")
    void testCreateApprovalNotification() {
        notificationService.createApprovalNotification(102L);

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepo, times(1)).save(captor.capture());

        NotificationEntity saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals(NotificationType.TRADE_APPROVED, saved.getType());
        assertEquals("Trade Approved", saved.getTitle());
    }

    @Test
    @DisplayName("Test 3: Verify Trade Settled Notification generation template constraints")
    void testCreateSettlementNotification() {
        notificationService.createSettlementNotification(103L, "SET-20260715-000001");

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepo, times(1)).save(captor.capture());

        NotificationEntity saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals(NotificationType.TRADE_SETTLED, saved.getType());
        assertTrue(saved.getMessage().contains("SET-20260715-000001"));
    }

    @Test
    @DisplayName("Test 4: Verify Portfolio Updated Notification generation template constraints")
    void testCreatePortfolioNotification() {
        notificationService.createPortfolioNotification(104L, 550L);

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepo, times(1)).save(captor.capture());

        NotificationEntity saved = captor.getValue();
        assertEquals(1L, saved.getUserId());
        assertEquals(NotificationType.PORTFOLIO_UPDATED, saved.getType());
        assertTrue(saved.getMessage().contains("Current Quantity: 550"));
    }

    @Test
    @DisplayName("Test 5: Verify Spring Data Pageable pagination transformation maps")
    void testGetUserNotificationsPagination() {
        NotificationEntity item = new NotificationEntity();
        item.setNotificationId(1L);
        item.setUserId(10L);
        item.setType(NotificationType.TRADE_CREATED);
        item.setTitle("Trade Created Successfully");
        item.setMessage("Test message content");
        item.setStatus(NotificationStatus.PENDING);

        Pageable pageable = PageRequest.of(0, 10);
        Page<NotificationEntity> mockPage = new PageImpl<>(List.of(item), pageable, 1);

        when(notificationRepo.findByUserId(10L, pageable)).thenReturn(mockPage);

        Page<NotificationResponseDTO> result = notificationService.getUserNotifications(10L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Trade Created Successfully", result.getContent().get(0).getTitle());
        verify(notificationRepo, times(1)).findByUserId(10L, pageable);
    }
}