package v1;

public class ObjError extends Obj {
	private String message;
	
	public ObjError(String message){
		this.message = message;
	}
	
	public ObjType type() {
		return ObjType.ERROR_OBJ;
	}
	public String inspect() {
		return message;
	}
}
