package add.bloodconnection.common.lifecycle;

import android.util.Log;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.ArrayList;
import java.util.List;

public class LeukocytesCount extends LifePoint {
    public LeukocytesCount(List<ReasonTimeCount> reasonsStack) {
        super(reasonsStack);
    }

    @Override
    public void produce() {
        long leftLimit, rightLimit;
        RandomDataGenerator rdg = new RandomDataGenerator();
        leftLimit = 40000L;
        rightLimit = 90000L;
        if(reasonsStack.size() != 0) {
            List<Long> values = new ArrayList<>();
            for (ReasonTimeCount res : reasonsStack) {
                if (!res.isPossibleToEntry()) {
                    reasonsStack.remove(res);
                    continue;
                } else {
                    leftLimit = 40000L;
                    rightLimit = 90000L;
                    switch (res.getReason()) {
                        case PREGNANCY:
                            res.entry();
                            leftLimit = 110000L;
                            rightLimit = 150000L;
                            break;
                    }
                    values.add(rdg.nextLong(leftLimit, rightLimit));
                }
                this.bloodCharacts = LifePoint.calculateAverage(values);
            }
        } else {
            this.bloodCharacts = rdg.nextLong(leftLimit, rightLimit);
        }
        //Log.w("Hello: ", this.bloodCharacts + "");
    }
}
