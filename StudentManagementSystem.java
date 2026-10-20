import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Student Attendance and Academic Performance Management System
 * -------------------------------------------------------------
 * A console-based mini project in core Java.
 *
 * Concepts used: OOP, Collections (List, Map), File I/O (Serialization),
 * Exception handling, java.time API, Comparator / sorting, Menu-driven UI.
 *
 * Compile : javac StudentManagementSystem.java
 * Run     : java StudentManagementSystem
 */

// ======================= MAIN APPLICATION =======================
public class StudentManagementSystem {

    private static final Scanner sc = new Scanner(System.in);
    private static Map<String, Student> students = DataStore.load();

    public static void main(String[] args) {
        int choice;
        do {
            printMenu();
            choice = readInt("Enter your choice: ", 0, 11);
            switch (choice) {
                case 1 -> addStudent();
                case 2 -> viewAllStudents();
                case 3 -> updateStudent();
                case 4 -> deleteStudent();
                case 5 -> markAttendanceForDate();
                case 6 -> enterMarks();
                case 7 -> viewReportCard();
                case 8 -> viewAttendanceReport();
                case 9 -> showRankList();
                case 10 -> showLowAttendanceStudents();
                case 11 -> showClassStatistics();
                case 0 -> System.out.println("\nData saved. Goodbye!");
            }
            DataStore.save(students);
        } while (choice != 0);
    }

    // ---------------- MENU ----------------
    private static void printMenu() {
        System.out.println("\n==============================================");
        System.out.println("  STUDENT ATTENDANCE & PERFORMANCE SYSTEM");
        System.out.println("==============================================");
        System.out.println(" 1. Add Student");
        System.out.println(" 2. View All Students");
        System.out.println(" 3. Update Student");
        System.out.println(" 4. Delete Student");
        System.out.println(" 5. Mark Attendance (for a date)");
        System.out.println(" 6. Enter / Update Marks");
        System.out.println(" 7. View Report Card");
        System.out.println(" 8. View Attendance Report");
        System.out.println(" 9. Rank List");
        System.out.println("10. Low Attendance Students (< 75%)");
        System.out.println("11. Class Statistics");
        System.out.println(" 0. Save & Exit");
        System.out.println("----------------------------------------------");
    }

    // ---------------- STUDENT CRUD ----------------
    private static void addStudent() {
        System.out.println("\n--- Add Student ---");
        String id = readString("Student ID: ").toUpperCase();
        if (id.isEmpty() || students.containsKey(id)) {
            System.out.println("ID is empty or already exists.");
            return;
        }
        String name = readString("Name: ");
        String course = readString("Course / Class: ");
        if (name.isEmpty() || course.isEmpty()) {
            System.out.println("Name and course cannot be empty.");
            return;
        }
        students.put(id, new Student(id, name, course));
        System.out.println("Student added successfully.");
    }

    private static void viewAllStudents() {
        if (students.isEmpty()) { System.out.println("\nNo students found."); return; }
        System.out.println("\n--- All Students ---");
        System.out.printf("%-8s %-20s %-15s %-12s %-6s%n",
                "ID", "Name", "Course", "Attendance", "Grade");
        System.out.println("-------------------------------------------------------------");
        for (Student s : students.values()) {
            System.out.printf("%-8s %-20s %-15s %9.1f%%   %-6s%n",
                    s.getId(), s.getName(), s.getCourse(),
                    s.getAttendancePercentage(), s.getGrade());
        }
    }

    private static void updateStudent() {
        Student s = findStudent();
        if (s == null) return;
        String name = readString("New name (blank to keep '" + s.getName() + "'): ");
        String course = readString("New course (blank to keep '" + s.getCourse() + "'): ");
        if (!name.isEmpty()) s.setName(name);
        if (!course.isEmpty()) s.setCourse(course);
        System.out.println("Student updated.");
    }

    private static void deleteStudent() {
        Student s = findStudent();
        if (s == null) return;
        String confirm = readString("Delete " + s.getName() + "? (y/n): ");
        if (confirm.equalsIgnoreCase("y")) {
            students.remove(s.getId());
            System.out.println("Student deleted.");
        } else {
            System.out.println("Cancelled.");
        }
    }

    // ---------------- ATTENDANCE ----------------
    private static void markAttendanceForDate() {
        if (students.isEmpty()) { System.out.println("\nAdd students first."); return; }
        LocalDate date = readDate("Date (yyyy-mm-dd, blank = today): ");
        System.out.println("Enter P (present) or A (absent) for each student on " + date + ":");
        for (Student s : students.values()) {
            while (true) {
                String in = readString(String.format("  %-8s %-20s [P/A]: ", s.getId(), s.getName()));
                if (in.equalsIgnoreCase("P")) { s.markAttendance(date, true); break; }
                if (in.equalsIgnoreCase("A")) { s.markAttendance(date, false); break; }
                System.out.println("  Please type P or A.");
            }
        }
        System.out.println("Attendance recorded for " + date + ".");
    }

    private static void viewAttendanceReport() {
        Student s = findStudent();
        if (s == null) return;
        System.out.println("\n--- Attendance Report: " + s.getName() + " (" + s.getId() + ") ---");
        if (s.getAttendance().isEmpty()) { System.out.println("No attendance recorded yet."); return; }
        for (Map.Entry<LocalDate, Boolean> e : s.getAttendance().entrySet()) {
            System.out.println("  " + e.getKey() + "  " + (e.getValue() ? "Present" : "Absent"));
        }
        System.out.println("-------------------------------------");
        System.out.println("Total classes : " + s.getTotalClasses());
        System.out.println("Days present  : " + s.getDaysPresent());
        System.out.printf("Attendance    : %.2f%%%n", s.getAttendancePercentage());
        if (s.isShortage()) System.out.println("WARNING: Attendance shortage (below 75%)!");
    }

    // ---------------- MARKS ----------------
    private static void enterMarks() {
        Student s = findStudent();
        if (s == null) return;
        System.out.println("Enter marks out of 100 for " + s.getName() + ":");
        for (String subject : Student.SUBJECTS) {
            int mark = readInt("  " + subject + ": ", 0, 100);
            s.setMark(subject, mark);
        }
        System.out.println("Marks saved.");
    }

    private static void viewReportCard() {
        Student s = findStudent();
        if (s == null) return;
        System.out.println("\n==========================================");
        System.out.println("              REPORT CARD");
        System.out.println("==========================================");
        System.out.println("Name   : " + s.getName());
        System.out.println("ID     : " + s.getId());
        System.out.println("Course : " + s.getCourse());
        System.out.println("------------------------------------------");
        if (!s.hasMarks()) {
            System.out.println("Marks not entered yet.");
        } else {
            for (Map.Entry<String, Integer> e : s.getMarks().entrySet()) {
                System.out.printf("%-20s %3d  %s%n", e.getKey(), e.getValue(),
                        e.getValue() < Student.PASS_MARK ? "(Fail)" : "");
            }
            System.out.println("------------------------------------------");
            System.out.printf("Total      : %d / %d%n", s.getTotal(), Student.SUBJECTS.length * 100);
            System.out.printf("Average    : %.2f%n", s.getAverage());
            System.out.println("Grade      : " + s.getGrade());
            System.out.println("Result     : " + (s.hasPassed() ? "PASS" : "FAIL"));
        }
        System.out.printf("Attendance : %.2f%%%s%n", s.getAttendancePercentage(),
                s.isShortage() ? "  (Shortage)" : "");
        System.out.println("==========================================");
    }

    // ---------------- ANALYTICS ----------------
    private static void showRankList() {
        List<Student> list = new ArrayList<>();
        for (Student s : students.values()) if (s.hasMarks()) list.add(s);
        if (list.isEmpty()) { System.out.println("\nNo marks entered for any student."); return; }
        list.sort(Comparator.comparingDouble(Student::getAverage).reversed());

        System.out.println("\n--- Rank List ---");
        System.out.printf("%-5s %-8s %-20s %-8s %-8s %-6s%n",
                "Rank", "ID", "Name", "Total", "Average", "Grade");
        System.out.println("---------------------------------------------------------");
        int rank = 1;
        for (Student s : list) {
            System.out.printf("%-5d %-8s %-20s %-8d %-8.2f %-6s%n",
                    rank++, s.getId(), s.getName(), s.getTotal(), s.getAverage(), s.getGrade());
        }
    }

    private static void showLowAttendanceStudents() {
        System.out.println("\n--- Students with Attendance below 75% ---");
        boolean found = false;
        for (Student s : students.values()) {
            if (s.isShortage()) {
                System.out.printf("%-8s %-20s %6.2f%%%n", s.getId(), s.getName(),
                        s.getAttendancePercentage());
                found = true;
            }
        }
        if (!found) System.out.println("None. Everyone meets the attendance requirement.");
    }

    private static void showClassStatistics() {
        if (students.isEmpty()) { System.out.println("\nNo students found."); return; }
        int withMarks = 0, passed = 0;
        double sumAvg = 0, sumAtt = 0;
        Student topper = null;
        for (Student s : students.values()) {
            sumAtt += s.getAttendancePercentage();
            if (s.hasMarks()) {
                withMarks++;
                sumAvg += s.getAverage();
                if (s.hasPassed()) passed++;
                if (topper == null || s.getAverage() > topper.getAverage()) topper = s;
            }
        }
        System.out.println("\n--- Class Statistics ---");
        System.out.println("Total students      : " + students.size());
        System.out.printf("Average attendance  : %.2f%%%n", sumAtt / students.size());
        if (withMarks > 0) {
            System.out.printf("Class average marks : %.2f%n", sumAvg / withMarks);
            System.out.printf("Pass percentage     : %.2f%% (%d of %d)%n",
                    passed * 100.0 / withMarks, passed, withMarks);
            System.out.printf("Topper              : %s (%.2f)%n", topper.getName(), topper.getAverage());
        } else {
            System.out.println("No marks entered yet.");
        }
    }

    // ---------------- INPUT HELPERS ----------------
    private static Student findStudent() {
        if (students.isEmpty()) { System.out.println("\nNo students found."); return null; }
        String id = readString("Enter Student ID: ").toUpperCase();
        Student s = students.get(id);
        if (s == null) System.out.println("Student not found.");
        return s;
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                int v = Integer.parseInt(readString(prompt));
                if (v >= min && v <= max) return v;
                System.out.println("Enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            String in = readString(prompt);
            if (in.isEmpty()) return LocalDate.now();
            try {
                return LocalDate.parse(in);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use format yyyy-mm-dd.");
            }
        }
    }
}

// ======================= MODEL CLASS =======================
class Student implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String[] SUBJECTS =
            {"Maths", "Data Structure", "Operating System ", "OOSE", "Computer Science"};
    public static final int PASS_MARK = 40;
    public static final double MIN_ATTENDANCE = 75.0;

    private final String id;
    private String name;
    private String course;
    private final Map<String, Integer> marks = new LinkedHashMap<>();
    private final Map<LocalDate, Boolean> attendance = new TreeMap<>();

    public Student(String id, String name, String course) {
        this.id = id;
        this.name = name;
        this.course = course;
    }

    // ---- basic getters / setters ----
    public String getId() { return id; }
    public String getName() { return name; }
    public String getCourse() { return course; }
    public void setName(String name) { this.name = name; }
    public void setCourse(String course) { this.course = course; }

    // ---- attendance ----
    public void markAttendance(LocalDate date, boolean present) {
        attendance.put(date, present);
    }

    public Map<LocalDate, Boolean> getAttendance() { return attendance; }

    public int getTotalClasses() { return attendance.size(); }

    public int getDaysPresent() {
        int count = 0;
        for (boolean p : attendance.values()) if (p) count++;
        return count;
    }

    public double getAttendancePercentage() {
        if (attendance.isEmpty()) return 0.0;
        return (getDaysPresent() * 100.0) / attendance.size();
    }

    public boolean isShortage() {
        return !attendance.isEmpty() && getAttendancePercentage() < MIN_ATTENDANCE;
    }

    // ---- marks ----
    public void setMark(String subject, int mark) { marks.put(subject, mark); }

    public Map<String, Integer> getMarks() { return marks; }

    public boolean hasMarks() { return marks.size() == SUBJECTS.length; }

    public int getTotal() {
        int total = 0;
        for (int m : marks.values()) total += m;
        return total;
    }

    public double getAverage() {
        return marks.isEmpty() ? 0.0 : (double) getTotal() / marks.size();
    }

    public boolean hasPassed() {
        if (!hasMarks()) return false;
        for (int m : marks.values()) if (m < PASS_MARK) return false;
        return true;
    }

    public String getGrade() {
        if (!hasMarks()) return "-";
        if (!hasPassed()) return "F";
        double avg = getAverage();
        if (avg >= 90) return "O";
        if (avg >= 80) return "A+";
        if (avg >= 70) return "A";
        if (avg >= 60) return "B";
        if (avg >= 50) return "C";
        return "D";
    }
}

// ======================= FILE STORAGE =======================
class DataStore {
    private static final String FILE_NAME = "students.dat";

    @SuppressWarnings("unchecked")
    public static Map<String, Student> load() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return new LinkedHashMap<>();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            return (Map<String, Student>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Could not read saved data, starting fresh: " + e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    public static void save(Map<String, Student> students) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(students);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }
}
