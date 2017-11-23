package add.bloodconnection.common;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Alexzander on 23.10.2017.
 */

public abstract class TransferMessage implements ITransferMessage, Serializable {

    private static final long serialVersionUID = 12358903454871L;

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
