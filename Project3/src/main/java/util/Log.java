package util;

/**
 * This class records some result of the execution of committing.
 */
public class Log {
    /**
     * represents the global abortion of the execution.
     */
    public static final String GLOBAL_ABORT = "GLOBAL_ABORT";

    /**
     * represents the global commit of the execution.
     */
    public static final String GLOBAL_COMMIT = "GLOBAL_COMMIT";

    /**
     * represents the abortion of the participant.
     */
    public static final String SELF_ABORT = "SELF_ABORT";

    /**
     * represents the commit of the participant.
     */
    public static final String SELF_COMMIT = "SELF_COMMIT";
}
