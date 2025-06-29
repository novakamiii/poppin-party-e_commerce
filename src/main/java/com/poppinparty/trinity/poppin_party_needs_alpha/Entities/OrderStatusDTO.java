package com.poppinparty.trinity.poppin_party_needs_alpha.Entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderStatusDTO(
    Long id,
    String itemName,
    BigDecimal amount,
    String imageLoc,
    int daysLeft,
    String shippingOption,
    String status,
    String transactionId,
    Long orderId,
    int quantity,
    LocalDateTime order_date
) {}

