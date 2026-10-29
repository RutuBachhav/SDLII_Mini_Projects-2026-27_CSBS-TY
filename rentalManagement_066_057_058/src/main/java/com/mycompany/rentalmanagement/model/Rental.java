package com.mycompany.rentalmanagement.model;
import java.math.BigDecimal;import java.time.LocalDate;import java.time.LocalDateTime;
public class Rental { public long id,customerId,vehicleId; public String customerName,vehicleName,status; public LocalDate startDate,expectedReturnDate,actualReturnDate; public BigDecimal estimatedTotal,finalTotal,paid; }
