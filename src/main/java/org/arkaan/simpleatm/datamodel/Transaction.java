package org.arkaan.simpleatm.datamodel;

public class Transaction {

    private volatile int increment;

    public enum Status {
        SUCCESS,
        FAILED
    }
    
    private final Integer id;
    private final Type type;
    private final String detail;
    private final Status status;
    private final int accountNumber;

    public Transaction(Type type, String detail, Status status, int accountNumber) {
        synchronized (this) {
            this.type = type;
            this.detail = detail;
            this.status = status;
            this.accountNumber = accountNumber;
            id = increment++;
        }
    }

    public Integer getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getDetail() {
        return detail;
    }

    public Status getStatus() {
        return status;
    }
    
    public int getAccountNumber() {
        return accountNumber;
    }
    
    @Override
    public String toString() {
        return String.join(",", type.toString(), detail, status.toString(), String.valueOf(accountNumber), "\n");
    }
}
