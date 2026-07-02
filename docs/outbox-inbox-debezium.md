# Transactional Outbox, Inbox (Idempotency) ve Debezium

Bu doküman, `order-service` ↔ `product-service` arasındaki olay tabanlı iletişimde kullanılan
üç deseni ve bunların projedeki kod karşılıklarını açıklar:

1. **Transactional Outbox Pattern** — event'i güvenli şekilde üretmek
2. **Debezium (CDC)** — outbox'tan Kafka'ya güvenli şekilde taşımak
3. **Inbox Pattern (Idempotency)** — event'i güvenli şekilde tüketmek

## Neden bu desenlere ihtiyaç var?

Bir servis hem kendi veritabanına yazıp hem de Kafka'ya mesaj basarsa, bu iki işlem **tek bir
atomik transaction** değildir: DB commit edilir ama Kafka'ya gönderim başarısız olursa (ya da
tam tersi) sistemler tutarsız kalır. Bu üç desen birlikte, "DB'ye yazılan her şey er ya da geç
Kafka'ya ulaşır, hiçbir mesaj asla iki kere işlenmez" garantisini sağlar.

---

## 1. Transactional Outbox Pattern

**Fikir:** Servis, Kafka'ya doğrudan mesaj basmak yerine, iş kaydıyla **aynı veritabanı
transaction'ı** içinde bir `outbox_events` tablosuna satır ekler. Kafka'ya gönderim işini
uygulama kodundan tamamen çıkarıp CDC'ye (Debezium) devreder.

### Şema — `outbox_events`

Her iki serviste de aynı yapıda bir tablo var:

| Kolon | Tip | Açıklama |
|---|---|---|
| `id` | bigint (identity) | PK |
| `aggregate_type` | varchar | Domain nesnesi, örn. `"Order"`, `"Product"` |
| `aggregate_id` | varchar | İlgili nesnenin id'si |
| `event_type` | varchar | Olay adı, örn. `"OrderCreated"` |
| `destination` | varchar | Hedef Kafka topic'i |
| `payload` | varchar(4000) | JSON serileştirilmiş event body |
| `created_at` | **timestamp (timezone'suz!)** | Bkz. [Debezium bölümündeki not](#önemli-detay-createdat-neden-timestamp-timezonesuz) |

Kod karşılığı:
- [order-service/.../outbox/OutboxEvent.java](../order-service/src/main/java/com/etiya/orderservice/outbox/OutboxEvent.java)
- [product-service/.../outbox/OutboxEvent.java](../product-service/src/main/java/com/etiya/productservice/outbox/OutboxEvent.java)

### Yazma katmanı — `OutboxService`

Her iki serviste `OutboxService.record(...)` metodu payload'ı JSON'a çevirip satırı ekler:

```java
// order-service/.../outbox/OutboxService.java
public OutboxEvent record(String aggregateType, String aggregateId, String eventType,
                          String destination, Object payload) {
    OutboxEvent event = new OutboxEvent(
            aggregateType, aggregateId, eventType, destination,
            serialize(payload), LocalDateTime.now());
    return outboxRepository.save(event);
}
```

### Kullanım noktaları

**order-service** — sipariş oluşturulunca `OrderCreated` event'i outbox'a yazılır:
```java
// order-service/.../services/concretes/OrderManager.java (add metodu)
@Transactional
public CreatedOrderResponse add(CreateOrderRequest request) {
    Order saved = orderRepository.save(order);
    outboxService.record("Order", String.valueOf(saved.getId()), "OrderCreated",
            "order-created", new OrderCreatedEvent(...));
    ...
}
```
`@Transactional` burada kritik: iş kaydı ile outbox insert'i aynı transaction'da olmalı ki biri
commit olup diğeri olmasın diye bir durum yaşanmasın (bkz. [OrderManager.java](../order-service/src/main/java/com/etiya/orderservice/services/concretes/OrderManager.java)).

**product-service** — tüketilen `OrderCreated` event'i işlenince (stok düşürülünce)
`ProductStockUpdated` event'i kendi outbox'ına yazılır:
```java
// product-service/.../services/concretes/ProductStockService.java
@Transactional
public void applyOrderCreated(OrderCreatedEvent event) {
    ...
    productRepository.save(product);
    outboxService.record("Product", ..., "ProductStockUpdated",
            "product-stock-updated", new ProductStockUpdatedEvent(...));
    processedEventRepository.save(new ProcessedEvent(...)); // bkz. Inbox bölümü
}
```
Bkz. [ProductStockService.java](../product-service/src/main/java/com/etiya/productservice/services/concretes/ProductStockService.java).

Böylece **her iki yönde** de outbox pattern zincirlenmiş durumda: order-service'in çıktısı
product-service'in girdisi, product-service'in çıktısı da (şu an tüketen kimse olmasa da) başka
bir servisin girdisi olabilecek şekilde yayınlanıyor.

---

## 2. Debezium (Change Data Capture)

**Fikir:** `outbox_events` tablosuna INSERT yapıldığında, Postgres bunu **write-ahead log
(WAL)**'a yazar. Debezium, Postgres'in mantıksal replikasyon (`logical replication`) özelliğini
kullanarak bu WAL'ı okur ve her satırı Kafka'ya basar — uygulama kodu Kafka'ya hiç dokunmaz.

### Altyapı parçaları — [infra/podman-compose.yml](../infra/podman-compose.yml)

- **postgres**: `wal_level=logical` ile başlatılıyor (WAL'ın mantıksal decode için yeterli
  detayda tutulmasını sağlar). `max_replication_slots` / `max_wal_senders` bu amaçla ayarlı.
- **kafka-connect** (`quay.io/debezium/connect:2.7`): Debezium'un PostgreSQL connector'ını
  içeren Kafka Connect worker'ı. REST API'si `:8083` portunda.
- **kafka**: Debezium'un ürettiği mesajların gittiği broker.

### Connector konfigürasyonları

İki ayrı connector var, her biri bir servisin DB'sini dinliyor:
- [infra/debezium/order-outbox-connector.json](../infra/debezium/order-outbox-connector.json) → `orderdb`
- [infra/debezium/product-outbox-connector.json](../infra/debezium/product-outbox-connector.json) → `productdb`

Her connector'ın kalbi **Outbox Event Router SMT** (Single Message Transform):

```json
"table.include.list": "public.outbox_events",
"transforms": "outbox",
"transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
"transforms.outbox.table.field.event.id": "id",
"transforms.outbox.table.field.event.key": "aggregate_id",
"transforms.outbox.table.field.event.type": "event_type",
"transforms.outbox.table.field.event.timestamp": "created_at",
"transforms.outbox.table.field.event.payload": "payload",
"transforms.outbox.route.by.field": "destination",
"transforms.outbox.route.topic.replacement": "${routedByValue}"
```

Bu SMT, ham WAL değişikliğini (bütün kolonlarıyla) almak yerine, `outbox_events` satırındaki
`destination` kolonunun değerini **doğrudan hedef Kafka topic adı** olarak kullanıp sadece
`payload` kolonunu mesaj body'si olarak gönderir. Yani outbox tablosuna
`destination="order-created"` yazılan bir satır otomatik olarak `order-created` topic'ine gider —
kod tarafında topic ismini hardcode etmek dışında Kafka'yla ilgili hiçbir şey yapılmıyor.

### Connector'ların kaydı — [infra/register-connectors.ps1](../infra/register-connectors.ps1)

Connector JSON dosyalarının var olması yetmez; Kafka Connect'in REST API'sine `POST` ile
kayıt edilmeleri gerekir. Bu script, `podman compose up -d`'den sonra çalıştırılıp her iki
connector'ı da (varsa günceller, yoksa oluşturur) kaydeder ve `RUNNING` durumunu doğrular.

### Önemli detay: `created_at` neden timestamp (timezone'suz)?

Geliştirme sırasında karşılaşılan gerçek bir hata: `createdAt` alanı ilk başta Java `Instant`
tipindeydi, bu da Postgres'te `timestamptz` (timezone'lu) kolona map ediliyordu. Debezium,
`timestamptz` kolonlarını **STRING** (ISO-8601, `ZonedTimestamp` logical type) olarak
serileştirir. Ama Outbox Event Router SMT'nin `event.timestamp` alanı **INT64** (epoch tabanlı)
bekliyor — bu da yalnızca timezone'suz `timestamp` kolonlarının ürettiği bir tip. Sonuç: connector
task'ı şu hatayla `FAILED` durumuna düşüyordu:

```
DataException: Field 'created_at' is not of type INT64
```

Düzeltme: `createdAt` alanı `LocalDateTime` yapıldı (→ Postgres `timestamp` kolonu, timezone'suz).
Bkz. `OutboxEvent.java` dosyalarındaki `@Column(columnDefinition = "TIMESTAMP")` notu.

---

## 3. Inbox Pattern (Idempotency)

**Fikir:** Kafka **at-least-once** (en az bir kez) teslimat garantisi verir — aynı mesaj,
consumer group rebalance'ı sonrası ya da outbox/Debezium tarafındaki bir retry nedeniyle **iki
kez** teslim edilebilir. Inbox pattern, bir event'in daha önce işlenip işlenmediğini kalıcı
olarak takip ederek bu tekrarları güvenle yutar.

### Şema — `processed_events` (sadece product-service'te)

| Kolon | Açıklama |
|---|---|
| `event_type` | Örn. `"OrderCreated"` |
| `source_id` | Event'i benzersiz kılan id (örn. sipariş id'si) |
| `processed_at` | İşlenme zamanı |

`(event_type, source_id)` üzerinde **unique constraint** var — aynı event iki kez insert
edilmeye çalışılırsa DB seviyesinde de engellenir.

Kod: [ProcessedEvent.java](../product-service/src/main/java/com/etiya/productservice/idempotency/ProcessedEvent.java)

### Kontrol noktası — `ProductStockService.applyOrderCreated`

```java
@Transactional
public void applyOrderCreated(OrderCreatedEvent event) {
    String sourceId = String.valueOf(event.orderId());
    if (processedEventRepository.existsByEventTypeAndSourceId("OrderCreated", sourceId)) {
        log.info("... already processed, skipping (idempotent)");
        return; // duplicate — hiçbir şey yapma
    }

    // stok güncelle + outbox'a ProductStockUpdated yaz + ProcessedEvent kaydet
    // -> üçü de AYNI @Transactional içinde
}
```

Kritik nokta: stok güncellemesi, outbox insert'i ve `ProcessedEvent` kaydı **tek transaction**
içinde yapılıyor. Bu sayede şu senaryo imkânsız hale geliyor: "event işlendi ama
`processed_events`'e yazılamadan servis çöktü" → event bir sonraki teslimatta tekrar (doğru
şekilde) işlenir. Ya da tam tersi: "işlem yapılmadı ama işlendi olarak işaretlendi" durumu da
oluşamaz, çünkü ikisi birlikte commit/rollback olur.

Tüketici tarafı: [OrderEventConsumer.java](../product-service/src/main/java/com/etiya/productservice/messaging/OrderEventConsumer.java)
(Spring Cloud Stream `@Bean Consumer<OrderCreatedEvent>`), gelen her mesajı doğrudan
`ProductStockService.applyOrderCreated`'a delege eder — idempotency kontrolü tamamen o metodun
içinde.

---

## Uçtan uca akış

```
┌────────────────┐   INSERT outbox_events    ┌──────────────┐
│  order-service  │ ─────────────────────────▶│  orderdb     │
│  (OrderManager) │   (aynı transaction'da)    │  (Postgres)  │
└────────────────┘                             └──────┬───────┘
                                                        │ WAL (logical replication)
                                                        ▼
                                              ┌───────────────────┐
                                              │  Debezium (Kafka   │
                                              │  Connect, :8083)   │
                                              │  EventRouter SMT   │
                                              └─────────┬──────────┘
                                                         │ topic: order-created
                                                         ▼
                                              ┌────────────────────┐
                                              │  product-service    │
                                              │  OrderEventConsumer │
                                              └─────────┬───────────┘
                                                         │ idempotency check (processed_events)
                                                         ▼
                                          stok güncelle + INSERT outbox_events (productdb)
                                                         │ WAL
                                                         ▼
                                              Debezium → topic: product-stock-updated
```

## İzleme / doğrulama araçları

- **Adminer** (`http://localhost:8081`) — `outbox_events` / `processed_events` tablolarını
  görsel olarak izlemek için (bkz. [podman-compose.yml](../infra/podman-compose.yml)).
- **Kafka console consumer** — topic'lerdeki mesajları canlı izlemek için:
  ```powershell
  podman exec etiya-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-created
  ```
- **Connector durumu**:
  ```powershell
  Invoke-RestMethod http://localhost:8083/connectors/order-outbox-connector/status
  ```

## Bilinen sınırlamalar / dikkat edilecekler

- `Product` ve `Order` domain entity'leri **in-memory**'dir (JPA entity değil) — sadece
  `outbox_events` ve `processed_events` gerçek Postgres tablolarıdır. Servis yeniden
  başlatıldığında ürün/sipariş verisi sıfırlanır, ama outbox/Debezium akışı bundan etkilenmez.
- `product-stock-updated` topic'ini şu an tüketen bir servis yok — outbox'tan Kafka'ya kadar
  akış çalışıyor, ama bu event bir sonraki adımda henüz kullanılmıyor.
- Kafka bir süre kapalı kalırsa (`delivery.timeout.ms` aşılırsa) connector task'ı `FAILED`
  olabilir; elle `POST /connectors/<name>/restart` gerekir. Ayrıca Kafka Connect worker'ı
  broker'a yeniden bağlandığında `scheduled.rebalance.max.delay.ms` (varsayılan 5 dakika)
  kadar connector'ları yeniden atamayı geciktirebilir — bu süre boyunca task durumu
  `UNASSIGNED`/stale görünebilir, veri kaybolmaz, sadece gecikir.
