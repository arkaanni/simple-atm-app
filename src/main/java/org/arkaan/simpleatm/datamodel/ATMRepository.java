package org.arkaan.simpleatm.datamodel;

import org.arkaan.simpleatm.datamodel.Transaction.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ATMRepository {
    private final List<Card> availableAccountList = new ArrayList<>();

    public void addAccount(Card account) {
        availableAccountList.add(account);
    }

    public void closeAccount(Card account) {
        availableAccountList.remove(account);
    }

    public Optional<Card> findOne(Integer accountNumber) {
        Card result = null;
        for (Card c : availableAccountList) {
            if (c.getAccountNumber().equals(accountNumber)) {
                result = c;
                break;
            }
        }
        return Optional.ofNullable(result);
    }

    private Optional<Card> findByAccountNumber(Integer accountNumber) {
        Card result = null;
        for (Card c : availableAccountList) {
            if (c.getAccountNumber().equals(accountNumber)) {
                result = c;
                break;
            }
        }
        return Optional.ofNullable(result);
    }

    public Status withdrawMoney(Integer amount, Card card, String date) {
        Customer customer = card.getCustomer();
        Status status;
        if (amount > customer.getBalance()) {
            System.out.printf("Insufficient balance: $%d %n", amount);
            status = Status.FAILED;
        } else {
            customer.reduceBalance(amount);
            status = Status.SUCCESS;
        }
        String desc = String.format("Amount: $%d [%s]", amount, date);
        customer.addTransaction(
                new Transaction(Type.WITHDRAWAL, desc, status));
        return status;
    }

    public Status depositMoney(Integer amount, Card card) {
        Customer customer = card.getCustomer();
        customer.addBalance(amount);
        customer.addTransaction(
                new Transaction(Type.DEPOSIT, "Amount: " + amount, Status.SUCCESS));
        return Status.SUCCESS;
    }

    public Status transferMoney(Integer amount, Card from, String to, String ref, String date) {
        Optional<Card> byAccountNumber = findByAccountNumber(Integer.valueOf(to));
        Customer sender = from.getCustomer();
        Status status;
        if (byAccountNumber.isPresent()) {
            if (sender.getBalance() > amount) {
                Customer recipient = byAccountNumber.get().getCustomer();
                sender.reduceBalance(amount);
                recipient.addBalance(amount);
                status = Status.SUCCESS;
                String msg = String.format("Destination: xxx%s. Amount: $%d. Ref: %s. [%s]",
                	to.substring(to.length() - 3), amount, ref, date);
                sender.addTransaction(
                        new Transaction(Type.TRANSFER, msg, status));
            } else {
        	String msg = String.format("Insufficient balance: $%d", amount);
        	System.out.println(msg);
        	status = Status.FAILED;
        	sender.addTransaction(
                        new Transaction(Type.TRANSFER, String.format("%s [%s] %n", msg, date), status));
            }
        } else {
            System.out.println("Invalid account");
            status = Status.FAILED;
        }
        return status;
    }

    public void displayTransactionHistory(Card card) {
        Customer customer = card.getCustomer();
        System.out.println("===================\nTransaction History\n");
        System.out.printf("%-5s %-15s %-10s %s %n", "NO", "TYPE", "STATUS", "DETAIL");
        int i = 1;
        for (Transaction tr : customer.getTransactionList()) {
            System.out.printf("%-5s %-15s %-10s %s %n", i++, tr.getType(), tr.getStatus(), tr.getDetail());
        }
        System.out.println();
    }
}
