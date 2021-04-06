package v1;

public class ObjNumber extends Obj {
	double value;
	public ObjNumber(double value) {
		this.value = value;
	}
	public ObjType type() {
		return ObjType.NUMBER_OBJ;
	}
	public String inspect() {
		if (value % 1 == 0) {			
			return String.valueOf((int)value);
		} else {
			return String.valueOf(value);
		}
	}
}
