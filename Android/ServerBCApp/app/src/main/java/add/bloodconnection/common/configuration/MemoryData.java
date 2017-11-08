package add.bloodconnection.common.configuration;

import java.util.Arrays;

public abstract class MemoryData implements Allocateable {
	
    protected long[] addressSpace;
    protected int size;
	protected boolean isFilled;
	protected boolean isEmpty;
    protected int len;
	
	public MemoryData() {
    	this.isFilled = false;
    	this.isEmpty = true;
        this.len = 0;
	}
	
	public MemoryData(MemoryData another) {
		this.size = another.size;
    	this.isFilled = false;
    	this.isEmpty = true;
    	this.addressSpace = Arrays.copyOfRange(another.addressSpace, 0, size);
        this.len = 0;
	}
	
	@Override
	public boolean isFilled() {
		return this.len == this.size;
	}

	@Override
	public boolean isEmpty() {
		return isEmpty;
	}
	
	@Override
	public int getDataSize() {
		return size;
	}
	
	@Override
	public void free() {
		this.addressSpace = new long[size];
    	this.isFilled = false;
    	this.isEmpty = true;
        this.len = 0;
	}

	@Override
	public void write(long data) throws Exception {
		if(this.len == this.size) {
			throw new Exception("Out of leucomemory");
		}
		this.addressSpace[this.size - this.len - 1] = data;
		this.isFilled = true;
    	this.isEmpty = false;
        this.len++;
	}

	@Override
	public synchronized long read(int memAddress) {
		return this.addressSpace[this.size - memAddress - 1];
	}

	@Override
	public int getDataLen() {
		return this.len;
	}
}
