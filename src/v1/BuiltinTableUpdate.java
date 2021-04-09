package v1;
import java.util.List;

public class BuiltinTableUpdate implements BuiltinBase {
	public Obj execute(List<Obj> objArgs, GlobalEnv globalEnv) {
		int size = objArgs.size();
		if (size > 0) {
			return new ObjError("unexpected arguments.");
		}
		// check for current connection
		if (globalEnv.currentConnection == null) {
			return new ObjError("there is not an active connection. Please use: SET CONNECTION command.");
		}
		// check for current alias
		if (globalEnv.currentAlias.isEmpty()) {
			return new ObjError("No table is open in the current work area.");
		}		
		String aliasName = globalEnv.currentAlias;
		ObjTable objTable = globalEnv.workArea.get(aliasName);
		if (objTable == null) {
			return new ObjError("Alias '" + aliasName + "' not found.");
		}
		// save data
		if (!objTable.tableUpdate()) {
			return new ObjError("could not update the table '" + aliasName + "'");
		}

		return new ObjBoolean(true);
	}
}
