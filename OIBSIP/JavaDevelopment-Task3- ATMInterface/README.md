# ATM Interface

A console-based ATM simulation built with Java using object-oriented design.

## Features
- Login with User ID and PIN (access denied after 3 wrong attempts)
- Transaction History for the current session
- Withdraw with balance check ("Insufficient Funds" message)
- Deposit
- Transfer between accounts
- Quit with goodbye message
- Transactions stored in an ArrayList
- Classes: ATM, Account, Transaction, Bank, Main

## How to Run
1. Install JDK 17 or newer.
2. Put all .java files in one folder.
3. Compile: `javac *.java`
4. Run: `java Main`

## Demo Accounts
| User ID | PIN |
|---------|------|
| 1001 | 1234 |
| 1002 | 4321 |
| 1003 | 1111 |

## Screenshots


![Login](login.png)




![Menu](menu.png)




![History](history.png)