package add.bloodconnection.common.configuration;

/**
 * Created by Alexzander on 28.10.2017.
 */

public class DeviceMac extends DeviceInfo {
    
    public DeviceMac() {
    	super();
    	this.size = MemoryCapacity.DeviceMac.getSize();
		this.addressSpace = new long[size];
    }

    public DeviceMac(DeviceMac another){
    	super(another);
    }

    @Override
    public synchronized String getMac() {
    	long[] mac = decode();
        StringBuilder bd = new StringBuilder();
        for(int i = mac.length - 1; i >= 0; --i) {
            bd.append(String.format("%X", mac[i]));
            if(i > 0) {
                bd.append(':');
            }
        }
        return bd.toString();
    }

    @Override
    protected synchronized long[] decode() {
    	long[] decoded = new long[addressSpace.length];
        for(int i = 0; i < decoded.length; ++i) {
            decoded[i] = ((((addressSpace[i] - 2) >> 2) / 10) >> 2);
        }
        return decoded;
    }
    
    @Override
    protected long encode(long data) {
        return ((((data << 2) * 10) << 2) + 2);
    }

    @Override
    public void setNewMac(long[] data) {
        if(data.length != this.addressSpace.length) throw new IllegalArgumentException();
        for(int i = this.addressSpace.length - 1; i >= 0; --i) {
            this.addressSpace[i] = encode(data[this.addressSpace.length - 1 - i]);
        }
        isEmpty = false;
        isFilled = true;
        this.len = 6;
    }

    @Override
    public void setNewMac(String val) {
        String[] data = val.split(":");
        if(data.length != this.addressSpace.length) throw new IllegalArgumentException();
        for(int i = this.addressSpace.length - 1; i >= 0; --i) {
            this.addressSpace[i] = encode(Long.parseLong(data[this.addressSpace.length - 1 - i], 16));
        }
        isEmpty = false;
        isFilled = true;
        this.len = 6;
    }

    @Override
	public void write(long data) {
		addressSpace[size - this.len - 1] = encode(data);
		this.isFilled = true;
    	this.isEmpty = false;
	}
}
