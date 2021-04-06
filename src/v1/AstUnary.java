package v1;

public class AstUnary extends Ast {
	public TokenType op;
	public Ast right = null;
	
	public AstUnary(TokenType op, Ast right) {
		this.op = op;
		this.right = right;
	}
}
