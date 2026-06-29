# DESIGN DECISIONS – FoodApp

## Table of Contents
1. Project Structure Decisions
2. Data Type Choices
3. Design Pattern Choices
4. OOP & SOLID Decisions
5. Exception Handling Strategy
6. Data Storage Strategy
7. Threading Approach
8. Stream API Usage
9. Java Keyword & Syntax Explanations
10. Input Handling Approach

---

## 1. Project Structure Decisions

**Why divide into multiple packages (`model`, `service`, `repository`, `ui`, `enums`, `exception`, `util`, `facade`, `factory`, `config`)?**
* **Separation of Concerns (SoC)**: Each layer has a specific responsibility. 
  - `model` holds data.
  - `repository` handles file I/O.
  - `service` contains business logic.
  - `ui` handles console interactions.
* **Maintainability**: If the UI changes (e.g., to a web interface later), we only rewrite the `ui` package. The `service` and `repository` layers remain untouched.

---

## 2. Data Type Choices

**Why `String id` instead of `int id` or `long id`?**
* **Chosen**: `String` populated with a UUID.
* **Why**: UUIDs (Universally Unique Identifiers) guarantee that no two entities will ever have the same ID, even across different runs of the application. Thread-safe to generate.
* **Alternative Rejected (`int` auto-increment)**: Would require a shared counter state. If multiple threads create items simultaneously, they could receive duplicate IDs unless strictly synchronized. Also, if data files are deleted, the counter resets, potentially causing issues with related records.

**Why `double price` and not `BigDecimal`?**
* **Chosen**: `double`.
* **Why**: The math syntax (`price1 + price2`) is extremely simple, readable, and perfectly fine for a basic food ordering app representing Rupees (₹).
* **Alternative Rejected (`BigDecimal`)**: Requires verbose syntax like `price1.add(price2)`. While mathematically precise for banking applications, it's overkill and adds unnecessary complexity for an intern-level console project.

**Why `boolean isActive` and not `enum UserStatus { ACTIVE, INACTIVE }`?**
* **Chosen**: `boolean`.
* **Why**: There are precisely two states. Using a boolean makes the code read naturally (e.g., `if (user.isActive())`).
* **Alternative Rejected (`enum`)**: Defining a whole new class for just two values is over-engineering when a boolean perfectly models the binary nature of the state.

**Why `String phone` instead of `long phone`?**
* **Chosen**: `String`.
* **Why**: Phone numbers can start with zeros (though rare in India, common elsewhere). More importantly, we don't perform math operations (addition, subtraction) on phone numbers. Regular Expressions (regex) can be used to validate String formats easily (`^[6-9]\\d{9}$`).
* **Alternative Rejected (`long`)**: Would truncate leading zeros and makes validation slightly more cumbersome.

**Why `List<CartItem>` instead of an Array `CartItem[]`?**
* **Chosen**: `List` (specifically `ArrayList`).
* **Why**: Lists grow dynamically. Users can add as many items to their cart as they want. Lists also support Java Streams (`.stream()`), making calculations like total price a one-liner.
* **Alternative Rejected (Array)**: Arrays have a fixed size. We'd have to guess the max number of items or write complex code to resize the array manually.

---

## 3. Design Pattern Choices

**Facade Pattern (`OrderProcessingFacade`)**
* **What**: Provides a simplified interface to a complex body of code.
* **Why**: Placing an order requires checking the cart, calculating discounts, processing payments, assigning a driver, and printing an invoice. This touches 5 different services. If the `CustomerMenu` did this, it would be heavily coupled and messy. The Facade wraps this into a single `placeOrder()` method.

**Strategy Pattern (`IPaymentStrategy`, `CashPayment`, `UpiPayment`)**
* **What**: Defines a family of algorithms, encapsulates each one, and makes them interchangeable.
* **Why**: Allows adding new payment methods (e.g., Wallet, Card) later without touching the existing `OrderProcessingFacade` code. This adheres strictly to the Open/Closed Principle.

**Factory Method Pattern (`ServiceRegistry.createUser()`)**
* **What**: Defines an interface for creating an object, but lets subclasses decide which class to instantiate.
* **Why**: Object creation logic is centralized. When a new user needs to be registered, we don't scatter `new Admin()`, `new Customer()` calls everywhere. We just pass a `Role` enum to the factory method.

**Singleton Pattern (`ServiceRegistry`)**
* **What**: Ensures a class has only one instance and provides a global point of access to it.
* **Why**: We want exactly one instance of our repositories (so they don't read/write over each other in files) and services. The `ServiceRegistry` ensures all dependencies are wired up perfectly exactly once at startup.

---

## 4. OOP & SOLID Decisions

**S - Single Responsibility Principle (SRP)**
* Example: `InputUtil` only reads and validates input. `CartService` only handles in-memory cart operations. No class is a "god class" doing everything.

**O - Open/Closed Principle (OCP)**
* Example: The system is open to extension (add new payment strategies) but closed for modification (no need to change existing order logic).

**L - Liskov Substitution Principle (LSP)**
* Example: `Admin`, `Customer`, and `DeliveryPartner` all inherit from `User`. In `MainApp.java`, the `AuthService` returns a `User`, and we can seamlessly pass it around regardless of the specific subclass.

**I - Interface Segregation Principle (ISP)**
* Example: The `Repository` interface has exactly 5 essential methods. We didn't bloat it with 20 methods.

**D - Dependency Inversion Principle (DIP)**
* Example: Services depend on the `Repository<T>` interface, not the concrete `FileRepository` class. This means if we upgrade to a Database later, the services don't have to change at all.

**Abstract Class vs. Interface (`User`)**
* We used an abstract class for `User` because Admin, Customer, and DeliveryPartner share actual state (fields like `username`, `password`) and implementation (getters/setters). Interfaces cannot hold instance fields.

---

## 5. Exception Handling Strategy

**Unchecked Exceptions (`RuntimeException`) vs. Checked Exceptions (`Exception`)**
* **Chosen**: `RuntimeException` via `AppException`.
* **Why**: Checked exceptions force every calling method in the hierarchy to have a `throws` clause or a `try-catch` block. This clutters the code heavily. By using unchecked exceptions, we let the errors bubble up naturally to the UI layer, where they are caught once and displayed cleanly to the user.

---

## 6. Data Storage Strategy

**Java Serialization (`FileRepository`) vs. Databases/JSON**
* **Chosen**: Java Object Serialization (`ObjectOutputStream` to `.dat` files).
* **Why**: Requires zero external libraries (like Gson or Jackson). It's built into the JDK. It persists the entire object graph exactly as it is in memory. Perfect for an intern-level console application where installing a database (MySQL, PostgreSQL) is out of scope.

---

## 7. Threading Approach

**Raw Threads (`Thread`) vs. ExecutorService**
* **Chosen**: Standard `Thread` instances in `MainApp.java` for async data loading.
* **Why**: Demonstrates core multithreading concepts. Since there are exactly 5 files to load, creating 5 explicit threads is straightforward and easy to understand. We use `thread.join()` to wait for completion.

**Synchronized Keyword in `FileRepository`**
* **Why**: When multiple threads read or write to the same `.dat` file, data corruption can occur. Slapping `synchronized` on the methods ensures that only one thread can execute a file operation on that repository at a time. It's simpler than `ReentrantLock`.

---

## 8. Stream API Usage

The Java 8 Stream API is heavily utilized to replace verbose `for` loops.
* **Filtering**: `.stream().filter(u -> u.isActive())` - Declarative way to say "give me active users".
* **Sorting**: `.stream().sorted(Comparator.comparingDouble(MenuItem::getPrice))` - One-line sorting without needing to write a separate Comparator class.
* **Mapping/Reducing**: `.stream().mapToDouble(CartItem::getSubtotal).sum()` - Replaces manually declaring a sum variable and looping to add it up.

---

## 9. Java Keyword & Syntax Explanations

* **`final`**: Used on service references in Menus to ensure the reference cannot be reassigned once set in the constructor. Guarantees immutability of dependencies.
* **Switch Expressions (`switch(choice) -> {}`)**: Introduced in Java 14. Eliminates the need for `break` statements (preventing fall-through bugs) and is more concise.
* **Method References (`MenuItem::getPrice`)**: A shorthand for a lambda expression `(item) -> item.getPrice()`. Makes the code cleaner and easier to read.

---

## 10. Input Handling Approach

* **Single `Scanner` Instance (`InputUtil`)**: Creating `new Scanner(System.in)` inside multiple methods is a notorious source of bugs in Java console apps because closing one scanner closes the underlying `System.in` stream for all of them. By keeping one static `Scanner` in `InputUtil`, we avoid `NoSuchElementException` and stream closure issues entirely.
