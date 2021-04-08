package v1;
import java.util.List;

public class BuiltinReccount implements BuiltinBase {

	public Obj execute(List<Obj> objArgs, GlobalEnv globalEnv) {
		// check for current connection
		if (globalEnv.currentConnection == null) {
			return new ObjError("there is not an active connection. Please use: SET CONNECTION command.");
		}
		// check for current alias
		if (globalEnv.currentAlias.isEmpty()) {
			return new ObjError("No table is open in the current work area.");
		}		
		int size = objArgs.size();
		if (size > 1) {
			return new ObjError("Unextected parameters: want 1 and got " + size);
		}
		String aliasName = globalEnv.currentAlias;
		if (size == 1) {
			if (objArgs.get(0).type() != ObjType.STRING_OBJ) {
				return new ObjError("Invalid type for alias argument. String expected");
			}
			aliasName = objArgs.get(0).inspect();
		}
		ObjTable objTable = globalEnv.workArea.get(aliasName);
		if (objTable == null) {
			return new ObjError("Alias '" + aliasName + "' not found.");
		}
		int reccount = objTable.reccount();
		if (reccount < 0) {
			return new ObjError("Invalid Alias '" + aliasName + "'");
		}

		return new ObjNumber(reccount);
	}
}
