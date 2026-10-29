package com.driveflow.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Rental {
    public enum Status { ACTIVE, RETURNED, CANCELLED }
    private int id, vehicleId, customerId; private LocalDate startDate,endDate,returnDate; private double totalAmount; private Status status;
    public Rental() {}
    public Rental(int id,int vehicleId,int customerId,LocalDate startDate,LocalDate endDate,double totalAmount,Status status){this.id=id;this.vehicleId=vehicleId;this.customerId=customerId;this.startDate=startDate;this.endDate=endDate;this.totalAmount=totalAmount;this.status=status;}
    public int getId(){return id;} public void setId(int v){id=v;}
    public int getVehicleId(){return vehicleId;} public void setVehicleId(int v){vehicleId=v;}
    public int getCustomerId(){return customerId;} public void setCustomerId(int v){customerId=v;}
    public LocalDate getStartDate(){return startDate;} public void setStartDate(LocalDate v){startDate=v;}
    public LocalDate getEndDate(){return endDate;} public void setEndDate(LocalDate v){endDate=v;}
    public LocalDate getReturnDate(){return returnDate;} public void setReturnDate(LocalDate v){returnDate=v;}
    public double getTotalAmount(){return totalAmount;} public void setTotalAmount(double v){totalAmount=v;}
    public Status getStatus(){return status;} public void setStatus(Status v){status=v;}
    public long getDays(){return Math.max(1, ChronoUnit.DAYS.between(startDate,endDate));}
}
