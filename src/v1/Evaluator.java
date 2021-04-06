package v1;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Evaluator {
	public GlobalEnv globalEnv;
	
	private final ObjBoolean TRUE = new ObjBoolean(true);
	private final ObjBoolean FALSE = new ObjBoolean(false);
	private final ObjNull NULL = new ObjNull();
	
	public Obj Eval(Ast node, Environment env) {
		if (node instanceof AstProgram) {
			return evalProgram((AstProgram)node, env);
		}
		else if (node instanceof AstConnection) {
			return evalAstConnection((AstConnection)node, env);
		}
		else if (node instanceof AstSetConnection) {
			return evalSetConnection((AstSetConnection)node, env);
		}
		else if (node instanceof AstCloseConnection) {
			return evalCloseConnection((AstCloseConnection)node, env);
		}
		else if (node instanceof AstUseTable) {
			return evalUseTable((AstUseTable)node, env);
		}
		else if (node instanceof AstUseIn) {
			return evalUseIn((AstUseIn)node, env);
		}
		else if (node instanceof AstSelectTable) {
			return evalSelectTable((AstSelectTable)node, env);
		}
		else if (node instanceof AstBrowse) {
			return evalBrowse((AstBrowse)node, env);
		}
		else if (node instanceof AstBoolean) {
			return ((AstBoolean)node).value ? TRUE : FALSE;
		}
		else if (node instanceof AstString) {
			return new ObjString(((AstString)node).value);
		}
		else if (node instanceof AstNull) {
			return new ObjNull();
		}
		else if (node instanceof AstBinOp) {
			return evalBinOp((AstBinOp)node, env);
		}
		else if (node instanceof AstUnary) {
			return evalUnary((AstUnary)node, env);
		}
		else if (node instanceof AstNumber) {
			return new ObjNumber(((AstNumber)node).value);
		}
		else if (node instanceof AstMessagebox) {
			return evalMessagebox((AstMessagebox)node, env);
		}
		else if (node instanceof AstGo) {
			return evalGo((AstGo)node, env);
		}
		else if (node instanceof AstIdentifier) {
			return evalIdentifier((AstIdentifier)node, env);
		}
		else if (node instanceof AstAssignment) {
			return evalAssignment((AstAssignment)node, env);
		}
		return null;
	}
	/***********************************************************************
	 *  IMPLEMENTATION BEGINS
	 ***********************************************************************/
	private Obj evalProgram(AstProgram program, Environment env) {
		Obj result = null;
		for (Ast command : program.commands) {
			result = Eval(command, env);
			if (result != null) {				
				if (result.type() == ObjType.ERROR_OBJ) {
					return result;
				}
			}
		}
		return result;
	}
	private Obj evalAssignment(AstAssignment astAssign, Environment env) {
		String identifier = "";
		if (!(astAssign.astIdent instanceof AstIdentifier)) {
			return new ObjError("Left hand of assignment shuld be an identifier");
		}
		identifier = ((AstIdentifier)astAssign.astIdent).value;
		Obj objValue = Eval(astAssign.astValue, env);
		if (isError(objValue)) {
			return objValue;
		}
		// register the symbol
		env.set(identifier, objValue);		
		return objValue;
	}
	private Obj evalIdentifier(AstIdentifier ident, Environment env) {
		// search in symbol table
		Obj objValue = env.get(ident.value);
		if (objValue == null) {
			return new ObjError("Variable '" + ident.value + "' is not found.");
		}
		return objValue;
	}
	private Obj evalGo(AstGo astGo, Environment env) {
		Obj objRow = Eval(astGo.astRow, env);
		String aliasName = "";
		if (isError(objRow)) {
			return objRow;
		}
		if (objRow.type() != ObjType.NUMBER_OBJ) {
			return new ObjError("row number must be a type of INTEGER.");
		}
		if (astGo.astAlias != null) {
			Obj objAlias = Eval(astGo.astAlias, env);
			if (isError(objAlias)) {
				return objAlias;
			}
			if (objAlias.type() != ObjType.STRING_OBJ) {
				return new ObjError("Alias name must be a type of STRING.");
			}
			aliasName = objAlias.inspect();
		} else {
			aliasName = globalEnv.currentAlias;
		}
		// check for current connection
		if (globalEnv.currentConn == null) {
			return new ObjError("there is not an active connection. Please use: SET CONNECTION command.");
		}
		// search for the alias
		Obj objResult = env.get(aliasName);
		if (objResult == null) {
			return new ObjError("Alias '" + aliasName + "' is not found.");
		}
		
		// get the table
		ObjTable objTable = (ObjTable)objResult;
		if (!objTable.goRecno((int)((ObjNumber)objRow).value)) {
			return runTimeError();
		}

		return null;		
	}
	private Obj evalUseTable(AstUseTable useTable, Environment env) {
		// check for current connection
		if (globalEnv.currentConn == null) {
			return new ObjError("there is not an active connection. Please use: SET CONNECTION command.");
		}

		// check for alias in use
		Obj value = env.get(useTable.alias);
		if (value != null && value.type() == ObjType.TABLE_OBJ) {
			return new ObjError("alias in use: " + useTable.alias);
		}
		// create the objTable
		ObjTable objTable = new ObjTable();
		objTable.alias = useTable.alias;			
		objTable.filter = useTable.filter;
		objTable.name = useTable.name;
		objTable.noData = useTable.noData;
		objTable.noUpdate = useTable.noUpdate;

		// open table
		if (!objTable.openTable(globalEnv.currentConn)) {
			return runTimeError();
		}
		
		// update current alias
		globalEnv.currentAlias = useTable.alias;
		
		// register in symbol table
		env.set(objTable.alias, objTable);
		
		return objTable;		
	}
	private Obj evalAstConnection(AstConnection astConn, Environment env) {
		ObjConnection objConn = new ObjConnection();
		// fill properties
		objConn.conId = astConn.conId;
		objConn.dataBase = astConn.dataBase;
		objConn.engine = astConn.engine;
		objConn.password = astConn.password;
		objConn.port = astConn.port;
		objConn.server = astConn.server;
		objConn.user = astConn.user;
		// register object in symbol table.
		env.set(astConn.conId, objConn);
		
		return objConn; // return the object		
	}
	private Obj evalSetConnection(AstSetConnection astSetConn, Environment env) {
		// Find the symbol
		Obj value = env.get(astSetConn.name);
		
		if (value == null) {
			return new ObjError("connection not found: " + astSetConn.name);
		}
		
		// Create the connection
		ObjConnection objConn = (ObjConnection)value;
		if (!objConn.createConnection()) {
			return runTimeError();
		}
		
		// Set the active connection
		globalEnv.currentConn = objConn.conn;
		
		// Return the updated objConn to SymbolTable
		env.set(astSetConn.name, objConn);
		
		return objConn;		
	}
	private Obj evalCloseConnection(AstCloseConnection astCloseConn, Environment env) {
		// Find the symbol
		Obj value = env.get(astCloseConn.name);
		
		if (value == null) {
			return new ObjError("connection not found: " + astCloseConn.name);
		}
		
		ObjConnection objConn = (ObjConnection)value;
		objConn.conn = null;
		
		// close from globalenv
		globalEnv.currentConn = null;
		
		// finally remove from environment
		env.remove(astCloseConn.name);

		return null; // nothing to return
	}
	
	private Obj evalUseIn(AstUseIn useIn, Environment env) {
		// search for alias name
		Obj value = env.get(useIn.alias);
		if (value == null) {
			return new ObjError("Alias '" + useIn.alias + "' is not found.");
		}
		// delete the alias object
		value = null;
		// delete from symboltable
		env.remove(useIn.alias);
		
		return null;
	}

	private Obj evalSelectTable(AstSelectTable astSel, Environment env) {
		// search for alias name
		Obj value = env.get(astSel.name);
		if (value == null) {
			return new ObjError("Alias '" + astSel.name + "' is not found.");
		}

		// update the current alias
		globalEnv.currentAlias = astSel.name;
		
		return null;
	}
	
	private Obj evalBrowse(AstBrowse astBrowse, Environment env) {
		if (globalEnv.currentAlias.isEmpty()) {
			return new ObjError("No table is open in the current work area.");
		}
		// search for alias name
		Obj value = env.get(globalEnv.currentAlias);
		if (value == null) {
			return new ObjError("Alias '" + globalEnv.currentAlias + "' is not found.");
		}
		ObjTable objTable = (ObjTable)value;
		String title = astBrowse.title.isEmpty() ? objTable.alias : astBrowse.title; 
		// hit the browse
		try {
			BrowseWindow browse = new BrowseWindow(objTable.cursor, title);
		} catch (Exception e) {
			return new ObjError("Runtime Error: " + e.getMessage());
		}
		
		return null;
	}
	private Obj evalUnary(AstUnary astUnary, Environment env) {
		Obj rightObj = Eval(astUnary.right, env);
		if (isError(rightObj)) {
			return rightObj;
		}
		
		if (astUnary.op == TokenType.MINUS) {			
			if (rightObj.type() != ObjType.NUMBER_OBJ) {
				return new ObjError("Invalid operand for the operator '-'.");
			}
			return new ObjNumber(((ObjNumber)rightObj).value * -1);						
		}
		else if(astUnary.op == TokenType.NOT) {
			if (rightObj.type() != ObjType.BOOL_OBJ) {
				return new ObjError("Invalid operand for the operator '!'.");
			}
			return new ObjBoolean(!((ObjBoolean)rightObj).value);									
		}
		return null;
	}
	private Obj evalBinOp(AstBinOp astBinOp, Environment env) {
		if (astBinOp.op == TokenType.AND || astBinOp.op == TokenType.OR) {
			return evalLogicalExpression(astBinOp, env);
		}
		Obj leftOp = Eval(astBinOp.left, env);
		if (isError(leftOp)) {
			return leftOp;
		}
		Obj rightOp = Eval(astBinOp.right, env);
		if (isError(rightOp)) {
			return rightOp;
		}
		if (astBinOp.op == TokenType.LESS ||
			astBinOp.op == TokenType.LESS_EQ ||
			astBinOp.op == TokenType.GREATER ||
			astBinOp.op == TokenType.GREATER_EQ ||
			astBinOp.op == TokenType.EQUAL ||
			astBinOp.op == TokenType.NOT_EQ) 
		{
			return evalRelationalExpression(leftOp, astBinOp.op, rightOp);
		}
		else if (astBinOp.op == TokenType.PLUS ||
				astBinOp.op == TokenType.MINUS ||
				astBinOp.op == TokenType.MUL ||
				astBinOp.op == TokenType.DIV)
		{
			return evalArithmeticExpression(leftOp, astBinOp.op, rightOp);
		}
		return null;
	}
	private Obj evalArithmeticExpression(Obj leftObj, TokenType typeOp, Obj rightObj) {
		if (leftObj.type() != ObjType.NUMBER_OBJ || rightObj.type() != ObjType.NUMBER_OBJ) {
			return new ObjError("Invalid operand for comparison expression");
		}
		ObjNumber left = (ObjNumber)leftObj;
		ObjNumber right = (ObjNumber)rightObj;
		switch (typeOp) {
		case PLUS:
			return new ObjNumber(left.value + right.value);
		case MINUS:
			return new ObjNumber(left.value - right.value);
		case MUL:
			return new ObjNumber(left.value * right.value);
		case DIV:
			if (right.value == 0) {
				return new ObjError("division by zero.");
			}
			return new ObjNumber(left.value / right.value);
		default:
			return new ObjError("unknown ");
		}
	}	
	private Obj evalRelationalExpression(Obj leftObj, TokenType typeOp, Obj rightObj) {
		if (leftObj.type() != ObjType.NUMBER_OBJ || rightObj.type() != ObjType.NUMBER_OBJ) {
			return new ObjError("Invalid operand for comparison expression");
		}
		ObjNumber left = (ObjNumber)leftObj;
		ObjNumber right = (ObjNumber)rightObj;
		switch (typeOp) {
		case LESS:
			return new ObjBoolean(left.value < right.value);
		case LESS_EQ:
			return new ObjBoolean(left.value <= right.value);
		case GREATER:
			return new ObjBoolean(left.value > right.value);
		case GREATER_EQ:
			return new ObjBoolean(left.value >= right.value);
		case EQUAL:
			return new ObjBoolean(left.value == right.value);
		case NOT_EQ:
			return new ObjBoolean(left.value != right.value);
		default:
			return new ObjError("unknown ");
		}
	}
	private Obj evalLogicalExpression(AstBinOp astBinOp, Environment env) {
		Obj left_obj = Eval(astBinOp.left, env);
		if (isError(left_obj)) {
			return left_obj;
		}		
		if (left_obj.type() != ObjType.BOOL_OBJ) {
			return new ObjError("Invalid operator for logical expression");
		}
		ObjBoolean left = (ObjBoolean)left_obj;
		if (astBinOp.op == TokenType.AND) {
			if (!left.value) {
				return FALSE;
			} else {
				return evalRightLogicalOperand(astBinOp.right, env);
			}
		}
		else if (astBinOp.op == TokenType.OR) {
			if (left.value) {
				return new ObjBoolean(true);
			} else {
				return evalRightLogicalOperand(astBinOp.right, env);				
			}
		}
		return null;
	}
	private Obj evalRightLogicalOperand(Ast astRight, Environment env) {
		Obj right_obj = Eval(astRight, env);
		if (isError(right_obj)) {
			return right_obj;
		}
		if (right_obj.type() != ObjType.BOOL_OBJ) {
			return new ObjError("Invalid operator for logical expression");
		}
		return ((ObjBoolean)right_obj).value ? TRUE : FALSE;
	}
	private Obj evalMessagebox(AstMessagebox astMbox, Environment env) {
		String content = "", title = "Foxship";
		int buttonType = 0, timeOut = 0;
		
		Obj objContent = Eval(astMbox.content, env);
		if (isError(objContent)) {
			return objContent;
		}
		content = objContent.inspect();
		
		Obj objTitle = Eval(astMbox.title, env);
		if (isError(objTitle)) {
			return objTitle;
		}
		if (objTitle != null) {			
			title = objTitle.inspect();
		}
		
		Obj objButtons = Eval(astMbox.buttons, env);
		if (isError(objButtons)) {
			return objButtons;
		}
		if (objButtons != null) {			
			if (objButtons.type() != ObjType.NUMBER_OBJ) {
				return new ObjError("button type in messagebox must be an INTEGER.");
			}
			buttonType = (int)((ObjNumber)objButtons).value;
		}
		
		Obj objTimeout = Eval(astMbox.timeout, env);
		if (isError(objTimeout)) {
			return objTimeout;
		}
		if (objTimeout != null) {			
			if (objTimeout.type() != ObjType.NUMBER_OBJ) {
				return new ObjError("timeout parameter in messagebox must be an INTEGER.");
			}
			timeOut = (int)((ObjNumber)objTimeout).value;
		}	
		// parse buttons and show message
		int result = 0, nReturn = 1;
		switch(buttonType) {
		case 16:
		{
			String[] opt = {"Aceptar"};
			result = showMessagebox(content, title, opt, JOptionPane.ERROR_MESSAGE);
		}
			break;
		case 17:{			
			String[] opt = {"Aceptar", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.ERROR_MESSAGE);
		}
			break;
		case 18:{
			String[] opt = {"Abortar", "Reintentar", "Ignorar"};
			result = showMessagebox(content, title, opt, JOptionPane.ERROR_MESSAGE);			
		}
			break;
		case 19:{
			String[] opt = {"Sí", "No", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.ERROR_MESSAGE);			
		}
			break;
		case 20:{
			String[] opt = {"Sí", "No"};
			result = showMessagebox(content, title, opt, JOptionPane.ERROR_MESSAGE);
		}
			break;
		case 21:{
			String[] opt = {"Reintentar", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.ERROR_MESSAGE);			
		}
			break;
		case 32:{
			String[] opt = {"Aceptar"};
			result = showMessagebox(content, title, opt, JOptionPane.QUESTION_MESSAGE);
		}
		break;
		case 33:{
			String[] opt = {"Aceptar", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.QUESTION_MESSAGE);
		}
		break;
		case 34:{
			String[] opt = {"Abortar", "Reintentar", "Ignorar"};
			result = showMessagebox(content, title, opt, JOptionPane.QUESTION_MESSAGE);
		}
		break;
		case 35:{
			String[] opt = {"Sí", "No", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.QUESTION_MESSAGE);
		}
		break;
		case 36:{
			String[] opt = {"Sí", "No"};
			result = showMessagebox(content, title, opt, JOptionPane.QUESTION_MESSAGE);
		}
		break;
		case 37:{
			String[] opt = {"Reintentar", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.QUESTION_MESSAGE);
		}		
		break;
		case 48:{
			String[] opt = {"Aceptar"};
			result = showMessagebox(content, title, opt, JOptionPane.WARNING_MESSAGE);
		}
		break;
		case 49:{
			String[] opt = {"Aceptar", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.WARNING_MESSAGE);
		}
		break;
		case 50:{
			String[] opt = {"Abortar", "Reintentar", "Ignorar"};
			result = showMessagebox(content, title, opt, JOptionPane.WARNING_MESSAGE);
		}
		break;
		case 51:{
			String[] opt = {"Sí", "No", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.WARNING_MESSAGE);
		}
		break;
		case 52:{
			String[] opt = {"Sí", "No"};
			result = showMessagebox(content, title, opt, JOptionPane.WARNING_MESSAGE);
		}
		break;
		case 53:{
			String[] opt = {"Reintentar", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.WARNING_MESSAGE);
		}
		break;		
		case 64:{
			String[] opt = {"Aceptar"};
			result = showMessagebox(content, title, opt, JOptionPane.INFORMATION_MESSAGE);
		}
		break;
		case 65:{
			String[] opt = {"Aceptar", "Calcelar"};
			result = showMessagebox(content, title, opt, JOptionPane.INFORMATION_MESSAGE);
		}
		break;
		case 66:{
			String[] opt = {"Abortar", "Reintentar", "Ignorar"};
			result = showMessagebox(content, title, opt, JOptionPane.INFORMATION_MESSAGE);
		}
		break;
		case 67:{
			String[] opt = {"Sí", "No", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.INFORMATION_MESSAGE);
		}
		break;
		case 68:{
			String[] opt = {"Sí", "No"};
			result = showMessagebox(content, title, opt, JOptionPane.INFORMATION_MESSAGE);
		}
		break;
		case 69:{
			String[] opt = {"Reintentar", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.INFORMATION_MESSAGE);
		}
		break;
		default:
			String[] opt = {"Aceptar"};
			result = showMessagebox(content, title, opt, JOptionPane.INFORMATION_MESSAGE);			
		}
		int[] opt1 = {0,16,32,48,64};
		int[] opt2 = {17, 33, 49, 65, (1 + 16 + 256), (1 + 32 + 256), (1 + 48 + 256), (1 + 64 + 256)};
		int[] opt3 = {18, 34, 50, 66, (2 + 16 + 256), (2 + 16 + 512), (2 + 32 + 256), (2 + 32 + 512), (2 + 48 + 256), (2 + 48 + 512), (2 + 64 + 256), (2 + 64 + 512)};
		int[] opt4 = {19, 35, 51, 67, (3 + 16 + 256), (3 + 16 + 512), (3 + 32 + 256), (3 + 32 + 512), (3 + 48 + 256), (3 + 48 + 512), (3 + 64 + 256), (3 + 64 + 512)};
		int[] opt5 = {20, 36, 52, 68, (4 + 16 + 256), (4 + 16 + 512), (4 + 32 + 256), (4 + 32 + 512), (4 + 48 + 256), (4 + 48 + 512), (4 + 64 + 256), (4 + 64 + 512)};
		int[] opt6 = {21, 37, 53, 69, (5 + 16 + 256), (5 + 16 + 512), (5 + 32 + 256), (5 + 32 + 512), (5 + 48 + 256), (5 + 48 + 512), (5 + 64 + 256), (5 + 64 + 512)};

		if (isInIntegerList(buttonType, opt1)) {
			return new ObjNumber(1);
		}
		if (isInIntegerList(buttonType, opt2)) {
			nReturn = result == 1 ? 1 : 2;
		}
		if (isInIntegerList(buttonType, opt3)) {
			if (result == 0) {
				nReturn = 3;
			} else if (result == 1) {
				nReturn = 4;
			} else if (result == 2) {
				nReturn = 5;
			}
		}
		if (isInIntegerList(buttonType, opt4)) {
			if (result == 0) {
				nReturn = 6;
			} else if (result == 1) {
				nReturn = 7;
			} else if (result == 2) {
				nReturn = 2;
			}
		}
		if (isInIntegerList(buttonType, opt5)) {
			if (result == 0) {
				nReturn = 6;
			} else if (result == 1) {
				nReturn = 7;
			}
		}
		if (isInIntegerList(buttonType, opt6)) {
			if (result == 0) {
				nReturn = 4;
			} else if (result == 1) {
				nReturn = 2;
			}
		}
		return new ObjNumber(nReturn);
	}
	private boolean isInIntegerList(int compareFrom, int...numbers) {
		for (int number : numbers) {
			if (number == compareFrom) {
				return true;
			}
		}
		return false;
	}
	private int showMessagebox(String content, String title, String[] choices, int buttons) {
		JFrame jf=new JFrame();
		jf.setAlwaysOnTop(true);
		
		int response = JOptionPane.showOptionDialog(
				jf,
                content,
                title,
                JOptionPane.CANCEL_OPTION,
                buttons,
                null,
                choices,
                "None");	
		return response;
	}
	private ObjError runTimeError() {
		return new ObjError("Runtime Error");
	}
	private boolean isError(Obj result) {
		return result != null && result.type() == ObjType.ERROR_OBJ;
	}
}