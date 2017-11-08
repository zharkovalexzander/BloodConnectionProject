package add.bloodconnection.common.lifecycle;

import android.util.Log;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexzander on 05.11.2017.
 */

public class HemoglobineCount extends LifePoint {


    public HemoglobineCount(List<ReasonTimeCount> reasonsStack) {
        super(reasonsStack);
    }

    @Override
    public void produce() {
        long leftLimit, rightLimit;
        RandomDataGenerator rdg = new RandomDataGenerator();
        leftLimit = 1200L;
        rightLimit = 1600L;
        if(reasonsStack.size() != 0) {
            List<Long> values = new ArrayList<>();
            for (ReasonTimeCount res : reasonsStack) {
                if (!res.isPossibleToEntry()) {
                    reasonsStack.remove(res);
                    continue;
                } else {
                    leftLimit = 1200L;
                    rightLimit = 1600L;
                    switch (res.getReason()) {
                        case SMOKING:
                            res.entry();
                            leftLimit = 1750L;
                            rightLimit = 2000L;
                            break;
                    }
                    values.add(rdg.nextLong(leftLimit, rightLimit));
                }
                this.bloodCharacts = LifePoint.calculateAverage(values);
            }
        } else {
            this.bloodCharacts = rdg.nextLong(leftLimit, rightLimit);
        }
    }
}
