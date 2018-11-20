package mullan.sean.coinz;

public class LeaderBoardUser {

    private String mUsername;
    private double mGold;

    public LeaderBoardUser(String username, double gold) {
        this.mUsername = username;
        this.mGold     = gold;
    }

    public String getUsername() {
        return mUsername;
    }

    public double getGold() {
        return mGold;
    }
}
