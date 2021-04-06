package v1;

public class ObjNull extends Obj {
	public ObjNull() {}
	public ObjType type() {
		return ObjType.NULL_OBJ;
	}
	public String inspect() {
		return "null";
	}
}
