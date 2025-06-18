package com.poppinparty.trinity.poppin_party_needs_alpha.Entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "item_name")
    private String itemName;

    private double price;
    private long stock;
    private String category;
    private String description;

    @Column(name = "image_loc")
    private String imageLoc;

    @Column(name = "created_at")
    private String createdAt;

    // getters and setters
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public long getStock() {
        return stock;
    }
    public void setStock(long stock) {
        this.stock = stock;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageLoc() {
        return imageLoc;
    }
    public void setImageLoc(String imageLoc) {
        this.imageLoc = imageLoc;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}