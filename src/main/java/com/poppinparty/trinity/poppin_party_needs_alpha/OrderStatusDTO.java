package com.poppinparty.trinity.poppin_party_needs_alpha;

import java.math.BigDecimal;

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
    int quantity
) {}

