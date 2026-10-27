package com.sdl.expense;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="expenses")
public class Expense {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=100) private String title;
    @Column(nullable=false, precision=10, scale=2) private BigDecimal amount;
    @Column(nullable=false, length=40) private String category;
    @Column(nullable=false) private LocalDate expenseDate;
    @Column(nullable=false, length=30) private String paymentMethod;
    @Column(length=500) private String description;
    protected Expense() {}
    public Expense(String title, BigDecimal amount, String category, LocalDate expenseDate, String paymentMethod, String description) {
        this.title=title; this.amount=amount; this.category=category; this.expenseDate=expenseDate; this.paymentMethod=paymentMethod; this.description=description;
    }
    public Long getId(){return id;} public String getTitle(){return title;} public BigDecimal getAmount(){return amount;}
    public String getCategory(){return category;} public LocalDate getExpenseDate(){return expenseDate;}
    public String getPaymentMethod(){return paymentMethod;} public String getDescription(){return description;}
    public void update(String title, BigDecimal amount, String category, LocalDate date, String method, String description) {
        this.title=title; this.amount=amount; this.category=category; this.expenseDate=date; this.paymentMethod=method; this.description=description;
    }
}
