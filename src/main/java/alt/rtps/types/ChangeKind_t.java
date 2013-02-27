package alt.rtps.types;

public enum ChangeKind_t {
	// ALIVE, NOT_ALIVE_DISPOSED, NOT_ALIVE_UNREGISTERED : DDS instance state kind 
	ALIVE(DDS.ALIVE_INSTANCE_STATE.value),
	NOT_ALIVE_DISPOSED(DDS.NOT_ALIVE_DISPOSED_INSTANCE_STATE.value),
	NOT_ALIVE_UNREGISTERED(DDS.NOT_ALIVE_NO_WRITERS_INSTANCE_STATE.value);
	
	ChangeKind_t(int value) {
		this.value = value;
	}
	
	private final int value;
	
	public int getValue() {
		return value;
	}
}
