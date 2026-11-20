package com.niranjankagri.patterns.model;

/**
 * A demo user and what should happen when it signs in.
 *
 * @param username the user name
 * @param password the password
 * @param state    "active" (signs in) or "locked" (gets the locked-out message)
 */
public record User(String username, String password, String state) {

    /** Whether this user can sign in. */
    public boolean canSignIn() {
        return "active".equals(state);
    }
}
