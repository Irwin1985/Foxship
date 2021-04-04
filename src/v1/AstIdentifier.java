package v1;

public class AstIdentifier extends Ast {
	public Token token;
	public String name;
	
	public AstIdentifier(Token token) {
		this.token = token;
		this.name = token.lexeme;
	}
}
