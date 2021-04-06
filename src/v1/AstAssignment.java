package v1;

public class AstAssignment extends Ast {
	public Ast astIdent = null;
	public Ast astValue = null;
	
	public AstAssignment() {}
	
	public AstAssignment(Ast astIdent, Ast astValue) {
		this.astIdent = astIdent;
		this.astValue = astValue;
	}
}
