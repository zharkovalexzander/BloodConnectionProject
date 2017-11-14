package add.bloodconnection.common.configuration;

public enum MemoryCapacity {
	
	DeviceMac(6), Battery(1), BloodBodiesResearch(4400), Glucoze(15552000);
	
	private int size;
	
	private MemoryCapacity(int size) {
		this.size = size;
	}
	
	public int getSize() {
		return size;
	}
}
