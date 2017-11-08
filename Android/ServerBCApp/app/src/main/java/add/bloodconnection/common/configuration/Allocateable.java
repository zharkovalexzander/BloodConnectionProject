package add.bloodconnection.common.configuration;

import java.io.Serializable;

public interface Allocateable extends Serializable {
	int getDataSize();
	boolean isFilled();
	boolean isEmpty();
	void free();
	void write(long data) throws Exception;
	long read(int memAddress);
	int getDataLen();
}
