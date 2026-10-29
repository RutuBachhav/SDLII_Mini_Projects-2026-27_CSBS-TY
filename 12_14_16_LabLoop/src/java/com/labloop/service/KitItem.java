package com.labloop.service;

import java.io.Serializable;

/** One line in a Practical Kit. Kept small and Serializable because it lives in the user's session. */
public class KitItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long equipmentId;
    private final String assetTag;
    private final String name;
    private final String labCode;

    public KitItem(Long equipmentId, String assetTag, String name, String labCode) {
        this.equipmentId = equipmentId;
        this.assetTag = assetTag;
        this.name = name;
        this.labCode = labCode;
    }

    public Long getEquipmentId() { return equipmentId; }
    public String getAssetTag() { return assetTag; }
    public String getName() { return name; }
    public String getLabCode() { return labCode; }
}
