package org.one.scheduler.agent.exception;

import java.io.IOException;

/**
 * Created by bin on 14-1-22.
 */
public class AgentException extends RuntimeException {

    public AgentException(String msg, IOException ioe)
    {
        super(msg);
    }

    public AgentException(String msg){
        super(msg);
    }
}
