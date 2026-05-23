# OOP-based Matrix Computation & Storage System

A Java Swing desktop application that performs matrix operations and persists every calculation to a MySQL database. Built with clean OOP principles: **encapsulation**, **inheritance**, and **abstraction**.

---

## 📸 Features

| Feature | Description |
|---------|-------------|
| **Matrix Input** | Resizable NxN grids (up to 5×5) with focus-highlighted cells |
| **Add / Subtract** | Element-wise addition and subtraction of two matrices |
| **Multiply** | Standard matrix product (A × B) |
| **Transpose** | Rows ↔ Columns flip of Matrix A |
| **Determinant** | Computed via Gaussian elimination with partial pivoting |
| **Auto-Save** | Every result is saved to MySQL automatically |
| **History** | Filterable, sortable dialog with double-click detail view |
| **Dark UI** | Modern dark theme with gradient header and glass-morphic buttons |

---

## 🗂 Project Structure

```
Matrix Computation & Storage System/
├── src/
│   └── matrix/
│       ├── Main.java                  ← entry point
│       ├── core/
│       │   ├── Matrix.java            ← base class (encapsulation + ops)
│       │   └── SquareMatrix.java      ← inherits Matrix, adds determinant()
│       ├── db/
│       │   ├── DatabaseManager.java   ← JDBC connection + schema bootstrap
│       │   └── MatrixOperationDAO.java← save / query operations
│       └── ui/
│           ├── MatrixApp.java         ← main JFrame window
│           └── HistoryDialog.java     ← calculation history dialog
├── sql/
│   └── setup.sql                      ← DB + table creation + sample data
├── lib/
│   └── (place mysql-connector-j-*.jar here)
├── compile.bat                        ← Windows compile script
├── run.bat                            ← Windows run script
├── compile_and_run.bat                ← one-click build + launch
└── README.md
```

---

## ⚙️ Prerequisites

| Requirement | Version |
|-------------|---------|
| **JDK** | 17 or higher |
| **MySQL Server** | 5.7+ / 8.x |
| **MySQL Connector/J** | 8.x (JDBC driver) |

---

## 🚀 Setup & Run

### 1. Download the MySQL JDBC Driver

Download **mysql-connector-j-8.x.x.jar** from:
- https://dev.mysql.com/downloads/connector/j/

Place the `.jar` file in the `lib/` folder of this project.

### 2. Set Up the Database

Open MySQL CLI or Workbench and run:

```sql
source sql/setup.sql
```

This creates the `matrix_db` database, the `matrix_operations` table, and inserts 5 sample rows.

### 3. Configure Database Credentials

If your MySQL username/password differs from the defaults (`root` / no password), edit:

📄 **`src/matrix/db/DatabaseManager.java`**

```java
private static final String DB_USER     = "root";       // ← your username
private static final String DB_PASSWORD = "";            // ← your password
```

### 4. Compile

```batch
compile.bat
```

### 5. Run

```batch
run.bat
```

Or use the one-click script:

```batch
compile_and_run.bat
```

---

## 🎮 How to Use

1. **Set matrix size** — use the spinner (1–5) and click **Apply**
2. **Enter values** — click any cell in Matrix A or Matrix B and type a number
3. **Click an operation**:
   - **Add / Subtract / Multiply** — uses both Matrix A and Matrix B
   - **Transpose / Determinant** — uses only Matrix A
4. **View the result** — appears in the table on the right panel
5. **Check history** — click the **📋 History** button in the header
6. **Filter history** — use the dropdown to filter by operation type
7. **View detail** — double-click any history row for a full breakdown

---

## 🏗 OOP Design

### Encapsulation
- `Matrix` fields (`data`, `rows`, `cols`) are `private final`
- Data is deep-copied on construction and on `getData()` to prevent external mutation
- Validation methods are `private`

### Inheritance
- `SquareMatrix extends Matrix`
- Inherits all arithmetic operations
- Adds the `determinant()` method (only valid for square matrices)
- Uses `protected` setter from `Matrix` for controlled access

### Abstraction
- Clean public API: `add()`, `subtract()`, `multiply()`, `transpose()`, `determinant()`
- Internal implementation details (pivoting, elimination) are hidden
- DAO layer abstracts all SQL behind `save()` and `getHistory()`

---

## 🗄 Database Schema

```sql
CREATE TABLE matrix_operations (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    operation_type VARCHAR(30)  NOT NULL,
    matrix_a       TEXT         NOT NULL,
    matrix_b       TEXT,
    result_matrix  TEXT         NOT NULL,
    scalar_result  DOUBLE,
    rows_a         INT          NOT NULL,
    cols_a         INT          NOT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🧪 Testing Operations Manually

| Input A | Input B | Operation | Expected Result |
|---------|---------|-----------|-----------------|
| `[[1,2],[3,4]]` | `[[5,6],[7,8]]` | ADD | `[[6,8],[10,12]]` |
| `[[1,2],[3,4]]` | `[[5,6],[7,8]]` | SUBTRACT | `[[-4,-4],[-4,-4]]` |
| `[[1,2],[3,4]]` | `[[2,0],[1,2]]` | MULTIPLY | `[[4,4],[10,8]]` |
| `[[1,2,3],[4,5,6]]` | — | TRANSPOSE | `[[1,4],[2,5],[3,6]]` |
| `[[3,8],[4,6]]` | — | DETERMINANT | `-14` |

---

## 📝 License

This project is for educational purposes. Free to use and modify.
