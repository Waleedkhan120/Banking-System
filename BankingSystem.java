import java.util.Scanner;
import java.sql.*;

class BankAccount {
    private String ownerName;
    private int accountNumber;
    private double balance;
    private String pin;  // Added PIN for security

    // MySQL DB credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/banking_db";
    private static final String USER = "root";
    private static final String PASS = "0000";

    public BankAccount(int accountNumber, String pin) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        fetchAccountDetails();
    }

    private void fetchAccountDetails() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "SELECT owner_name, balance FROM bank_accounts WHERE account_number = ? AND pin = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, accountNumber);
            stmt.setString(2, pin);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ownerName = rs.getString("owner_name");
                balance = rs.getDouble("balance");
            } else {
                throw new IllegalArgumentException("Invalid account number or PIN");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateBalance() {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String sql = "UPDATE bank_accounts SET balance = ? WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, balance);
            stmt.setInt(2, accountNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static BankAccount createAccount(Scanner sc) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println("\n--- Create New Account ---");
            
            System.out.print("Enter your full name: ");
            String name = sc.nextLine();
            
            System.out.print("Create a 4-digit PIN: ");
            String pin = sc.nextLine();
            
            System.out.print("Initial deposit amount: $");
            double balance = sc.nextDouble();
            sc.nextLine();  // Consume newline
            
            // Generate account number
            String sql = "INSERT INTO bank_accounts (owner_name, pin, balance) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, name);
            stmt.setString(2, pin);
            stmt.setDouble(3, balance);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int accountNumber = rs.getInt(1);
                System.out.println("\nAccount created successfully!");
                System.out.println("Your account number: " + accountNumber);
                System.out.println("Remember your PIN and keep it secure!");
                return new BankAccount(accountNumber, pin);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            updateBalance();
            System.out.println("Deposited $" + amount + ". New Balance: $" + balance);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            updateBalance();
            System.out.println("Withdrew $" + amount + ". New Balance: $" + balance);
        } else {
            System.out.println("Insufficient balance or invalid amount.");
        }
    }

    public void transfer(Scanner sc) {
        System.out.print("Enter recipient account number: ");
        int recipientNumber = sc.nextInt();
        sc.nextLine();  // Consume newline
        
        System.out.print("Enter amount to transfer: $");
        double amount = sc.nextDouble();
        sc.nextLine();  // Consume newline

        if (amount <= 0 || balance < amount) {
            System.out.println("Transfer failed: Insufficient balance or invalid amount.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            // Verify recipient exists
            String verifySql = "SELECT owner_name FROM bank_accounts WHERE account_number = ?";
            PreparedStatement verifyStmt = conn.prepareStatement(verifySql);
            verifyStmt.setInt(1, recipientNumber);
            ResultSet rs = verifyStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Recipient account not found!");
                return;
            }
            String recipientName = rs.getString("owner_name");

            // Perform transfer
            conn.setAutoCommit(false);  // Start transaction
            
            // Deduct from sender
            String deductSql = "UPDATE bank_accounts SET balance = balance - ? WHERE account_number = ?";
            PreparedStatement deductStmt = conn.prepareStatement(deductSql);
            deductStmt.setDouble(1, amount);
            deductStmt.setInt(2, accountNumber);
            deductStmt.executeUpdate();

            // Add to recipient
            String addSql = "UPDATE bank_accounts SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement addStmt = conn.prepareStatement(addSql);
            addStmt.setDouble(1, amount);
            addStmt.setInt(2, recipientNumber);
            addStmt.executeUpdate();

            conn.commit();  // Commit transaction
            balance -= amount;  // Update local balance
            
            System.out.println("Transferred $" + amount + " to " + recipientName);
            System.out.println("Your new balance: $" + balance);
            
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Transfer failed due to technical error.");
        }
    }

    public void checkBalance() {
        System.out.println("\n--- Account Details ---");
        System.out.println("Account Holder: " + ownerName);
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Current Balance: $" + balance);
    }
}

public class BankingSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        BankAccount currentAccount = null;

        // Initialize database connection
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found!");
            return;
        }

        while (true) {
            if (currentAccount == null) {
                System.out.println("\n--- Welcome to Banking System ---");
                System.out.println("1. Login");
                System.out.println("2. Create New Account");
                System.out.println("3. Exit");
                System.out.print("Enter choice (1-3): ");
                
                int choice = sc.nextInt();
                sc.nextLine();  // Consume newline
                
                switch (choice) {
                    case 1:
                        System.out.print("\nEnter account number: ");
                        int accNumber = sc.nextInt();
                        sc.nextLine();  // Consume newline
                        
                        System.out.print("Enter PIN: ");
                        String pin = sc.nextLine();
                        
                        try {
                            currentAccount = new BankAccount(accNumber, pin);
                            System.out.println("\nLogin successful! Welcome, " + currentAccount.ownerName);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                        break;
                    case 2:
                        currentAccount = BankAccount.createAccount(sc);
                        break;
                    case 3:
                        System.out.println("Thank you for using our banking system!");
                        sc.close();
                        return;
                    default:
                        System.out.println("Invalid choice!");
                }
            } else {
                System.out.println("\n--- Banking Menu ---");
                System.out.println("1. Deposit");
                System.out.println("2. Withdraw");
                System.out.println("3. Transfer");
                System.out.println("4. Check Balance");
                System.out.println("5. Logout");
                System.out.print("Enter choice (1-5): ");
                
                int choice = sc.nextInt();
                sc.nextLine();  // Consume newline
                
                switch (choice) {
                    case 1:
                        System.out.print("Enter amount to deposit: $");
                        double depAmount = sc.nextDouble();
                        sc.nextLine();  // Consume newline
                        currentAccount.deposit(depAmount);
                        break;
                    case 2:
                        System.out.print("Enter amount to withdraw: $");
                        double withAmount = sc.nextDouble();
                        sc.nextLine();  // Consume newline
                        currentAccount.withdraw(withAmount);
                        break;
                    case 3:
                        currentAccount.transfer(sc);
                        break;
                    case 4:
                        currentAccount.checkBalance();
                        break;
                    case 5:
                        currentAccount = null;
                        System.out.println("Logged out successfully.");
                        break;
                    default:
                        System.out.println("Invalid choice!");
                }
            }
        }
    }
}
