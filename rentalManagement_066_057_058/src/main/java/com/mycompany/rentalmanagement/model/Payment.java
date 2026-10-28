package com.mycompany.rentalmanagement.model;
import java.math.BigDecimal;import java.time.LocalDate;import java.time.LocalDateTime;
public class Payment { public long id,rentalId; public BigDecimal amount; public String method,referenceNumber; public LocalDateTime paidAt; }
