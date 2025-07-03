import java.util.Scanner;

class BankAccount {
    private String ownerName;
    private int accountNumber;
    private double balance;

    public BankAccount(String ownerName, int accountNumber, double initialBalance) {
        this.ownerName = ownerName;
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Deposited $" + amount + ". New Balance: $" + balance);
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            System.out.println("Withdrew $" + amount + ". New Balance: $" + balance);
        } else {
            System.out.println("Insufficient balance or invalid amount.");
        }
    }

    public void transfer(BankAccount recipient, double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            recipient.balance += amount;
            System.out.println("Transferred $" + amount + " to " + recipient.ownerName);
            System.out.println("Your new balance: $" + balance);
        } else {
            System.out.println("Transfer failed: Insufficient balance or invalid amount.");
        }
    }

    public void checkBalance() {
        System.out.println("Account Holder: " + ownerName);
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Current Balance: $" + balance);
    }
}

public class BankingSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Creating two accounts for demo
        BankAccount userAccount = new BankAccount("John ", 1001, 5000);
        BankAccount anotherAccount = new BankAccount("Ali", 1002, 3000);

        int choice;
        do {
            System.out.println("\n--- Banking System Menu ---");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. Check Balance");
            System.out.println("5. Exit");
            System.out.print("Enter your choice (1-5): ");
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter amount to deposit: $");
                    double depAmount = sc.nextDouble();
                    userAccount.deposit(depAmount);
                    break;
                case 2:
                    System.out.print("Enter amount to withdraw: $");
                    double withAmount = sc.nextDouble();
                    userAccount.withdraw(withAmount);
                    break;
                case 3:
                    System.out.print("Enter amount to transfer to Ali: $");
                    double transferAmount = sc.nextDouble();
                    userAccount.transfer(anotherAccount, transferAmount);
                    break;
                case 4:
                    userAccount.checkBalance();
                    break;
                case 5:
                    System.out.println("Thank you for using our banking system!");
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        } while (choice != 5);

        sc.close();
    }
}
