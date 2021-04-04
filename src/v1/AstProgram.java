package v1;
import java.util.List;

public class AstProgram extends Ast {
	List<Ast> commands;
	
	public AstProgram() {}
	
	public AstProgram(List<Ast> commands){
		this.commands = commands;
	}
}
