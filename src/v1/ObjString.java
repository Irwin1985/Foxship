package v1;

public class ObjString extends Obj {
	public String value;
	
	public ObjString(String value) {
		this.value = value;
	}
	public ObjType type() {
		return ObjType.STRING_OBJ;
	}
	public String inspect() {
		return value;
	}
}
