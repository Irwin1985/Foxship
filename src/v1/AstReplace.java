package v1;
import java.util.List;
import java.util.ArrayList;

public class AstReplace extends Ast {
	public List<AstKeyValuePair> replacements = new ArrayList<AstKeyValuePair>();
	public Ast astCondition = null;
	public Ast astAliasName = null;
	
	public AstReplace() {
		
	}
}
