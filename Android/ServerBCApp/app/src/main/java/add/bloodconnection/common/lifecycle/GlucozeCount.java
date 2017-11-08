package add.bloodconnection.common.lifecycle;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Alexzander on 07.11.2017.
 */

public class GlucozeCount extends LifePoint {

    private boolean diabetics;
    private Random rand;

    public GlucozeCount(List<ReasonTimeCount> reasonsStack) {
        super(reasonsStack);
        rand = new Random();
        diabetics = false;
    }

    @Override
    public void produce() {
        long leftLimit, rightLimit;
        RandomDataGenerator rdg = new RandomDataGenerator();
        if(!diabetics) {
            leftLimit = 39 * 11312;
            rightLimit = 50 * 11312;
        } else {
            leftLimit = 39 * 11312;
            rightLimit = 90 * 11312;
        }
        if(reasonsStack.size() != 0) {
            List<Long> values = new ArrayList<>();
            for (ReasonTimeCount res : reasonsStack) {
                if (!res.isPossibleToEntry()) {
                    reasonsStack.remove(res);
                    continue;
                } else {
                    if(!diabetics) {
                        leftLimit = 39 * 11312;
                        rightLimit = 50 * 11312;
                        switch (res.getReason()) {
                            case FOOD_DRINK:
                                res.entry();
                                leftLimit = 39 * 11312L;
                                rightLimit = 70 * 11312;
                                break;
                            case THIRST:
                                res.entry();
                                leftLimit = 30 * 11312;
                                rightLimit = 40 * 11312;
                                break;
                        }
                    } else {
                        leftLimit = 39 * 11312;
                        rightLimit = 90 * 11312;
                        switch (res.getReason()) {
                            case FOOD_DRINK:
                                res.entry();
                                leftLimit = 80 * 11312;
                                rightLimit = 180 * 11312;
                                break;
                            case THIRST:
                                res.entry();
                                leftLimit = 15 * 11312;
                                rightLimit = 50 * 11312;
                                break;
                        }
                    }
                    values.add(rdg.nextLong(leftLimit, rightLimit));
                }
                this.bloodCharacts = LifePoint.calculateAverage(values);
            }
        } else {
            this.bloodCharacts = rdg.nextLong(leftLimit, rightLimit);
        }
    }

    public boolean isDiabetics() {
        return diabetics;
    }

    public void tryToMakeDiabetic() {
        if(isDiabetics()) {
            return;
        }
        double chance = rand.nextDouble();
        if(chance <= 0.02) {
            diabetics = true;
        }
    }
}
