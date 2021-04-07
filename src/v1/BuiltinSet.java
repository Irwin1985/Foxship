package v1;
import java.util.List;
public class BuiltinSet implements BuiltinBase {
	/**
	 * Set displays multiple information related to environments.
	 */
	public Obj execute(List<Obj> objArgs, GlobalEnv globalEnv) {
		if (objArgs.size() == 0) {
			return new ObjError("Too few arguments.");
		}
		if (objArgs.get(0).type() != ObjType.STRING_OBJ) {
			return new ObjError("Invalid data type for the argument");
		}
		String value = objArgs.get(0).inspect().toLowerCase();
		
		if (value.equals("connection")) {
			return new ObjString(globalEnv.currentConnectionName);
		} else {
			return new ObjError("Invalid argument used with the SET function.");
		}
	}
}
