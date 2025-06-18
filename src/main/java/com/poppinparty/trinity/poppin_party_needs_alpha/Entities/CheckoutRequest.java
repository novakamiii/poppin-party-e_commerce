package com.poppinparty.trinity.poppin_party_needs_alpha.Entities;

import java.util.List;

public class CheckoutRequest {
    private List<CartItemDTO> items;
    private String paymentMethod;
    private String shippingOption;
    private String shippingAddress;
    public List<CartItemDTO> getItems() {
        return items;
    }
    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    public String getShippingOption() {
        return shippingOption;
    }
    public void setShippingOption(String shippingOption) {
        this.shippingOption = shippingOption;
    }
    public String getShippingAddress() {
        return shippingAddress;
    }
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
