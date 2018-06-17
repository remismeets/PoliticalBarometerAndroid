package be.kdg.androidbarometer.model;

public class UserInformation {
    private String FirstName;
    private String LastName;
    private String ProfilePicture;

    public UserInformation(String firstName, String lastName, String profilePicture) {
        FirstName = firstName;
        LastName = lastName;
        ProfilePicture = profilePicture;
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    public String getProfilePicture() {
        return ProfilePicture;
    }
}
