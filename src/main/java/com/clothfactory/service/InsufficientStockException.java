package com.clothfactory.service;

public class InsufficientStockException extends Exception {
    public InsufficientStockException() {
        super("Stock-out quantity exceeds available stock.");
    }
}