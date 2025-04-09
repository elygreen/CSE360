package application;

import java.util.Set;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, and roles.
 * <p>
 * Users can have multiple roles, which determine their permissions within the system.
 * Available roles include "student", "reviewer", and "instructor".
 * </p>
 */
public class User {
    /** The unique username identifier for this user */
    private String userName;
    
    /** The password used for authentication */
    private String password;
    
    /** The set of roles assigned to this user */
    private Set<String> roles;

    /**
     * Constructs a new User with the specified userName, password, and roles.
     *
     * @param userName the unique identifier for this user
     * @param password the password for authentication
     * @param roles the set of roles assigned to this user
     */
    public User(String userName, String password, Set<String> roles) {
        this.userName = userName;
        this.password = password;
        this.roles = roles;
    }

    /**
     * Adds a role to this user's set of roles.
     *
     * @param role the role to add
     */
    public void addRole(String role) {
        roles.add(role);
    }

    /**
     * Removes a role from this user's set of roles.
     *
     * @param role the role to remove
     */
    public void removeRole(String role) {
        roles.remove(role);
    }

    /**
     * Checks if the user has a specific role.
     *
     * @param role the role to check
     * @return true if the user has the specified role, false otherwise
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * Gets the user's userName.
     *
     * @return the userName
     */
    public String getUserName() { 
        return userName; 
    }
    
    /**
     * Gets the user's password.
     *
     * @return the password
     */
    public String getPassword() { 
        return password; 
    }
    
    /**
     * Gets the set of roles assigned to this user.
     *
     * @return the set of roles
     */
    public Set<String> getRole() { 
        return roles; 
    }

    /**
     * Checks if the user has permission to ask questions.
     * This permission is granted to users with the "student" role.
     *
     * @return true if the user can ask questions, false otherwise
     */
    public boolean canAskQuestions() { 
        return roles.contains("student"); 
    }
    
    /**
     * Checks if the user has permission to answer questions.
     * This permission is granted to users with the "student" role.
     *
     * @return true if the user can answer questions, false otherwise
     */
    public boolean canAnswerQuestions() { 
        return roles.contains("student"); 
    }

    /**
     * Checks if the user has permission to update reviews.
     * This permission is granted to users with the "reviewer" role.
     *
     * @return true if the user can update reviews, false otherwise
     */
    public boolean canUpdateReview() { 
        return roles.contains("reviewer"); 
    }
    
    /**
     * Checks if the user has permission to create reviews.
     * This permission is granted to users with the "reviewer" role.
     *
     * @return true if the user can create reviews, false otherwise
     */
    public boolean canCreateReview() { 
        return roles.contains("reviewer"); 
    }
    
    /**
     * Checks if the user has permission to view reviews.
     * This permission is granted to users with the "reviewer" role.
     *
     * @return true if the user can view reviews, false otherwise
     */
    public boolean canViewReview() { 
        return roles.contains("reviewer"); 
    }
    
    /**
     * Checks if the user has permission to delete reviews.
     * This permission is granted to users with the "reviewer" role.
     *
     * @return true if the user can delete reviews, false otherwise
     */
    public boolean canDeleteReview() { 
        return roles.contains("reviewer"); 
    }

    /**
     * Checks if the user has permission to promote other users to reviewer status.
     * This permission is granted to users with the "instructor" role.
     *
     * @return true if the user can promote reviewers, false otherwise
     */
    public boolean canPromoteReviewer() { 
        return roles.contains("instructor"); 
    }
}