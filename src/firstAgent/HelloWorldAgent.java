package firstAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;




public class HelloWorldAgent extends Agent {

    // Classe pour stocker les différents paramètres du joueur
    class Stats {
        int age;
        int score;
        // findGame est à true quand l'agent a trouvé un groupe pour jouer. Ainsi il est retiré du matchmaking.
        boolean findGame;
    }
    // Les stats de cet Agent
    Stats ownStats = new Stats();

    // Le nombre total d'agents
    int maxAgent = 10;
    // La liste des stats des agents
    ArrayList<Stats> listStatsPlayer= new ArrayList<Stats>();

    // Son indice dans la liste
    int me;

    protected void setup() {

        me = Integer.valueOf(getLocalName());

        // Tirage de l'âge en Random entre 2 bornes
        int maxAge = 70;
        int minAge = 10;
        ownStats.age = ThreadLocalRandom.current().nextInt(minAge, maxAge + 1);
        // Tirage du score en Random entre 2 bornes
        int maxScore = 10000;
        int minScore = 0;
        ownStats.score = ThreadLocalRandom.current().nextInt(minScore, maxScore + 1);
        // Au démarrage, le joueur est seul
        ownStats.findGame = false;
        System.out.println("Hello World! My name is "+getLocalName() + " Age : " + ownStats.age + " Score : " + ownStats.score);

        // Variable des critères de différence. C'est un final int[] pour passer la variable dans la méthode lambda
        final int[] ageDifference = {10};
        // L'incrémentation quand il allège ses critères
        int ageInc = 10;
        final int[] scoreDifference = {1000};
        int scoreInc = 1000;

        // Pour commencer l'agent va dire à tous les autres ses stats.
        // Dans un vrai jeu, il demanderait au serveur du jeu les stats des joueurs dans la liste de matchmaking
        for(int i = 0;i<maxAgent;i++) {
            // On récupère l'AID de l'agent i
            AID idI = new AID(String.valueOf(i),AID.ISLOCALNAME);
            // On lui envoie l'âge de l'agent ...
            ACLMessage msgAge = new ACLMessage(ACLMessage.INFORM);
            msgAge.addReceiver(idI);
            msgAge.setOntology("AGE");
            msgAge.setContent(String.valueOf(ownStats.age));
            send(msgAge);

            // ... son score ...
            ACLMessage msgScore = new ACLMessage(ACLMessage.INFORM);
            msgScore.addReceiver(idI);
            msgScore.setOntology("SCORE");
            msgScore.setContent(String.valueOf(ownStats.score));
            send(msgScore);

            // ... sa valeur de findGame
            ACLMessage msgFindGame = new ACLMessage(ACLMessage.INFORM);
            msgFindGame.addReceiver(idI);
            msgFindGame.setOntology("findGame");
            msgFindGame.setContent(String.valueOf(ownStats.findGame));
            send(msgFindGame);

            listStatsPlayer.add(new Stats());
        }


        // Variable qui s'encrémente à chaque fois que l'agent réfléchit
        // Tous les 10 fois, les critères sont allégés, pour eviter les temps d'attente trop longs pour le joueur
        final int[] j = {1};


        addBehaviour(new TickerBehaviour(this,1000) {
            @Override
            protected void onTick() {
                // Si le joueur est toujours dans le matchmaking
                if(!ownStats.findGame) {
                    System.out.print(".");
                    // On récupère les messages destinés à l'agent
                    ACLMessage msg = receive();
                    // S'il y en a ...
                    if (msg != null) {
                        // ... et tant qu'il y en a
                        while (msg != null) {
                            if(!ownStats.findGame) {
                                // ON récupère l'indice de l'agent qui a envoyé le message
                                int indice = -1;
                                String[] sender = msg.getSender().getName().split("@");
                                indice = Integer.parseInt(sender[0]);


                                if (msg.getOntology().equals("AGE")) {
                                    // Si le message est un renseignement sur l'âge on le stocke
                                    listStatsPlayer.get(indice).age = Integer.parseInt(msg.getContent());
                                } else if (msg.getOntology().equals("SCORE")) {
                                    // Si le message est un renseignement sur le score on le stocke
                                    listStatsPlayer.get(indice).score = Integer.parseInt(msg.getContent());
                                } else if (msg.getOntology().equals("findGame")) {
                                    // Si le message est un renseignement sur le boolean findGame on le stocke
                                    if (msg.getContent().equals("true")) {
                                        listStatsPlayer.get(indice).findGame = true;
                                    } else {
                                        listStatsPlayer.get(indice).findGame = false;
                                    }
                                } else if (msg.getOntology().equals("REQUEST")) {
                                    // Si c'est une requête
                                    if (msg.getContent().equals("PLAY?")) {
                                        // Si c'est une première demande pour jouer ...
                                        if (Math.abs(listStatsPlayer.get(indice).age - ownStats.age) < ageDifference[0]
                                                && Math.abs(listStatsPlayer.get(indice).score - ownStats.score) < scoreDifference[0]) {
                                            // .. et que les critères du joueur sont bons
                                            AID idI = new AID(String.valueOf(indice), AID.ISLOCALNAME);
                                            ACLMessage msgScore = new ACLMessage(ACLMessage.INFORM);
                                            msgScore.addReceiver(idI);
                                            msgScore.setOntology("REQUEST");
                                            msgScore.setContent("OK");
                                            send(msgScore);
                                            // On répond une request OK
                                        }

                                        // Sinon on attend
                                    } else {
                                        if (msg.getContent().equals("OK")) {
                                            // Si l'agent nous a répondu OK à notre demande de PLAY? alors on lui répond OK à notre tour.
                                            AID idI = new AID(String.valueOf(indice), AID.ISLOCALNAME);
                                            ACLMessage msgScore = new ACLMessage(ACLMessage.INFORM);
                                            msgScore.addReceiver(idI);
                                            msgScore.setOntology("REQUEST");
                                            msgScore.setContent("OK");
                                            send(msgScore);

                                            // On informe tous les autres agents que l'agent n'est plus dans le matchmaking
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
                                            // On sort du matchmaking
                                            ownStats.findGame = true;
                                        }
                                    }

                                }

                            }

                            // On passe au message suivant
                            msg = receive();
                        }


                    } else {
                        // Toutes les 10 fois, les critères sont allégés, pour eviter les temps d'attente trop longs pour le joueur
                        if (j[0] % 10 == 0) {
                            ageDifference[0] = ageDifference[0] + ageInc;
                            scoreDifference[0] = scoreDifference[0] + scoreInc;
                        }

                        // On parcoure la liste des agents ...
                        for (int i = 0; i < maxAgent; i++) {
                            // ... si c'est un autre agent encore dans le matchmaking ...
                            if (i != me && !listStatsPlayer.get(i).findGame) {
                                // ... et que ses critères correspondent à nos attentes ...
                                if (Math.abs(listStatsPlayer.get(i).age - ownStats.age) < ageDifference[0]
                                        && Math.abs(listStatsPlayer.get(i).score - ownStats.score) < scoreDifference[0]) {
                                    // ... on lui envoie une requete de jeu
                                    AID idI = new AID(String.valueOf(i), AID.ISLOCALNAME);
                                    ACLMessage msgScore = new ACLMessage(ACLMessage.INFORM);
                                    msgScore.addReceiver(idI);
                                    msgScore.setOntology("REQUEST");
                                    msgScore.setContent("PLAY?");
                                    send(msgScore);
                                }

                            }
                        }

                        // Incrémentation de j
                        j[0] = j[0] + 1;
                    }
                } else {
                    // Dans le cas où l'agent n'est plus dans le matchmaking on vide juste les messages arrivant dans son buffer
                    ACLMessage msg = receive();
                    if (msg != null) {
                        msg = receive();
                    }
                }

            }
        });
    }

}

