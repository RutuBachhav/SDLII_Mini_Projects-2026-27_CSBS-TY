package com.mycompany.hostelmanagementsystem.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "room")
public class Room implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "room_no", unique = true, nullable = false, length = 20)
    private String roomNo;

    @Column(name = "room_type", nullable = false, length = 20)
    private String roomType;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "current_occupancy", nullable = false)
    private Integer currentOccupancy = 0;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "AVAILABLE";

    @OneToMany(mappedBy = "room")
    private List<Student> students;

    public Room() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getCurrentOccupancy() { return currentOccupancy; }
    public void setCurrentOccupancy(Integer currentOccupancy) { this.currentOccupancy = currentOccupancy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Student> getStudents() { return students; }
    public void setStudents(List<Student> students) { this.students = students; }
}
