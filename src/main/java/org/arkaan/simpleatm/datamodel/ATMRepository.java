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

    private Optional<Card> findByAccountNumber(Long accountNumber) {
        Card result = null;
        for (Card c : availableAccountList) {
            if (Long.valueOf(c.getAccountNumber()).equals(accountNumber)) {
                result = c;
                break;
            }
        }
        return Optional.ofNullable(result);
    }

    public Status withdrawMoney(Integer amount, Card card) {
        Customer customer = card.getCustomer();
        Status status;
        if (amount > customer.getBalance()) {
            System.out.printf("Insufficient balance: %d", amount);
            status = Status.FAILED;
        } else {
            customer.reduceBalance(amount);
            status = Status.SUCCESS;
        }
        customer.addTransaction(
                new Transaction(Type.WITHDRAWAL, "Amount: " + amount, status));
        return status;
    }

    public Status depositMoney(Integer amount, Card card) {
        Customer customer = card.getCustomer();
        customer.addBalance(amount);
        customer.addTransaction(
                new Transaction(Type.DEPOSIT, "Amount: " + amount, Status.SUCCESS));
        return Status.SUCCESS;
    }

    public Status transferMoney(Integer amount, Card from, Long to) {
        Optional<Card> byAccountNumber = findByAccountNumber(to);
        Customer sender = from.getCustomer();
        Status status;
        if (byAccountNumber.isPresent() && sender.getBalance() > amount) {
            Customer recipient = byAccountNumber.get().getCustomer();
            sender.reduceBalance(amount);
            recipient.addBalance(amount);
            status = Status.SUCCESS;
        } else {
            status = Status.FAILED;
        }
        sender.addTransaction(
                new Transaction(Type.TRANSFER, "Amount: " + amount, status));
        return status;
    }

    public void displayTransactionHistory(Card card) {
        Customer customer = card.getCustomer();
        System.out.println("===================");
        System.out.println("Transaction History");
        System.out.println();
        String header = String.format("%-5s %-15s %-10s %s %n", "NO", "TYPE", "STATUS", "DETAIL");
        System.out.printf(header);
        int i = 1;
        for (Transaction tr : customer.getTransactionList()) {
            System.out.printf("%-5s %-15s %-10s %s %n", i++, tr.getType(), tr.getStatus(), tr.getDetail());
        }
        System.out.println();
    }
}
