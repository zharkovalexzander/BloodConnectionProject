package add.bloodconnection.common.lifecycle;

import java.util.concurrent.atomic.AtomicLong;

public class ReasonTimeCount {
    private Reason reason;
    private long counts;

    public ReasonTimeCount(Reason reason, long counts) {
        this.counts = counts;
        this.reason = reason;
    }

    public Reason getReason() {
        return this.reason;
    }

    public Long getCounts() {
        return counts;
    }

    public void entry() {
        if(isPossibleToEntry()) {
            --counts;
        }
    }

    public boolean isPossibleToEntry() {
        return this.counts > 0;
    }
}
