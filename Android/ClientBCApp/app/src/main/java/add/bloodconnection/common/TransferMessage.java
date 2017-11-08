package add.bloodconnection.common;

import java.io.Serializable;
import java.util.*;
import java.text.*;

/**
 * Created by Alexzander on 23.10.2017.
 */

public abstract class TransferMessage implements ITransferMessage, Serializable {
    protected String responseMsg;
    protected Date messageDateTime;
    protected Response repStatus;

    public TransferMessage(String responseMsg, Date messageDateTime, Response repStatus) {
        this.responseMsg = responseMsg;
        this.repStatus = repStatus;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        this.messageDateTime = messageDateTime;
    }

    public String getMessage() {
        return responseMsg;
    }

    public Date getDateTime() {
        return messageDateTime;
    }

    public Response getReponseStatus() {
        return repStatus;
    }
}