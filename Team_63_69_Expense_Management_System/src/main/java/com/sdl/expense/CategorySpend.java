package com.sdl.expense;

import java.math.BigDecimal;

public class CategorySpend {
    private final String category;
    private final BigDecimal amount;
    private final BigDecimal sharePercent;
    public CategorySpend(String category,BigDecimal amount,BigDecimal sharePercent){this.category=category;this.amount=amount;this.sharePercent=sharePercent;}
    public String getCategory(){return category;}
    public BigDecimal getAmount(){return amount;}
    public BigDecimal getSharePercent(){return sharePercent;}
}
