package add.bloodconnection.common.configuration;

import java.util.Arrays;

/**
 * Created by Alexzander on 07.11.2017.
 */

public class GlucozeData extends BloodParts {

    public GlucozeData() {
        super();
        this.size = MemoryCapacity.Glucoze.getSize();
        this.addressSpace = new long[this.size];
        this.fileMemoryName = "gluData.bcmf";
    }

    public GlucozeData(HemoglobineData another) {
        super(another);
    }

    @Override
    public double[] format() {
        double[] result = new double[this.size];
        double power = Math.pow(10, 3);
        for(int i = 0; i < this.size; ++i) {
            result[i] = round((this.addressSpace[i] / power), 2);
        }
        return result;
    }

    @Override
    public boolean isFilled() {
        return this.len == this.size;
    }

    @Override
    public String toString() {
        return "Glucoze: " + Arrays.toString(format());
    }
}
