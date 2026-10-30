# DEIHS Library System

A complete, feature-rich desktop Library Management System built using **Java Swing** and **Object Serialization**. Designed specifically for the **DEIHS School Library**, this application offers an intuitive and responsive user interface for managing printed books, e-books, user transactions, and system catalogs.

---

##  Features

- **Interactive Dashboard:** 
  - Real-time statistics tracking **Total Books**, **Available Books**, **Borrowed Books**, **Printed Books**, and **E-Books**.
  - Integrated book catalog table equipped with live search and category filtering.
- **Book Management:**
  - **Add a Book:** Supports both *Printed Books* (with page count details) and *E-Books* (with file size in MB).
  - **Remove a Book:** Securely remove individual books by ID or clear the entire catalog with confirmation safety prompts.
- **Library Transactions:**
  - **Borrow Book:** Instantly verify book availability and record borrowings by Book ID.
  - **Return Book:** Seamlessly process returns to update availability status.
- **Data Import & Persistence:**
  - **Upload Book File:** Bulk import books via `.txt` or `.csv` files.
  - **Auto-Persistence:** Automatically saves and serializes catalog data to `library_data.ser` to prevent data loss between sessions.

---

##  Tech Stack & Requirements

- **Language:** Java (JDK 8 or higher recommended)
- **GUI Framework:** Java Swing (`javax.swing`, `java.awt`)
- **Data Storage:** Java Object Serialization (`java.io.Serializable`)

---

##  Getting Started

### 1. Prerequisites
Ensure you have the Java Development Kit (JDK) installed on your system. You can verify your installation via terminal/command prompt:
```bash
java -version
