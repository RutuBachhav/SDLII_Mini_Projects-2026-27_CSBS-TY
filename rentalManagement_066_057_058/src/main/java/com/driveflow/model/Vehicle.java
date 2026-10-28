package com.driveflow.model;

public class Vehicle {
    public enum Status { AVAILABLE, RENTED, MAINTENANCE }
    private int id;
    private String registrationNo, brand, model, type, fuelType;
    private int year;
    private double dailyRate;
    private Status status;

    public Vehicle() {}
    public Vehicle(int id, String registrationNo, String brand, String model, String type, String fuelType, int year, double dailyRate, Status status) {
        this.id=id; this.registrationNo=registrationNo; this.brand=brand; this.model=model; this.type=type; this.fuelType=fuelType; this.year=year; this.dailyRate=dailyRate; this.status=status;
    }
    public int getId(){return id;} public void setId(int v){id=v;}
    public String getRegistrationNo(){return registrationNo;} public void setRegistrationNo(String v){registrationNo=v;}
    public String getBrand(){return brand;} public void setBrand(String v){brand=v;}
    public String getModel(){return model;} public void setModel(String v){model=v;}
    public String getType(){return type;} public void setType(String v){type=v;}
    public String getFuelType(){return fuelType;} public void setFuelType(String v){fuelType=v;}
    public int getYear(){return year;} public void setYear(int v){year=v;}
    public double getDailyRate(){return dailyRate;} public void setDailyRate(double v){dailyRate=v;}
    public Status getStatus(){return status;} public void setStatus(Status v){status=v;}
    public String getDisplayName(){return brand+" "+model;}
}
