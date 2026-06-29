package com.tss.FoodApp.util;

import java.util.Scanner;
import com.tss.FoodApp.config.AppConfig;
import com.tss.FoodApp.exception.AppException;

public class InputUtil {
    private static final Scanner scanner = new Scanner(System.in);

    private InputUtil() {}

    // ========================= INPUT METHODS =========================

    public static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            printError("Input cannot be empty. Please try again.");
        }
    }

    public static String readLine(String prompt) {
        return readString(prompt);
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                printError("Please enter a valid number.");
            }
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            printError("Please enter a number between " + min + " and " + max + ".");
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                printError("Please enter a valid number.");
            }
        }
    }

    public static double readDouble(String prompt, double min, double max) {
        while (true) {
            double value = readDouble(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            printError("Please enter a number between " + min + " and " + max + ".");
        }
    }

    public static boolean readYesNo(String prompt) {
        while (true) {
            String input = readString(prompt + " (y/n): ").toLowerCase();
            if (input.equals("y") || input.equals("yes")) return true;
            if (input.equals("n") || input.equals("no")) return false;
            printError("Please enter 'y' or 'n'.");
        }
    }

    public static <E extends Enum<E>> E readEnum(String prompt, Class<E> enumClass) {
        E[] values = enumClass.getEnumConstants();
        System.out.println(prompt + ":");
        for (int i = 0; i < values.length; i++) {
            System.out.println("  " + (i + 1) + ". " + values[i].name());
        }
        int choice = readInt("  Select option: ", 1, values.length);
        return values[choice - 1];
    }

    public static String repeat(String str, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    // ========================= OUTPUT METHODS =========================

    public static void printHeader(String title) {
        int width = 45;
        String border = repeat("═", width);
        System.out.println();
        System.out.println("╔" + border + "╗");
        int padding = (width - title.length()) / 2;
        String centeredTitle = repeat(" ", Math.max(0, padding)) + title
                + repeat(" ", Math.max(0, width - padding - title.length()));
        System.out.println("║" + centeredTitle + "║");
        System.out.println("╚" + border + "╝");
    }

    public static void printDivider() {
        System.out.println(repeat("-", 47));
    }

    public static void printSuccess(String message) {
        System.out.println("  " + message);
    }

    public static void printError(String message) {
        System.out.println("  " + message);
    }

    public static void printWarning(String message) {
        System.out.println("  " + message);
    }

    public static void printMenuOption(int number, String text) {
        System.out.printf("  %-3d. %s%n", number, text);
    }

    public static <T> void printNumberedList(java.util.List<T> items, String title) {
        if (items.isEmpty()) {
            printWarning("No " + title.toLowerCase() + " found.");
            return;
        }
        printDivider();
        System.out.println("  " + title + " (" + items.size() + " items)");
        printDivider();
        for (int i = 0; i < items.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + items.get(i).toString());
        }
        printDivider();
    }

    // ========================= VALIDATION METHODS =========================

    public static String readValidUsername(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (input.equalsIgnoreCase("exit")) return null;
            if (validateUsername(input)) return input;
            System.out.println("  (Type 'exit' to cancel)");
        }
    }

    public static String readValidPassword(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (input.equalsIgnoreCase("exit")) return null;
            if (validatePassword(input)) return input;
            System.out.println("  (Type 'exit' to cancel)");
        }
    }

    public static String readValidPhone(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (input.equalsIgnoreCase("exit")) return null;
            if (validatePhone(input)) return input;
            System.out.println("  (Type 'exit' to cancel)");
        }
    }

    public static boolean validateUsername(String username) {
        if (username.length() < AppConfig.MIN_USERNAME_LENGTH || username.length() > AppConfig.MAX_USERNAME_LENGTH) {
            printError("Username must be " + AppConfig.MIN_USERNAME_LENGTH + "-" + AppConfig.MAX_USERNAME_LENGTH + " characters.");
            return false;
        }
        if (!username.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            printError("Username must start with a letter. Only letters, digits, and underscore allowed.");
            return false;
        }
        return true;
    }

    public static boolean validatePassword(String password) {
        if (password.length() < AppConfig.MIN_PASSWORD_LENGTH) {
            printError("Password must be at least " + AppConfig.MIN_PASSWORD_LENGTH + " characters.");
            return false;
        }
        return true;
    }

    public static boolean validatePhone(String phone) {
        if (!phone.matches("^[6-9]\\d{9}$")) {
            printError("Phone must be 10 digits starting with 6-9.");
            return false;
        }
        return true;
    }

    public static void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public static void runSubMenu(String title, String[] options, Runnable[] actions) {
        boolean back = false;
        while (!back) {
            printHeader(title);
            for (int i = 0; i < options.length; i++) {
                printMenuOption(i + 1, options[i]);
            }
            System.out.println("  ─────────────");
            printMenuOption(options.length + 1, "Back");
            printDivider();
            int choice = readInt("  Enter choice: ", 1, options.length + 1);
            if (choice == options.length + 1) {
                back = true;
            } else {
                try {
                    actions[choice - 1].run();
                } catch (AppException e) {
                    printError(e.getMessage());
                }
            }
        }
    }

    public static void close() {
        scanner.close();
    }
}
