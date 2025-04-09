package application;

/**
 * A utility class that provides validation for review texts.
 * This class ensures reviews meet length requirements and 
 * do not contain potential SQL injection patterns.
 */
public class ReviewValidator {
    /** The minimum allowed length for a review text */
    public static final int MIN_REVIEW_LENGTH = 2;
    
    /** The maximum allowed length for a review text */
    public static final int MAX_REVIEW_LENGTH = 350;

    /**
     * An inner class that represents the result of a validation operation.
     * Contains information about whether the validation passed and 
     * a descriptive message.
     */
    public static class ValidationResult {
        /** Flag indicating whether the validation was successful */
        private boolean valid;
        
        /** Message describing the validation result or error */
        private String message;
        
        /**
         * Constructs a new ValidationResult with the specified validity and message.
         *
         * @param valid whether the validation was successful
         * @param message descriptive message about the validation result
         */
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        /**
         * Returns whether the validation was successful.
         *
         * @return true if validation passed, false otherwise
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * Returns the message describing the validation result.
         *
         * @return descriptive message about the validation result
         */
        public String getMessage() {
            return message;
        }
    }

    /**
     * Validates the given review text against various criteria.
     * Checks if the review:
     * - Is not empty
     * - Meets minimum length requirements
     * - Does not exceed maximum length
     * - Does not contain potential SQL injection patterns
     *
     * @param reviewText the review text to validate
     * @return a ValidationResult object indicating whether the review is valid and providing a descriptive message
     * @throws NullPointerException if reviewText is null
     */
    public static QuestionValidator.ValidationResult validateReview(String reviewText) {
        // check if empty
        String trimmed_review = reviewText.trim();
        if (trimmed_review.isEmpty()) {
            return new QuestionValidator.ValidationResult(false, "Review cannot be empty.");
        }

        // check min length
        if (trimmed_review.length() < MIN_REVIEW_LENGTH) {
            return new QuestionValidator.ValidationResult(false, 
                String.format("Review must be at least %d characters.", MIN_REVIEW_LENGTH));
        }

        // check max length
        if (trimmed_review.length() > MAX_REVIEW_LENGTH) {
            return new QuestionValidator.ValidationResult(false, 
                String.format("Review cannot be more than %d characters.", MAX_REVIEW_LENGTH));
        }

        // check SQL injection
        if (containsSQLInjection(trimmed_review)) {
            return new QuestionValidator.ValidationResult(false, 
                String.format("Review contains potential SQL injection. Please rephrase"));
        }

        return new QuestionValidator.ValidationResult(true, "Accepted Review");
    }

    /**
     * Checks if the given text contains common SQL injection patterns.
     * This is a simple pattern-matching check for basic SQL keywords and syntax.
     *
     * @param text the text to check for SQL injection patterns
     * @return true if potential SQL injection is detected, false otherwise
     */
    private static boolean containsSQLInjection(String text) {
        String text_lowercase = text.toLowerCase();
        String[] patterns = {"drop table", "delete from", "insert into", "update ", "select ", ";", 
                            "--", "/*", "*/", "exec ", "execute ", "xp_", "sp_"};

        for (String pattern : patterns) {
            if (text_lowercase.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}