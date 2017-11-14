package add.bloodconnection.common.configuration;

public abstract class DeviceInfo extends MemoryData {

	protected abstract String getMac();
	protected abstract long[] decode();
	protected abstract long encode(long data);
	protected abstract void setNewMac(long[] data);
    protected abstract void setNewMac(String data);
	
	public DeviceInfo() {
		super();
	}
	
	public DeviceInfo(MemoryData another) {
		super();
	}
	
	@Override
	public synchronized String toString() {
		return "MAC-Address: " + getMac();
	}
}
