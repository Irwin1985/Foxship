package v1;

public class ObjBoolean extends Obj {
	public boolean value;
	
	public ObjBoolean(boolean value) {
		this.value = value;
	}	
	public ObjType type() {
		return ObjType.BOOL_OBJ;
	}
	public String inspect() {
		return value ? "true" : "false";
	}
}
