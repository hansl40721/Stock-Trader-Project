package src.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import src.main.model.Trade;
import src.main.model.Trade.Stock;
import src.main.model.Trade.Type;
import src.main.model.account.Account;
import src.main.model.account.Personal;
import src.main.model.account.TFSA;
import src.main.utils.Color;

public class Main {

    static Account account; 
    static final double INITIAL_DEPOSIT = 4000;
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        explainApp();
        switch (accountChoice()) {
            case "a": account = new Personal(INITIAL_DEPOSIT); break;
            case "b": account = new TFSA(INITIAL_DEPOSIT); break;
        }

        initialBalance();

        for (int day = 1; day <= 2160; day++) {

            displayPrices(day);

            while(true) {

            
            String choice = buyOrSell();
            String stock = chooseStock();
            int shares = numShares(choice);
        
            String result = account.makeTrade(new Trade(
                Stock.valueOf(stock),
                choice.equals("buy") ? Type.MARKET_BUY : Type.MARKET_SELL,
                Double.parseDouble(getPrice(Stock.valueOf(stock), day)),
                shares
            )) ? "successful" : "unsuccessful";

            tradeStatus(result);
            if(!keepTrading()) break;
            }
        }

        scanner.close();
    }

    public static void explainApp() {
        System.out.println(Color.BLUE + "\n    Welcome to the Stock Trader Simulator!");
        wait(1200);
        System.out.println("\n    First, let's decide what type of account you would like to create.");
        System.out.println(Color.BLUE + "\n    There are two types of accounts you can use to trade stocks: ");
        wait(800);
        System.out.print(Color.BLUE + "\n - PERSONAL: ");
        System.out.println(Color.YELLOW + "Every sale made in a personal account is charged a 5% fee.");
        System.out.print(Color.BLUE + "\n - TFSA: ");
        System.out.println(Color.YELLOW + "Every trade (buy/sell) made from a TFSA is charged a 1% fee.\n");
        System.out.println(Color.BLUE + " - Neither account has a limit on the amount of trades that can be made." + Color.RESET);
    }

    public static void initialBalance() {
        System.out.print("\n\n  You created a " + Color.YELLOW + account.getClass().getSimpleName() + Color.RESET + " account.");
        System.out.println(" Your account balance is " + Color.GREEN + "$" + account.getFunds() + Color.RESET);
        System.out.print("\n  Enter anything to start trading: ");
        scanner.nextLine();
    }


    public static String accountChoice() {
        System.out.print("\n  Respectively, type 'a' or 'b' to create a Personal account or TFSA: ");
        String choice = scanner.nextLine();
        while (!choice.equals("a") && !choice.equals("b")) {
            System.out.print("  Respectively, type 'a' or 'b' to create a Personal account or TFSA: ");
            choice = scanner.nextLine();
        }
        return choice;
    }

    public static String getPrice(Stock stock, int day) {
        Path path = getPath(stock.toString());
        try {
           return Files.lines(path)
            .skip(1)
            .filter((line) -> Integer.valueOf(line.split(",")[0]) == day)
            .map((line) -> line.split(",")[1])
            .findFirst()
            .orElse(null);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static Path getPath(String stock) {
        try {
            return Paths.get(Thread.currentThread().getContextClassLoader().getResource("src/main/data/"+stock+".csv").toURI());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static String buyOrSell() {
        System.out.print("\n\n  Would you like to 'buy' or 'sell': ");
        String choice = scanner.nextLine();
        while (!choice.equals("buy") && !choice.equals("sell")) {
            System.out.print("  Would you like to 'buy' or 'sell': ");
            choice = scanner.nextLine();
        }
        return choice;
    }

    public static String chooseStock() {
        System.out.print("  Choose a stock: ");
        String stock = scanner.nextLine(); 
        while (!stock.equals("AAPL") && !stock.equals("FB") && !stock.equals("GOOG") && !stock.equals("TSLA") ) {
            System.out.print("  Choose a stock: ");
            stock = scanner.nextLine();
        }
        return stock;
    }

    public static int numShares(String choice) {
        System.out.print("  Enter the number of shares you'd like to " + choice + ": ");
        int shares = Integer.parseInt(scanner.nextLine());
        while (shares <= 0) {
            System.out.print("  Enter the number of shares you'd like to " + choice + ": ");
            shares = Integer.parseInt(scanner.nextLine());
        }
        return shares;
    }

    public static void displayPrices(int day) {
        System.out.println("\n\n\t  DAY " + day + " PRICES\n");
        wait(500);

        System.out.println("  " + Color.BLUE + Stock.AAPL + "\t\t" + Color.GREEN + getPrice(Stock.AAPL, day) + displayValueShifts(Stock.AAPL, day));
        wait(500);
        System.out.println("  " + Color.BLUE + Stock.FB + "\t\t" + Color.GREEN + getPrice(Stock.FB, day) + displayValueShifts(Stock.FB, day));
        wait(500);
        System.out.println("  " + Color.BLUE + Stock.GOOG + "\t\t" + Color.GREEN + getPrice(Stock.GOOG, day) + displayValueShifts(Stock.GOOG, day));
        wait(500);
        System.out.println("  " + Color.BLUE + Stock.TSLA + "\t\t" + Color.GREEN + getPrice(Stock.TSLA, day)  + displayValueShifts(Stock.AAPL, day) + Color.RESET);
        wait(500);

    }

    public static void tradeStatus(String result) {
        System.out.println("\n  The trade was " + (result.equals("successful") ? Color.GREEN : Color.RED) + result + Color.RESET + ". Here is your portfolio:");
        System.out.println(account);
        System.out.print("\n  Press anything to continue");
        scanner.nextLine();
    }

    public static boolean keepTrading() {
        System.out.println("\nWould you like to keep trading? Enter 'yes' or 'no'.");
        String choice = scanner.nextLine();
        while(!choice.equals("yes") && !choice.equalsIgnoreCase("no")) {
            System.out.println("\nWould you like to keep trading? Enter 'yes' or 'no'.");
            choice = scanner.nextLine();
        }
        return choice.equals("yes");
    }

    public static String displayValueShifts(Stock stock, int day) {
        if(day > 1) {
            Double diff = (Double.parseDouble(getPrice(stock, day)) - Double.parseDouble(getPrice(stock, day - 1)));
            if(diff > 0) {
                return "  " + Color.GREEN + "\t" + '+' + diff;
            } else {
                return "  " + Color.RED + "\t" + diff;
            }
        }
        return " ";
    }

    public static void wait(int milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
             System.out.println(e.getMessage());
        }
    }

    // I think parts of this application that work really well once, but I want to potentially reuse them if I want to be able to sell and buy stocks more than once a day
    // So the object for tomorrow would be to create functionality to reuse the 'buy/sell' methods
    // Using the stream seems like the best way to avoid loops in the code while also letting us use the existing functionality 
}
