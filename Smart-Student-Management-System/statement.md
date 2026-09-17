# Problem Statement

## 1. Problem Statement

Most small colleges, coaching institutes and individual departments still track
student records and marks in scattered spreadsheets or paper registers. This
creates four recurring problems:

1. **Data duplication and inconsistency** — the same student appears with
   different spellings, ages or departments in different sheets.
2. **No validation** — marks above 100, blank names or malformed email ids are
   entered silently and discovered only at the end of the semester.
3. **Manual analysis** — computing averages, GPA, pass percentage and rank
   lists by hand is slow and error prone, so faculty rarely do it more than
   once a term.
4. **Late identification of weak students** — because analytics are not
   available on demand, students who are slipping are spotted too late for
   remedial help.

**The Smart Student Management & Academic Performance System (SSMAPS)** solves
this by providing a single, validated, file-backed Java application that stores
student and course records, captures marks against enrollments, and instantly
derives grades, GPA, rank lists and course-level statistics.

## 2. Scope of the Project

### In scope
- Creating, reading, updating and deleting student records.
- Maintaining a course catalogue and enrolling students into courses.
- Recording and updating marks for a valid (student, course) enrollment.
- Automatic conversion of marks into letter grades and credit-weighted GPA.
- Report cards, GPA rank lists, top performers, course statistics, a marks
  matrix, an institute summary, and export of reports to a text file.
- Persistence of all data to CSV files using Java I/O streams.
- Background auto-save using a daemon thread.

### Out of scope
- Graphical user interface (the system is console based by design).
- Multi-user login, roles and authentication over a network.
- Relational database / JDBC connectivity — flat file storage is used instead
  so that the project runs with only a JDK installed.
- Attendance, fee management, timetable scheduling.

## 3. Target Users

| User | How they use the system |
|---|---|
| **Faculty / Course coordinator** | Adds courses, enrolls students, records marks, reviews course statistics and grade distribution. |
| **Academic administrator / HOD** | Manages the student master list, views rank lists, top performers and the institute summary, exports reports. |
| **Class advisor / mentor** | Opens an individual report card to identify weak subjects and counsel the student. |

## 4. High-Level Features

### Module 1 — Student Management
- Add a student with validated ID, name, age, email, department and semester.
- View all students sorted alphabetically.
- Search students by partial name, or by name plus department.
- Update any subset of a student's fields.
- Delete a student and cascade-remove that student's grade records.

### Module 2 — Course & Enrollment Management
- Add and list courses with code, title, credits and faculty.
- Enroll and un-enroll a student in a course, with duplicate protection.
- List all students registered for a course.
- Record or update marks — only allowed if the student is actually enrolled.
- Delete a course and automatically clear related enrollments.

### Module 3 — Academic Performance Analytics
- Individual report card with per-course marks, letter grade and pass/fail.
- Credit-weighted GPA on a 10-point scale.
- Rank list of all students, ordered by GPA.
- Top-N performers.
- Course statistics: average, highest, lowest, pass percentage and a grade
  distribution histogram.
- Students × courses marks matrix built on a 2-D array.
- Institute summary and export of the full report to a timestamped text file.

### Supporting capabilities
- Custom checked-exception hierarchy for all domain errors.
- Centralised input validation with regex-based rules.
- CSV persistence through character streams (Reader/Writer).
- Background auto-save thread with synchronized access to shared data.
- A dependency-free test runner covering 38 validation checks.
