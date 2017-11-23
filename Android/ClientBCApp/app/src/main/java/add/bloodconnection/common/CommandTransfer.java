package add.bloodconnection.common;

/**
 * Created by Alexzander on 23.10.2017.
 */

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CommandTransfer extends TransferMessage implements Serializable {

    private static final long serialVersionUID = 12358903454870L;

    public CommandTransfer(String responseMsg, Date messageDateTime, Response repStatus) {
        super(responseMsg, messageDateTime, repStatus);
    }

    @Override
    public String toString() {
        return String.format("Message: %s; Date: %s; Response status: %s", this.responseMsg,
                new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(this.messageDateTime), repStatus.getTextReponse());
    }
}
