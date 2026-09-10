package com.example.access;

import java.util.*;

// ==========================================
// 1. CUSTOM EXCEPTIONS & ENUMS
// ==========================================
class InvalidEmployeeDataException extends Exception {
    public InvalidEmployeeDataException(String message) {
        super(message);
    }
}

enum EmploymentStatus { ACTIVE, INACTIVE }
enum SecurityClearance { LOW, MEDIUM, HIGH }
enum EligibilityStatus { ELIGIBLE, CONDITIONALLY_ELIGIBLE, NOT_ELIGIBLE }

// ==========================================
// 2. DOMAIN MODELS
// ==========================================
class Employee {
    private final String id;
    private final String name;
    private final int age;
    private final String department;
    private final EmploymentStatus status;
    private final SecurityClearance clearance;
    private final boolean isIdValid;

    public Employee(String id, String name, int age, String department, 
                    EmploymentStatus status, SecurityClearance clearance, boolean isIdValid) 
                    throws InvalidEmployeeDataException {
        
        // Hardcoded constructor-level data verification
        if (id == null || id.trim().isEmpty()) throw new InvalidEmployeeDataException("Employee ID cannot be empty.");
        if (name == null || name.trim().isEmpty()) throw new InvalidEmployeeDataException("Employee name cannot be empty.");
        if (age < 0) throw new InvalidEmployeeDataException("Age cannot be negative.");
        if (department == null || department.trim().isEmpty()) throw new InvalidEmployeeDataException("Department cannot be empty.");
        if (status == null) throw new InvalidEmployeeDataException("Employment status cannot be null.");
        if (clearance == null) throw new InvalidEmployeeDataException("Security clearance cannot be null.");

        this.id = id.trim();
        this.name = name.trim();
        this.age = age;
        this.department = department.trim().toUpperCase();
        this.status = status;
        this.clearance = clearance;
        this.isIdValid = isIdValid;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getDepartment() { return department; }
    public EmploymentStatus getStatus() { return status; }
    public SecurityClearance getClearance() { return clearance; }
    public boolean isIdValid() { return isIdValid; }
}

class EligibilityResult {
    private final EligibilityStatus status;
    private final List<String> reasons;

    public EligibilityResult(EligibilityStatus status, List<String> reasons) {
        this.status = status;
        this.reasons = reasons != null ? reasons : new ArrayList<>();
    }

    public EligibilityStatus getStatus() { return status; }
    public List<String> getReasons() { return reasons; }

    @Override
    public String toString() {
        if (status == EligibilityStatus.ELIGIBLE) {
            return "Status: ELIGIBLE";
        }
        return "Status: " + status + " | Reasons: " + String.join(", ", reasons);
    }
}

// ==========================================
// 3. CORE ELIGIBILITY ENGINE
// ==========================================
class AccessControlService {
    private static final Set<String> AUTHORIZED_DEPARTMENTS = 
            Set.of("IT", "HR", "FINANCE", "ADMINISTRATION");

    public EligibilityResult evaluateAccess(Employee emp, SecurityClearance requiredClearance) {
        List<String> rejectionReasons = new ArrayList<>();

        // Rule 1: Age Check
        if (emp.getAge() < 21) {
            rejectionReasons.add("Employee is under 21 years old (Age: " + emp.getAge() + ").");
        }

        // Rule 2: Department Check
        if (!AUTHORIZED_DEPARTMENTS.contains(emp.getDepartment())) {
            rejectionReasons.add("Department '" + emp.getDepartment() + "' is not authorized.");
        }

        // Rule 3: Employment Status Check
        if (emp.getStatus() != EmploymentStatus.ACTIVE) {
            rejectionReasons.add("Employment status is not ACTIVE.");
        }

        // Rule 4: ID Validity Check
        if (!emp.isIdValid()) {
            rejectionReasons.add("Employee ID is invalid or expired.");
        }

        // Rule 5: Security Clearance Check
        boolean clearanceFailed = emp.getClearance().ordinal() < requiredClearance.ordinal();

        // Structural Decision Engine Matrix
        if (!rejectionReasons.isEmpty()) {
            return new EligibilityResult(EligibilityStatus.NOT_ELIGIBLE, rejectionReasons);
        } else if (clearanceFailed) {
            List<String> warnings = List.of("Insufficient clearance level. Has: " + emp.getClearance() + ", Needs: " + requiredClearance);
            return new EligibilityResult(EligibilityStatus.CONDITIONALLY_ELIGIBLE, warnings);
        }

        return new EligibilityResult(EligibilityStatus.ELIGIBLE, Collections.emptyList());
    }
}

// ==========================================
// 4. APPLICATION ENTRY POINT (Using Predefined Hardcoded Datasets)
// ==========================================
public class App {
    public static void main(String[] args) {
        AccessControlService service = new AccessControlService();
        List<Employee> employees = new ArrayList<>();

        System.out.println("=== INITIALIZING HARDCODED EMPLOYEE RESOURCE DATA ===");
        try {
            // Scenario A: Fully Eligible Dataset
            employees.add(new Employee("EMP-100", "Alice Vance", 28, "IT", 
                    EmploymentStatus.ACTIVE, SecurityClearance.HIGH, true));
            
            // Scenario B: Conditionally Eligible Dataset (Fails the required clearance check tier)
            employees.add(new Employee("EMP-200", "Bob Rickman", 35, "HR", 
                    EmploymentStatus.ACTIVE, SecurityClearance.LOW, true));
            
            // Scenario C: Multiple Failure Dataset (Fails all core metrics)
            employees.add(new Employee("EMP-300", "Charlie Miller", 19, "MARKETING", 
                    EmploymentStatus.INACTIVE, SecurityClearance.MEDIUM, false));
                    
            // Scenario D: Boundary Condition Evaluation Dataset (Exactly 21)
            employees.add(new Employee("EMP-400", "David Wright", 21, "FINANCE", 
                    EmploymentStatus.ACTIVE, SecurityClearance.MEDIUM, true));
                    
        } catch (InvalidEmployeeDataException e) {
            System.err.println("Fatal Data Ingestion Failure: " + e.getMessage());
            return;
        }

        // Target Verification Metric Definition (Evaluating against a High-Clearance Resource Requirement)
        SecurityClearance targetResourceRequirement = SecurityClearance.HIGH;
        System.out.println("\n=== PROCESSING ACCESS ELIGIBILITY DETERMINATION MATRIX ===");
        System.out.println("Global Target Resource Requirement Level: " + targetResourceRequirement + "\n");
        
        for (Employee emp : employees) {
            System.out.println("Checking profile matching ID [" + emp.getId() + "] - Name: " + emp.getName());
            EligibilityResult result = service.evaluateAccess(emp, targetResourceRequirement);
            System.out.println(result);
            System.out.println("--------------------------------------------------------------------------------");
        }
    }
}
