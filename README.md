# Smart Student Management & Academic Performance System

A console-based Java application that manages students, courses and enrollments,
records marks, and turns them into grades, GPA, rank lists and course-level
analytics. Built for **CSE2006 – Programming in Java** using only core Java
(no external libraries, no database server required).

---

## Overview

Departments that keep student records in spreadsheets end up with duplicate
entries, unvalidated marks and no easy way to see who is falling behind. This
project replaces that with a single application that validates every input,
stores data in CSV files through Java I/O streams, and computes academic
analytics on demand.

The system is organised in three functional modules layered over a shared
model, storage and utility core:

| Module | Responsibility |
|---|---|
| **1. Student Management** | Full CRUD, search and sorting over student records |
| **2. Course & Enrollment** | Course catalogue, enrollment links, marks entry |
| **3. Performance Analytics** | Report cards, GPA, rank lists, statistics, export |

---

## Features

### Student Management
- Add students with validated ID (`S101` format), name, age, email, department and semester
- List all students sorted alphabetically using `Comparable`
- Search by partial name, or overloaded search by name + department
- Update any subset of fields (blank input keeps the old value)
- Delete a student with cascade removal of their grade records

### Course & Enrollment Management
- Add courses with code (`CSE2006` format), title, credits and faculty
- Enroll / un-enroll students, with duplicate-enrollment protection
- List all students registered for a course
- Record or update marks — rejected if the student is not enrolled
- Delete a course and automatically clear stale enrollments

### Academic Performance Analytics
- Formatted **report card** per student with letter grade and pass/fail
- **Credit-weighted GPA** on a 10-point scale (S=10, A=9, B=8, C=7, D=6, F=0)
- **Rank list** of all students ordered by GPA
- **Top-N performers**
- **Course statistics**: average, highest, lowest, pass %, grade histogram
- **Marks matrix** (students × courses) built on a 2-D array
- **Institute summary** and **export to a timestamped `.txt` report**

### System-level features
- Custom checked exception hierarchy (`SMSException` → 3 subclasses)
- Centralised regex-based input validation
- CSV persistence with `BufferedReader` / `BufferedWriter`
- Background **auto-save daemon thread** every 30 seconds, with `synchronized`
  access to shared collections
- Shutdown hook that saves data if the process is terminated
- 38 dependency-free validation tests

---

## Technologies / Tools Used

| Item | Detail |
|---|---|
| Language | Java (JDK 8 or later; tested on JDK 21) |
| Paradigm | Object-oriented, layered architecture |
| Storage | Flat CSV files via `java.io` character streams |
| Concurrency | `Thread`, `synchronized`, `volatile`, daemon threads |
| Collections | `LinkedHashMap`, `ArrayList`, `Comparator`, `Collections.sort` |
| Testing | Custom lightweight test runner (no JUnit dependency) |
| Version control | Git / GitHub |

### Syllabus concepts demonstrated

| Unit | Concept | Where it appears |
|---|---|---|
| 1 | Variables, operators, I/O, `if/else`, `switch`, loops, `break` | `ConsoleUI`, `ReportService` |
| 2 | Classes, objects, constructors (overloaded), `this`, `final` | `Student`, `Course`, `GradeRecord` |
| 2 | Inheritance, `super`, method overriding | `Person` → `Student` |
| 2 | Abstract class, interface + default method | `Person`, `Gradable` |
| 2 | Polymorphism (overloading & overriding) | `MathUtil.sum/average`, `StudentService.search` |
| 2 | Encapsulation, access modifiers | all model classes (private fields + getters) |
| 2 | `enum` with constructor and methods | `LetterGrade` |
| 2 | Recursion | `MathUtil.sum(List, int)` |
| 2 | Anonymous class | `Comparator` in `ReportService.rankList()` |
| 3 | Exceptions, `try/catch`, `throw`, `throws`, custom exceptions | `exception` package, `ConsoleUI` |
| 3 | Multithreading, thread life cycle, synchronization | `AutoSaveService`, all services |
| 3 | User-defined packages | `com.sms.*` |
| 4 | Strings, `String.format`, `StringBuilder`, 1-D & 2-D arrays | `ReportService.marksMatrix()` |
| 4 | Collections Framework: List, ArrayList, Map | all services |
| 4 | I/O streams, Reader/Writer | `FileStorage` |

> **Note on JDBC (Unit 5):** the storage layer is deliberately isolated behind
> `FileStorage`, so swapping CSV persistence for a JDBC `DataSource` requires
> changing only that one class. This is discussed under *Future Enhancements*.

---

## Project Structure

```
Smart-Student-Management-System/
├── README.md
├── statement.md
├── .gitignore
├── run.sh / run.bat          # compile + run
├── test.sh                   # compile + run tests
├── docs/
│   └── diagrams.md           # architecture, use case, class, sequence, ER, workflow
├── data/                     # CSV files created at runtime
└── src/com/sms/
    ├── app/
    │   ├── Main.java             # entry point, wiring, seed data
    │   └── ConsoleUI.java        # menus, input handling, error reporting
    ├── model/
    │   ├── Person.java           # abstract base class
    │   ├── Student.java          # extends Person, Comparable
    │   ├── Course.java
    │   ├── GradeRecord.java      # implements Gradable
    │   ├── Gradable.java         # interface with default method
    │   └── LetterGrade.java      # enum with constructor
    ├── service/
    │   ├── StudentService.java   # Module 1
    │   ├── CourseService.java    # Module 2
    │   ├── GradeService.java     # Module 3a
    │   ├── ReportService.java    # Module 3b
    │   └── AutoSaveService.java  # background thread
    ├── storage/
    │   └── FileStorage.java      # CSV read/write
    ├── exception/
    │   ├── SMSException.java
    │   ├── InvalidInputException.java
    │   ├── DuplicateRecordException.java
    │   └── RecordNotFoundException.java
    ├── util/
    │   ├── InputValidator.java
    │   └── MathUtil.java
    └── test/
        └── SimpleTests.java      # 38 validation tests
```

---

## Steps to Install & Run

### Prerequisites
- JDK 8 or later installed and on your `PATH` (check with `java -version` and `javac -version`)

### Clone
```bash
git clone https://github.com/<your-username>/Smart-Student-Management-System.git
cd Smart-Student-Management-System
```

### Run — Linux / macOS
```bash
./run.sh
```

### Run — Windows
```bat
run.bat
```

### Run — manual (any OS)
```bash
mkdir out
javac -d out src/com/sms/app/*.java src/com/sms/model/*.java src/com/sms/service/*.java src/com/sms/storage/*.java src/com/sms/exception/*.java src/com/sms/util/*.java src/com/sms/test/*.java
java -cp out com.sms.app.Main
```

On the first run the application seeds 3 sample students, 3 courses and 9 grade
records so every menu is immediately usable. Data is then saved to `data/`.

---

## Instructions for Testing

```bash
./test.sh
```
or manually:
```bash
javac -d out $(find src -name "*.java")
java -cp out com.sms.test.SimpleTests
```

Expected output ends with:
```
---------------------------------------
PASSED: 38   FAILED: 0
---------------------------------------
```

The suite covers input validation, recursion and overloading in `MathUtil`,
percentage-to-grade mapping, CSV round-tripping, student CRUD, duplicate and
missing-record exceptions, enrollment rules, credit-weighted GPA, and every
report generator.

### Manual test cases worth trying

| # | Action | Expected result |
|---|---|---|
| 1 | Add a student with ID `101` | Error: ID must be `S` + 3–5 digits |
| 2 | Add a student with email `abc` | Error: email format is not valid |
| 3 | Add a duplicate student ID | Error: student already exists |
| 4 | Record marks for a student not enrolled | Error: not enrolled in that course |
| 5 | Enter marks `150` | Error: marks must be between 0 and 100 |
| 6 | Enroll the same student twice | Error: already enrolled |
| 7 | Report card for an unknown ID | Error: no student found |
| 8 | Add data, exit, restart | Data reloaded from `data/*.csv` |

---

## Sample Output

```
------------------------------------------------------------------------
REPORT CARD - Kabir Nair (S103)
Department : IT    Semester : 3
------------------------------------------------------------------------
CODE       COURSE                       MARKS    GRADE    RESULT
CSE2006    Programming in Java          45.00    F        FAIL
CSE2001    Data Structures              58.00    D        PASS
MAT2002    Discrete Mathematics         69.00    C        PASS
------------------------------------------------------------------------
Average marks : 57.33
GPA (10 scale): 4.50
------------------------------------------------------------------------
```

```
------------------------------------------------------------------------
MARKS MATRIX
------------------------------------------------------------------------
STUDENT               CSE2006   CSE2001   MAT2002   AVG
Aarav Sharma          88.0      92.0      76.0      85.33
Diya Verma            74.0      65.0      81.0      73.33
Kabir Nair            45.0      58.0      69.0      57.33
------------------------------------------------------------------------
```

---

## Non-Functional Requirements

| # | Requirement | How it is met |
|---|---|---|
| 1 | **Reliability** | Auto-save thread + shutdown hook; corrupt CSV rows are skipped, not fatal |
| 2 | **Usability** | Numbered menus, clear prompts, blank-to-skip editing, readable tables |
| 3 | **Maintainability** | Layered packages; each class has one responsibility; storage is swappable |
| 4 | **Error handling** | Single `SMSException` hierarchy; all user errors caught in one place in the UI |
| 5 | **Performance** | Hash-map lookups are O(1); report generation is linear in record count |
| 6 | **Resource efficiency** | No external libraries; buffered streams; daemon thread sleeps between saves |

---

## Future Enhancements

- Replace `FileStorage` with a JDBC implementation (MySQL/SQLite) behind the same
  interface, then add JPA entities for `Student`, `Course` and `GradeRecord`
- Attendance tracking and attendance-weighted eligibility
- Semester-wise CGPA history and progress trend per student
- Role-based login for admin, faculty and student views
- Export reports to PDF/CSV and email them to mentors
- JavaFX or Spring Boot web front end over the same service layer

---

## Author

Built as a VITyarthi "Build Your Own Project" submission for CSE2006 – Programming in Java.# java-project-Smart-Student-Management---Academic-Performance-System
