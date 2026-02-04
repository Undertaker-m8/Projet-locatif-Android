package com.example.miniprojet.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Advertisement implements Serializable {
    private String id;
    private String userId;
    private String userEmail;
    private String userName;
    private String title;
    private String description;
    private double price;
    private double surface;
    private int rooms;
    private String address;
    private String city;
    private double latitude;
    private double longitude;
    private List<String> amenities = new ArrayList<>();
    private boolean hasParking;
    private boolean hasElevator;
    private boolean hasBalcony;
    private List<String> imageUrls = new ArrayList<>();
    private double rating;
    private int reviewCount;
    private Date createdAt;
    private Date updatedAt;
    private double distance; // Pour la distance de l'utilisateur

    // Constructeurs
    public Advertisement() {
        this.rating = 0.0;
        this.reviewCount = 0;
        this.imageUrls = new ArrayList<>();
        this.amenities = new ArrayList<>();
        this.distance = 0.0;
    }

    public Advertisement(String id, String userId, String title, String description,
                         double price, double surface, int rooms, String address, String city) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.surface = surface;
        this.rooms = rooms;
        this.address = address;
        this.city = city;
        this.rating = 0.0;
        this.reviewCount = 0;
        this.imageUrls = new ArrayList<>();
        this.amenities = new ArrayList<>();
        this.distance = 0.0;
    }

    
    // Formate le prix (ex: "750 €")
    public String getFormattedPrice() {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.FRANCE);
        format.setMaximumFractionDigits(0);
        return format.format(price) + " €";
    }

    // Formate la surface (ex: "35 m²")
    public String getFormattedSurface() {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.FRANCE);
        format.setMaximumFractionDigits(0);
        return format.format(surface) + " m²";
    }

    // Alias pour reviewCount (pour compatibilité)
    public int getRatingCount() {
        return reviewCount;
    }

    // Getter pour distance
    public double getDistance() {
        return distance;
    }

    // Setter pour distance
    public void setDistance(double distance) {
        this.distance = distance;
    }

    // Récupère la première image URL (pour compatibilité)
    public String getImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return null;
    }

    // Formate le nombre de pièces
    public String getFormattedRooms() {
        if (rooms == 1) {
            return rooms + " pièce";
        } else {
            return rooms + " pièces";
        }
    }

    // Formate l'évaluation
    public String getFormattedRating() {
        return String.format(Locale.FRANCE, "%.1f", rating);
    }

    // Vérifie si l'annonce a des images
    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }

    // Récupère l'adresse complète
    public String getFullAddress() {
        return address + ", " + city;
    }

    

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getSurface() { return surface; }
    public void setSurface(double surface) { this.surface = surface; }

    public int getRooms() { return rooms; }
    public void setRooms(int rooms) { this.rooms = rooms; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }

    public boolean isHasParking() { return hasParking; }
    public void setHasParking(boolean hasParking) { this.hasParking = hasParking; }

    public boolean isHasElevator() { return hasElevator; }
    public void setHasElevator(boolean hasElevator) { this.hasElevator = hasElevator; }

    public boolean isHasBalcony() { return hasBalcony; }
    public void setHasBalcony(boolean hasBalcony) { this.hasBalcony = hasBalcony; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
