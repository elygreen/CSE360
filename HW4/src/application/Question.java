package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Collections;	// imp
import java.util.Comparator;	// Imp

/**
 * Represents a question in a QandA application.
 * This class handles the storage and manipulation of question data,
 * including the question body, author, votes, and associated answers.
 */
public class Question {
    /** The text content of the question */
    private String body;
    
    /** Username of the person who asked the question */
    private String askedBy;
    
    /** Number of upvotes received by this question */
    private int upvotes;
    
    /** Number of downvotes received by this question */
    private int downvotes;
    
    /** Observable list containing all answers to this question */
    private ObservableList<Answer> answers;
    
    /**
     * Constructs a new Question with the given body and author.
     * Initializes votes to zero and creates an empty answers list.
     *
     * @param body The text content of the question
     * @param askedBy Username of the person asking the question
     */
    public Question(String body, String askedBy) {
        this.body = body;
        this.askedBy = askedBy;
        this.answers = FXCollections.observableArrayList();
    }
    
    /**
     * Gets the text content of the question.
     *
     * @return The question's body text
     */
    public String getBody() {
    	return body;
    }
    
    /**
     * Gets the number of upvotes for this question.
     *
     * @return The upvote count
     */
    public int getUpvotes() {
    	return upvotes;
    }
    
    /**
     * Gets the number of downvotes for this question.
     *
     * @return The downvote count
     */
    public int getDownvotes() {
    	return downvotes;
    }
    
    /**
     * Sets the number of upvotes for this question.
     *
     * @param upvotes The new upvote count
     */
    public void setUpvotes(int upvotes) {
    	this.upvotes = upvotes;
    }
    
    /**
     * Sets the number of downvotes for this question.
     *
     * @param downvotes The new downvote count
     */
    public void setDownvotes(int downvotes) {
    	this.downvotes = downvotes;
    }
    
    /**
     * Increments the upvote count by one.
     */
    public void upvote() {
    	upvotes++;
    }
    
    /**
     * Increments the downvote count by one.
     */
    public void downvote() {
    	downvotes++;
    }
    
    /**
     * Updates the text content of the question.
     *
     * @param body The new question text
     */
    public void setBody(String body) {
    	this.body = body;
    }
    
    /**
     * Gets the username of the person who asked the question.
     *
     * @return The username of the question's author
     */
    public String getAskedBy() {
    	return askedBy;
    }
    
    /**
     * Updates the username of the question's author.
     *
     * @param askedBy The new username of the author
     */
    public void setAskedBy(String askedBy) {
    	this.askedBy = askedBy;
    }
    
    /**
     * Gets the list of answers to this question.
     *
     * @return An ObservableList containing all answers
     */
    public ObservableList<Answer> getAnswers(){
    	return answers;
    }
    
    /**
     * Adds a new answer to this question.
     *
     * @param answer The answer to add
     */
    public void addAnswer(Answer answer) {
    	this.answers.add(answer);
    }
    
    /**
     * Sorts the answers associated with this question.
     * Answers are sorted first by correctness (correct answers first), then by score (upvotes minus downvotes, in descending order).
     */
    public void sortAnswers() {
        FXCollections.sort(answers, Comparator
                .comparing(Answer::isCorrect, Comparator.reverseOrder()) // Correct answers first
                .thenComparing(a -> a.getUpvotes() - a.getDownvotes(), Comparator.reverseOrder())); // Sort by score
    }
    
    /** Flag indicating whether this question has been marked as sensitive */
    private boolean isSensitive;

    /**
     * Checks if this question is marked sensitive.
     *
     * @return true if the question is marked as sensitive, false otherwise
     */
    public boolean isSensitive() {
        return isSensitive;
    }

    /**
     * Marks this question as sensitive.
     */
    public void markAsSensitive() {
        this.isSensitive = true;
    }

    /**
     * Unmarks this question as sensitive.
     */
    public void unmarkAsSensitive() {
        this.isSensitive = false;
    }
    
}