package Player;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;


public class GroupManagerAgent extends Agent {
    // Group informations
    private AID serverAID;

    // Average of the players stats
    GroupStats groupStats;

    // All the players gathered in this group
    private ArrayList<PlayerAgent> groupPlayerAgents;

    // Setting up the correct description for the service we look for (that is player available, looking for a group)
    private DFAgentDescription templatePlayerAvailable;

    // Setting up the list of the names of the players interested
    private AID[] playerAgentsFound;


    protected void setup() {
        serverAID = new AID(getLocalName(), AID.ISLOCALNAME);
        serverAID.setName(getLocalName());

        // Setting up the class for averaging players level
        groupStats = new GroupStats(getLocalName());

        // Displaying basic informations about the GroupManagerAgent created
        System.out.println(groupStats.toString());

        // Setting up the arraylist for the list of player in the group
        groupPlayerAgents = new ArrayList<>();

        // Adding the correct service to look for in the DFAgentDescription
        ServiceDescription sd = new ServiceDescription();
        sd.setType("player-looking-for-match");
        templatePlayerAvailable = new DFAgentDescription();
        templatePlayerAvailable.addServices(sd);

        System.out.println("Service lookup created for group " + groupStats.getGroupName() + "\n");

        //////////////////////////////////////////////////////////////////////////
        //                    Searching for player behaviour                    //
        //////////////////////////////////////////////////////////////////////////
        // Add a TickerBehaviour that schedules a request to all available PlayerAgents registered every 20 seconds
        addBehaviour(new TickerBehaviour(this, 20000) {
            protected void onTick() {
                System.out.println(groupStats.getGroupName() + " behaviour on : looking for players...\n");
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, templatePlayerAvailable);

                    playerAgentsFound = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        playerAgentsFound[i] = result[i].getName();
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }

                // Perform the request to match with a player
                myAgent.addBehaviour(new RequestPerformer());
            }
        });
    }


    //////////////////////////////////////////////////////////////////////////
    //               RequestPerformer handling player search                //
    //////////////////////////////////////////////////////////////////////////
    private class RequestPerformer extends Behaviour {
        private AID firstFittingPlayer;                     // The first player to provide a fitting offer
        PlayerStats playerStatsReceived;                    // The stats received from interested PlayerAgents
        private int repliesCnt = 0;                         // The counter of replies from PlayerAgents
        private MessageTemplate mt;                         // The template to receive replies

        private int step = 0;                               // The state we are currently in


        public void action() {
            switch (step) {
                case 0:
                    System.out.println(groupStats.getGroupName() + " sending message for players...\n");

                    // Send the cfp to all PlayerAgents
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

                    cfp.setConversationId("player-looking-for-match");
                    for (int i = 0; i < playerAgentsFound.length; i++) {
                        cfp.addReceiver(playerAgentsFound[i]);
                    }

                    myAgent.send(cfp);

                    // Prepare the template to get proposals
                    mt = MessageTemplate.MatchConversationId("player-looking-for-match");
                    step = 1;
                    break;

                case 1:
                    System.out.println(groupStats.getGroupName() + " handling replies from players...");

                    // Receive all proposals / refusals from PlayerAgents
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer
                            System.out.println(groupStats.getGroupName() + " offer received");
                            try {
                                playerStatsReceived = (PlayerStats)reply.getContentObject();
                                System.out.println(groupStats.getGroupName() + " received object " + firstFittingPlayer.toString() + "\n");
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                        }

                        // Handling case empty group
                        if(groupStats.getNbPlayersInGroup() == 0) {
                            System.out.println(groupStats.getGroupName() + " group empty, taking first player");
                            firstFittingPlayer = playerStatsReceived.getPlayerAID();
                            step = 2;
                        }
                        else {
                            // Checking if the player is in the criteria
                            System.out.println(groupStats.getGroupName() + " group not empty : " + groupStats.getNbPlayersInGroup() + ", checking the criteria");

                            if(Math.abs(groupStats.getAverageNumberOfGamePlayedInGroup() - playerStatsReceived.getNumberOfGamePlayed()) < groupStats.getToleratedGapInNumberOfGamePlayed()) {
                                if(Math.abs(groupStats.getAverageScoreInGroup() - playerStatsReceived.getAverageScore()) < groupStats.getToleratedGapInAverageScore()) {
                                    firstFittingPlayer = playerStatsReceived.getPlayerAID();
                                    step = 2;
                                }
                            }
                        }

                        repliesCnt++;
                        if (repliesCnt >= playerAgentsFound.length) {
                            // We received all replies, and none matching
                            System.out.println(groupStats.getGroupName() + " no player answers OK");

                            firstFittingPlayer = null;
                            step = 2;
                        }
                    }
                    else {
                        block();
                    }
                    break;
                case 2:
                    System.out.println(groupStats.getGroupName() + " Handling player selection...\n");
                    if(groupStats.getNbPlayersInGroup() >= groupStats.nbPlayersInGroupMax) {
                        // The group is already full
                        ACLMessage order = new ACLMessage(ACLMessage.REJECT_PROPOSAL);

                        myAgent.send(order);

                        firstFittingPlayer = null;
                        step = 2;
                    }
                    else {
                        // Send the selection order to the PlayerAgent that provided the offer
                        ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                        order.addReceiver(firstFittingPlayer);
                        try {
                            order.setContentObject(groupStats);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        System.out.println(groupStats.getGroupName() + " accepting player " + firstFittingPlayer.getName() + "\n");

                        order.setConversationId("player-looking-for-match");
                        myAgent.send(order);

                        // Prepare the template to get the purchase order reply
                        mt = MessageTemplate.MatchConversationId("player-looking-for-match");

                        step = 3;
                    }
                    break;

                case 3:
                    // Receive the order reply
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Order reply received
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // Successful. We can terminate
                            System.out.println("Player " + firstFittingPlayer.getName() + " successfuly added to group " + groupStats.getGroupName() + "\n");
                        }
                        step = 4;
                    }
                    else {
                        block();
                    }
                    break;
            }
        }
        public boolean done() {
            if(step == 2 && firstFittingPlayer == null) {
                // Lightening criteria because of a no match
                groupStats.setToleratedGapInNumberOfGamePlayed(groupStats.getToleratedGapInNumberOfGamePlayed() + 10);
                groupStats.setToleratedGapInAverageScore(groupStats.getToleratedGapInAverageScore() + 5000);
            }
            else {
                // We found a PlayerAgent matching the criteria
                int nbPlayerBefore = groupStats.getNbPlayersInGroup();

                groupStats.setAverageNumberOfGamePlayedInGroup(groupStats.getAverageNumberOfGamePlayedInGroup() * (nbPlayerBefore / (nbPlayerBefore + 1)));
                groupStats.setAverageScoreInGroup(groupStats.getAverageScoreInGroup() * (nbPlayerBefore / (nbPlayerBefore + 1)));

                groupStats.setNbPlayersInGroup(nbPlayerBefore + 1);
            }

            if(groupStats.getNbPlayersInGroup() >= groupStats.nbPlayersInGroupMax) {
                //System.out.println(groupStats.toString());
            }

            return ((step == 2 && firstFittingPlayer == null) || step == 4);
        }
    }
}
