package com.labloop.entity;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A physical laboratory room. One Lab has many Equipment items
 * (same idea as Department -> Employees in Assignment 3).
 */
@Entity
@Table(name = "LAB")
public class Lab implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Lab code is required")
    @Pattern(regexp = "[A-Z]{2,5}-\\d{3}", message = "Lab code must look like CSL-101")
    @Column(nullable = false, unique = true, length = 12)
    private String code;

    @NotBlank(message = "Lab name is required")
    @Size(min = 3, max = 80, message = "Lab name must be 3-80 characters")
    @Column(nullable = false, length = 80)
    private String name;

    @NotBlank(message = "Building / room is required")
    @Size(max = 60)
    @Column(length = 60)
    private String location;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 300, message = "Capacity cannot exceed 300")
    private int capacity;

    @NotBlank(message = "Lab in-charge is required")
    @Size(max = 60)
    @Column(length = 60)
    private String inCharge;

    /** Inverse side of the relationship. @JsonbTransient stops Lab -> Equipment -> Lab loops in REST JSON. */
    @OneToMany(mappedBy = "lab")
    @JsonbTransient
    private List<Equipment> equipment = new ArrayList<>();

    public Lab() { }

    public Lab(String code, String name, String location, int capacity, String inCharge) {
        this.code = code;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.inCharge = inCharge;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getInCharge() { return inCharge; }
    public void setInCharge(String inCharge) { this.inCharge = inCharge; }
    public List<Equipment> getEquipment() { return equipment; }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Lab)) return false;
        Lab other = (Lab) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public String toString() { return "Lab[" + code + "]"; }
}
