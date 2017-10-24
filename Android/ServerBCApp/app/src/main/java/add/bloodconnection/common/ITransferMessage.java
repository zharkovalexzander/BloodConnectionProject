package add.bloodconnection.common;

import java.util.Date;

/**
 * Created by Alexzander on 23.10.2017.
 */

public interface ITransferMessage {
    public String getMessage();
    public Date getDateTime();
    public Response getReponseStatus();
}
