package application;

/////////////////////////////////////////////////
/// Validator class for Questions and Answers ///
/////////////////////////////////////////////////

public class QuestionValidator {
	public static final int MIN_QUESTION_LENGTH = 5;
	public static final int MAX_QUESTION_LENGTH = 150;
	public static final int MIN_ANSWER_LENGTH = 1;
	public static final int MAX_ANSWER_LENGTH = 500;
	
	// Check if question is valid
	public static ValidationResult validateQuestion(String questionText) {
		// check if empty
		if (questionText == null) {
			return new ValidationResult(false, "Question cannot be empty.");
		}
		
		String trimmed_question = questionText.trim();
		if (trimmed_question.isEmpty()) {
			return new ValidationResult(false, "Question cannot be empty.");
		}
		
		// min length
		if (trimmed_question.length() < MIN_QUESTION_LENGTH) {
			return new ValidationResult(false, String.format("Question must be at least %d characters.", MIN_QUESTION_LENGTH));
		}
		
		// max length
		if (trimmed_question.length() > MAX_QUESTION_LENGTH) {
			return new ValidationResult(false, String.format("Question cannot be more than %d characters.", MAX_QUESTION_LENGTH));
		}
		
		// SQL injection check
		if (containsSQLInjection(trimmed_question)) {
			return new ValidationResult(false, String.format("Question contains potential SQL injection. Please rephrase"));
		}
		
		return new ValidationResult(true, "Accepted Question");
	}
	
	// Check if answer is valid
	public static ValidationResult validateAnswer(String answerText) {
		// check if empty
		if (answerText == null) {
			return new ValidationResult(false, "Answer cannot be empty.");
		}
		
		String trimmed_answer = answerText.trim();
		if (trimmed_answer.isEmpty()) {
			return new ValidationResult(false, "Answer cannot be empty.");
		}
		
		// min length
		if (trimmed_answer.length() < MIN_ANSWER_LENGTH) {
			return new ValidationResult(false, String.format("Answer must be at least %d characters.", MIN_ANSWER_LENGTH));
		}
		
		// max length
		if (trimmed_answer.length() > MAX_ANSWER_LENGTH) {
			return new ValidationResult(false, String.format("Answer cannot be more than %d characters.", MAX_ANSWER_LENGTH));
		}
		
		// SQL injection check
		if (containsSQLInjection(trimmed_answer)) {
			return new ValidationResult(false, String.format("Answer contains potential SQL injection. Please rephrase"));
		}
		
		return new ValidationResult(true, "Accepted Answer");
	}
	
	// Return bool of if text contains common SQL injection phrases
	private static boolean containsSQLInjection(String text) {
		String text_lowercase = text.toLowerCase();
		String[] patterns = {"drop table", "delete from", "insert into", "update ", "select ", ";", "--", "/*", "*/", "exec ", "execute ", "xp_", "sp_"};
		
		for (String pattern : patterns) {
			if (text_lowercase.contains(pattern)) {
				return true;
			}
		}
		return false;
	}
	
	
	// ValidationResult helper class that QuestionValidator uses to return information
	// about whether a question was valid and why
	public static class ValidationResult {
		private boolean valid;
		private String message;
		// constructor
		public ValidationResult(boolean valid, String message) {
			this.valid = valid;
			this.message = message;
		}
		// valid getter
		public boolean isValid() {
			return valid;
		}
		// message getter
		public String getMessage() {
			return message;
		}
		
	}
	
}