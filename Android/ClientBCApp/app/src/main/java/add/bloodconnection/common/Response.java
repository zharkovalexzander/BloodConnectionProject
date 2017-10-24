package add.bloodconnection.common;

/**
 * Created by Alexzander on 23.10.2017.
 */

public enum Response {
    OK(0x00, "OK"), ERROR(0x01, "ERROR");

    private int reponseValue;
    private String msgReponse;

    private Response(int repStatus, String msgReponse) {
        reponseValue = repStatus;
        this.msgReponse = msgReponse;
    }

    public boolean sameIs(Integer reponseValue) {
        return this.reponseValue == reponseValue;
    }

    public String getTextReponse() {
        return msgReponse;
    }
}
