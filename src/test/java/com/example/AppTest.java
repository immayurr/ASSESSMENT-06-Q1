package com.example.access;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class AppTest {

    private AccessControlService service;

    @BeforeEach
    public void setUp() {
        service = new AccessControlService();
    }

    // ==========================================
    // NORMAL SCENARIO
    // ==========================================
    @Test
    public void testEligibleEmployee() throws InvalidEmployeeDataException {
        Employee emp = new Employee("TEST-01", "John Doe", 30, "IT", 
                EmploymentStatus.ACTIVE, SecurityClearance.HIGH, true);
        
        EligibilityResult result = service.evaluateAccess(emp, SecurityClearance.MEDIUM);
        
        assertEquals(EligibilityStatus.ELIGIBLE, result.getStatus());
        assertTrue(result.getReasons().isEmpty());
    }

    // ==========================================
    // BOUNDARY SCENARIOS
    // ==========================================
    @Test
    public void testAgeBoundaryConditionExact21() throws InvalidEmployeeDataException {
        Employee emp = new Employee("TEST-02", "Jane Boundary", 21, "HR", 
                EmploymentStatus.ACTIVE, SecurityClearance.MEDIUM, true);
        
        EligibilityResult result = service.evaluateAccess(emp, SecurityClearance.MEDIUM);
        assertEquals(EligibilityStatus.ELIGIBLE, result.getStatus());
    }

    @Test
    public void testAgeBoundaryConditionUnder21() throws InvalidEmployeeDataException {
        Employee emp = new Employee("TEST-03", "Junior Dev", 20, "IT", 
                EmploymentStatus.ACTIVE, SecurityClearance.HIGH, true);
        
        EligibilityResult result = service.evaluateAccess(emp, SecurityClearance.LOW);
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, result.getStatus());
        assertTrue(result.getReasons().get(0).contains("under 21"));
    }

    // ==========================================
    // CONDITIONAL ELIGIBILITY SCENARIOS
    // ==========================================
    @Test
    public void testConditionallyEligibleDueToClearance() throws InvalidEmployeeDataException {
        Employee emp = new Employee("TEST-04", "Alice Key", 25, "FINANCE", 
                EmploymentStatus.ACTIVE, SecurityClearance.LOW, true);
        
        EligibilityResult result = service.evaluateAccess(emp, SecurityClearance.HIGH);
        
        assertEquals(EligibilityStatus.CONDITIONALLY_ELIGIBLE, result.getStatus());
        assertEquals(1, result.getReasons().size());
        assertTrue(result.getReasons().get(0).contains("Insufficient clearance level"));
    }

    // ==========================================
    // MULTIPLE FAILURE SCENARIO
    // ==========================================
    @Test
    public void testMultipleRuleFailuresAccumulation() throws InvalidEmployeeDataException {
        Employee emp = new Employee("TEST-05", "Bad Profile", 19, "SALES", 
                EmploymentStatus.INACTIVE, SecurityClearance.LOW, false);
        
        EligibilityResult result = service.evaluateAccess(emp, SecurityClearance.LOW);
        
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, result.getStatus());
        List<String> reasons = result.getReasons();
        
        assertEquals(4, reasons.size());
        assertTrue(reasons.stream().anyMatch(r -> r.contains("under 21")));
        assertTrue(reasons.stream().anyMatch(r -> r.contains("not authorized")));
        assertTrue(reasons.stream().anyMatch(r -> r.contains("not ACTIVE")));
        assertTrue(reasons.stream().anyMatch(r -> r.contains("invalid or expired")));
    }

    // ==========================================
    // INVALID INPUT DATA VALIDATION SCENARIOS
    // ==========================================
    @Test
    public void testInvalidInputEmptyIdThrowsException() {
        assertThrows(InvalidEmployeeDataException.class, () -> {
            new Employee("", "Valid Name", 30, "IT", 
                    EmploymentStatus.ACTIVE, SecurityClearance.MEDIUM, true);
        });
    }

    @Test
    public void testInvalidInputNegativeAgeThrowsException() {
        assertThrows(InvalidEmployeeDataException.class, () -> {
            new Employee("TEST-06", "Valid Name", -5, "IT", 
                    EmploymentStatus.ACTIVE, SecurityClearance.MEDIUM, true);
        });
    }
}
