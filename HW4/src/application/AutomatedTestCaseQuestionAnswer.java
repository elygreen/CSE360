package application;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for creating and running automated test cases.
 * This class provides a framework for defining test cases, running tests,
 * and collecting test results.
 */
public class AutomatedTestCaseQuestionAnswer {
    
    /**
     * Represents the result of a test case execution.
     * Contains information about the test name, whether it passed, and a message.
     */
    public static class TestResult {
        /** The name of the test */
        private String testName;
        
        /** Flag indicating whether the test passed */
        private boolean passed;
        
        /** A message associated with the test result, usually describing the reason for failure */
        private String message;
        
        /**
         * Constructs a new TestResult with the specified parameters.
         *
         * @param testName the name of the test
         * @param passed whether the test passed
         * @param message a message describing the test result
         */
        public TestResult(String testName, boolean passed, String message) {
            this.testName = testName;
            this.passed = passed;
            this.message = message;
        }
        
        /**
         * Gets the name of the test.
         *
         * @return the test name
         */
        public String getTestName() {
            return testName;
        }
        
        /**
         * Checks if the test passed.
         *
         * @return true if the test passed, false otherwise
         */
        public boolean isPassed() {
            return passed;
        }
        
        /**
         * Gets the message associated with the test result.
         *
         * @return the test result message
         */
        public String getMessage() {
            return message;
        }    
    }
    
    /**
     * Interface for defining test cases.
     * Implementers of this interface must provide a run method that returns a TestResult.
     */
    public interface TestCase {
        /**
         * Runs the test case and returns the result.
         *
         * @return the result of running the test
         */
        TestResult run();
    }
    
    /** List of test cases to be run */
    private List<TestCase> testCases = new ArrayList<>();
    
    /**
     * Adds a test case to the collection of tests.
     *
     * @param testCase the test case to add
     */
    public void addTest(TestCase testCase) {
        testCases.add(testCase);
    }
    
    /**
     * Runs all test cases and collects their results.
     *
     * @return a list of test results from all test cases
     */
    public List<TestResult> runAllTests() {
        List<TestResult> results = new ArrayList<>();
        for (TestCase testCase : testCases) {
            results.add(testCase.run());
        }
        return results;
    }
    
    /**
     * Creates a TestResult that asserts a condition is true.
     *
     * @param testName the name of the test
     * @param condition the condition that should be true
     * @param message the message to include in the result
     * @return a TestResult indicating whether the condition was true
     */
    public static TestResult assertTrue(String testName, boolean condition, String message) {
        return new TestResult(testName, condition, message);
    }
    
    /**
     * Creates a TestResult that asserts a condition is false.
     *
     * @param testName the name of the test
     * @param condition the condition that should be false
     * @param message the message to include in the result
     * @return a TestResult indicating whether the condition was false
     */
    public static TestResult assertFalse(String testName, boolean condition, String message) {
        return new TestResult(testName, !condition, message);
    }
    
    /**
     * Creates a TestResult that asserts two objects are equal.
     *
     * @param testName the name of the test
     * @param expected the expected object
     * @param actual the actual object
     * @param message the message to include in the result
     * @return a TestResult indicating whether the objects were equal
     */
    public static TestResult assertEquals(String testName, Object expected, Object actual, String message) {
        boolean result = (expected == null && actual == null) || (expected != null && expected.equals(actual));
        return new TestResult(testName, result, message);
    }
    
}