package add.bloodconnection.common.configuration;

import java.util.Arrays;

public class ErythrocytesData extends BloodParts {

	public ErythrocytesData() {
		super();
		this.size = MemoryCapacity.BloodBodiesResearch.getSize();
		this.addressSpace = new long[size];
	}
	
	public ErythrocytesData(ErythrocytesData another) {
		super(another);
	}

	@Override
	public double[] format() {
		double[] result = new double[this.size];
		double power = Math.pow(10, 12);
		for(int i = 0; i < this.size; ++i) {
			result[i] = round((this.addressSpace[i] / power), 2);
		}
		return result;
	}
	
	@Override 
	public String toString() {
		return "Erythrocytes: " + Arrays.toString(format());
	}

}
