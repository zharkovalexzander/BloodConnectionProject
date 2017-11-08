package add.bloodconnection.common.lifecycle;

import java.util.List;

public abstract class LifePoint implements ILifePoint {
    protected List<ReasonTimeCount> reasonsStack;
    protected long bloodCharacts;

    public LifePoint(List<ReasonTimeCount> reasonsStack) {
        this.reasonsStack = reasonsStack;
    }

    public abstract void produce();

    public long getBloodCharacts() {
        return bloodCharacts;
    }

    public static long calculateAverage(List<Long> marks) {
        if (marks == null || marks.isEmpty()) {
            return 0;
        }

        double sum = 0;
        for (Long mark : marks) {
            sum += mark;
        }

        return (long) (sum / marks.size());
    }
}
