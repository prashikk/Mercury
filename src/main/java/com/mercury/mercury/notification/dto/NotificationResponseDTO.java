package com.mercury.mercury.notification.dto;

import com.mercury.mercury.notification.domain.Enum.NotificationStatus;
import com.mercury.mercury.notification.domain.Enum.NotificationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponseDTO {

    private Long notificationId;
    private Long userId;
    private Long tradeId;
    private NotificationType type;
    private String title;
    private String message;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
