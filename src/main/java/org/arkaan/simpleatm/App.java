package org.arkaan.simpleatm;

import org.arkaan.simpleatm.datamodel.Card;
import org.arkaan.simpleatm.datamodel.ATMRepository;
import org.arkaan.simpleatm.datamodel.Transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

public class App {
    enum State {
        AUTHENTICATED,
        IDLE,
        OFFLINE
    }

    private Card currentUser;
    private final ATMRepository atmRepository;
    private State state;
    private final Scanner stdIn;
    private final Random random;
    private final DateTimeFormatter dateTimeFormatter;

    App(ATMRepository atmRepository) {
        this.atmRepository = atmRepository;
        state = State.IDLE;
        stdIn = new Scanner(System.in);
        random = new Random();
        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    private void setCurrentUser(Card currentUser) {
        this.currentUser = currentUser;
    }

    void authenticate() {
        System.out.println("Enter your account number:");
        String accountNumber = stdIn.next();
        if (!validateAuth(accountNumber, "Account Number")) return;
        System.out.println("Enter your pin:");
        String pin = stdIn.next();
        if (!validateAuth(pin, "PIN")) return;
        Optional<Card> auth = atmRepository.findOne(Integer.valueOf(accountNumber));
        boolean isValid = false;
        if (auth.isPresent()) {
            Card card = auth.get();
            if (Integer.valueOf(pin).equals(card.getPin())) {
                setCurrentUser(card);
                state = State.AUTHENTICATED;
                isValid = true;
            }
        }
        if (!isValid) System.out.println("Invalid account number / pin");
    }
    
    private boolean validateAuth(String input, String s) {
	if (input.length() != 6) {
	    System.out.printf("%s should have 6 digits length%n", s);
	    return false;
	}
	if (!input.matches("^[0-9]+")) {
	    System.out.printf("%s should only contains number%n", s);
	    return false;
	}
	return true;
    }

    private void withdrawMoney() {
        Integer[] amountList = {10, 50, 100};
        System.out.println("==============\n1. $10\n2. $50\n3. $100\n4. Other\n5. Back\n");
        System.out.print("Choose amount: ");
        int amountSelect = stdIn.nextInt();
        int amount;
        if (amountSelect == 4) {
            while (true) {
        	System.out.print("Enter amount: ");
        	String amountInput = stdIn.next();
                if (!amountInput.matches("^[0-9]+")) {
                    System.out.println("Invalid amount");
                    continue;
                }
                amount = Integer.valueOf(amountInput);
                if (amount % 10 != 0) {
                    System.out.println("Invalid amount");
                    continue;
                }
                if (amount > 1000.0) {
                    System.out.println("Maximum amount to withdraw is $1000");
                    continue;
                }
                break;
            }
        } else if (amountSelect >= 5) {
            return;
        } else {
            amount = amountList[amountSelect - 1];
        }
        try {
            String date = LocalDateTime.now().format(dateTimeFormatter);
            Transaction.Status status = atmRepository
                    .withdrawMoney(amount, currentUser, date);
            if (status == Transaction.Status.FAILED) {
                System.out.println("Withdraw failed.");
            } else {
        	System.out.println("================================");
                System.out.printf("Summary (%s) %nAmount: $%d %n", date, amount);
                System.out.println("================================\n");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Please select appropriate input.");
        }
    }

    private void depositMoney() {
        Integer[] amountList = {10, 50, 100};
        System.out.println("==============\n1. $10\n2. $50\n3. $100\n4. Other\n5. Back\n");
        int amount = stdIn.nextInt();
        try {
            Transaction.Status status = atmRepository
                    .depositMoney(amountList[amount - 1], currentUser);
            if (status == Transaction.Status.FAILED) {
                System.out.println("Deposit failed.");
            } else {
                System.out.println("Deposit success.");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Please select appropriate input.");
        }
    }

    private void transferMoney() {
        System.out.println("=================");
        System.out.println("||  Transfer  ||");
        System.out.print("Enter account number (destination): ");
        stdIn.nextLine();
        String destinationInput = stdIn.nextLine();
        if (destinationInput.isBlank()) return;
        System.out.print("Enter amount: ");
        String amountInput = stdIn.nextLine();
        if (amountInput.isBlank()) return;
        
        String ref = String.format("%04d", random.nextInt(999999));
        System.out.println("\n=================================");
        System.out.printf("Transfer Confirmation %nDestination account\t: %s %nAmount\t\t\t: $%s %nRef. Number\t\t: %s %n",
        	destinationInput, amountInput, ref);
        System.out.println("=================================\n");
        System.out.println("1. Confirm transfer\n2. Cancel");
        int confirm = stdIn.nextInt();
        if (confirm == 2) return;
        
        if (!destinationInput.matches("^[0-9]+")) {
            System.out.println("Invalid account");
            return;
        }
        
        if (!amountInput.matches("^[0-9]+")) {
            System.out.println("Invalid amount");
            return;
        }
        
        Integer amount = Integer.valueOf(amountInput);
        if (amount < 1) {
            System.out.println("Minimum amount to transfer is $1");
            return;
        }
        
        if (amount > 1000) {
            System.out.println("Maximum amount to transfer is $1000");
            return;
        }
        
        String date = LocalDateTime.now().format(dateTimeFormatter);
        
        Transaction.Status status = atmRepository
                .transferMoney(amount, currentUser, destinationInput, ref, date);
        if (status == Transaction.Status.FAILED) {
            System.out.println("Transfer failed.\n");
        } else {
            System.out.println("\n=================================");
            System.out.printf("Transfer Summary (%s) %nDestination account\t: %s %nAmount\t\t\t: $%s %nRef. Number\t\t: %s %nBalance\t\t\t: $%d %n",
            	date, destinationInput, amountInput, ref, currentUser.getAccountBalance());
            System.out.println("=================================\n");
        }
    }

    private void viewTransactionHistory() {
        atmRepository.displayTransactionHistory(currentUser);
    }

    private void getBalance() {
        System.out.printf("Your balance: $%d%n%n", currentUser.getAccountBalance());
    }

    State getState() {
        return state;
    }

    void displayMenu() {
        int selected = 99;
        while (selected != 0) {
            System.out.println("=====================");
            System.out.println("||     Welcome     ||");
            System.out.println("=====================");
            System.out.println("1. Withdraw");
            System.out.println("2. Transfer");
            System.out.println("3. Deposit");
            System.out.println("4. View Balance");
            System.out.println("5. Transaction History");
            System.out.println("0. Exit");
            selected = stdIn.nextInt();
            switch (selected) {
                case 1: {
                    withdrawMoney();
                    break;
                }
                case 2: {
                    transferMoney();
                    break;
                }
                case 3: {
                    depositMoney();
                    break;
                }
                case 4: {
                    getBalance();
                    break;
                }
                case 5: {
                    viewTransactionHistory();
                    break;
                }
                case 0: {
                    setCurrentUser(null);
                    stdIn.reset();
                    state = State.IDLE;
                }
            }
        }
    }
}

class SimpleAtm {

    public static void main(String[] args) {
        ATMRepository atmRepository = new ATMRepository();
        atmRepository.addAccount(new Card(123456, "user1", 1_000, 776643));
        atmRepository.addAccount(new Card(123456, "user2", 1_000, 774921));
        atmRepository.addAccount(new Card(123456, "user3", 1_000, 777106));
        App app = new App(atmRepository);

        do {
            switch (app.getState()) {
                case IDLE: {
                    app.authenticate();
                    break;
                }
                case AUTHENTICATED: {
                    app.displayMenu();
                    break;
                }
                case OFFLINE: {
                    break;
                }
            }
        } while (app.getState() != App.State.OFFLINE);
    }
}
