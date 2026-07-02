package com.etiya.notificationservice.services.concretes;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etiya.notificationservice.entities.Notification;
import com.etiya.notificationservice.events.OrderCreatedEvent;
import com.etiya.notificationservice.events.PaymentCreatedEvent;
import com.etiya.notificationservice.events.ProductStockUpdatedEvent;
import com.etiya.notificationservice.repositories.NotificationRepository;

/**
 * Turns consumed order/payment/stock events into persisted notifications.
 *
 * <p><b>Idempotent consumer:</b> Kafka only guarantees at-least-once delivery, so each method
 * checks {@link NotificationRepository#existsByEventTypeAndSourceId} before inserting - a
 * redelivered message for an already-recorded {@code (eventType, sourceId)} is skipped.</p>
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private static final String ORDER_CREATED_EVENT_TYPE = "OrderCreated";
    private static final String PAYMENT_CREATED_EVENT_TYPE = "PaymentCreated";
    private static final String PRODUCT_STOCK_UPDATED_EVENT_TYPE = "ProductStockUpdated";

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void recordOrderCreated(OrderCreatedEvent event) {
        String sourceId = String.valueOf(event.orderId());
        if (alreadyRecorded(ORDER_CREATED_EVENT_TYPE, sourceId)) {
            return;
        }
        String message = "Yeni sipariş oluşturuldu: #%d, müşteri %d, tutar %s"
                .formatted(event.orderId(), event.customerId(), event.totalPrice());
        save(ORDER_CREATED_EVENT_TYPE, sourceId, message);
    }

    @Transactional
    public void recordPaymentCreated(PaymentCreatedEvent event) {
        String sourceId = String.valueOf(event.paymentId());
        if (alreadyRecorded(PAYMENT_CREATED_EVENT_TYPE, sourceId)) {
            return;
        }
        String message = "Ödeme %s: sipariş #%d, tutar %s"
                .formatted(event.status(), event.orderId(), event.amount());
        save(PAYMENT_CREATED_EVENT_TYPE, sourceId, message);
    }

    @Transactional
    public void recordProductStockUpdated(ProductStockUpdatedEvent event) {
        String sourceId = event.productId() + "-" + event.orderId();
        if (alreadyRecorded(PRODUCT_STOCK_UPDATED_EVENT_TYPE, sourceId)) {
            return;
        }
        String message = "Stok güncellendi: ürün #%d, kalan stok %d"
                .formatted(event.productId(), event.remainingStock());
        save(PRODUCT_STOCK_UPDATED_EVENT_TYPE, sourceId, message);
    }

    private boolean alreadyRecorded(String eventType, String sourceId) {
        if (notificationRepository.existsByEventTypeAndSourceId(eventType, sourceId)) {
            log.info("{} event for {} already processed, skipping (idempotent)", eventType, sourceId);
            return true;
        }
        return false;
    }

    private void save(String eventType, String sourceId, String message) {
        notificationRepository.save(new Notification(eventType, sourceId, message, Instant.now()));
        log.info("Notification recorded: {}", message);
    }
}
