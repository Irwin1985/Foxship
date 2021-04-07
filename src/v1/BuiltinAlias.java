package v1;
import java.util.List;
public class BuiltinAlias implements BuiltinBase {
	/**
	 * Alias() get the current alias.
	 */
	public Obj execute(List<Obj> objArgs, GlobalEnv globalEnv) {
		return new ObjString(globalEnv.currentAlias);
	}
}
