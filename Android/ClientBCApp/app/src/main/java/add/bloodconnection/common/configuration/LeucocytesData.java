package add.bloodconnection.common.configuration;

import java.util.Arrays;

public class LeucocytesData extends BloodParts {
	
	public LeucocytesData() {
		super();
		this.size = MemoryCapacity.BloodBodiesResearch.getSize();
		this.addressSpace = new long[this.size];
		this.fileMemoryName = "leuData.bcmf";
	}
	
	public LeucocytesData(LeucocytesData another) {
		super(another);
	}

	@Override
	public boolean isFilled() {
		return this.len == this.size;
	}

	@Override
	public double[] format() {
		double[] result = new double[this.size];
		double power = Math.pow(10, 9);
		for(int i = 0; i < this.size; ++i) {
			result[i] = round((this.addressSpace[i] / power), 2);
		}
		return result;
	}
	
	@Override 
	public String toString() {
		return "Leucocytes: " + Arrays.toString(format());
	}

}
