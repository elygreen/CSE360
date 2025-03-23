/**
 * QuestionAnswerTestMainline.java
 * 
 * Stand alone automated test class that validates both question and answer input
 * functionality as well as the database storage. 
 * 
 * Validates:
 * 	- Question Length Constraints
 *  - Question SQL Injection prevention
 *	- Question Database persistance
 *	- Answer Length Constraints
 *  - Answer SQL Injection prevention
 *	- Answer Database persistance
 * 
 * @author Ely Greenstein
 * @version 1.0
 * @since March 21 2025
 *
 */

package application;
import java.util.List;

import databasePart1.DatabaseHelper;;

public class QuestionAnswerTestMainline {
	
	/**
	 * Main method that executes the automated tests in sequence and prints out
	 * a report of all the results. 
	 * 
	 * @param args Command line arguments (not used)
	 */
	
	
	public static void main(String[] args) {
		System.out.println("---Q&A Input & Database Validation Automated Test---");
		// Create DB connection
		DatabaseHelper databaseHelper = new DatabaseHelper();
		try {
			databaseHelper.connectToDatabase();
			System.out.println("Connected to database");
		}
		catch (Exception e) {
			System.out.println("Failed to connect to database; error mesage: " + e.getMessage());
			return;
		}
		
		// Use AutomatedTestCaseQuestionAnswer.java testRunner
		AutomatedTestCaseQuestionAnswer testRunner = new AutomatedTestCaseQuestionAnswer();
		User testUser = new User("TestUser", "password", "student");
		
		runAllTests(testRunner, databaseHelper, testUser);
		
		// Close DB connection
		databaseHelper.closeConnection();
		System.out.println("Automated tests completed.");
	}
	
	/**
	 * Runs all test cases sequentially for Q and A validation and database operations 
	 * This method defines and executes all the tests
	 * 
	 * @param testRunner The test framework for running and tracking test results
	 * @param databaseHelper Database connection helper for persistence operations
	 * @param testUser Test user account used for creating questions and answers for the test cases
	 */
	
	private static void runAllTests(AutomatedTestCaseQuestionAnswer testRunner, DatabaseHelper databaseHelper, User testUser) {
		
		/** Test Case 1: Valid Question Input
		 * 
		 * Verifies that a properly formatted question is
		 * 1. Accepted by the validation system
		 * 2. Saved to the database
		 * 3. Assigned a valid ID by the database
		 * 
		 * After verification, the test question is deleted to maintain the database
		 */
		
    	testRunner.addTest(() -> {
    		String validQuestion = "When is the assignment due?";
    		QuestionValidator.ValidationResult result = QuestionValidator.validateQuestion(validQuestion);
    		if (result.isValid()) {
    			Question testQuestion = new Question(validQuestion, testUser.getUserName());
    			int questionID = databaseHelper.saveQuestion(testQuestion);
    			if (questionID > 0) {
    				databaseHelper.deleteQuestion(testQuestion);
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case #1, Valid Question: ", true, "Valid question; was accepted & saved to DB.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case #1, Valid Question: 1", false, "Valid question; was accepted but failed to save to database.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case #1, Valid Question: ", false, "Question rejected: " + result.getMessage());
    		}
    	});
    	
		/** Test case 2: Question Too Short Test
		 * 
		 * Verifies that questions shorter than the minimum required length are:
		 * 1. Identified by the validation system
		 * 2. Rejected with an error message
		 * 
		 * A question with only two characters is used to test the minimum length validation.
		 */
    	
    	testRunner.addTest(() -> {
    		String validQuestion = "..";
    		QuestionValidator.ValidationResult result = QuestionValidator.validateQuestion(validQuestion);
    		if (result.isValid()) {
    			Question testQuestion = new Question(validQuestion, testUser.getUserName());
    			int questionID = databaseHelper.saveQuestion(testQuestion);
    			if (questionID > 0) {
    				databaseHelper.deleteQuestion(testQuestion);
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 2: Question too short", true, "Valid question; was accepted & saved to DB.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 2: Question too short", false, "Valid question; was accepted but failed to save to database.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 2: Question too short", false, "Question rejected: " + result.getMessage());
    		}
    	});
    	
		/** Test case 3: Question Too Long Test
		 * 
		 * Verifies that questions longer than the maximum required length are:
		 * 1. Identified by the validation system
		 * 2. Rejected with an error message
		 * 
		 * A question with over 400 characters is used to test the maximum length validation.
		 */
    	
    	testRunner.addTest(() -> {
    		String validQuestion = "......................................................................"
    				+ "..................................................................................."
    				+ "..................................................................................."
    				+ "..................................................................................."
    				+ "..................................................................................."
    				+ "..................................................................................."
    				+ "..................................................................................."
    				+ "..................................................................................."
    				+ "...................................................................................";
    		QuestionValidator.ValidationResult result = QuestionValidator.validateQuestion(validQuestion);
    		if (result.isValid()) {
    			Question testQuestion = new Question(validQuestion, testUser.getUserName());
    			int questionID = databaseHelper.saveQuestion(testQuestion);
    			if (questionID > 0) {
    				databaseHelper.deleteQuestion(testQuestion);
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 2: Question too long", true, "Valid question; was accepted & saved to DB.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 2: Question too long", false, "Valid question; was accepted but failed to save to database.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 2: Question too long", false, "Question rejected: " + result.getMessage());
    		}
    	});
    	
		/** Test case 4: Question With SQL Injection Test
		 * 
		 * Verifies that questions containing SQL injection keywords are:
		 * 1. Identified by the validation system
		 * 2. Rejected with an error message
		 * 
		 * A question with the keywords "DROP TABLE Questions xp__ -- __" is used to test the SQL Injection validation.
		 */
    	
    	testRunner.addTest(() -> {
    		String validQuestion = "DROP TABLE Questions xp__ -- __";
    		QuestionValidator.ValidationResult result = QuestionValidator.validateQuestion(validQuestion);
    		if (result.isValid()) {
    			Question testQuestion = new Question(validQuestion, testUser.getUserName());
    			int questionID = databaseHelper.saveQuestion(testQuestion);
    			if (questionID > 0) {
    				databaseHelper.deleteQuestion(testQuestion);
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 4: SQL Injection", true, "Valid question; was accepted & saved to DB.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 4: SQL Injection", false, "Valid question; was accepted but failed to save to database.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 4: SQL Injection", false, "Question rejected: " + result.getMessage());
    		}
    	});
    	
		/** Test Case 5: Valid Answer Input
		 * 
		 * Verifies that a properly formatted Answer is
		 * 1. Accepted by the validation system
		 * 2. Saved to the database
		 * 3. Assigned a valid ID by the database
		 * 
		 * After verification, the test Answer is deleted to maintain the database
		 */
    	
    	testRunner.addTest(() -> {
    		String validQuestion = "Valid question to ask";
    		Question question = new Question(validQuestion, testUser.getUserName());
    		int questionID = databaseHelper.saveQuestion(question);
    		
    		String input_answer = "This is a valid answer";
    		QuestionValidator.ValidationResult validationResult = QuestionValidator.validateAnswer(input_answer);
    		if (validationResult.isValid()) {
    			Answer answer = new Answer(input_answer, testUser.getUserName());
    			boolean answerSaved = databaseHelper.saveAnswer(questionID, answer);
    			databaseHelper.deleteQuestion(question);
    			
    			if (answerSaved) {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case #5: Valid answer", true, "Answer was accepted and saved.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case #5: Valid answer", true, "Answer was accepted but not saved.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case #5: Valid answer", false, "Answer was not accepted: " + validationResult.getMessage());
    		}
    	});
    	
		/** Test case 6: Answer Too Short Test
		 * 
		 * Verifies that answer shorter than the minimum required length are:
		 * 1. Identified by the validation system
		 * 2. Rejected with an error message
		 * 
		 * An answer with only two characters is used to test the minimum length validation.
		 */
    	
    	testRunner.addTest(() -> {
    		String validQuestion = "Valid question";
    		Question question = new Question(validQuestion, testUser.getUserName());
    		int questionID = databaseHelper.saveQuestion(question);
    		
    		String input_answer = "";
    		QuestionValidator.ValidationResult validationResult = QuestionValidator.validateAnswer(input_answer);
    		if (validationResult.isValid()) {
    			Answer answer = new Answer(input_answer, testUser.getUserName());
    			boolean answerSaved = databaseHelper.saveAnswer(questionID, answer);
    			databaseHelper.deleteQuestion(question);
    			
    			if (answerSaved) {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 6: empty answer", true, "Answer was accepted and saved.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 6: empty answer", true, "Answer was accepted but not saved.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 6: empty answer", false, "Answer was not accepted: " + validationResult.getMessage());
    		}
    	});
    	
		/** Test case 7: Answer Too Long Test
		 * 
		 * Verifies that answers longer than the maximum required length are:
		 * 1. Identified by the validation system
		 * 2. Rejected with an error message
		 * 
		 * An answer with over 400 characters is used to test the maximum length validation.
		 */
    	
    	testRunner.addTest(() -> {
    		String validQuestion = "Hello";
    		Question question = new Question(validQuestion, testUser.getUserName());
    		int questionID = databaseHelper.saveQuestion(question);
    		
    		String input_answer = "\"......................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\";";
    		QuestionValidator.ValidationResult validationResult = QuestionValidator.validateAnswer(input_answer);
    		if (validationResult.isValid()) {
    			Answer answer = new Answer(input_answer, testUser.getUserName());
    			boolean answerSaved = databaseHelper.saveAnswer(questionID, answer);
    			databaseHelper.deleteQuestion(question);
    			
    			if (answerSaved) {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 7: too long of answer", true, "Answer was accepted and saved.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 7: too long of answer", true, "Answer was accepted but not saved.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 7: too long of answer", false, "Answer was not accepted: " + validationResult.getMessage());
    		}
    	});
    	
		/** Test case 8: Answer With SQL Injection Test
		 * 
		 * Verifies that answers containing SQL injection keywords are:
		 * 1. Identified by the validation system
		 * 2. Rejected with an error message
		 * 
		 * An answer with the keywords "DROP TABLE Questions xp__ -- __" is used to test the SQL Injection validation.
		 */
    	
    	testRunner.addTest(() -> {
    		String validQuestion = "Valid question";
    		Question question = new Question(validQuestion, testUser.getUserName());
    		int questionID = databaseHelper.saveQuestion(question);
    		
    		String input_answer = "TDROP TABLE -- __ update ";
    		QuestionValidator.ValidationResult validationResult = QuestionValidator.validateAnswer(input_answer);
    		if (validationResult.isValid()) {
    			Answer answer = new Answer(input_answer, testUser.getUserName());
    			boolean answerSaved = databaseHelper.saveAnswer(questionID, answer);
    			databaseHelper.deleteQuestion(question);
    			
    			if (answerSaved) {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 8: SQL injection answer", true, "Answer was accepted and saved.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 8: SQL injection answer", true, "Answer was accepted but not saved.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 8: SQL injection answer", false, "Answer was not accepted: " + validationResult.getMessage());
    		}
    	});
    	
    	// Run all tests and output the results
    	List<AutomatedTestCaseQuestionAnswer.TestResult> results = testRunner.runAllTests();
    	System.out.println("\n----- TEST SUMMARY -----");
    	System.out.println("Total tests ran: " + results.size());
    	
    	int passCount = 0;
    	for (AutomatedTestCaseQuestionAnswer.TestResult result : results) {
    		System.out.println(result.getTestName() + ": " + (result.isPassed() ? "PASSED" : "FAILED") + " - " + result.getMessage());
    		if (result.isPassed()) {
    			passCount++;
    		}
    	}
    
    	System.out.println("------------------------");
    	System.out.println("Tests passed: " + passCount + "/" + results.size() + " (" + (passCount * 100 / results.size()) + "%)");
	}
	
	
}