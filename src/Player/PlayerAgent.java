package Player;

import jade.core.AID;
import jade.core.Agent;

import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.security.acl.Group;


public class PlayerAgent extends Agent {
    //////////////////////////////////////////////////////////////////////////
    //                             Player variables                         //
    //////////////////////////////////////////////////////////////////////////
    // The stats of this agent
    private PlayerStats ownStats;


    //////////////////////////////////////////////////////////////////////////
    //                          Player setup and take down                  //
    //////////////////////////////////////////////////////////////////////////

    protected void setup() {
        // Initializing the player using its local name
        ownStats = new PlayerStats(getLocalName(), new AID(getLocalName(), AID.ISLOCALNAME));
        ownStats.getPlayerAID().setName(ownStats.getPlayerName());

        // Displaying basic informations about the PlayerAgent created
        System.out.println(ownStats.toString());


        //////////////////////////////////////////////////////////////////////////
        //                      Yellow pages subscription                       //
        //////////////////////////////////////////////////////////////////////////
        // Saving the agent on setup
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("player-looking-for-match");
        sd.setName("PlayerAgent looking for a game room");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new OfferRequestsServer());
    }


    // Cyclic behaviour from AgentPlayer, waiting for a group to ask for their stats
    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                // Message received. Process it
                ACLMessage reply = msg.createReply();
                reply.setConversationId("player-looking-for-match");

                if(msg.getPerformative() == ACLMessage.CFP) {
                    // A group wants to receive this player informations
                    System.out.println(ownStats.getPlayerName() + " has received a CFP");

                    if(ownStats.getIsGroupFound()) {
                        reply.setPerformative(ACLMessage.REFUSE);
                        myAgent.send(reply);
                    }
                    else {
                        System.out.println(ownStats.getPlayerName() + " sending proposal");

                        reply.setPerformative(ACLMessage.PROPOSE);
                        try {
                            reply.setContentObject(ownStats);
                            System.out.println(ownStats.getPlayerName() + " proposal serialized\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        myAgent.send(reply);
                    }
                }

                if(msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                    // The offer was rejected, going on
                    msg = null;
                }

                if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    // The player is accepted by the group
                    System.out.println(ownStats.getPlayerName() + " has received an ACCEPT_PROPOSAL");

                    if(ownStats.getIsGroupFound()) {
                        // We are already in a group
                        reply.setPerformative(ACLMessage.FAILURE);
                        myAgent.send(reply);
                    }
                    else {
                        GroupStats groupFound = new GroupStats("Default");
                        try {
                            groupFound = (GroupStats)msg.getContentObject();
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }

                        ownStats.setIsGroupFound(true);
                        System.out.println("Player " + ownStats.getPlayerName() + " has found a place in the group " + groupFound.getGroupName() + "\n");

                        reply.setPerformative(ACLMessage.INFORM);
                        myAgent.send(reply);

                        myAgent.doDelete();
                    }
                }
            }
        }
    }


    protected void takeDown() {
        // Getting out of the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // On signale la fin de cet agent
        System.out.println("Player " + getAID().getName() + " terminating");
    }
}
