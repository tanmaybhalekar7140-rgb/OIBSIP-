import java.util.HashMap;
import java.util.Map;

/** Holds all accounts, checks logins and performs transfers between accounts. */
public class Bank {

    private final Map<String, Account> accounts = new HashMap<>();

    public Bank() {
        // Demo accounts: ID / PIN
        addAccount(new Account("1001", "Amit Sharma", "1234", 5000));
        addAccount(new Account("1002", "Riya Patil", "4321", 3000));
        addAccount(new Account("1003", "Rahul Deshmukh", "1111", 10000));
    }

    private void addAccount(Account account) {
        accounts.put(account.getAccountId(), account);
    }

    /** Returns the account if ID and PIN are correct, otherwise null. */
    public Account authenticate(String id, String pin) {
        Account account = accounts.get(id);
        if (account != null && account.checkPin(pin)) {
            return account;
        }
        return null;
    }

    public Account findAccount(String id) {
        return accounts.get(id);
    }

    /**
     * Moves money from one account to another.
     * Returns "OK" on success, otherwise a message explaining the problem.
     */
    public String transfer(Account from, String toId, double amount) {
        Account to = accounts.get(toId);
        if (to == null) {
            return "Recipient account not found.";
        }
        if (to == from) {
            return "You cannot transfer to your own account.";
        }
        if (!from.transferOut(amount, toId)) {
            return "Insufficient Funds";
        }
        to.transferIn(amount, from.getAccountId());
        return "OK";
    }
}