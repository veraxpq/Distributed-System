package paxos;

import Client.Client;
import Server.MapService;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

/**
 * This class is the implementation of the learner.
 */
public class LearnerImpl implements Learner {

    /**
     * This is a inner class of the LearnerImpl class, which is a proposal class containing the count of accepts, the
     * count of retention, and the value of the proposal.
     */
    class Proposal {
        int acceptCount;
        int retentionCount;
        Object value;

        /**
         * This is the constructor of the Proposal, initializes the proposal object.
         *
         * @param acceptCount the count that accepts.
         * @param retentionCount the count that retentions.
         * @param value the value of the proposal.
         */
        Proposal(int acceptCount, int retentionCount, Object value) {
            this.acceptCount = acceptCount;
            this.retentionCount = retentionCount;
            this.value = value;
        }
    }

    private Messenger messenger;
    private final int quorumSize;
    private HashMap<String, Proposal> proposals = new HashMap<>();
    private HashMap<String, ProposalID> acceptors = new HashMap<String, ProposalID>();
    private Object finalValue = null;
    private ProposalID finalProposalID = null;
    private String host = "localhost";
    private final int messengerPort = 50050;
    private MapService mapService = null;
    private String learnerUID;

    /**
     * This is the constructor of the LearnerImpl class, initializes the attributes.
     *
     * @param quorumSize the half size of the acceptors.
     * @param mapService the object of the map service.
     * @param learnerUID UID of the learner.
     */
    public LearnerImpl(int quorumSize, MapService mapService, String learnerUID) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, messengerPort);
            this.messenger = (Messenger) registry.lookup("messenger");
            this.learnerUID = learnerUID;
        } catch (AccessException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.quorumSize = quorumSize;
        this.mapService = mapService;
    }

    @Override
    public boolean isComplete() {
        return finalValue != null;
    }

    @Override
    public void receiveAccepted(String acceptorUID, ProposalID proposalID, Object acceptedValue, Client client) throws RemoteException {
        if (isComplete()) {
            return;
        }
        ProposalID oldPID = acceptors.get(acceptorUID);
        if (oldPID != null && !proposalID.isGreaterThan(oldPID)) {
            return;
        }
        acceptors.put(acceptorUID, proposalID);
        if (oldPID != null) {
            Proposal oldProposal = proposals.get(oldPID.toString());
            oldProposal.retentionCount -= 1;
            if (oldProposal.retentionCount == 0) {
                proposals.remove(oldPID.toString());
            }
        }
        if (!proposals.containsKey(proposalID.toString())) {
            proposals.put(proposalID.toString(), new Proposal(0, 0, acceptedValue));
        }
        Proposal thisProposal = proposals.get(proposalID.toString());
        thisProposal.acceptCount += 1;
        thisProposal.retentionCount += 1;
        if (thisProposal.acceptCount == quorumSize) {
            finalProposalID = proposalID;
            finalValue = null;
            proposals.clear();
            acceptors.clear();
            String res = "Server" + learnerUID + ": " + mapService.map(proposalID.getValue());
            if (learnerUID.equals(proposalID.getUid())) {
                messenger.onResolution(proposalID, res, client);
            }
        }
    }

    @Override
    public Object getFinalValue() {
        return finalValue;
    }

    @Override
    public ProposalID getFinalProposalID() {
        return finalProposalID;
    }
}
