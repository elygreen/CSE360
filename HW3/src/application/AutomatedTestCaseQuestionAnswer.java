/**
 * AutomatedTestCaseQuestionAnswer.java
 * 
 * Test framework that provides functionality for creating, running, and evaluating automated test cases.
 * This class supports adding test cases, running tests, and collecting test results with pass and fail statuses and messages.
 * 
 * The framework provides:
 * 	- A TestResult class for storing test outcomes
 * 	- A TestCase interface for implementing test logic
 * 	- Methods for adding and running tests
 * 	- Assertion utilities
 * 
 * @author Ely Greenstein
 * @version 1.0
 * @since March 21, 2025
 */



package application;

import java.util.ArrayList;
import java.util.List;

public class AutomatedTestCaseQuestionAnswer{
	
	/**
	 * TestResult is an inner class that encapsulates the outcome of test execution. It stores the name, pass / fail, and result message.
	 */
	
	public static class TestResult{
		private String testName;
		private boolean passed;
		private String message;
		
		/**
		 * Constructs a new TestResult with specific parameters
		 *
		 * @param testName Name of test
		 * @param passed True if test passed, false if test failed
		 * @param message Descriptive message about test result
		 */
		public TestResult(String testName, boolean passed, String message) {
			this.testName = testName;
			this.passed = passed;
			this.message = message;
		}
		
		/**
		 * Gets name of test
		 * 
		 * @return Test name
		 */
		public String getTestName() {
			return testName;
		}
		
		/**
		 * Determines if test passed
		 * 
		 * @return True if test passed, false if test failed
		 */
		public boolean isPassed() {
			return passed;
		}
		
		/**
		 * Gets descriptive message about the test result.
		 * 
		 * @return Test result message
		 */
		public String getMessage() {
			return message;
		}	
	}
	
	
	/**
	 * TestCase is a functional interface that defines the contract for test implementation. Each test must implement the
	 * run method which returns a TestResult.
	 */
	public interface TestCase {
		TestResult run();
	}
	
	private List<TestCase> testCases = new ArrayList<>();
	
	
	/**
	 * Add test case to collection of tests intended to be run.
	 * 
	 * @param testCase Test case to add
	 */
	public void addTest(TestCase testCase) {
		testCases.add(testCase);
	}
	
	
	/**
	 * Runs all test cases added to test runner.
	 * 
	 * @return List of TestResult objects containing data about their outcomes.
	 */
	public List<TestResult> runAllTests(){
		List<TestResult> results = new ArrayList<>();
		for (TestCase testCase : testCases) {
			results.add(testCase.run());
		}
		return results;
	}
	
	
	/**
	 * Creates a TestResult object that asserts a condition is true
	 * 
	 * @param testName Name of test
	 * @param condition Condition to evaluate
	 * @param message Descriptive message about test
	 * @return TestResult indicating whether the condition was true
	 */
	public static TestResult assertTrue(String testName, boolean condition, String message) {
		return new TestResult(testName, condition, message);
	}
	
	
	/**
	 * Creates a TestResult that asserts a condition is false
	 * 
	 * @param testName Name of test
	 * @param condition Condition to evaluate
	 * @param message Descriptive message about test
	 * @return TestResult indicating whether the condition was false
	 */
	public static TestResult assertFalse(String testName, boolean condition, String message) {
		return new TestResult(testName, !condition, message);
	}
	
	
	/**
	 * Creates a TestResult that asserts two objects are equivalent.
	 * 
	 * @param testName Name of test
	 * @param expected Expected object
	 * @param actual The actual object for comparison
	 * @param message Descriptive message about the test
	 * @return TestResult indicating whether the objects were equal
	 */
	public static TestResult assertEquals(String testName, Object expected, Object actual, String message) {
		boolean result = (expected == null && actual == null) || (expected != null && expected.equals(actual));
		return new TestResult(testName, result, message);
	}
	
}