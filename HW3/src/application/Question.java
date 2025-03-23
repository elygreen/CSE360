package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Collections;	// imp
import java.util.Comparator;	// Imp


public class Question {
    private String body;
    private String askedBy;
    private ObservableList<Answer> answers;
    public Question(String body, String askedBy) {
        this.body = body;
        this.askedBy = askedBy;
        this.answers = FXCollections.observableArrayList();
    }

    public String getBody() {
    	return body;
    }
    
    public void setBody(String body) {
    	this.body = body;
    }
    
    public String getAskedBy() {
    	return askedBy;
    }
    
    public void setAskedBy(String askedBy) {
    	this.askedBy = askedBy;
    }
    
    public ObservableList<Answer> getAnswers(){
    	return answers;
    }
    
    public void addAnswer(Answer answer) {
    	this.answers.add(answer);
    }
    
    public void sortAnswers() {
        FXCollections.sort(answers, Comparator
                .comparing(Answer::isCorrect, Comparator.reverseOrder()) // Correct answers first
                .thenComparing(a -> a.getUpvotes() - a.getDownvotes(), Comparator.reverseOrder())); // Sort by score
    }
    
}
