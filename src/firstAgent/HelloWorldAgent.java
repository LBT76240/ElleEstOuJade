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
    }

    Stats ownStats = new Stats();

    int maxAgent = 10;

    ArrayList<Stats> listStatsPlayer= new ArrayList<Stats>();

    int me;

    protected void setup() {
        System.out.println("Hello World! My name is "+getLocalName());
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
            listStatsPlayer.add(new Stats());
        }


        //doDelete();

        addBehaviour(new TickerBehaviour(this,1000) {
            @Override
            protected void onTick() {

                ACLMessage msg = receive();
                if(msg != null) {
                    while(msg != null) {
                        if(msg.getOntology().equals("AGE")) {
                            int i = Integer.parseInt(msg.getSender().getLocalName());
                            listStatsPlayer.get(i).age = Integer.parseInt(msg.getContent());
                        }

                        if(msg.getOntology().equals("SCORE")) {
                            int i = Integer.parseInt(msg.getSender().getLocalName());
                            listStatsPlayer.get(i).score = Integer.parseInt(msg.getContent());
                        }
                        msg = receive();
                    }


                } else {
                    System.out.println("The stats that i know is : ");
                    for(int i = 0;i<maxAgent;i++) {
                        if(me == i) {
                            System.out.println("me <3<3 : ");
                        } else {
                            System.out.println("Agents "+i+" : ");
                        }

                        System.out.println("Age : " + listStatsPlayer.get(i).age);
                        System.out.println("Score : " +listStatsPlayer.get(i).score);
                    }
                }

            }
        });
    }

}

// On ne va pas s'intéresser à comprendre chaque ligne du code mais juste comment compiler et lancer l'agent.