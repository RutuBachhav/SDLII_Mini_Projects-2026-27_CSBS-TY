package com.driveflow.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Payment {
    public enum Method { CASH, CARD, UPI, BANK_TRANSFER }
    public enum Status { PAID, PENDING, REFUNDED }
    private int id,rentalId; private double amount; private Method method; private Status status; private LocalDateTime paidAt;
    public Payment() {}
    public Payment(int id,int rentalId,double amount,Method method,Status status,LocalDateTime paidAt){this.id=id;this.rentalId=rentalId;this.amount=amount;this.method=method;this.status=status;this.paidAt=paidAt;}
    public int getId(){return id;} public int getRentalId(){return rentalId;} public double getAmount(){return amount;} public Method getMethod(){return method;} public Status getStatus(){return status;} public LocalDateTime getPaidAt(){return paidAt;}
    public String getPaidAtText(){return paidAt==null?"-":paidAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));}
}
