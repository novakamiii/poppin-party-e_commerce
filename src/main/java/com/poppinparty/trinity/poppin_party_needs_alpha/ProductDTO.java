package com.poppinparty.trinity.poppin_party_needs_alpha;

public class ProductDTO {
    private Long id;
    private String itemName;
    private String imageLoc;
    private Double price;
    private Long stock;
    private String category;


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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
    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }
    public Long getStock() {
        return stock;
    }
    public void setStock(Long stock) {
        this.stock = stock;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

}

