package v1;

public class AstFunctionDeclaration extends Ast {
	public Ast[] parameters;
	
	public AstFunctionDeclaration(Ast[] parameters) {
		this.parameters = parameters;
	}
}
