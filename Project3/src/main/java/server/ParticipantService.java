package server;

import util.Log;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * This class is the implementation of Participant interface.
 */
public class ParticipantService implements Participant, Serializable {
    private static final Logger LOGGER = Logger.getLogger(ParticipantService.class.getName());

    MapService mapService;

    /**
     * Constructor of the ParticipantService class, initialize the mapService.
     */
    public ParticipantService() {
        mapService = new MapServiceImpl();
    }

    @Override
    public String vote(String command) {
        try {
            mapService.map(command);
            return Log.SELF_COMMIT;
        } catch (Exception e) {
            return Log.SELF_ABORT;
        }

    }

    @Override
    public void abort(String command) {
        mapService.map(command);
        LOGGER.info("operation: " + command);
    }

}
