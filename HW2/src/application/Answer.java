package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Answer {
	private String text;
	private String answeredBy;
	
	public Answer(String text, String answeredBy) {
		this.text = text;
		this.answeredBy = answeredBy;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getAnsweredBy() {
		return answeredBy;
	}
	
	public void setAnsweredBy(String answeredBy) {
		this.answeredBy = answeredBy;
	}
	
	
	
}