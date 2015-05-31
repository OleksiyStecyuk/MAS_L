/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package protocolslab;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.AchieveREInitiator;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import java.util.Vector;

/**
   This example shows how to implement nested protocols.
   In this case in particular we want to implement a broker agent
   that forwards incoming requests to perform actions to another 
   agent. To implement that we use an <code>AchieveREResponder</code> ("Achieve Rational effect") 
   and we register an <code>AchieveREInitiator</code> in the state where the 
   requested action has to be performed.
   @author Giovanni Caire - TILAB
 */
public class BrokerAgent extends Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7213730789706355897L;
//	private AID responder;

	  AID responder;
	  Object[] globalArgs;// New object for saving global arguments
  protected void setup() {
  	// Read the name of agent to forward requests to
  	Object[] args = getArguments();
  	globalArgs=args;// initial setting global arguments to local
  	
  	if (args != null && args.length > 0) {
		 System.out.println("Agent "+getLocalName()+" waiting for requests...");//modified for better execution. also allows multiple requests
  		for (int i=0;i<args.length;i++)
  		{
  			responder = new AID((String) args[i], AID.ISLOCALNAME);
  		  	MessageTemplate template = MessageTemplate.and(
  			  		MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
  			  		MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
			AchieveREResponder arer = new AchieveREResponder(this, template) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 4680575776103371550L;

				protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
					System.out.println("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());
					ACLMessage agree = request.createReply();
					agree.setPerformative(ACLMessage.AGREE);
					return agree;
				}
			};
			
			// Register an AchieveREInitiator in the PREPARE_RESULT_NOTIFICATION state
						arer.registerPrepareResultNotification(new AchieveREInitiator(this, null) {
							/**
							 * 
							 */
							private static final long serialVersionUID = 5860705423199679972L;


							// Since we don't know what message to send to the responder
							// when we construct this AchieveREInitiator, we redefine this 
							// method to build the request on the fly
							protected Vector<ACLMessage> prepareRequests(ACLMessage request) {
								// Retrieve the incoming request from the DataStore
								String incomingRequestKey = (String) ((AchieveREResponder) parent).REQUEST_KEY;
								ACLMessage incomingRequest = (ACLMessage) getDataStore().get(incomingRequestKey);
								// Prepare the request to forward to the responder
								Vector<ACLMessage> v = new Vector<ACLMessage>(globalArgs.length); //new vector to handle multiple requests
								for (int j=0;j<globalArgs.length;j++)
								{
									AID localResponder = new AID((String) globalArgs[j], AID.ISLOCALNAME);
									System.out.println("Agent "+getLocalName()+": Forward the request to "+localResponder.getName());
									ACLMessage outgoingRequest = new ACLMessage(ACLMessage.REQUEST);
									outgoingRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
									outgoingRequest.addReceiver(localResponder);
									outgoingRequest.setContent(incomingRequest.getContent());
									outgoingRequest.setReplyByDate(incomingRequest.getReplyByDate());
									v.addElement(outgoingRequest);
								}
								return v;
							}
							
							protected void handleInform(ACLMessage inform) {
								storeNotification(ACLMessage.INFORM);
							}
							
							protected void handleRefuse(ACLMessage refuse) {
								storeNotification(ACLMessage.FAILURE);
							}
							
							protected void handleNotUnderstood(ACLMessage notUnderstood) {
								storeNotification(ACLMessage.FAILURE);
							}
							
							protected void handleFailure(ACLMessage failure) {
								storeNotification(ACLMessage.FAILURE);
							}
							
							protected void handleAllResultNotifications(Vector notifications) {
								if (notifications.size() == 0) {
									// Timeout
									storeNotification(ACLMessage.FAILURE);
								}
							}
							
							private void storeNotification(int performative) {
								if (performative == ACLMessage.INFORM) {
									System.out.println("Agent "+getLocalName()+": brokerage successful");
								}
								else {
									System.out.println("Agent "+getLocalName()+": brokerage failed");
								}
									
								// Retrieve the incoming request from the DataStore
								String incomingRequestkey = (String) ((AchieveREResponder) parent).REQUEST_KEY;
								ACLMessage incomingRequest = (ACLMessage) getDataStore().get(incomingRequestkey);
								// Prepare the notification to the request originator and store it in the DataStore
								ACLMessage notification = incomingRequest.createReply();
								notification.setPerformative(performative);
								String notificationkey = (String) ((AchieveREResponder) parent).RESULT_NOTIFICATION_KEY;
								getDataStore().put(notificationkey, notification);
							}
						} );
						
						addBehaviour(arer);
  		}	  				
  	}
  	else {
  		System.out.println("No agent to forward requests to specified.");
  	}
  }
}

