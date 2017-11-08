package add.bloodconnection.common.configuration;

public abstract class BloodParts extends MemoryData {
	
	public BloodParts() {
		super();
	}
	
	public BloodParts(BloodParts another) {
		super(another);
	}
	
	public abstract double[] format();
	
	public double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
}
