package application;

/**
 * Represents a review for an answer in the question and answer system.
 * This class stores information about the review content, the reviewer,
 * the answer being reviewed, and tracks helpful/not helpful votes.
 * 
 * @author Application Team
 * @version 1.0
 */
public class Review {
    /** The text content of the review */
    private String reviewBody;
    
    /** Username of the person who wrote the review */
    private String reviewedBy;
    
    /** Unique identifier of the answer being reviewed */
    private int answerID;
    
    /** Counter for the number of users who found this review helpful */
    private int helpfulCount;
    
    /** Counter for the number of users who found this review not helpful */
    private int notHelpfulCount;

    /**
     * Constructs a new Review object with the specified details.
     * Initializes the helpful and not helpful counts to zero.
     *
     * @param reviewBody The text content of the review
     * @param reviewedBy The username of the user who wrote the review
     * @param answerID The ID of the answer being reviewed
     */
    public Review(String reviewBody, String reviewedBy, int answerID) {
        this.reviewBody = reviewBody;
        this.reviewedBy = reviewedBy;
        this.answerID = answerID;
        this.helpfulCount = 0;
        this.notHelpfulCount = 0;
    }

    /**
     * Constructs a new Review object with the specified details including
     * initial helpful and not helpful vote counts.
     *
     * @param reviewBody The text content of the review
     * @param reviewedBy The username of the user who wrote the review
     * @param answerID The ID of the answer being reviewed
     * @param helpfulCount Number of helpful votes to initialize with
     * @param notHelpfulCount Number of not helpful votes to initialize with
     */
    public Review(String reviewBody, String reviewedBy, int answerID, int helpfulCount, int notHelpfulCount) {
        this.reviewBody = reviewBody;
        this.reviewedBy = reviewedBy;
        this.answerID = answerID;
        this.helpfulCount = helpfulCount;
        this.notHelpfulCount = notHelpfulCount;
    }

    /**
     * Returns the text content of this review.
     *
     * @return The review's text content
     */
    public String getReviewBody() {
        return reviewBody;
    }

    /**
     * Returns the username of the person who wrote this review.
     *
     * @return The reviewer's username
     */
    public String getReviewedBy() {
        return reviewedBy;
    }

    /**
     * Returns the ID of the answer being reviewed.
     *
     * @return The answer ID
     */
    public int getAnswerID() {
        return answerID;
    }

    /**
     * Returns the current count of helpful votes for this review.
     *
     * @return The number of helpful votes
     */
    public int getHelpfulCount() {
        return helpfulCount;
    }

    /**
     * Returns the current count of not helpful votes for this review.
     *
     * @return The number of not helpful votes
     */
    public int getNotHelpfulCount() {
        return notHelpfulCount;
    }

    /**
     * Updates the text content of this review.
     *
     * @param reviewBody The new text content
     */
    public void setReviewBody(String reviewBody) {
        this.reviewBody = reviewBody;
    }

    /**
     * Updates the username of the reviewer.
     *
     * @param reviewedBy The new reviewer username
     */
    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    /**
     * Updates the ID of the answer being reviewed.
     *
     * @param answerID The new answer ID
     */
    public void setAnswerID(int answerID) {
        this.answerID = answerID;
    }

    /**
     * Updates the count of helpful votes for this review.
     *
     * @param helpfulCount The new helpful votes count
     */
    public void setHelpfulCount(int helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    /**
     * Updates the count of not helpful votes for this review.
     *
     * @param notHelpfulCount The new not helpful votes count
     */
    public void setNotHelpfulCount(int notHelpfulCount) {
        this.notHelpfulCount = notHelpfulCount;
    }

    /**
     * Increments the helpful votes count by one.
     */
    public void incrementHelpful() {
        this.helpfulCount++;
    }

    /**
     * Increments the not helpful votes count by one.
     */
    public void incremenetNotHelpful() {
        this.notHelpfulCount++;
    }

    /**
     * Calculates a score for the review based on the ratio of helpful votes to total votes.
     * 
     * @return An integer score from 0-100 representing the percentage of
     *         helpful votes, or 0 if there are no votes
     */
    public int getReviewScore() {
        int totalVotes = helpfulCount + notHelpfulCount;
        if (totalVotes == 0) {
            return 0;
        }
        int reviewScore = (int)(((double)helpfulCount / totalVotes) * 100);
        return reviewScore;
    }
}