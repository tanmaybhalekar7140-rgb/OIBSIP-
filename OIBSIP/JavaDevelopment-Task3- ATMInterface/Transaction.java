import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** One entry in the transaction history. */
public class Transaction {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final String type;
    private final double amount;
    private final double balanceAfter;
    private final String details;
    private final LocalDateTime time;

    public Transaction(String type, double amount, double balanceAfter, String details) {
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.details = details;
        this.time = LocalDateTime.now();
    }

    public String getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }
    public String getDetails() { return details; }
    public LocalDateTime getTime() { return time; }

    @Override
    public String toString() {
        return String.format("%s | %-12s | %10.2f | Balance: %10.2f | %s",
                time.format(FMT), type, amount, balanceAfter, details);
    }
}
