import java.util.ArrayList;

/** A bank account. Fields are private (encapsulation); access is through methods. */
public class Account {

    private final String accountId;
    private final String holderName;
    private final String pin;
    private double balance;
    private final ArrayList<Transaction> history = new ArrayList<>();

    public Account(String accountId, String holderName, String pin, double balance) {
        this.accountId = accountId;
        this.holderName = holderName;
        this.pin = pin;
        this.balance = balance;
    }

    // ---------- getters ----------
    public String getAccountId() { return accountId; }
    public String getHolderName() { return holderName; }
    public double getBalance() { return balance; }

    /** Returns a copy so outside code cannot change the real history. */
    public ArrayList<Transaction> getHistory() { return new ArrayList<>(history); }

    public boolean checkPin(String input) {
        return pin.equals(input);
    }

    // ---------- operations ----------
    public void deposit(double amount) {
        balance += amount;
        history.add(new Transaction("DEPOSIT", amount, balance, "Cash deposit"));
    }

    /** Returns false (and changes nothing) if the balance is too low. */
    public boolean withdraw(double amount) {
        if (amount > balance) {
            return false;
        }
        balance -= amount;
        history.add(new Transaction("WITHDRAW", amount, balance, "Cash withdrawal"));
        return true;
    }

    public boolean transferOut(double amount, String toAccountId) {
        if (amount > balance) {
            return false;
        }
        balance -= amount;
        history.add(new Transaction("TRANSFER OUT", amount, balance, "To account " + toAccountId));
        return true;
    }

    public void transferIn(double amount, String fromAccountId) {
        balance += amount;
        history.add(new Transaction("TRANSFER IN", amount, balance, "From account " + fromAccountId));
    }
}