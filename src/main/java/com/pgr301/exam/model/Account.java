package com.pgr301.exam.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
@Data
@RequiredArgsConstructor
public class Account {
    
    //This is a class

    private String currency = "NOK";
    private String id;
    private BigDecimal balance = BigDecimal.valueOf(0);

    public BigDecimal getBalance() {
        return balance;
    }
}
