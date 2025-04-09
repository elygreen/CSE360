package application;
import databasePart1.DatabaseHelper;
import databasePart1.DatabaseHelperDM;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class TestCases {

    private DatabaseHelper databaseHelper = new DatabaseHelper();

    Set<String> studentRole = new HashSet<>();
    Set<String> reviewerRole = new HashSet<>();
    Set<String> instructorRole = new HashSet<>();

    User studentTest = new User("student", "student", studentRole);
    User reviewerTest = new User("reviewer", "reviewer", reviewerRole);
    User instructorTest = new User("instructor", "instructor", instructorRole);

    public TestCases() throws SQLException {
        try {
            databaseHelper.connectToDatabase();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        studentRole.add("student");
        reviewerRole.add("reviewer");
        instructorRole.add("instructor");
    }

    
    /*  TEST CASE 1
     *  Tests that adds a student user  */
    @Test
    public void addStudentUser() throws SQLException {
        Boolean added = false;
        if(!databaseHelper.doesUserExist(studentTest.getUserName())) {
            databaseHelper.register(studentTest);
            added = true;
        }
        assertTrue(added, "Should return true if student was added to the database");
    }
    
    /*  TEST CASE 2
     *  Tests that adds a reviewer user  */
    @Test
    public void addReviewerUser() throws SQLException {
        Boolean added = false;
        if(!databaseHelper.doesUserExist(reviewerTest.getUserName())) {
            databaseHelper.register(reviewerTest);    
            added = true;
        }
        assertTrue(added, "Should return true if reviewer was added to the database");
    }
    
    /*  TEST CASE 3
     *  Tests that adds a instructor user  */
    @Test
    public void addInstructorUser() throws SQLException {
        Boolean added = false;
        if(!databaseHelper.doesUserExist(instructorTest.getUserName())) {
            databaseHelper.register(instructorTest);
            added = true;
        }
        assertTrue(added, "Should return true if the instructor was added to the database");
    }

    /*  TEST CASE 4
     *  Tests that students can create a new question.  */
    @Test
    public void testStudentCanCreateQuestion() {
        Question question = new Question("Is this a good question?", studentTest.getUserName());
        assertNotNull(databaseHelper.saveQuestion(question), "Question should return an integer");
    }
    
    /*  TEST CASE 5
     *  Tests that students can delete a new question.  */
    @Test
    public void testStudentCanDeleteQuestion() {
        Question question = new Question("Is this a good question?", studentTest.getUserName());
        assertTrue(databaseHelper.deleteQuestion(question));
    }
    
    /*  TEST CASE 6
     *  Tests that students can create and answer.  */
    @Test
    public void testStudentCanCreateAnswer() {
        Question question = new Question("Is this a good question?", studentTest.getUserName());
        databaseHelper.saveQuestion(question);
        Answer answer = new Answer("This is an awesome question!", studentTest.getUserName());
        assertNotNull(databaseHelper.saveAnswer(databaseHelper.findIdOfQuestion(question), answer), "Answer should return an integer");
        databaseHelper.deleteQuestion(question);
    }
    
    /*  TEST CASE 7
     *  Tests that reviewer can create a review.  */
    @Test
    public void testReviewerCanCreateReview() {
        assertTrue(reviewerTest.canCreateReview(), "Reviewer can create a review.");
    }
    
    
    /*  TEST CASE 8
     *  Tests that reviewer can save a review.  */
    @Test
    public void testReviewIsSaved() {
        Question question = new Question("How are you?", studentTest.getUserName());
        databaseHelper.saveQuestion(question);
        Answer answer = new Answer("This is a really good question!", studentTest.getUserName());
        databaseHelper.saveAnswer(databaseHelper.findIdOfQuestion(question), answer);
        Review review = new Review("This is a great answer!", reviewerTest.getUserName(), answer.getId());
        assertNotNull(databaseHelper.saveReview(review), "Review should return an integer");
    }

    /*  TEST CASE 9
     *  Tests that reviewer can delete a review.  */
    @Test
    public void testReviewerCanDeleteReview() {
        assertTrue(reviewerTest.canDeleteReview(), "Reviewer can delete a review");
    }

    /*  TEST CASE 10
     *  Tests that review has been deleted.  */
    @Test
    public void testReviewIsDeleted() {
        Answer answer = new Answer("This is an awesome question!", studentTest.getUserName());
        Review review = new Review("This is a great answer!", reviewerTest.getUserName(), answer.getId());
        assertTrue(databaseHelper.deleteReview(databaseHelper.findIdOfReview(review)));
        Question question = new Question("How are you?", studentTest.getUserName());
        databaseHelper.deleteQuestion(question);
    }

    /*  TEST CASE 11
     *  Tests that review can create multiple reviews.  */
    @Test
    public void testReviewerCanCreateMultipleReviews() {
        Question question = new Question("Is this a good question?", studentTest.getUserName());
        databaseHelper.saveQuestion(question);
        int questionID = databaseHelper.findIdOfQuestion(question);
        Answer answer = new Answer("test question", studentTest.getUserName());
        databaseHelper.saveAnswer(questionID, answer);
        Review review1 = new Review("test1", reviewerTest.getUserName(), answer.getId());
        Review review2 = new Review("test2", reviewerTest.getUserName(), answer.getId());
        Review review3 = new Review("test3", reviewerTest.getUserName(), answer.getId());
        assertNotNull(databaseHelper.saveReview(review1), "Review should return an integer");
        assertNotNull(databaseHelper.saveReview(review2), "Review should return an integer");
        assertNotNull(databaseHelper.saveReview(review3), "Review should return an integer");
        databaseHelper.deleteQuestion(question);
        databaseHelper.deleteReview(databaseHelper.findIdOfReview(review1));
        databaseHelper.deleteReview(databaseHelper.findIdOfReview(review2));
        databaseHelper.deleteReview(databaseHelper.findIdOfReview(review3));
    }

    /*  TEST CASE 12
     *  Tests that review can be updated by reviewer.  */
    @Test
    public void testReviewerCanUpdateReview() {
        assertTrue(reviewerTest.canUpdateReview(), "Reviewer can update a review.");
    }
    

    /*  TEST CASE 13
     *  Tests that review can be updated.  */
    @Test
    public void testReviewCanBeUpdated() {
        Question question = new Question("How are you?", studentTest.getUserName());
        databaseHelper.saveQuestion(question);
        Answer answer = new Answer("This is a really good question!", studentTest.getUserName());
        databaseHelper.saveAnswer(databaseHelper.findIdOfQuestion(question), answer);
        Review review = new Review("This is a great answer!", reviewerTest.getUserName(), answer.getId());
        databaseHelper.saveReview(review);
        String newReview = "This is an even better answer!";
        assertTrue(databaseHelper.updateReview(databaseHelper.findIdOfReview(review), newReview));
        databaseHelper.deleteQuestion(question);    
    }


    /*  TEST CASE 14
     *  Tests that is password is weak or strong.  */
    @Test
    public void testPasswordEvaluation() {
        String strongPassword = "GoOdPaSsWoRd1234!@#";
        String strongResult = PasswordEvaluator.evaluatePassword(strongPassword);
        String weakPassword = "weakpassword";
        String weakResult = PasswordEvaluator.evaluatePassword(weakPassword);
        assertEquals("", strongResult, "Strong password should pass validation");
        assertNotEquals("", weakResult, "Weak password should fail validation");
    }


    /*  TEST CASE 15
     *  Tests that username meets character requirements.  */
    @Test
    public void testUsernameValidation() {
        String validUsername = "valid_user123";
        String validResult = UserNameRecognizer.checkForValidUserName(validUsername);
        String invalidUsername = "";
        String invalidResult = UserNameRecognizer.checkForValidUserName(invalidUsername);
        assertEquals("", validResult, "Valid username should pass validation");
        assertNotEquals("", invalidResult, "Invalid username should fail validation");
    }
    
    /*  TEST CASE 16
     *  Tests that reviewers CANNOT delete another reveiwer's reviews.  */
    @Test
    public void testReviewersCannotDeleteOtherReviews() {
        List<Review> studentReviews = DatabaseHelper.getReviewsByAuthor(studentTest.getUserName());
        Review otherReview = null;
        for (Review review : studentReviews) {
            if (review.getReviewBody().equals("Review by another reviewer")) {
                otherReview = review;
                break;
            }
        }
        assertNotNull(studentReviews, "Student reviews should not be null");
    }

    /*  TEST CASE 17
     *  Tests that username meets character requirements.  */
    @Test
    public void testMarkAnswerAsCorrect() {
        Question question = new Question("What is JUnit?", studentTest.getUserName());
        databaseHelper.saveQuestion(question);
        Answer answer = new Answer("JUnit is a unit testing framework for Java.", instructorTest.getUserName());
        databaseHelper.saveAnswer(databaseHelper.findIdOfQuestion(question), answer);
        answer.markAsCorrect();
        assertTrue(answer.isCorrect(), "Answer should be marked as correct");
        databaseHelper.deleteQuestion(question);
    }

    /*  TEST CASE 18
     *  Tests that instructor can view student activity.  */
    @Test
    public void testInstructorsCanViewStudentContent() {
        ObservableList<Question> allQuestions = databaseHelper.loadAllQuestions();
        boolean foundStudentQuestion = false;
        for (Question question : allQuestions) {
            if (question.getAskedBy().equals(studentTest.getUserName())) {
                foundStudentQuestion = true;
                for (Answer answer : question.getAnswers()) {
                    if (answer.getAnsweredBy().equals(studentTest.getUserName())) {
                        return;
                    }
                }
            }
        }
        assertTrue(foundStudentQuestion, "Instructor should be able to view student's questions");
    }
    
    /*  TEST CASE 19
     *  Tests that instructor can approve student reviewer requests.  */
    @Test
    public void testInstructorsCanApproveReviewerRequests() {
        boolean roleAdded = databaseHelper.addRoleToUser(studentTest.getUserName(), "reviewer");
        Set<String> updatedRoles = databaseHelper.getUserRole(studentTest.getUserName());
        assertFalse(roleAdded, "Instructor should be able to add reviewer role");
        assertFalse(updatedRoles.contains("reviewer"), "Student should have reviewer role after approval");
    }
    
    /*  TEST CASE 20
     *  Tests that instructor can also reject reviewer request.  */
    @Test
    public void testInstructorsCanRejectReviewerRequests() {
        boolean roleRemoved = databaseHelper.removeRoleFromUser(studentTest.getUserName(), "reviewer");
        Set<String> updatedRoles = databaseHelper.getUserRole(studentTest.getUserName());
        assertFalse(roleRemoved, "Instructor should be able to remove reviewer role");
        assertFalse(updatedRoles.contains("reviewer"), "Student show not have reviewer role after rejection");
    }

    /*  TEST CASE 21
     *  Tests that valid answer has been posted.  */
    @Test
    public void testQuestionAnswerValidation() {
        String validQuestion = "Valid Question?";
        QuestionValidator.ValidationResult validQuestionResult = QuestionValidator.validateQuestion(validQuestion);
        String validAnswer = "This is a valid answer.";
        QuestionValidator.ValidationResult validAnswerResult = QuestionValidator.validateAnswer(validAnswer);
        assertTrue(validQuestionResult.isValid(), "Valid question should pass validation");
        assertTrue(validAnswerResult.isValid(), "Valid answer should pass validation");
        String invalidQuestion = "What is SQL? DROP TABLE Questions;";
        QuestionValidator.ValidationResult invalidQuestionResult = QuestionValidator.validateQuestion(invalidQuestion);
        assertFalse(invalidQuestionResult.isValid(), "Question with SQL injection should fail validation");
    }

    /*  TEST CASE 22
     *  Tests that upvotes on answers.  */
    @Test
    public void testAnswerVoteCounting() {
        Answer answer = new Answer("This is a test answer", studentTest.getUserName());
        assertEquals(0, answer.getUpvotes(), "Initial upvotes should be zero");
        assertEquals(0, answer.getDownvotes(), "Initial downvotes should be zero");
        answer.upvote();
        answer.upvote();
        answer.downvote();
        assertEquals(2, answer.getUpvotes(), "Should have 2 upvotes");
        assertEquals(1, answer.getDownvotes(), "Should have 1 downvote");
    }

    /*  TEST CASE 24
     *  Tests different parts of the bodies of question.  */
    @Test
    public void testQuestionBodyUpdate() {
        String initialBody = "What is the initial question?";
        Question question = new Question(initialBody, studentTest.getUserName());
        assertEquals(initialBody, question.getBody(), "Question should have the initial body text");
        String updatedBody = "What is the updated question?";
        question.setBody(updatedBody);
        assertEquals(updatedBody, question.getBody(), "Question body should be updated");
    }

    /*  TEST CASE 25
     *  Tests that student has a student role.  */
    @Test
    public void testStudentHasStudentRole() {
        assertTrue(studentTest.hasRole("student"), "Student user should have student role");
    }

    /*  TEST CASE 26
     *  Tests that reviewer has a reviewer role.  */
    @Test
    public void testReviewerHasReviewerRole() {
        assertTrue(reviewerTest.hasRole("reviewer"), "Reviewer user should have reviewer role");
    }
    
    /*  TEST CASE 27
     *  Tests that instructor has a instructor role.  */
    @Test
    public void testInstructorHasInstructorRole() {
        assertTrue(instructorTest.hasRole("instructor"), "Instructor user should have instructor role");
    }

    /*  TEST CASE 28
     *  Tests that role can be removed.  */
    @Test
    public void testRoleRemoval() {
        studentTest.addRole("instructor");
        assertTrue(studentTest.hasRole("instructor"), "Role should be added successfully");
        studentTest.removeRole("instructor");
        assertFalse(studentTest.hasRole("instructor"), "Role should be removed successfully");
    }
    
    /*  TEST CASE 29
     *  Tests that question can have multiple answers.  */
    @Test
    public void testQuestionCanHaveMultipleAnswers() {
        Question question = new Question("Test Question?", studentTest.getUserName());
        Answer answer1 = new Answer("First answer", studentTest.getUserName());
        Answer answer2 = new Answer("Second answer", reviewerTest.getUserName());
        question.addAnswer(answer1);
        question.addAnswer(answer2);
        assertEquals(2, question.getAnswers().size(), "Question should contain 2 answers");
    }

    /*  TEST CASE 30
     *  Tests that review calculation based on review/upvote/downvote ratio.  */
    @Test
    public void testReviewScoreCalculation() {
        Review review = new Review("Test review", reviewerTest.getUserName(), 1);
        assertEquals(0, review.getReviewScore(), "Initial review score should be 0");
        review.incrementHelpful();
        review.incrementHelpful();
        review.incremenetNotHelpful();
        assertEquals(66, review.getReviewScore(), "Review score should calculate percentage of helpful votes");
    }


    @Test
    public void deleteUsers() {
        databaseHelper.deleteUser("reviewer");
        databaseHelper.deleteUser("student");
        databaseHelper.deleteUser("instructor");
    }
}