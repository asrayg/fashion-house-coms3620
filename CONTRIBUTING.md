# Contributing to the Fashion House System — Iteration 1

## Who Does What

| Person | Use Case | Controller to implement |
|--------|----------|------------------------|
| Person 1 | UC1 — Create Collection | `CollectionController.java` |
| Person 2 | UC2 — Add Garment Design | `GarmentDesignController.java` |
| Person 3 | UC3 — Define Product Specification | `ProductSpecController.java` |
| Person 4 | UC4 — Register Material | `MaterialController.java` |
| Person 5 | UC5 — Place Material Order | `MaterialOrderController.java` |

Each person owns **one controller file**. You do not touch anyone else's file.

---

## Project Structure

```
FashionHouseSystem/
├── src/main/java/org/example/
│   ├── Main.java                        ← entry point, main menu (DO NOT EDIT)
│   ├── util/
│   │   └── FileManager.java             ← shared I/O utility (DO NOT EDIT)
│   ├── model/
│   │   ├── Collection.java              ← data class (DO NOT EDIT)
│   │   ├── GarmentDesign.java           ← data class (DO NOT EDIT)
│   │   ├── ProductSpecification.java    ← data class (DO NOT EDIT)
│   │   ├── Material.java                ← data class (DO NOT EDIT)
│   │   └── MaterialOrder.java           ← data class (DO NOT EDIT)
│   └── controller/
│       ├── CollectionController.java    ← Person 1 implements this
│       ├── GarmentDesignController.java ← Person 2 implements this
│       ├── ProductSpecController.java   ← Person 3 implements this
│       ├── MaterialController.java      ← Person 4 implements this
│       └── MaterialOrderController.java ← Person 5 implements this
└── data/
    ├── collections.csv
    ├── garments.csv
    ├── specifications.csv
    ├── materials.csv
    └── material_orders.csv
```

- **Model classes** are plain Java objects (fields + toCSV/fromCSV). They are done — do not change them.
- **FileManager** handles all file reads and writes. Use it, do not replace it.
- **Your controller** is the only thing you need to write.

---

## How to Run

1. Open the project in IntelliJ (or any IDE).
2. Make sure Java 17+ is configured.
3. Run `Main.java` from the project root directory so that the `data/` folder path resolves correctly.
4. Use the numbered menu to navigate.

To run from terminal:
```bash
mvn compile
mvn exec:java -Dexec.mainClass="org.example.Main"
```

---

## How to Implement Your Use Case

Every controller follows the same pattern. Here is the complete step-by-step:

### Step 1 — Read the TODO comment in your controller

Open your controller file. Find the `// TODO` block inside your use case method. The comment lists every step you need to take in order.

### Step 2 — Prompt for input

Use the shared `scanner` (passed in via constructor) to read user input:

```java
System.out.print("Enter name: ");
String name = scanner.nextLine().trim();
```

### Step 3 — Validate input

Check that required fields are not blank:

```java
if (name.isEmpty()) {
    System.out.println("Name cannot be blank.");
    return;
}
```

For numeric fields, wrap parsing in try/catch:

```java
int qty;
try {
    qty = Integer.parseInt(scanner.nextLine().trim());
} catch (NumberFormatException e) {
    System.out.println("Invalid number.");
    return;
}
if (qty <= 0) {
    System.out.println("Quantity must be greater than zero.");
    return;
}
```

### Step 4 — Check cross-record dependencies

Some use cases require that a parent record exists. Use the static `findById` helper on the relevant controller:

```java
// Example: UC2 needs a collection to exist
Collection c = CollectionController.findById(collectionId);
if (c == null) {
    System.out.println("Collection not found.");
    return;
}
```

### Step 5 — Check for duplicates

Load existing records and loop through them:

```java
List<String> lines = FileManager.readLines(FILE);
for (String line : lines) {
    Collection existing = Collection.fromCSV(line);
    if (existing.getName().equalsIgnoreCase(name) && existing.getSeason().equalsIgnoreCase(season)) {
        System.out.println("A collection with this name and season already exists.");
        return;
    }
}
```

### Step 6 — Generate an ID

```java
int id = FileManager.nextId(FILE);
```

This returns `(number of existing records + 1)`. It is simple and works as long as records are never deleted mid-session.

### Step 7 — Create the object and save it

```java
Collection newCollection = new Collection(id, name, season, releasePeriod, description);
FileManager.appendLine(FILE, newCollection.toCSV());
System.out.println("Collection created: " + newCollection);
```

### Full example — UC1 implemented

```java
private void createCollection() {
    System.out.print("Collection name: ");
    String name = scanner.nextLine().trim();

    System.out.print("Season (e.g. Spring, Fall): ");
    String season = scanner.nextLine().trim();

    System.out.print("Release period (e.g. Mar-May 2025): ");
    String releasePeriod = scanner.nextLine().trim();

    System.out.print("Description: ");
    String description = scanner.nextLine().trim();

    if (name.isEmpty() || season.isEmpty() || releasePeriod.isEmpty()) {
        System.out.println("Name, season, and release period are required.");
        return;
    }

    List<String> lines = FileManager.readLines(FILE);
    for (String line : lines) {
        Collection existing = Collection.fromCSV(line);
        if (existing.getName().equalsIgnoreCase(name) &&
            existing.getSeason().equalsIgnoreCase(season)) {
            System.out.println("A collection with this name and season already exists.");
            return;
        }
    }

    int id = FileManager.nextId(FILE);
    Collection c = new Collection(id, name, season, releasePeriod, description);
    FileManager.appendLine(FILE, c.toCSV());
    System.out.println("Collection created: " + c);
}
```

---

## FileManager API — Quick Reference

| Method | What it does |
|--------|-------------|
| `FileManager.readLines(FILE)` | Returns all lines in the CSV as a `List<String>` |
| `FileManager.appendLine(FILE, line)` | Adds one line to the end of the file |
| `FileManager.writeLines(FILE, lines)` | Overwrites the entire file (used for updates) |
| `FileManager.nextId(FILE)` | Returns `size + 1` as the next safe integer ID |
| `FileManager.hasRecords(FILE)` | Returns `true` if the file has at least one line |

---

## CSV Format Reference

Each model knows its own format via `toCSV()` and `fromCSV()`. You never build CSV strings manually.

| File | Format |
|------|--------|
| `collections.csv` | `id,name,season,releasePeriod,description` |
| `garments.csv` | `id,collectionId,name,type,style,targetAudience,notes` |
| `specifications.csv` | `id,garmentId,sizeRange,colorOptions,fabricType,measurements` |
| `materials.csv` | `id,name,category,unitCost,stockLevel` |
| `material_orders.csv` | `id,materialId,supplierName,quantity,expectedDelivery,status` |

---

## Rules

1. Only edit your assigned controller file.
2. Do not rename packages, move files, or change method signatures.
3. Do not delete the `TODO` comments — replace the `System.out.println("[TODO]...")` line with your real code.
4. Test your use case manually by running `Main.java` and exercising your menu option.
5. Make sure no `NullPointerException` or `NumberFormatException` can crash the app — catch and print a message instead.

---

## Dependency Chain (important for testing order)

```
UC4 Register Material
    ↓
UC5 Place Material Order    UC1 Create Collection
                                ↓
                            UC2 Add Garment Design
                                ↓
                            UC3 Define Product Specification
```

- You can test UC1, UC4 independently right away.
- UC2 requires UC1 to have at least one collection saved.
- UC3 requires UC2 to have at least one garment saved.
- UC5 requires UC4 to have at least one material saved.
