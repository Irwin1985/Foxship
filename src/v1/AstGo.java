package v1;

public class AstGo extends Ast {
	public Ast astRow = null;
	public Ast astAlias = null;
	
	public AstGo() {}
	
	public AstGo(Ast astRow, Ast astAlias) {
		this.astRow = astRow;
		this.astAlias = astAlias;
	}
}
