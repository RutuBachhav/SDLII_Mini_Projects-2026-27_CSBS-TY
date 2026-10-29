package com.labloop.rest;

import com.labloop.entity.Equipment;
import com.labloop.entity.EquipmentCategory;
import java.time.LocalDate;

/**
 * Request body for POST/PUT /api/equipment. A separate DTO so clients send "labId": 2
 * instead of a whole nested Lab object, and cannot set the status or id directly.
 */
public class EquipmentRequest {

    private String assetTag;
    private String name;
    private String model;
    private EquipmentCategory category;
    private Long labId;
    private LocalDate purchaseDate;
    private double purchaseCost;
    private int serviceIntervalDays = 180;
    private LocalDate lastServicedOn;
    private String notes;

    public Equipment toEntity() {
        Equipment e = new Equipment();
        e.setAssetTag(assetTag);
        e.setName(name);
        e.setModel(model);
        e.setCategory(category);
        e.setPurchaseDate(purchaseDate);
        e.setPurchaseCost(purchaseCost);
        e.setServiceIntervalDays(serviceIntervalDays);
        e.setLastServicedOn(lastServicedOn);
        e.setNotes(notes);
        return e;
    }

    public String getAssetTag() { return assetTag; }
    public void setAssetTag(String assetTag) { this.assetTag = assetTag; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public EquipmentCategory getCategory() { return category; }
    public void setCategory(EquipmentCategory category) { this.category = category; }
    public Long getLabId() { return labId; }
    public void setLabId(Long labId) { this.labId = labId; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public double getPurchaseCost() { return purchaseCost; }
    public void setPurchaseCost(double purchaseCost) { this.purchaseCost = purchaseCost; }
    public int getServiceIntervalDays() { return serviceIntervalDays; }
    public void setServiceIntervalDays(int serviceIntervalDays) { this.serviceIntervalDays = serviceIntervalDays; }
    public LocalDate getLastServicedOn() { return lastServicedOn; }
    public void setLastServicedOn(LocalDate lastServicedOn) { this.lastServicedOn = lastServicedOn; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
