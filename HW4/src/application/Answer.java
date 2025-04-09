package application;

/**
 * Represents an answer to a question in a QandA application.
 * This class manages the answer's content, author, voting statistics, and correctness status.
 */
public class Answer {
    /** The text content of the answer */
    private String text;
    
    /** The name or identifier of the person who provided this answer */
    private String answeredBy;
    
    /** The count of upvotes this answer has received */
    private int upVote;
    
    /** The count of downvotes this answer has received */
    private int downVote;
    
    /** The unique identifier for this answer */
    private int id;
    
    /** Flag indicating whether this answer has been marked as correct */
    private boolean isCorrect;

	private boolean hasReview;

    /**
     * Constructs a new Answer with the specified text and author.
     * New answers start with zero votes and are not marked as correct.
     *
     * @param text The text content of the answer
     * @param answeredBy The name or identifier of the person providing the answer
     */
    public Answer(String text, String answeredBy) {
        this.text = text;
        this.answeredBy = answeredBy;
        this.upVote = 0;
        this.downVote = 0;
        this.isCorrect = false;
        this.hasReview = false;
    }

    /**
     * Gets the text content of this answer.
     *
     * @return The answer's text content
     */
    public String getText() {
        return text;
    }

    /**
     * Updates the text content of this answer.
     *
     * @param text The new text content
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Gets the name or identifier of the person who provided this answer.
     *
     * @return The answerer's name or identifier
     */
    public String getAnsweredBy() {
        return answeredBy;
    }

    /**
     * Updates the name or identifier of the person who provided this answer.
     *
     * @param answeredBy The new answerer's name or identifier
     */
    public void setAnsweredBy(String answeredBy) {
        this.answeredBy = answeredBy;
    }

    /**
     * Gets the number of upvotes this answer has received.
     *
     * @return The count of upvotes
     */
    public int getUpvotes() {
        return upVote;
    }

    /**
     * Gets the number of downvotes this answer has received.
     *
     * @return The count of downvotes
     */
    public int getDownvotes() {
        return downVote;
    }

    /**
     * Gets the unique identifier for this answer.
     *
     * @return The answer's ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this answer.
     *
     * @param id The new ID value
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Increments the upvote count by one.
     */
    public void upvote() {
        upVote++;
    }

    /**
     * Increments the downvote count by one.
     */
    public void downvote() {
        downVote++;
    }

    /**
     * Sets the upvote count to a specific value.
     *
     * @param upvote The new upvote count
     */
    public void setUpvote(int upvote) {
        this.upVote = upvote;
    }

    /**
     * Sets the downvote count to a specific value.
     *
     * @param downvote The new downvote count
     */
    public void setDownvote(int downvote) {
        this.downVote = downvote;
    }

    /**
     * Checks if this answer has been marked as correct.
     *
     * @return {@code true} if the answer is marked as correct, {@code false} otherwise
     */
    public boolean isCorrect() {
        return isCorrect;
    }

    /**
     * Marks this answer as correct.
     * Once marked as correct, the status cannot be reverted in the current implementation.
     */
    public void markAsCorrect() {
        this.isCorrect = true;
    }
    
    /**
     * Marks this answer as reviewed.
     * @return bool depending on when it's reviewed or not
     */
	public boolean hasReview() {
		// TODO Auto-generated method stub
		return this.hasReview = true;
	}
	
	/** Flag indicating whether this answer has been marked as sensitive */
	private boolean isSensitive;

	/**
	 * Checks if this answer is marked sensitive.
	 *
	 * @return true if the answer is marked as sensitive, false otherwise
	 */
	public boolean isSensitive() {
	    return isSensitive;
	}

	/**
	 * Marks this answer as sensitive.
	 */
	public void markAsSensitive() {
	    this.isSensitive = true;
	}

	/**
	 * Unmarks this answer as sensitive.
	 */
	public void unmarkAsSensitive() {
	    this.isSensitive = false;
	}
	
}