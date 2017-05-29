package ltj.message.constant;

public enum CarrierCode {
	SMTPMAIL("S"),
	WEBMAIL("W"),
	READONLY("R");

	private String value;
	CarrierCode(String value) {
		this.value=value;
	}
	public String value() {
		return value;
	}
	@Override public String toString() {
		return value();
	}
	public CarrierCode fromValue(String value) {
		for (CarrierCode v : CarrierCode.values()) {
			if (v.value().equalsIgnoreCase(value)) {
				return v;
			}
		}
		return null;
	}
}
