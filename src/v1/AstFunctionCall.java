package v1;
import java.util.List;

public class AstFunctionCall extends Ast {
	public Ast astName;
	public List<Ast> arguments;

	public AstFunctionCall(Ast astName, List<Ast> arguments) {
		this.astName = astName;
		this.arguments = arguments;
	}
}
