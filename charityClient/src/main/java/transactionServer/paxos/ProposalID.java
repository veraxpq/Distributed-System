package transactionServer.paxos;


import org.json.simple.JSONArray;
import transactionServer.Client.TransactionClient;
import transactionServer.bankService.BankOperation;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents a proposal.
 */
public class ProposalID implements Serializable {
    private int number;
    private final String uid;
    private TransactionClient client;
    private List<BankOperation> value;
    private JSONArray response;

    /**
     * This is the constructor of a proposal, initializes the uid and number.
     *
     * @param number the number of the proposal.
     * @param uid    the UID of the proposal.
     */
    public ProposalID(int number, String uid) {
        this.number = number;
        this.uid = uid;
    }

    /**
     * This method returns the response json object.
     *
     * @return the response json object.
     */
    public JSONArray getResponse() {
        return response;
    }

    /**
     * This method set the response json object.
     *
     * @param response the response json object.
     */
    public void setResponse(JSONArray response) {
        this.response = response;
    }

    /**
     * This method return the number of the proposal
     *
     * @return the number of the proposal
     */
    public int getNumber() {
        return number;
    }

    /**
     * This method returns a string containing the uid and the value of the proposal.
     *
     * @return returns a string containing the uid and the value of the proposal.
     */
    public String toString() {
        String idInfo = uid + " " + value + " " + number;
        return response != null ? idInfo + " " + response.toJSONString() : idInfo;
    }

    /**
     * This method increments the number of the proposal by one.
     */
    public void incrementNumber() {
        this.number += 1;
    }

    /**
     * This method compare two proposals, and return 1 if this proposal has lager number than the other, returns 0 if
     * the numbers are equal, -1 when the number of this is smaller than the other.
     *
     * @param rhs the other proposal object.
     * @return 1 when the number of this > the number of the other proposal;
     * 0 when the number of this = the number of the other proposal;
     * -1 when the number of this < the number of the other proposal;
     */
    public int compare(ProposalID rhs) {
        if (equals(rhs)) {
            return 0;
        }
        if (number < rhs.number || (number == rhs.number && uid.compareTo(rhs.uid) < 0)) {
            return -1;
        }
        return 1;
    }

    /**
     * This method sets the client of this class with the given client object.
     *
     * @param client the remote object of a client.
     */
    public void setClient(TransactionClient client) {
        this.client = client;
    }

    /**
     * This method returns the client object.
     *
     * @return the remote object of the client.
     */
    public TransactionClient getClient() {
        return this.client;
    }

    /**
     * This method returns true when this proposal has greater number than the other proposal, false otherwise.
     *
     * @param rhs the other proposal object.
     * @return returns true when this proposal has greater number than the other proposal, false otherwise.
     */
    public boolean isGreaterThan(ProposalID rhs) {
        return compare(rhs) > 0;
    }

    /**
     * This method returns true when this proposal has greater number than the other proposal, false otherwise.
     *
     * @param rhs the other proposal object.
     * @return returns true when this proposal has greater number than the other proposal, false otherwise.
     */
    public boolean isGreaterOrEqualThan(ProposalID rhs) {
        return compare(rhs) >= 0;
    }

    /**
     * This method get the value of the proposal.
     *
     * @return the value of the proposal.
     */
    public List<BankOperation> getValue() {
        return value;
    }

    /**
     * This method set the value of the proposal with the given value.
     *
     * @param value the given value.
     */
    public void setValue(List<BankOperation> value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProposalID other = (ProposalID) obj;
        if (number != other.number) {
            return false;
        }
        if (uid == null) {
            if (other.uid != null) {
                return false;
            }
        } else if (!uid.equals(other.uid)) {
            return false;
        }
        return true;
    }

    /**
     * This method returns the UID of the proposal.
     *
     * @return the UID of the proposal.
     */
    public String getUid() {
        return uid;
    }
}
