package transactionServer.paxos;

import org.json.simple.JSONArray;
import transactionServer.bankService.BankService;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class is the implementation of the learner.
 */
public class LearnerImpl implements Learner {
    private static final Logger LOGGER = Logger.getLogger(LearnerImpl.class.getName());

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
         * @param acceptCount    the count that accepts.
         * @param retentionCount the count that retentions.
         * @param value          the value of the proposal.
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
    private BankService bankService = null;
    private String learnerUID;
    private List<Integer> clientPorts = new ArrayList<>();
    private int clientPort = 30001;

    /**
     * This is the constructor of the LearnerImpl class, initializes the attributes.
     *
     * @param quorumSize  the half size of the acceptors.
     * @param bankService the object of the map service.
     * @param learnerUID  UID of the learner.
     */
    public LearnerImpl(int quorumSize, BankService bankService, String learnerUID) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, messengerPort);
            this.messenger = (Messenger) registry.lookup("messenger");
            this.learnerUID = learnerUID;
        } catch (AccessException e) {
            LOGGER.warning("access exception: " + e.getMessage());
        } catch (NotBoundException e) {
            LOGGER.warning("not bound exception: " + e.getMessage());
        } catch (RemoteException e) {
            LOGGER.warning("remote exception: " + e.getMessage());
        }
        this.quorumSize = quorumSize;
        this.bankService = bankService;
    }

    @Override
    public boolean isComplete() {
        return finalValue != null;
    }

    @Override
    public boolean receiveAccepted(String acceptorUID, ProposalID proposalID, Object acceptedValue) throws RemoteException {
        if (isComplete()) {
            return false;
        }
        ProposalID oldPID = acceptors.get(acceptorUID);
        if (oldPID != null && !proposalID.isGreaterThan(oldPID)) {
            return false;
        }
        acceptors.put(acceptorUID, proposalID);
        if (oldPID != null) {
            Proposal oldProposal = proposals.get(oldPID.toString());
            oldProposal.retentionCount -= 1;
            if (oldProposal.retentionCount == 0) {
                proposals.remove(oldPID.toString());
            }
        }
        String str = proposalID.toString();
        if (!proposals.containsKey(str)) {
            proposals.put(proposalID.toString(), new Proposal(0, 0, acceptedValue));
        }
        Proposal thisProposal = proposals.get(proposalID.toString());
        thisProposal.acceptCount += 1;
        thisProposal.retentionCount += 1;
        if (thisProposal.acceptCount == quorumSize) {
            JSONArray resObj = bankService.map(proposalID.getValue());
            proposalID.setResponse(resObj);
            finalProposalID = proposalID;
            finalValue = null;
            proposals.clear();
            acceptors.clear();
            if (learnerUID.equals(proposalID.getUid())) {
                messenger.onResolution(proposalID, resObj);
                return true;
            }
        }
        return false;
    }

    @Override
    public void revert() throws RemoteException {
        bankService.revert();
    }

    @Override
    public Object getFinalValue() {
        return finalValue;
    }

    @Override
    public ProposalID getFinalProposalID() {
        return finalProposalID;
    }

    @Override
    public int getClientPort() throws RemoteException {
        clientPorts.add(clientPort);
        messenger.sendClientPort(clientPort);
        return clientPort++;
    }

    @Override
    public void receiveClientPort(int port) {
        if (clientPorts.size() != 0 && clientPorts.get(clientPorts.size() - 1) == port) {
            return;
        }
        clientPort = port;
        clientPorts.add(clientPort++);
    }
}
