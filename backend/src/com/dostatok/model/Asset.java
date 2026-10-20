package com.dostatok.model;

import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain model representing physical capital assets (Movable or Immovable).
 */
public class Asset {
    private int id;
    private int userId;
    private String name;
    private String assetCategory; // MOVABLE, IMMOVABLE
    private BigDecimal purchaseValue;
    private BigDecimal currentValue;
    private String acquisitionDate;
    private String notes;
    private String createdAt;

    public Asset() {
        this.purchaseValue = BigDecimal.ZERO;
        this.currentValue = BigDecimal.ZERO;
        this.assetCategory = "MOVABLE";
    }

    public Asset(int id, int userId, String name, String assetCategory,
                 BigDecimal purchaseValue, BigDecimal currentValue,
                 String acquisitionDate, String notes, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.assetCategory = assetCategory != null ? assetCategory : "MOVABLE";
        this.purchaseValue = CurrencyUtil.round(purchaseValue);
        this.currentValue = CurrencyUtil.round(currentValue);
        this.acquisitionDate = acquisitionDate;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // Business Methods
    public BigDecimal getValueChange() {
        return CurrencyUtil.round(currentValue.subtract(purchaseValue));
    }

    public BigDecimal getAppreciationPercentage() {
        if (purchaseValue == null || purchaseValue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal diff = currentValue.subtract(purchaseValue);
        return diff.multiply(BigDecimal.valueOf(100)).divide(purchaseValue, 2, RoundingMode.HALF_UP);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssetCategory() {
        return assetCategory;
    }

    public void setAssetCategory(String assetCategory) {
        this.assetCategory = assetCategory;
    }

    public BigDecimal getPurchaseValue() {
        return purchaseValue;
    }

    public void setPurchaseValue(BigDecimal purchaseValue) {
        this.purchaseValue = CurrencyUtil.round(purchaseValue);
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = CurrencyUtil.round(currentValue);
    }

    public String getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(String acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

