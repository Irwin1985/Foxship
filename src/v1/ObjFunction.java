package v1;
import java.util.List;

public class ObjFunction extends Obj {
	public List<String> parameters;
	
	public ObjFunction(List<String> parameters) {
		this.parameters = parameters;
	}
	public ObjType type() {
		return ObjType.FUNCTION_OBJ;
	}
	public String inspect() {
		return "true";
	}
}
