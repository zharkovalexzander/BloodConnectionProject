package add.bloodconnection.common.lifecycle;

import android.util.Log;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexzander on 04.11.2017.
 */

public class ErythrocytesCount extends LifePoint {
    public ErythrocytesCount(List<ReasonTimeCount> reasonsStack) {
        super(reasonsStack);
    }

    @Override
    public void produce() {
        long leftLimit, rightLimit;
        RandomDataGenerator rdg = new RandomDataGenerator();
        leftLimit = 38000000L;
        rightLimit = 51500000L;
        if(reasonsStack.size() != 0) {
            List<Long> values = new ArrayList<>();
            for (ReasonTimeCount res : reasonsStack) {
                if (!res.isPossibleToEntry()) {
                    reasonsStack.remove(res);
                    continue;
                } else {
                    leftLimit = 38000000L;
                    rightLimit = 51500000L;
                    switch (res.getReason()) {
                        case PREGNANCY:
                            res.entry();
                            leftLimit = 35000000L;
                            rightLimit = 50000000L;
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
