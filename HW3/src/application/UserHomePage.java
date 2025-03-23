package application;

// question & answer loading / storing
import databasePart1.DatabaseHelper;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ListCell;
import javafx.util.Callback;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This page displays a simple welcome message for the user.
 */

public class UserHomePage {
	
	private ObservableList<Question> questions;
	private DatabaseHelper databaseHelper;
	private Map<Question, Integer> questionIDs;
	// TODO: Change currentUser to fetch student's name when Student class is implemented
	// Changed to User object to access names
	private User currentUser;

	public void setCurrentUser(User user) {
		this.currentUser = user;
	}
	
	public UserHomePage() {
		databaseHelper = new DatabaseHelper();
		questionIDs = new HashMap<>();
		try {
			databaseHelper.connectToDatabase();
		} catch (SQLException e) {
			showErrorMessage("connectToDatabase error", "Failed to connect to database");
		}
	}
	
	// Stage and User parameter
    public void show(Stage primaryStage, User user) {
    	setCurrentUser(user);
    	// LOAD QUESTIONS
    	questions = loadQuestionsFromDatabase();
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label userLabel = new Label("Hello, " + user.getRole() + "!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    ListView<Question> questionListView = new ListView<>(questions);
	    questionListView.setPrefSize(300, 200);
	    questionListView.setCellFactory(new Callback<ListView<Question>, ListCell<Question>>() {
	        @Override
	        public ListCell<Question> call(ListView<Question> param) {
	            return new ListCell<Question>() {
	                @Override
	                protected void updateItem(Question item, boolean empty) {
	                    super.updateItem(item, empty);
	                    if (empty || item == null) {
	                        setGraphic(null);
	                    } else {
	                        // Question Box
	                        Label questionLabel = new Label(item.getBody() + " - Asked by " + item.getAskedBy());
	                        HBox questionBox = new HBox(questionLabel);
	                        questionBox.setSpacing(10);

	                        // create the button for upvote, give it a label, then set the counter to zero
	                        Button upvoteButton = new Button("upvote");
	                        Label upvoteCounter = new Label(String.valueOf(0)); // give a starting value of zero
	                        HBox upvoteBox = new HBox(upvoteButton, upvoteCounter);
	                        upvoteBox.setSpacing(5);

	                        // create the button for downvote, give it a label, then set the counter to zero
	                        Button downvoteButton = new Button("downvote");
	                        Label downvoteCounter = new Label(String.valueOf(0)); // give a starting value of zero
	                        HBox downvoteBox = new HBox(downvoteButton, downvoteCounter);
	                        downvoteBox.setSpacing(5);

	                        // when upbutton is pushed increment
	                        upvoteButton.setOnAction(e -> {
	                            int count = Integer.parseInt(upvoteCounter.getText()) + 1;
	                            upvoteCounter.setText(String.valueOf(count));
	                        });

	                        // when downvote is pushed increment
	                        downvoteButton.setOnAction(e -> {
	                            int count = Integer.parseInt(downvoteCounter.getText()) + 1;
	                            downvoteCounter.setText(String.valueOf(count));
	                        });

	                        
	                        HBox mainBox = new HBox(questionBox, upvoteBox, downvoteBox);
	                        mainBox.setSpacing(20);

	                        // Set the graphic to the HBox
	                        setGraphic(mainBox);
	                    }
	                }
	            };
	        }
	    });
	    
	    TextField searchField = new TextField();
	    searchField.setPromptText("Enter search term");
	    searchField.setPrefWidth(200);

	    Button searchButton = new Button("Search");
	    searchButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
	    searchButton.setOnAction(e -> {
	        String searchTerm = searchField.getText().trim();
	        System.out.println("it worked: " + searchTerm);
	        
	        // Find matching questions
	        ObservableList<Question> searchResults = searchQuestions(searchTerm);
	        
	        // Update the ListView with search results
	        questionListView.setItems(searchResults);
	        
	        // If no results found, show a message
	        if (searchResults.isEmpty()) {
	            showErrorMessage("Search Results", "No questions or answers found containing: " + searchTerm);
	        }
	    });
	    
	    Button clearSearchButton = new Button("Clear Search");
	    clearSearchButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
	    clearSearchButton.setOnAction(e -> {
	        // Clear the search field
	        searchField.clear();
	        
	        // Reset the list view to show all questions
	        questionListView.setItems(questions);
	    });
	    
	    HBox searchBox = new HBox(10); // 10 is the spacing between elements
	    searchBox.setStyle("-fx-alignment: center;");
	    searchBox.getChildren().addAll(searchField, searchButton, clearSearchButton);
	    
	    // Button: Ask question
	    Button addButton = new Button("Ask a Question");
	    addButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
	    // Check if user has student role
	    if (!currentUser.canAskQuestions())
	    	addButton.setDisable(true);
	    addButton.setOnAction(e -> promptForNewQuestion(questionListView));	    
	    // Button: Delete highlighted question
	    Button deleteButton = new Button("Delete Question");
	    deleteButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
	    deleteButton.setOnAction(e -> {
	    	// Get index of question and check if current user owns the question
	        int selectedIndex = questionListView.getSelectionModel().getSelectedIndex();
	        if (selectedIndex != -1) {
	            Question selectedQuestion = questions.get(selectedIndex);
	            if (selectedQuestion.getAskedBy().equals(currentUser.getUserName())) {
	            	// Delete question from database
	            	boolean was_deleted = databaseHelper.deleteQuestion(selectedQuestion);
	            	if (was_deleted) {
	            		questions.remove(selectedIndex);
	            		questionIDs.remove(selectedQuestion);
	            	}
	            	else {
	            		showErrorMessage("Error", "Failed to delete question from database");
	            	}
	            }
	            else {
	            	showErrorMessage("Error", "You can only delete questions that you have asked.");
	            }
	        }
	    });
	    
	    // Button: Answer question
	    Button answerButton = new Button("Answer Question");
        answerButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        // Check if user has student role
        if (!currentUser.canAnswerQuestions())
        	answerButton.setDisable(true);
        answerButton.setOnAction(e -> {
            int selectedIndex = questionListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex != -1) {
                promptForAnswer(questionListView, selectedIndex);
            }
        });
        
        // Button: Update Question
        Button updateButton = new Button("Update Question");
        updateButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        updateButton.setOnAction(e -> {
            int selectedIndex = questionListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex != -1) {
                Question selectedQuestion = questions.get(selectedIndex);
                promptForUpdatedQuestion(questionListView, selectedQuestion, selectedIndex);
            }
        });
        
        // Button: Refresh DB for displayed questions
        Button refreshButton = new Button("Refresh Questions");
        answerButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
        refreshButton.setOnAction(e -> {
        	questions.clear();
        	questions.addAll(loadQuestionsFromDatabase());
        	questionListView.refresh();
        });
        
        // Button: Automated Test Cases
        Button testCasesButton = new Button("Run Automated Test Cases");
	    testCasesButton.setStyle("-fx-font-size: 14px; -fx-padding: 5;");
	    testCasesButton.setOnAction(e-> runTestCases(primaryStage));
	    
	    layout.getChildren().addAll(userLabel, questionListView, searchBox, addButton, deleteButton, answerButton, updateButton, refreshButton, testCasesButton);
	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("User Page");
	    primaryStage.setOnCloseRequest(e -> {
	    	databaseHelper.closeConnection();
	    });
    }
    
    
    private ObservableList<Question> loadQuestionsFromDatabase(){
    	ObservableList<Question> loadedQuestions = FXCollections.observableArrayList();
    	Map<Question, Integer> loadedQuestionIDs = databaseHelper.loadAllQuestionsWithIDs();
    	if (loadedQuestionIDs.isEmpty()) {
            Question q1 = new Question("What time is class?", "User A");
            Question q2 = new Question("When is the homework assignment due?", "User B");
            Question q3 = new Question("What computer labs are available to CS students?", "User C");
            //save to database and track IDs
            int id1 = databaseHelper.saveQuestion(q1);
            int id2 = databaseHelper.saveQuestion(q2);
            int id3 = databaseHelper.saveQuestion(q3);
            loadedQuestions.addAll(q1, q2, q3);
            //store IDs for later reference
            questionIDs.put(q1, id1);
            questionIDs.put(q2, id2);
            questionIDs.put(q3, id3);
        }
    	else {
    		for (Map.Entry<Question, Integer> entry : loadedQuestionIDs.entrySet()) {
    			loadedQuestions.add(entry.getKey());
    			questionIDs.put(entry.getKey(), entry.getValue());
    		}
    	}
        return loadedQuestions;
    }
    
    // popup window prompting user to enter question text
    private void promptForNewQuestion(ListView<Question> questionListView) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Question");
        dialog.setHeaderText("Add a new question");
        dialog.setContentText("Please enter your question:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(questionText -> {
        	// validate question
        	QuestionValidator.ValidationResult validationResult = QuestionValidator.validateQuestion(questionText);
        	// if question is valid
            if (!validationResult.isValid()) {
                showErrorMessage("Invalid Question", validationResult.getMessage());
            }
            else {
	        	// Create question object & store to DB
	        	Question newQuestion = new Question(questionText, currentUser.getUserName());	// obtain username
	        	int questionID = databaseHelper.saveQuestion(newQuestion);
	        	if (questionID != -1) {
	        		questions.add(newQuestion);
	        		questionIDs.put(newQuestion, questionID);
	        	}
	        	else {
	        		showErrorMessage("Database Error", "Something went wrong trying to store this question to the database");
	        	}
            }
        });
    }
    
    
    private void promptForAnswer(ListView<Question> questionListView, int selectedIndex) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Answer Question");
        dialog.setHeaderText("Provide an answer");
        dialog.setContentText("Enter your answer:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(answerText -> {
        	// Validate answer
        	QuestionValidator.ValidationResult validationResult = QuestionValidator.validateAnswer(answerText);
        	// Valid answer
            if (!validationResult.isValid()) {
                showErrorMessage("Invalid Answer", validationResult.getMessage());
            }
            // get question ID to update question in DB
            else {
                Question question = questionListView.getItems().get(selectedIndex);
                Answer answer = new Answer(answerText, currentUser.getUserName());
                Integer questionID = questionIDs.get(question);
                System.out.println(questionID);
                // question ID not cached
                if (questionID == null) {
                	questionID = -1;
                }
                if (questionID != -1) {
                	boolean added_answer = databaseHelper.saveAnswer(questionID, answer);
                	// database fetch success
                	if (added_answer) {
                		question.addAnswer(answer);
                		questionListView.refresh();
                	}
                	else {
                		showErrorMessage("Database Error", "Something went wrong saving the answer to the database.");
                	}
                }
                else {
            		showErrorMessage("Database Error #3", "Error finding question in database");
            	}
            }
        });
    }
    
    private void promptForUpdatedQuestion(ListView<Question> questionListView, Question question, int index) {
        TextInputDialog dialog = new TextInputDialog(question.getBody());
        dialog.setTitle("Update Question");
        dialog.setHeaderText("Update your question");
        dialog.setContentText("Edit your question:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(updatedText -> {
            if (updatedText.trim().isEmpty()) {
                showErrorMessage("Error", "The updated question cannot be empty.");
            } else {
                Integer questionID = questionIDs.get(question);
                if (questionID != null) {
                    boolean updated = databaseHelper.updateQuestion(questionID, updatedText);
                    if (updated) {
                        question.setBody(updatedText);
                        questionListView.refresh();
                    } else {
                        showErrorMessage("Database Error", "Failed to update the question in the database.");
                    }
                } else {
                    showErrorMessage("Database Error", "Question ID not found.");
                }
            }
        });
    }

    private void runTestCases(Stage primaryStage) {
    	System.out.println("\n RUNNING QUESTION & ANSWER TEST CASES\n");	
    	AutomatedTestCaseQuestionAnswer testRunner = new AutomatedTestCaseQuestionAnswer();
    	
    	// Test case 1: valid question input
    	testRunner.addTest(() -> {
    		String validQuestion = "When is the assignment due?";
    		QuestionValidator.ValidationResult result = QuestionValidator.validateQuestion(validQuestion);
    		if (result.isValid()) {
    			Question testQuestion = new Question(validQuestion, currentUser.getUserName());
    			int questionID = databaseHelper.saveQuestion(testQuestion);
    			if (questionID > 0) {
    				databaseHelper.deleteQuestion(testQuestion);
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case #1, Valid Question: ", true, "Valid question; was accepted & saved to DB.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case #1, Valid Question: 1", false, "Valid question; was accepted but failed to save to database.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case #1, Valid Question: ", false, "Question rejected: " + result.getMessage());
    		}
    	});
    	
    	// Test Case 2: Question too short
    	testRunner.addTest(() -> {
    		String validQuestion = "..";
    		QuestionValidator.ValidationResult result = QuestionValidator.validateQuestion(validQuestion);
    		if (result.isValid()) {
    			Question testQuestion = new Question(validQuestion, currentUser.getUserName());
    			int questionID = databaseHelper.saveQuestion(testQuestion);
    			if (questionID > 0) {
    				databaseHelper.deleteQuestion(testQuestion);
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 2: Question too short", true, "Valid question; was accepted & saved to DB.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 2: Question too short", false, "Valid question; was accepted but failed to save to database.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 2: Question too short", false, "Question rejected: " + result.getMessage());
    		}
    	});
    	
    	// Test Case 3: Question too long
    	testRunner.addTest(() -> {
    		String validQuestion = "......................................................................"
    				+ "..................................................................................."
    				+ "..................................................................................."
    				+ "..................................................................................."
    				+ "..................................................................................."
    				+ "..................................................................................."
    				+ "..................................................................................."
    				+ "..................................................................................."
    				+ "...................................................................................";
    		QuestionValidator.ValidationResult result = QuestionValidator.validateQuestion(validQuestion);
    		if (result.isValid()) {
    			Question testQuestion = new Question(validQuestion, currentUser.getUserName());
    			int questionID = databaseHelper.saveQuestion(testQuestion);
    			if (questionID > 0) {
    				databaseHelper.deleteQuestion(testQuestion);
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 2: Question too long", true, "Valid question; was accepted & saved to DB.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 2: Question too long", false, "Valid question; was accepted but failed to save to database.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 2: Question too long", false, "Question rejected: " + result.getMessage());
    		}
    	});
    	
    	// Test Case 4: SQL Injection
    	testRunner.addTest(() -> {
    		String validQuestion = "DROP TABLE Questions xp__ -- __";
    		QuestionValidator.ValidationResult result = QuestionValidator.validateQuestion(validQuestion);
    		if (result.isValid()) {
    			Question testQuestion = new Question(validQuestion, currentUser.getUserName());
    			int questionID = databaseHelper.saveQuestion(testQuestion);
    			if (questionID > 0) {
    				databaseHelper.deleteQuestion(testQuestion);
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 4: SQL Injection", true, "Valid question; was accepted & saved to DB.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 4: SQL Injection", false, "Valid question; was accepted but failed to save to database.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 4: SQL Injection", false, "Question rejected: " + result.getMessage());
    		}
    	});
    	
    	// Test Case 5: Valid answer
    	testRunner.addTest(() -> {
    		String validQuestion = "Valid question to ask";
    		Question question = new Question(validQuestion, currentUser.getUserName());
    		int questionID = databaseHelper.saveQuestion(question);
    		
    		String input_answer = "This is a valid answer";
    		QuestionValidator.ValidationResult validationResult = QuestionValidator.validateAnswer(input_answer);
    		if (validationResult.isValid()) {
    			Answer answer = new Answer(input_answer, currentUser.getUserName());
    			boolean answerSaved = databaseHelper.saveAnswer(questionID, answer);
    			databaseHelper.deleteQuestion(question);
    			
    			if (answerSaved) {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case #5: Valid answer", true, "Answer was accepted and saved.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case #5: Valid answer", true, "Answer was accepted but not saved.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case #5: Valid answer", false, "Answer was not accepted: " + validationResult.getMessage());
    		}
    	});
    	
    	// Test Case 6: empty answer
    	testRunner.addTest(() -> {
    		String validQuestion = "Valid question";
    		Question question = new Question(validQuestion, currentUser.getUserName());
    		int questionID = databaseHelper.saveQuestion(question);
    		
    		String input_answer = "";
    		QuestionValidator.ValidationResult validationResult = QuestionValidator.validateAnswer(input_answer);
    		if (validationResult.isValid()) {
    			Answer answer = new Answer(input_answer, currentUser.getUserName());
    			boolean answerSaved = databaseHelper.saveAnswer(questionID, answer);
    			databaseHelper.deleteQuestion(question);
    			
    			if (answerSaved) {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 6: empty answer", true, "Answer was accepted and saved.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 6: empty answer", true, "Answer was accepted but not saved.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 6: empty answer", false, "Answer was not accepted: " + validationResult.getMessage());
    		}
    	});
    	
    	// Test Case 7: too long of answer
    	testRunner.addTest(() -> {
    		String validQuestion = "Hello";
    		Question question = new Question(validQuestion, currentUser.getUserName());
    		int questionID = databaseHelper.saveQuestion(question);
    		
    		String input_answer = "\"......................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\"\r\n"
    				+ "    				+ \"...................................................................................\";";
    		QuestionValidator.ValidationResult validationResult = QuestionValidator.validateAnswer(input_answer);
    		if (validationResult.isValid()) {
    			Answer answer = new Answer(input_answer, currentUser.getUserName());
    			boolean answerSaved = databaseHelper.saveAnswer(questionID, answer);
    			databaseHelper.deleteQuestion(question);
    			
    			if (answerSaved) {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 7: too long of answer", true, "Answer was accepted and saved.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 7: too long of answer", true, "Answer was accepted but not saved.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 7: too long of answer", false, "Answer was not accepted: " + validationResult.getMessage());
    		}
    	});
    	
    	// Test Case 8: SQL injection answer
    	testRunner.addTest(() -> {
    		String validQuestion = "Valid question";
    		Question question = new Question(validQuestion, currentUser.getUserName());
    		int questionID = databaseHelper.saveQuestion(question);
    		
    		String input_answer = "TDROP TABLE -- __ update ";
    		QuestionValidator.ValidationResult validationResult = QuestionValidator.validateAnswer(input_answer);
    		if (validationResult.isValid()) {
    			Answer answer = new Answer(input_answer, currentUser.getUserName());
    			boolean answerSaved = databaseHelper.saveAnswer(questionID, answer);
    			databaseHelper.deleteQuestion(question);
    			
    			if (answerSaved) {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 8: SQL injection answer", true, "Answer was accepted and saved.");
    			}
    			else {
    				return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 8: SQL injection answer", true, "Answer was accepted but not saved.");
    			}
    		}
    		else {
    			return AutomatedTestCaseQuestionAnswer.assertTrue("Test Case 8: SQL injection answer", false, "Answer was not accepted: " + validationResult.getMessage());
    		}
    	});
    	
    	List<AutomatedTestCaseQuestionAnswer.TestResult> results = testRunner.runAllTests();
    	System.out.println("RUNNING " + results.size() + " TESTS");
    	for (AutomatedTestCaseQuestionAnswer.TestResult result : results) {
    		System.out.println(result.getTestName() + ": " + (result.isPassed() ? "PASSED" : "FAILED"));
    		System.out.println((result.isPassed() ? "PASSED: " : "FAIL: ") + result.getMessage());
    		System.out.println("---");
    	}
    	System.out.println("Automatic testing completed");
    }
     
    private void showErrorMessage(String title, String content) {
    	Alert alert = new Alert(AlertType.ERROR);
    	alert.setTitle(title);
    	alert.setHeaderText(null);
    	alert.setContentText(content);
    	alert.showAndWait();
    }
    
    private ObservableList<Question> searchQuestions(String searchTerm) {
        // If search term is empty, return all questions
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return loadQuestionsFromDatabase(); // Return all questions
        }
        
        // Create a new list for search results
        ObservableList<Question> searchResults = FXCollections.observableArrayList();
        
        // Iterate through all questions
        for (Question question : questions) {
            boolean matchFound = false;
            
            // Check if search term is in question body
            if (question.getBody().toLowerCase().contains(searchTerm.toLowerCase())) {
                matchFound = true;
            } else {
                // Check if search term is in any of the answers
                for (Answer answer : question.getAnswers()) {
                    if (answer.getText().toLowerCase().contains(searchTerm.toLowerCase())) {
                        matchFound = true;
                        break; // No need to check other answers
                    }
                }
            }
            
            // Add question to results if it matched
            if (matchFound) {
                searchResults.add(question);
            }
        }
        
        return searchResults;
    }
}