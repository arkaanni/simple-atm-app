package org.arkaan.simpleatm.datamodel;

public class Card {
    private Integer pin;
    private final Customer customer;
    private final Integer accountNumber;

    public Card(Integer pin, String username, Integer initialBalance, Integer accountNumber) {
        this.pin = pin;
        customer = new Customer(username, initialBalance);
        this.accountNumber = accountNumber;
    }

    public Integer getPin() {
        return pin;
    }

    Customer getCustomer() {
        return customer;
    }

    public void setPin(Integer pin) {
        this.pin = pin;
    }

    public Integer getAccountBalance() {
        return customer.getBalance();
    }

    public Integer getAccountNumber() {
        return accountNumber;
    }
}
