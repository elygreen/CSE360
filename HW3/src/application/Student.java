package application;

public class Student extends User {
	
	public Student(String username, String password) {
		super(username, password, "student");
	}
	
	@Override
    public boolean canAskQuestions() {
        return true;
    }

    @Override
    public boolean canAnswerQuestions() {
        return true;
    }
}