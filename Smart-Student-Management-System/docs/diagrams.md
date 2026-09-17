# Design Diagrams

All diagrams are written in Mermaid, which GitHub renders automatically.
For the PDF report, open this file on GitHub and take screenshots, or paste the
code into <https://mermaid.live> to export PNG/SVG.

---

## 1. System Architecture

```mermaid
flowchart TB
    subgraph P["Presentation Layer"]
        UI["ConsoleUI<br/>menus, prompts, error display"]
        MAIN["Main<br/>bootstrap and wiring"]
    end
    subgraph S["Service Layer (business logic)"]
        SS["StudentService<br/>Module 1"]
        CS["CourseService<br/>Module 2"]
        GS["GradeService<br/>Module 3a"]
        RS["ReportService<br/>Module 3b"]
        AS["AutoSaveService<br/>background thread"]
    end
    subgraph M["Domain Model"]
        PER["Person (abstract)"]
        STU["Student"]
        COU["Course"]
        GR["GradeRecord"]
        LG["LetterGrade (enum)"]
        GA["Gradable (interface)"]
    end
    subgraph U["Cross-cutting"]
        IV["InputValidator"]
        MU["MathUtil"]
        EX["Exception hierarchy"]
    end
    subgraph D["Persistence Layer"]
        FS["FileStorage"]
        CSV[("data/*.csv")]
    end

    MAIN --> UI
    UI --> SS & CS & GS & RS
    UI --> IV
    SS & CS & GS --> M
    RS --> SS & CS & GS
    RS --> MU
    AS --> SS & CS & GS
    SS & CS & GS & RS --> FS
    FS --> CSV
    S -.throws.-> EX
```

---

## 2. Use Case Diagram

```mermaid
flowchart LR
    ADMIN(("Administrator"))
    FACULTY(("Faculty"))
    MENTOR(("Class Mentor"))

    subgraph SYSTEM["Smart Student Management System"]
        UC1["Add / Update / Delete Student"]
        UC2["Search & List Students"]
        UC3["Manage Course Catalogue"]
        UC4["Enroll / Un-enroll Student"]
        UC5["Record & Update Marks"]
        UC6["View Report Card"]
        UC7["View Rank List / Top Performers"]
        UC8["View Course Statistics"]
        UC9["View Marks Matrix"]
        UC10["Export Institute Report"]
        UC11["Save Data"]
    end

    ADMIN --> UC1
    ADMIN --> UC2
    ADMIN --> UC7
    ADMIN --> UC10
    ADMIN --> UC11
    FACULTY --> UC3
    FACULTY --> UC4
    FACULTY --> UC5
    FACULTY --> UC8
    FACULTY --> UC9
    MENTOR --> UC6
    MENTOR --> UC2
```

---

## 3. Class Diagram

```mermaid
classDiagram
    class Person {
        <<abstract>>
        -String id
        -String name
        -int age
        -String email
        +getRole()* String
        +toString() String
    }
    class Student {
        -String department
        -int semester
        -List~String~ enrolledCourses
        +Student(id,name,age,email,dept,sem)
        +Student(id,name,age,email,dept)
        +getRole() String
        +enroll(code) boolean
        +unenroll(code) boolean
        +isEnrolled(code) boolean
        +toCsv() String
        +fromCsv(line)$ Student
        +compareTo(Student) int
    }
    class Course {
        -String code
        -String title
        -int credits
        -String faculty
        +toCsv() String
        +fromCsv(line)$ Course
    }
    class Gradable {
        <<interface>>
        +getPercentage() double
        +getLetterGrade() LetterGrade
        +isPass() boolean
    }
    class GradeRecord {
        -String studentId
        -String courseCode
        -double marks
        +key() String
        +getPercentage() double
        +getLetterGrade() LetterGrade
    }
    class LetterGrade {
        <<enumeration>>
        S A B C D F
        -String description
        -double gradePoint
        -double minPercentage
        +fromPercentage(double)$ LetterGrade
    }
    class StudentService {
        -Map~String,Student~ students
        +addStudent(Student)
        +getStudent(id) Student
        +updateStudent(...)
        +deleteStudent(id)
        +search(name) List
        +search(name,dept) List
        +load()
        +save()
    }
    class CourseService {
        -Map~String,Course~ courses
        +addCourse(Course)
        +enroll(sid,code)
        +unenroll(sid,code)
        +studentsInCourse(code) List
        +deleteCourse(code)
    }
    class GradeService {
        -Map~String,GradeRecord~ records
        +recordMarks(sid,code,marks)
        +getByStudent(sid) List
        +getByCourse(code) List
        +calculateGpa(sid) double
        +averageMarks(sid) double
    }
    class ReportService {
        +reportCard(sid) String
        +rankList() List
        +topPerformers(n) List
        +courseStatistics(code) String
        +marksMatrix() String
        +instituteSummary() String
        +exportReport() String
    }
    class AutoSaveService {
        -boolean running
        -long intervalMillis
        +run()
        +saveAll()
        +shutdown()
    }
    class FileStorage {
        -File directory
        +readLines(file) List
        +writeLines(file,lines)
        +writeText(file,content)
    }
    class SMSException
    class InvalidInputException
    class DuplicateRecordException
    class RecordNotFoundException
    class Thread
    class Exception

    Person <|-- Student
    Gradable <|.. GradeRecord
    GradeRecord --> LetterGrade
    Thread <|-- AutoSaveService
    Exception <|-- SMSException
    SMSException <|-- InvalidInputException
    SMSException <|-- DuplicateRecordException
    SMSException <|-- RecordNotFoundException

    StudentService o-- Student
    CourseService o-- Course
    CourseService --> StudentService
    GradeService o-- GradeRecord
    GradeService --> StudentService
    GradeService --> CourseService
    ReportService --> StudentService
    ReportService --> CourseService
    ReportService --> GradeService
    AutoSaveService --> StudentService
    AutoSaveService --> CourseService
    AutoSaveService --> GradeService
    StudentService --> FileStorage
    CourseService --> FileStorage
    GradeService --> FileStorage
```

---

## 4. Sequence Diagram — Recording Marks

```mermaid
sequenceDiagram
    actor F as Faculty
    participant UI as ConsoleUI
    participant IV as InputValidator
    participant GS as GradeService
    participant SS as StudentService
    participant CS as CourseService
    participant AS as AutoSaveService
    participant FS as FileStorage

    F->>UI: choose "Record / update marks"
    UI->>F: prompt student ID, course code, marks
    F->>UI: S101, CSE2006, 88
    UI->>IV: requireMarks("88")
    IV-->>UI: 88.0
    UI->>GS: recordMarks("S101","CSE2006",88.0)
    GS->>SS: getStudent("S101")
    alt student missing
        SS--xGS: RecordNotFoundException
        GS--xUI: propagate
        UI->>F: [ERROR] No student found with ID S101
    else student exists
        SS-->>GS: Student
        GS->>CS: getCourse("CSE2006")
        CS-->>GS: Course
        GS->>GS: check enrollment & range 0-100
        GS->>GS: store GradeRecord
        GS-->>UI: GradeRecord (88.0, grade A, PASS)
        UI->>F: Recorded: S101 CSE2006 88.00 A PASS
    end
    Note over AS,FS: every 30s the daemon thread flushes data
    AS->>FS: writeLines("grades.csv", ...)
```

---

## 5. Sequence Diagram — Generating a Report Card

```mermaid
sequenceDiagram
    actor M as Mentor
    participant UI as ConsoleUI
    participant RS as ReportService
    participant SS as StudentService
    participant GS as GradeService
    participant CS as CourseService

    M->>UI: Analytics > Student report card > S103
    UI->>RS: reportCard("S103")
    RS->>SS: getStudent("S103")
    SS-->>RS: Student
    RS->>GS: getByStudent("S103")
    GS-->>RS: List<GradeRecord>
    loop for each record
        RS->>CS: getCourse(code)
        CS-->>RS: Course title
        RS->>RS: record.getLetterGrade(), isPass()
    end
    RS->>GS: averageMarks("S103")
    GS-->>RS: 57.33
    RS->>GS: calculateGpa("S103")
    GS-->>RS: 4.50
    RS-->>UI: formatted report card
    UI->>M: display report card
```

---

## 6. Process / Workflow Diagram

```mermaid
flowchart TD
    A([Start application]) --> B[Load students.csv, courses.csv, grades.csv]
    B --> C{Any data found?}
    C -- No --> D[Seed sample data]
    C -- Yes --> E
    D --> E[Start auto-save daemon thread]
    E --> F[Display main menu]
    F --> G{Option?}
    G -- 1 --> H[Student Management menu]
    G -- 2 --> I[Course & Enrollment menu]
    G -- 3 --> J[Analytics menu]
    G -- 4 --> K[Save all data now]
    G -- 0 --> L[Save and exit]
    H --> M{Input valid?}
    I --> M
    J --> M
    M -- No --> N[Catch SMSException and show message] --> F
    M -- Yes --> O[Update in-memory collections / build report] --> F
    K --> F
    L --> P([Stop thread, flush to CSV, End])
```

---

## 7. ER / Storage Design

```mermaid
erDiagram
    STUDENT ||--o{ ENROLLMENT : "registers for"
    COURSE  ||--o{ ENROLLMENT : "is taken by"
    ENROLLMENT ||--o| GRADE_RECORD : "is evaluated by"

    STUDENT {
        string student_id PK "S101 format"
        string name
        int    age
        string email
        string department
        int    semester
        string enrolled_courses "pipe separated"
    }
    COURSE {
        string course_code PK "CSE2006 format"
        string title
        int    credits
        string faculty
    }
    ENROLLMENT {
        string student_id FK
        string course_code FK
    }
    GRADE_RECORD {
        string student_id FK
        string course_code FK
        double marks "0-100"
    }
```

### Physical file schema

| File | Format | Example row |
|---|---|---|
| `data/students.csv` | `id,name,age,email,department,semester,course1\|course2\|...` | `S101,Aarav Sharma,19,aarav@vit.ac.in,CSE,3,CSE2006\|CSE2001` |
| `data/courses.csv` | `code,title,credits,faculty` | `CSE2006,Programming in Java,3,Dr. Ashwin` |
| `data/grades.csv` | `studentId,courseCode,marks` | `S101,CSE2006,88.0` |
| `data/report_*.txt` | Plain text export | generated on demand |

**Note:** the enrollment relation is denormalised into the `enrolled_courses`
column of `students.csv`, since flat-file storage has no join capability. The
`GRADE_RECORD` composite key `(student_id, course_code)` is represented in
memory as the map key `studentId::courseCode`.
