package add.bloodconnection.common;

/**
 * Created by Alexzander on 23.10.2017.
 */

public enum Response {
    OK(0x00, "OK"), ERROR(0x01, "ERROR"), BRACELET_MAC(0x02, "MAC"), BRACELET_VIBRATION(0x03, "VIB"),
    BRACELET_LISTENHEARTRATE(0x04, "LHR"), BRACELET_STARTHEARTRATE(0x05, "SHR"), BRACELET_BATTERY(0x06, "BAT");

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
