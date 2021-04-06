package v1;

public class AstBinOp extends Ast {
	public Ast left;
	public TokenType op;
	public Ast right;
	
	public AstBinOp(Ast left, TokenType op, Ast right) {
		this.left = left;
		this.op = op;
		this.right = right;
	}
}
