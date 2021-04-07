package v1;

public class ObjBuiltin extends Obj {
	public BuiltinBase functionName;

	public ObjBuiltin(BuiltinBase functionName) {
		this.functionName = functionName;
	}
	public ObjType type() {
		return ObjType.BUILTIN_OBJ;
	}
	public String inspect() {
		return "true";
	}
}
