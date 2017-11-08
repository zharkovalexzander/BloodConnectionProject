package add.bloodconnection.common.configuration;

public class BatteryInfo extends MemoryData {
	
	public BatteryInfo() {
		super();
    	this.size = MemoryCapacity.Battery.getSize();
		this.addressSpace = new long[size];
	}
	
	public BatteryInfo(BatteryInfo another){
    	super(another);
    }
	
	@Override
	public synchronized String toString() {
		return "Battery: " + addressSpace[0] + "%";
	}

}
