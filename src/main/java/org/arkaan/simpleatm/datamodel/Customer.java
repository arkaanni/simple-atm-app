package org.arkaan.simpleatm.datamodel;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private final String name;
    private Integer balance;
    private final List<Transaction> transactionList;

    Customer(String name, Integer balance) {
        transactionList = new ArrayList<>();
        this.name = name;
        this.balance = balance;
    }

    void addBalance(Integer amount) {
        balance += amount;
    }

    void reduceBalance(Integer amount) {
        balance -= amount;
    }

    String getName() {
        return name;
    }

    Integer getBalance() {
        return balance;
    }

    void addTransaction(Transaction transaction) {
        transactionList.add(transaction);
    }

    List<Transaction> getTransactionList() {
        return transactionList;
    }
}
