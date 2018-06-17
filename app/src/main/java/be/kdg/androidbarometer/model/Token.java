package be.kdg.androidbarometer.model;

public class Token {
    private String access_token;
    private String token_type;
    private boolean signedIn;

    public String getAccess_token() {
        return access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public boolean isSignedIn() {
        return signedIn;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public void setSignedIn(boolean signedIn) {
        this.signedIn = signedIn;
    }
}
