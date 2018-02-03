package Player;

import jade.core.AID;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;


public class PlayerStats implements Serializable {

    //////////////////////////////////////////////////////////////////////////
    //                          Player variables                            //
    //////////////////////////////////////////////////////////////////////////
    private String playerName;
    private AID playerAID;

    private int numberOfGamePlayed;
    private int averageScore;

    private boolean isGroupFound;


    //////////////////////////////////////////////////////////////////////////
    //                             Class methods                            //
    //////////////////////////////////////////////////////////////////////////
    PlayerStats(String newPlayerID, AID newPlayerAID) {
        playerName = newPlayerID;
        playerAID = newPlayerAID;

        numberOfGamePlayed = ThreadLocalRandom.current().nextInt(0, 100);
        averageScore = ThreadLocalRandom.current().nextInt(1000, 100000);

        isGroupFound = false;
    }

    @Override
    public String toString() {
        String stringToReturn;

        stringToReturn = "Hello world! My name is " + playerName + ", with the AID " + playerAID + "\n";
        stringToReturn += "I have played " + numberOfGamePlayed + " games already, and have an average score of " + averageScore + "\n";

        return stringToReturn;
    }


    //////////////////////////////////////////////////////////////////////////
    //                          Getters and setters                         //
    //////////////////////////////////////////////////////////////////////////
    public String getPlayerName() {
        return this.playerName;
    }


    public AID getPlayerAID() {
        return this.playerAID;
    }


    public int getNumberOfGamePlayed() {
        return this.numberOfGamePlayed;
    }


    public int getAverageScore() {
        return this.averageScore;
    }


    public void setIsGroupFound(boolean isGroupFound) {
        this.isGroupFound = isGroupFound;
    }

    public boolean getIsGroupFound() {
        return this.isGroupFound;
    }
}
