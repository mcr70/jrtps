package net.sf.jrtps.types;

public enum ChangeKind_t {
	// ALIVE, NOT_ALIVE_DISPOSED, NOT_ALIVE_UNREGISTERED : DDS instance state kind 
	ALIVE(0x0001 << 0 /* DDS.ALIVE_INSTANCE_STATE.value */),
	NOT_ALIVE_DISPOSED(0x0001 << 1 /* DDS.NOT_ALIVE_DISPOSED_INSTANCE_STATE.value */),
	NOT_ALIVE_UNREGISTERED(0x0001 << 2 /* DDS.NOT_ALIVE_NO_WRITERS_INSTANCE_STATE.value */);
	
	ChangeKind_t(int value) {
		this.value = value;
	}
	
	private final int value;
	
	public int getValue() {
		return value;
	}
}
