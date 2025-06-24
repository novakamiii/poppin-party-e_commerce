package com.poppinparty.trinity.poppin_party_needs_alpha.Entities;

public class CartItemDTO {
    private Long id;
    private Long productId;
    private String itemName;
    private String imageLoc;
    private Double unitPrice;
    private int quantity;
    private Double subtotal;

    private boolean isCustom;
    private String customSize;
    private String eventType;
    private String personalizedMessage;
    private String tarpaulinThickness;
    private String tarpaulinFinish;

    

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Getters & Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getImageLoc() {
        return imageLoc;
    }

    public void setImageLoc(String imageLoc) {
        this.imageLoc = imageLoc;
    }

    public Double getSubtotal() {
        return unitPrice * quantity;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
