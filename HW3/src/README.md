Question & Answer System:
This is a JavaFX-based application that allows users to post, update, and answer questions. The system includes database integration for persistence.

Features:
- Users can add new questions.
- Reviewers can update existing questions.
- Users can delete questions they posted.
- Users with the correct permissions can answer questions.
- The question list can be refreshed to show the latest updates from the database.

Installation:
- Make sure JavaFX and JDBC dependencies are set up.

Usage:
- Log in as a user (students can ask questions, reviewers can update them).
- Use the ListView to select questions to update, delete, or answer.
- Use the provided buttons to perform CRUD operations.
- Changes are saved to the database automatically.

Database Schema:
- Questions Table (id, body, askedBy)
- Answers Table (id, questionID, text, answeredBy)

Technologies Used:
- Java 11+
- JavaFX
- SQLite / MySQL


