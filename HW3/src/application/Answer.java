package application;

public class Answer {
	private String text;
	private String answeredBy;
	
	private int upVote;
	private int downVote;
	private boolean isCorrect;
	
	public Answer(String text, String answeredBy) {
		this.text = text;
		this.answeredBy = answeredBy;
		this.upVote = 0;
		this.downVote = 0;
		this.isCorrect = false;
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
	
	public int getUpvotes() {
        return upVote;
    }
	
	public int getDownvotes() {
        return downVote;
    }

    public void upvote() {
        upVote++;
    }

    public void downvote() {
        downVote++;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void markAsCorrect() {
        this.isCorrect = true;
    }
	
	
}