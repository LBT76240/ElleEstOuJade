package firstAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;

/**
 This example shows a minimal agent that just prints "Hello World!"
 and then terminates.
 @author Giovanni Caire - TILAB
 */


public class HelloWorldAgent extends Agent {

    class Stats {
        int age;
        int score;
        boolean findGame;
    }

    Stats ownStats = new Stats();

    int maxAgent = 10;

    ArrayList<Stats> listStatsPlayer= new ArrayList<Stats>();

    int me;

    protected void setup() {

        me = Integer.valueOf(getLocalName());


        /*
        Random rn = new Random();
        int n = maximum - minimum + 1;
        int i = rn.nextInt() % n;
        randomNum =  minimum + i;*/
        int maxAge = 70;
        int minAge = 10;
        ownStats.age = ThreadLocalRandom.current().nextInt(minAge, maxAge + 1);
        int maxScore = 10000;
        int minScore = 0;
        ownStats.score = ThreadLocalRandom.current().nextInt(minScore, maxScore + 1);
        ownStats.findGame = false;
        System.out.println("Hello World! My name is "+getLocalName() + " Age : " + ownStats.age + " Score : " + ownStats.score);
        final int[] ageDifference = {10};
        int ageInc = 10;
        final int[] scoreDifference = {1000};
        int scoreInc = 1000;

        for(int i = 0;i<maxAgent;i++) {
            AID idI = new AID(String.valueOf(i),AID.ISLOCALNAME);
            ACLMessage msgAge = new ACLMessage(ACLMessage.INFORM);
            msgAge.addReceiver(idI);
            msgAge.setOntology("AGE");
            msgAge.setContent(String.valueOf(ownStats.age));
            send(msgAge);

            ACLMessage msgScore = new ACLMessage(ACLMessage.INFORM);
            msgScore.addReceiver(idI);
            msgScore.setOntology("SCORE");
            msgScore.setContent(String.valueOf(ownStats.score));
            send(msgScore);

            ACLMessage msgFindGame = new ACLMessage(ACLMessage.INFORM);
            msgFindGame.addReceiver(idI);
            msgFindGame.setOntology("findGame");
            msgFindGame.setContent(String.valueOf(ownStats.findGame));
            send(msgFindGame);

            listStatsPlayer.add(new Stats());
        }



        final int[] j = {1};
        //doDelete();

        addBehaviour(new TickerBehaviour(this,1000) {
            @Override
            protected void onTick() {
                if(!ownStats.findGame) {
                    System.out.print(".");
                    ACLMessage msg = receive();
                    if (msg != null) {
                        while (msg != null) {
                            if(!ownStats.findGame) {

                                int indice = -1;

                                String[] sender = msg.getSender().getName().split("@");
                                indice = Integer.parseInt(sender[0]);
                                if (msg.getOntology().equals("AGE")) {
                                    listStatsPlayer.get(indice).age = Integer.parseInt(msg.getContent());
                                } else if (msg.getOntology().equals("SCORE")) {
                                    listStatsPlayer.get(indice).score = Integer.parseInt(msg.getContent());
                                } else if (msg.getOntology().equals("findGame")) {
                                    if (msg.getContent().equals("true")) {
                                        listStatsPlayer.get(indice).findGame = true;
                                    } else {
                                        listStatsPlayer.get(indice).findGame = false;
                                    }
                                } else if (msg.getOntology().equals("REQUEST")) {
                                    if (msg.getContent().equals("PLAY?")) {
                                        if (Math.abs(listStatsPlayer.get(indice).age - ownStats.age) < ageDifference[0] && Math.abs(listStatsPlayer.get(indice).score - ownStats.score) < scoreDifference[0]) {
                                            AID idI = new AID(String.valueOf(indice), AID.ISLOCALNAME);
                                            ACLMessage msgScore = new ACLMessage(ACLMessage.INFORM);
                                            msgScore.addReceiver(idI);
                                            msgScore.setOntology("REQUEST");
                                            msgScore.setContent("OK");
                                            send(msgScore);
                                        }
                                    } else {
                                        if (msg.getContent().equals("OK")) {
                                            AID idI = new AID(String.valueOf(indice), AID.ISLOCALNAME);
                                            ACLMessage msgScore = new ACLMessage(ACLMessage.INFORM);
                                            msgScore.addReceiver(idI);
                                            msgScore.setOntology("REQUEST");
                                            msgScore.setContent("OK");
                                            send(msgScore);

                                            for (int i = 0; i < maxAgent; i++) {
                                                if (i != me && !listStatsPlayer.get(i).findGame) {


                                                    ACLMessage msgFindGame = new ACLMessage(ACLMessage.INFORM);
                                                    msgFindGame.addReceiver(idI);
                                                    msgFindGame.setOntology("findGame");
                                                    msgFindGame.setContent(String.valueOf(true));
                                                    send(msgFindGame);

                                                    listStatsPlayer.add(new Stats());
                                                }
                                            }
                                            System.out.println("\n" + me + "+" + indice);
                                            ownStats.findGame = true;
                                        }
                                    }

                                }

                            }


                            msg = receive();
                        }


                    } else {

                        if (j[0] % 10 == 0) {
                            ageDifference[0] = ageDifference[0] + ageInc;
                            scoreDifference[0] = scoreDifference[0] + scoreInc;
                        }


                        for (int i = 0; i < maxAgent; i++) {
                            if (i != me && !listStatsPlayer.get(i).findGame) {

                                if (Math.abs(listStatsPlayer.get(i).age - ownStats.age) < ageDifference[0] && Math.abs(listStatsPlayer.get(i).score - ownStats.score) < scoreDifference[0]) {

                                    AID idI = new AID(String.valueOf(i), AID.ISLOCALNAME);
                                    ACLMessage msgScore = new ACLMessage(ACLMessage.INFORM);
                                    msgScore.addReceiver(idI);
                                    msgScore.setOntology("REQUEST");
                                    msgScore.setContent("PLAY?");
                                    send(msgScore);
                                }

                            }
                        }

                        j[0] = j[0] + 1;
                    }
                } else {
                    ACLMessage msg = receive();
                    if (msg != null) {
                        msg = receive();
                    }
                }

            }
        });
    }

}

// On ne va pas s'intéresser à comprendre chaque ligne du code mais juste comment compiler et lancer l'agent.