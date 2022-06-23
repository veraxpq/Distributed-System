package server;

/**
 * This command class lists out all the command input that will be executed in our app. The called
 * command will be set to true and be used in the receiver class.
 */
public class Command {

    private String message;
    private final String userInput;
    private boolean isCommand = false;
    private boolean enterRoom = false;
    private boolean leaveRoom = false;
    private boolean isDM = false;
    private boolean offline = false;
    private boolean sync = false;
    private boolean trans = false;
    private boolean checkExist;

    /**
     * Command class constructor
     * @param userInput user command input
     */
    public Command(String userInput) {
        this.userInput = userInput;
        this.execute();
    }

    /**
     * Command line for users
     * @return
     */
    private String execute() {
        // If user input starts with slash
        if (this.userInput.startsWith("/")) {
            this.isCommand = true;
            switch (this.userInput.split(" ")[0]) {
                case "/trans" :
                    trans = true;
                    break;
                case "/offline":
                    this.offline = true;
                    break;
                case "/sync":
                    this.sync = true;
                    break;
                case "/dm":
                    this.isDM = true;
                    break;
                case "/enter":
                    this.enterRoom = true;
                    break;
                case "/leave":
                    this.leaveRoom = true;
                    break;
                case "/info":
                    this.message = "Commands:";
                    this.message += "\n\t/users";
                    this.message += "\n\t/enter {room_name}";
                    this.message += "\n\t/help";
                    this.message += "\n\t/dm {user_name} {message}";
                    break;
                case "/exist":
                    this.checkExist = true;
                    break;
                default:
                    this.message = "Command does not exists.";
                    break;
            }
        } else {
            this.isCommand = false;
        }

        return "";
    }

    /**
     * Check if the input is a command or not
     * @return Is a command or not
     */
    public boolean isCommand() {
        return this.isCommand;
    }

    /**
     * Check if the command is a DM or not
     * @return Is a DM or not
     */
    public boolean isDM() {
        return this.isDM;
    }

    /**
     * Check if the command is enter or not
     * @return Is enter or not
     */
    public boolean isEnterRoom() {
        return this.enterRoom;
    }

    /**
     * Check if the command is leave or not
     * @return Is leave or not
     */
    public boolean isLeaveRoom() {
        return this.leaveRoom;
    }

    /**
     * Check if the command is offLine or not
     * @return Is offline or not
     */
    public boolean isOffline() {
        return offline;
    }

    /**
     * Get the message for user
     * @return user's message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Check if the command is exist or not
     * @return Is exist or not
     */
    public boolean checkExist() {
        return this.checkExist;
    }

    /**
     * Check if the command is sync or not
     * @return Is sync or not
     */
    public boolean isSync() {
        return sync;
    }

    /**
     * Set the direct message
     * @param DM direct message
     */
    public void setDM(boolean DM) {
        isDM = DM;
    }

    /**
     * Set the sync object
     * @param sync sync object
     */
    public void setSync(boolean sync) {
        this.sync = sync;
    }

    /**
     * Check if the command is trans or not
     * @return Is trans or not
     */
    public boolean isTrans() {
        return trans;
    }

    /**
     * Set the trans
     * @param trans trans
     */
    public void setTrans(boolean trans) {
        this.trans = trans;
    }
}
