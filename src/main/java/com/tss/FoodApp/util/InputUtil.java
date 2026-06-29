package com.tss.FoodApp.util;

import java.util.Scanner;

/**
 * Centralized utility for ALL console input/output operations.
 * ONE Scanner instance shared across the entire app.
 *
 * Why single static Scanner?
 * → Multiple Scanner(System.in) instances cause input bugs (one consumes input meant for another).
 * → Static = shared. No need to pass Scanner as parameter to every class.
 * → Alternative: Create Scanner per method call → causes BufferedInputStream conflicts.
 *
 * Why combine input + output + validation in one class?
 * → For a 33-file project, splitting into 3 files is overkill.
 * → All these operations relate to "user interaction" — cohesive enough for one class.
 * → If app grows, split into InputUtil + OutputUtil + ValidationUtil.
 *
 * SRP exception: This class has "console interaction" as its single responsibility.
 */
public class InputUtil {

    private static final Scanner scanner = new Scanner(System.in);

    private InputUtil() {} // Prevent instantiation — all methods are static

    // ========================= INPUT METHODS =========================

    /**
     * Read a non-empty trimmed string from console.
     * Keeps asking until user enters something non-empty.
     */
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

    /**
     * Read a line that can contain spaces (for addresses, names, etc.).
     * Same as readString but named differently for clarity.
     */
    public static String readLine(String prompt) {
        return readString(prompt);
    }

    /**
     * Read an integer from console. Retries on invalid input.
     */
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

    /**
     * Read an integer within a range (inclusive).
     * Used for menu choices: readInt("Choice: ", 1, 15)
     */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            printError("Please enter a number between " + min + " and " + max + ".");
        }
    }

    /**
     * Read a double from console. Retries on invalid input.
     */
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

    /**
     * Read a double within a range (inclusive).
     */
    public static double readDouble(String prompt, double min, double max) {
        while (true) {
            double value = readDouble(prompt);
            if (value >= min && value <= max) {
                return value;
            }
            printError("Please enter a number between " + min + " and " + max + ".");
        }
    }

    /**
     * Read a yes/no response. Returns true for yes, false for no.
     */
    public static boolean readYesNo(String prompt) {
        while (true) {
            String input = readString(prompt + " (y/n): ").toLowerCase();
            if (input.equals("y") || input.equals("yes")) return true;
            if (input.equals("n") || input.equals("no")) return false;
            printError("Please enter 'y' or 'n'.");
        }
    }

    /**
     * Read an enum value by displaying numbered options.
     * Example: readEnum("Category", FoodCategory.class) shows:
     *   1. VEG
     *   2. NON_VEG
     *   3. BEVERAGE
     *   4. DESSERT
     * User enters number, gets back the enum value.
     *
     * Why generic method? → Works with ANY enum (Role, FoodCategory, PaymentMode, etc.)
     */
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

    /**
     * Print a header box with a title.
     * Example: printHeader("ADMIN DASHBOARD") produces:
     * ╔══════════════════════════════════════╗
     * ║       ADMIN DASHBOARD               ║
     * ╚══════════════════════════════════════╝
     */
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

    /**
     * Print a horizontal divider line.
     */
    public static void printDivider() {
        System.out.println(repeat("-", 47));
    }

    /**
     * Print a success message with green checkmark.
     */
    public static void printSuccess(String message) {
        System.out.println("  " + message);
    }

    /**
     * Print an error message with red cross.
     */
    public static void printError(String message) {
        System.out.println("  " + message);
    }

    /**
     * Print a warning message.
     */
    public static void printWarning(String message) {
        System.out.println("  " + message);
    }

    /**
     * Print a menu option line.
     */
    public static void printMenuOption(int number, String text) {
        System.out.printf("  %-3d. %s%n", number, text);
    }

    /**
     * Display a list of items in a formatted table.
     * Shows index number for each item using its toString().
     */
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

    /**
     * Validate username: 3-20 chars, alphanumeric + underscore, starts with letter.
     * Returns true if valid, false if not. Prints error message on failure.
     */
    public static boolean validateUsername(String username) {
        if (username.length() < 3 || username.length() > 20) {
            printError("Username must be 3-20 characters.");
            return false;
        }
        if (!username.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            printError("Username must start with a letter. Only letters, digits, and underscore allowed.");
            return false;
        }
        return true;
    }

    /**
     * Validate password: minimum 6 characters.
     */
    public static boolean validatePassword(String password) {
        if (password.length() < 6) {
            printError("Password must be at least 6 characters.");
            return false;
        }
        return true;
    }

    /**
     * Validate phone: exactly 10 digits, starts with 6-9.
     */
    public static boolean validatePhone(String phone) {
        if (!phone.matches("^[6-9]\\d{9}$")) {
            printError("Phone must be 10 digits starting with 6-9.");
            return false;
        }
        return true;
    }

    /**
     * Pause and wait for user to press Enter.
     */
    public static void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Close the Scanner (call only at app shutdown).
     */
    public static void close() {
        scanner.close();
    }
}