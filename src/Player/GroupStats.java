package Player;

import java.io.Serializable;


public class GroupStats implements Serializable {
    //////////////////////////////////////////////////////////////////////////
    //                          Player variables                            //
    //////////////////////////////////////////////////////////////////////////
    private String groupName;

    private int nbPlayersInGroup;
    public final int nbPlayersInGroupMax = 5;

    private int averageNumberOfGamePlayedInGroup;
    private int averageScoreInGroup;

    private int toleratedGapInNumberOfGamePlayed;
    private int toleratedGapInAverageScore;


    //////////////////////////////////////////////////////////////////////////
    //                             Class methods                            //
    //////////////////////////////////////////////////////////////////////////
    GroupStats(String newGroupName) {
        groupName = newGroupName;

        nbPlayersInGroup = 0;

        averageNumberOfGamePlayedInGroup = 0;
        averageScoreInGroup = 0;

        toleratedGapInNumberOfGamePlayed = 10;
        toleratedGapInAverageScore = 5000;
    }

    @Override
    public String toString() {
        String stringToReturn;

        stringToReturn = "Hello world! Group " + groupName + " here, I currently have " + nbPlayersInGroup + " out of " + nbPlayersInGroupMax + " players\n";
        stringToReturn += "The average number of game played is " + averageNumberOfGamePlayedInGroup + ", and an average score of " + averageScoreInGroup + "\n";
        stringToReturn += "Criteria for player search : gap in number of game played " + toleratedGapInNumberOfGamePlayed + ", gap in average score " + toleratedGapInAverageScore;

        return stringToReturn;
    }


    //////////////////////////////////////////////////////////////////////////
    //                          Getters and setters                         //
    //////////////////////////////////////////////////////////////////////////
    public String getGroupName() {
        return groupName;
    }


    public void setNbPlayersInGroup(int newNbPlayerInGroup) {
        nbPlayersInGroup = newNbPlayerInGroup;
    }

    public int getNbPlayersInGroup() {
        return nbPlayersInGroup;
    }


    public void setAverageNumberOfGamePlayedInGroup(int newAverageNumberOfGamePlayedInGroup) {
        averageNumberOfGamePlayedInGroup = newAverageNumberOfGamePlayedInGroup;
    }

    public int getAverageNumberOfGamePlayedInGroup() {
        return averageNumberOfGamePlayedInGroup;
    }


    public void setAverageScoreInGroup(int newAverageScoreInGroup) {
        averageScoreInGroup = newAverageScoreInGroup;
    }

    public int getAverageScoreInGroup() {
        return averageScoreInGroup;
    }


    public void setToleratedGapInNumberOfGamePlayed(int newToleratedGapInNumberOfGamePlayed) {
        toleratedGapInNumberOfGamePlayed = newToleratedGapInNumberOfGamePlayed;
    }

    public int getToleratedGapInNumberOfGamePlayed() {
        return toleratedGapInNumberOfGamePlayed;
    }


    public void setToleratedGapInAverageScore(int newToleratedGapInAverageScore) {
        toleratedGapInAverageScore = newToleratedGapInAverageScore;
    }

    public int getToleratedGapInAverageScore() {
        return toleratedGapInAverageScore;
    }
}
