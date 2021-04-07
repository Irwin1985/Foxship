package v1;
import java.util.List;

public interface BuiltinBase {
	public Obj execute(List<Obj> objArgs, GlobalEnv globalEnv);
}
