package com.poppinparty.trinity.poppin_party_needs_alpha.Entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;


@Entity
@Table(name = "archived_order_items")
public class ArchivedOrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "product_ref")
    private String productRef;

    private int quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    // === Custom Tarpaulin Fields ===
    @Column(name = "is_custom")
    private boolean isCustom = false; // true = custom tarpaulin

    @Column(name = "custom_size")
    private String customSize;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "personalized_message", length = 255)
    private String personalizedMessage;

    @Column(name = "tarpaulin_thickness")
    private String tarpaulinThickness;

    @Column(name = "tarpaulin_finish")
    private String tarpaulinFinish;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getProductRef() {
        return productRef;
    }

    public void setProductRef(String productRef) {
        this.productRef = productRef;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean isCustom) {
        this.isCustom = isCustom;
    }

    public String getCustomSize() {
        return customSize;
    }

    public void setCustomSize(String customSize) {
        this.customSize = customSize;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPersonalizedMessage() {
        return personalizedMessage;
    }

    public void setPersonalizedMessage(String personalizedMessage) {
        this.personalizedMessage = personalizedMessage;
    }

    public String getTarpaulinThickness() {
        return tarpaulinThickness;
    }

    public void setTarpaulinThickness(String tarpaulinThickness) {
        this.tarpaulinThickness = tarpaulinThickness;
    }

    public String getTarpaulinFinish() {
        return tarpaulinFinish;
    }

    public void setTarpaulinFinish(String tarpaulinFinish) {
        this.tarpaulinFinish = tarpaulinFinish;
    }

    
}
