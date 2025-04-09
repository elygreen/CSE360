package application;

/**
 * Validator class for Questions and Answers.
 * 
 * This class provides static methods to validate question and answer text inputs
 * according to defined constraints, including length requirements and SQL injection checks.
 */
public class QuestionValidator {
    /** Minimum allowed length for a question in characters. */
    public static final int MIN_QUESTION_LENGTH = 5;
    
    /** Maximum allowed length for a question in characters. */
    public static final int MAX_QUESTION_LENGTH = 150;
    
    /** Minimum allowed length for an answer in characters. */
    public static final int MIN_ANSWER_LENGTH = 1;
    
    /** Maximum allowed length for an answer in characters. */
    public static final int MAX_ANSWER_LENGTH = 500;

    /**
     * Validates if the provided question text meets all requirements.
     * 
     * @param questionText The question text to validate
     * @return A ValidationResult object containing validation status and message
     */
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

    /**
     * Validates if the provided answer text meets all requirements.
     * 
     * @param answerText The answer text to validate
     * @return A ValidationResult object containing validation status and message
     */
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

    /**
     * Checks if the provided text contains common SQL injection patterns.
     * 
     * @param text The text to check for SQL injection patterns
     * @return true if potential SQL injection is detected, false otherwise
     */
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

    /**
     * Helper class that encapsulates the result of a validation operation.
     * 
     * This class stores whether the validation was successful and a message
     * describing the validation result.
     */
    public static class ValidationResult {
        /** Flag indicating whether the validation was successful. */
        private boolean valid;
        
        /** Message describing the validation result. */
        private String message;
        
        /**
         * Constructs a new ValidationResult with the specified validity and message.
         * 
         * @param valid true if validation succeeded, false otherwise
         * @param message description of the validation result
         */
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        /**
         * Returns whether the validation was successful.
         * 
         * @return true if validation succeeded, false otherwise
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * Returns the message describing the validation result.
         * 
         * @return a string containing the validation message
         */
        public String getMessage() {
            return message;
        }
    }
}