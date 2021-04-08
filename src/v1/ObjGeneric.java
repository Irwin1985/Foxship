package v1;

public class ObjGeneric extends Obj {
	Object value;
	public ObjGeneric (Object value) {
		this.value = value;
	}
	public ObjType type() {
		return ObjType.GENERIC_OBJ;
	}
	public String inspect() {
		return value.toString();
	}
}
