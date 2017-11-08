package add.bloodconnection.common.configuration;

import java.util.Arrays;

public class HemoglobineData extends BloodParts {

	public HemoglobineData() {
		super();
        this.size = MemoryCapacity.BloodBodiesResearch.getSize();
        this.addressSpace = new long[this.size];
	}

	public HemoglobineData(HemoglobineData another) {
		super(another);
	}

	@Override
	public double[] format() {
		double[] result = new double[this.size];
		double power = Math.pow(10, 6);
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
		return "Hemoglobine: " + Arrays.toString(format());
	}

}
