package v1;
import java.util.Map;
import java.util.HashMap;

public class Environment {
	private Environment parent;
	private Map<String, Obj> symbolTable = new HashMap<String, Obj>();

	public Environment() {}
	public Environment(Environment parent) {
		this.parent = parent;
	}
	
	public void set(String name, Obj value) {
		symbolTable.put(name, value);
	}
	
	public Obj get(String name) {
		Obj value = symbolTable.get(name);
		if (value == null && parent != null) {
			value = parent.get(name);
		}
		return value;
	}
	
	public void remove(String name) {
		symbolTable.remove(name);
	}
}
