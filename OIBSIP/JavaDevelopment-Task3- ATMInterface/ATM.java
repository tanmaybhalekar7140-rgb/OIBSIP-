import java.util.ArrayList;
import java.util.Scanner;

/** The ATM screen: login, menu and all user interaction. */
public class ATM {

    private static final int MAX_ATTEMPTS = 3;

    private final Bank bank;
    private final Scanner sc = new Scanner(System.in);

    public ATM(Bank bank) {
        this.bank = bank;
    }

    public void start() {
        System.out.println("=====================================");
        System.out.println("          WELCOME TO JAVA ATM");
        System.out.println("=====================================");

        Account user = login();
        if (user == null) {
            System.out.println("\nToo many incorrect attempts. Access denied.");
            return;
        }
        System.out.println("\nLogin successful. Welcome, " + user.getHolderName() + "!");
        showMenu(user);
    }

    // ---------- login (max 3 attempts) ----------
    private Account login() {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            System.out.print("Enter User ID: ");
            String id = sc.nextLine().trim();
            System.out.print("Enter PIN: ");
            String pin = sc.nextLine().trim();

            Account account = bank.authenticate(id, pin);
            if (account != null) {
                return account;
            }
            int left = MAX_ATTEMPTS - attempt;
            if (left > 0) {
                System.out.println("Invalid User ID or PIN. Attempts left: " + left + "\n");
            }
        }
        return null;
    }

    // ---------- main menu ----------
    private void showMenu(Account user) {
        boolean running = true;
        while (running) {
            System.out.println("\n------------- MAIN MENU -------------");
            System.out.println("1. Transaction History");
            System.out.println("2. Withdraw");
            System.out.println("3. Deposit");
            System.out.println("4. Transfer");
            System.out.println("5. Quit");
            System.out.printf("Current balance: %.2f%n", user.getBalance());
            System.out.print("Choose an option (1-5): ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1":
                    showHistory(user);
                    break;
                case "2":
                    withdraw(user);
                    break;
                case "3":
                    deposit(user);
                    break;
                case "4":
                    transfer(user);
                    break;
                case "5":
                    System.out.println("\nThank you for using Java ATM. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 5.");
            }
        }
    }

    // ---------- 1. transaction history ----------
    private void showHistory(Account user) {
        ArrayList<Transaction> list = user.getHistory();
        System.out.println("\n========== TRANSACTION HISTORY ==========");
        if (list.isEmpty()) {
            System.out.println("No transactions yet in this session.");
            return;
        }
        int no = 1;
        for (Transaction t : list) {
            System.out.println(no++ + ". " + t);
        }
    }

    // ---------- 2. withdraw ----------
    private void withdraw(Account user) {
        double amount = readAmount("Enter amount to withdraw: ");
        if (amount < 0) return;

        if (amount > user.getBalance()) {                 // balance check first
            System.out.printf("Insufficient Funds. Available balance: %.2f%n", user.getBalance());
            return;
        }
        user.withdraw(amount);
        System.out.printf("Please collect your cash: %.2f%nNew balance: %.2f%n", amount, user.getBalance());
    }

    // ---------- 3. deposit ----------
    private void deposit(Account user) {
        double amount = readAmount("Enter amount to deposit: ");
        if (amount < 0) return;

        user.deposit(amount);
        System.out.printf("Deposited: %.2f%nNew balance: %.2f%n", amount, user.getBalance());
    }

    // ---------- 4. transfer ----------
    private void transfer(Account user) {
        System.out.print("Enter recipient Account ID: ");
        String toId = sc.nextLine().trim();
        double amount = readAmount("Enter amount to transfer: ");
        if (amount < 0) return;

        if (amount > user.getBalance()) {                 // balance check first
            System.out.printf("Insufficient Funds. Available balance: %.2f%n", user.getBalance());
            return;
        }
        String result = bank.transfer(user, toId, amount);
        if (result.equals("OK")) {
            System.out.printf("Transferred %.2f to account %s%nNew balance: %.2f%n",
                    amount, toId, user.getBalance());
        } else {
            System.out.println(result);
        }
    }

    /** Reads a positive number. Returns -1 if the input is invalid. */
    private double readAmount(String prompt) {
        System.out.print(prompt);
        try {
            double value = Double.parseDouble(sc.nextLine().trim());
            if (value <= 0) {
                System.out.println("Amount must be greater than zero.");
                return -1;
            }
            return value;
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a number.");
            return -1;
        }
    }
}