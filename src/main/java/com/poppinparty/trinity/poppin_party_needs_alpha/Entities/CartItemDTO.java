package com.poppinparty.trinity.poppin_party_needs_alpha.Entities;

public class CartItemDTO {
    private Long productId;
    private String itemName;
    private String imageLoc;
    private Double unitPrice;
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

    private int quantity;
    private Double subtotal;

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
}

