package v1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Evaluator {
	public GlobalEnv globalEnv;
	
	private final ObjBoolean TRUE = new ObjBoolean(true);
	private final ObjBoolean FALSE = new ObjBoolean(false);
	private final ObjNull NULL = new ObjNull();
	private final String MSG_CONNECTION_NOT_EXISTS = "there is not an active connection. Please use: SET CONNECTION command.";
	private final String MSG_NOT_OPEN_TABLE = "No table is open in the current work area.";
	private HashMap<String, ObjBuiltin> builtins;

	public Evaluator() {
		// fill builtins
		builtins = new HashMap<String, ObjBuiltin>();
		builtins.put("set", new ObjBuiltin(new BuiltinSet()));
		builtins.put("alias", new ObjBuiltin(new BuiltinAlias()));
		builtins.put("messagebox", new ObjBuiltin(new BuiltinMessagebox()));
		builtins.put("reccount", new ObjBuiltin(new BuiltinReccount()));
		builtins.put("tableupdate", new ObjBuiltin(new BuiltinTableUpdate()));
	}
	
	public Obj Eval(Ast node, Environment env) {
		if (node instanceof AstProgram) {
			return evalProgram((AstProgram)node, env);
		}
		else if (node instanceof AstConnection) {
			return evalAstConnection((AstConnection)node, env);
		}
		else if (node instanceof AstConnectionFromFile) {
			return evalConnectionFromFile((AstConnectionFromFile)node, env);
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
		else if (node instanceof AstGo) {
			return evalGo((AstGo)node, env);
		}
		else if (node instanceof AstIdentifier) {
			return evalIdentifier((AstIdentifier)node, env);
		}
		else if (node instanceof AstAssignment) {
			return evalAssignment((AstAssignment)node, env);
		}
		else if (node instanceof AstExportConnection) {
			return evalExportConnection((AstExportConnection)node, env);
		}
		else if (node instanceof AstFunctionCall) {
			return evalFunctionCall((AstFunctionCall) node, env);			
		}
		else if (node instanceof AstAppendBlank) {
			return evalAppendBlank((AstAppendBlank)node, env);
		}
		else if (node instanceof AstReplace) {
			return evalReplace((AstReplace) node, env);
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
	private Obj evalReplace(AstReplace replace, Environment env) {
		// check for current connection
		if (globalEnv.currentConnection == null) {
			return new ObjError(MSG_CONNECTION_NOT_EXISTS);
		}
		// get the alias name
		String aliasName = globalEnv.currentAlias;
		if (replace.astAliasName != null) {
			if (replace.astAliasName instanceof AstIdentifier) {
				aliasName = ((AstIdentifier)replace.astAliasName).value;
			} else {				
				Obj objResult = Eval(replace.astAliasName, env);
				if (isError(objResult)) {
					return objResult;
				}
				if (objResult == null || objResult.type() != ObjType.STRING_OBJ) {
					return new ObjError("Invalid type for Alias name");
				}
				aliasName = objResult.inspect();
			}
		}
		if (aliasName.isEmpty()) {
			return new ObjError("No table is open in the current work area");
		}
		// request for alias in work area
		ObjTable objTable = globalEnv.workArea.get(aliasName);
		if (objTable == null) {
			return new ObjError("Alias '" + aliasName + "' is not found.");
		}
		
		// check for valid field
		String fieldName = "";
		if (replace.astField instanceof AstIdentifier) {
			fieldName = ((AstIdentifier)replace.astField).value;
		} else {
			Obj objResult = Eval(replace.astField, env);
			if (isError(objResult)) {
				return objResult;
			}
			if (objResult == null || objResult.type() != ObjType.STRING_OBJ) {
				return new ObjError("Invalid type for field name");
			}
			fieldName = objResult.inspect();			
		}
		
		// evaluate the field value
		Obj fieldValue = Eval(replace.astFieldValue, env);
		if (isError(fieldValue)) {
			return fieldValue;
		}
		
		// call the replace method
		if (!objTable.replace(fieldName, fieldValue.inspect(), null)) {
			return runTimeError();
		}
		return TRUE;
	}
	private Obj evalAppendBlank(AstAppendBlank appendBlank, Environment env) {
		// check for current connection
		if (globalEnv.currentConnection == null) {
			return new ObjError(MSG_CONNECTION_NOT_EXISTS);
		}
		String aliasName = globalEnv.currentAlias;
		if (appendBlank.aliasName != null) {
			if (appendBlank.aliasName instanceof AstIdentifier) {
				aliasName = ((AstIdentifier)appendBlank.aliasName).value;
			} else {				
				Obj objResult = Eval(appendBlank.aliasName, env);
				if (isError(objResult)) {
					return objResult;
				}
				if (objResult == null) {
					return new ObjError("Invalid type for Alias name");
				}
				aliasName = objResult.inspect();
			}
		}
		// request for alias in work area
		ObjTable objTable = globalEnv.workArea.get(aliasName);
		if (objTable == null) {
			return new ObjError("Alias '" + aliasName + "' is not found.");
		}
		if (!objTable.appendBlank()) {
			return new ObjError("Could not append a new record in alias '" + aliasName + "'");
		}
		return TRUE;
	}
	private Obj evalFunctionCall(AstFunctionCall astFunction, Environment env) {
		// eval the name ast
		Obj objResult = null;
		String functionName = "";
		if (astFunction.astName instanceof AstIdentifier) {
			functionName = ((AstIdentifier)astFunction.astName).value;
		} else {			
			objResult = Eval(astFunction.astName, env);
			if (isError(objResult)) {
				return objResult;
			}
			if (objResult == null || objResult.type() != ObjType.STRING_OBJ) {
				return new ObjError("invalid data type for function name.");
			}
			// check for registered function
			functionName = ((ObjString)objResult).value; 
		}
		objResult = env.get(functionName.toLowerCase());
		if (objResult == null) {
			// check for builtin function
			objResult = builtins.get(functionName.toLowerCase());
			if (objResult == null) {
				return new ObjError("function not found: '" + functionName + "'");
			}
		}
		if (objResult.type() == ObjType.FUNCTION_OBJ) {
			// TODO: implement user defined function here.
			
		}
		else if (objResult.type() == ObjType.BUILTIN_OBJ) {
			ObjBuiltin objBuiltin = (ObjBuiltin)objResult;
			List<Obj> objArgs = new ArrayList<Obj>();
			if (astFunction.arguments != null) {
				// eval arguments
				for (Ast argument : astFunction.arguments) {
					objResult = Eval(argument, env);
					if (isError(objResult)) {
						return objResult;
					}
					objArgs.add(objResult);
				}
			}
			// execute builtin function
			return objBuiltin.functionName.execute(objArgs, globalEnv);
		}
		return TRUE;
	}
	private Obj evalConnectionFromFile(AstConnectionFromFile astCon, Environment env) {
		Obj objResult = Eval(astCon.astFile, env);
		if (isError(objResult)) {
			return objResult;
		}
		if (objResult == null) {
			return new ObjError("Invalid file path or name.");
		}
		if (objResult.type() != ObjType.STRING_OBJ) {
			return new ObjError("Invalid data type for file path.");
		}
		
		try {
			File fileCheck = new File(objResult.inspect());
			if (!fileCheck.exists()) {
				return new ObjError("File does not exists: '" + objResult.inspect() + "'");
			}
			String content = Files.readString(Path.of(objResult.inspect()));
			// parse the content
			Tokenizer tokenizer = new Tokenizer(content);
			Parser parser = new Parser(tokenizer);
			AstProgram astProgram = new AstProgram();
			astProgram.commands = parser.parseCommand();
			// eval the content
			Obj objValue = Eval(astProgram, env);
			if (isError(objValue)) {
				return objValue;
			}
			if (objValue == null) {
				return new ObjError("Runtime error: invalid object result");
			}
			if (objValue.type() != ObjType.CONN_OBJ) {
				return new ObjError("Runtime error: object is not a connection type.");
			}
			return objValue;
		} catch (IOException e) {
			return new ObjError("Runtime Error: " + e.getMessage());
		}
	}
	private Obj evalExportConnection(AstExportConnection astExport, Environment env) {
		Obj objOutput = Eval(astExport.fileName, env);
		if (isError(objOutput)) {
			return objOutput;
		}
		if (objOutput.type() != ObjType.STRING_OBJ) {
			return new ObjError("invalid filename");
		}
		if (globalEnv.currentConnectionName.isEmpty()) {
			return new ObjError(MSG_CONNECTION_NOT_EXISTS);
		}
		// get the connection data
		Obj objResult = env.get(globalEnv.currentConnectionName.toLowerCase());
		if (objResult == null) {
			return new ObjError("Connection '"+  globalEnv.currentConnectionName +"' does not exist.");
		}
		if (objResult.type() != ObjType.CONN_OBJ) {
			return new ObjError("Identifier '" + globalEnv.currentConnectionName + "' is not a connection object.");
		}
		ObjConnection objConn = (ObjConnection)objResult;
		StringBuilder output = new StringBuilder();
		//CREATE CONNECTION MyCon ENGINE "MySQL" SERVER "localhost" PORT "3306" DATABASE "classicmodels" USER "root" PASSWORD "1234"
		output.append("CREATE CONNECTION ");
		output.append(objConn.conId);
		output.append(" ENGINE \"" + objConn.engineToString() + "\"");
		output.append(" SERVER \"" + objConn.server + "\"");
		output.append(" PORT \"" + objConn.port + "\"");
		output.append(" DATABASE \"" + objConn.dataBase + "\"");
		output.append(" USER \"" + objConn.user + "\"");
		output.append(" PASSWORD \"" + objConn.password + "\"");
		// generate file
		try {
			FileWriter file = new FileWriter(objOutput.inspect());
			file.write(output.toString());
			file.close();
			return TRUE;
		} catch (Exception e) {
			return new ObjError("Runtime error: " + e.getMessage());
		}
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
		env.set(identifier.toLowerCase(), objValue);		
		return objValue;
	}
	private Obj evalIdentifier(AstIdentifier ident, Environment env) {
		// search in current alias in workarea
		if (globalEnv.currentConnection != null && !globalEnv.currentAlias.isEmpty()) {
			ObjTable objTable = globalEnv.workArea.get(globalEnv.currentAlias.toLowerCase());
			if (objTable != null && objTable.type() == ObjType.TABLE_OBJ) {
				// search identifier in resultset columns
				Object result = objTable.findColumn(ident.value); 
				if (result != null) {
					return new ObjGeneric(result);
				}
			}
		}
		// search in symbol table
		Obj objValue = env.get(ident.value.toLowerCase());
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
		if (globalEnv.currentConnection == null) {
			return new ObjError(MSG_CONNECTION_NOT_EXISTS);
		}
		// search for the alias
		Obj objResult = globalEnv.workArea.get(aliasName.toLowerCase());
		if (objResult == null) {
			return new ObjError("Alias '" + aliasName + "' is not found.");
		}
		
		// get the table
		ObjTable objTable = (ObjTable)objResult;
		if (!objTable.goRecno((int)((ObjNumber)objRow).value)) {
			return runTimeError();
		}

		return NULL;		
	}
	private Obj evalUseTable(AstUseTable useTable, Environment env) {
		// check for current connection
		if (globalEnv.currentConnection == null) {
			return new ObjError(MSG_CONNECTION_NOT_EXISTS);
		}

		Obj objResult = null;
		// evaluate the alias name
		String aliasName = "";
		if (useTable.alias instanceof AstString) {
			aliasName = ((AstString)useTable.alias).value;
		} else {			
			objResult = Eval(useTable.alias, env);		
			if (isError(objResult)) {
				return objResult;
			}
			if (objResult == null || objResult.type() != ObjType.STRING_OBJ) {
				return new ObjError("invalid name or data type for alias");
			}
			aliasName = objResult.inspect();
		}
		
		// check for alias in use
		Obj value = globalEnv.workArea.get(aliasName.toLowerCase());
		if (value != null && value.type() == ObjType.TABLE_OBJ) {
			return new ObjError("Alias is in use: '" + aliasName + "'");
		}
		// create the objTable
		ObjTable objTable = new ObjTable();
		objTable.alias = aliasName;
		
		// filter parameter
		if (useTable.filter != null) {			
			objResult = Eval(useTable.filter, env);
			if (isError(objResult)) {
				return objResult;
			}
			if (objResult.type() != ObjType.STRING_OBJ) {				
				objTable.filter = "";
			} else {
				objTable.filter = objResult.inspect();
			}
		}
		// name parameter
		if (useTable.name != null) {
			if (useTable.name instanceof AstString) {
				objTable.name = ((AstString)useTable.name).value;
			} else {				
				objResult = Eval(useTable.name, env);
				if (isError(objResult)) {
					return objResult;
				}
				if (objResult.type() != ObjType.STRING_OBJ) {				
					objTable.name = "";
				} else {
					objTable.name = objResult.inspect();
				}
			}
		}
		if (useTable.noData != null) {			
			objTable.noData = ((AstBoolean)useTable.noData).value;
		}
		if (useTable.noUpdate != null) {
			objTable.noUpdate = ((AstBoolean)useTable.noUpdate).value;
		}

		// open table
		if (!objTable.requery(globalEnv.currentConnection)) {
			return runTimeError();
		}
		
		// update current alias
		globalEnv.currentAlias = aliasName;
		
		// register in symbol table
		globalEnv.workArea.put(objTable.alias.toLowerCase(), objTable);		
		
		return objTable;		
	}
	private Obj evalAstConnection(AstConnection astConn, Environment env) {
		ObjConnection objConn = new ObjConnection();

		// fill properties
		Obj objResult = null;
		// connection name
		objConn.conId = astConn.conId;
		// Database
		objResult = Eval(astConn.dataBase, env);		
		if (isError(objResult)) {
			return objResult;
		}
		if (objResult == null || objResult.type() != ObjType.STRING_OBJ) {
			return new ObjError("invalid data type for DATABASE command.");
		}
		objConn.dataBase = objResult.inspect();
		// Engine
		objResult = Eval(astConn.engine, env);		
		if (isError(objResult)) {
			return objResult;
		}
		if (objResult == null || objResult.type() != ObjType.STRING_OBJ) {
			return new ObjError("invalid data type for ENGINE command.");
		}		
		if (objResult.inspect().toLowerCase().equals("mysql")) {			
			objConn.engine = EngineType.MYSQL;
		}
		// password
		objResult = Eval(astConn.password, env);		
		if (isError(objResult)) {
			return objResult;
		}
		if (objResult == null || objResult.type() != ObjType.STRING_OBJ) {
			return new ObjError("invalid data type for PASSWORD command.");
		}		
		objConn.password = objResult.inspect();
		// port
		objResult = Eval(astConn.port, env);		
		if (isError(objResult)) {
			return objResult;
		}
		if (objResult == null || objResult.type() != ObjType.STRING_OBJ) {
			return new ObjError("invalid data type for PORT command.");
		}		
		objConn.port = objResult.inspect();
		// server
		objResult = Eval(astConn.server, env);		
		if (isError(objResult)) {
			return objResult;
		}
		if (objResult == null || objResult.type() != ObjType.STRING_OBJ) {
			return new ObjError("invalid data type for SERVER command.");
		}		
		objConn.server = objResult.inspect();
		// user
		objResult = Eval(astConn.user, env);		
		if (isError(objResult)) {
			return objResult;
		}
		if (objResult == null || objResult.type() != ObjType.STRING_OBJ) {
			return new ObjError("invalid data type for USER command.");
		}				
		objConn.user = objResult.inspect();
		
		// register object in symbol table.
		env.set(astConn.conId.toString().toLowerCase(), objConn);
		
		return objConn; // return the object		
	}
	private Obj evalSetConnection(AstSetConnection astSetConn, Environment env) {
		// Find the symbol
		Obj value = env.get(astSetConn.name.toLowerCase());
		
		if (value == null) {
			return new ObjError("connection not found: " + astSetConn.name);
		}
		
		// Create the connection
		ObjConnection objConn = (ObjConnection)value;
		if (!objConn.createConnection()) {
			return runTimeError();
		}
		
		// Set the active connection
		globalEnv.currentConnection = objConn.conn;
		globalEnv.currentConnectionName = objConn.conId;
		
		// Return the updated objConn to SymbolTable
		env.set(astSetConn.name.toLowerCase(), objConn);
		
		return objConn;		
	}
	private Obj evalCloseConnection(AstCloseConnection astCloseConn, Environment env) {
		// Find the symbol
		Obj value = env.get(astCloseConn.name.toLowerCase());
		
		if (value == null) {
			return new ObjError("connection not found: " + astCloseConn.name);
		}
		
		ObjConnection objConn = (ObjConnection)value;
		objConn.conn = null;
		
		// close from globalenv
		globalEnv.currentConnection = null;
		globalEnv.currentConnectionName = "";
		
		// finally remove from environment
		env.remove(astCloseConn.name);

		return TRUE;
	}
	
	private Obj evalUseIn(AstUseIn useIn, Environment env) {
		String aliasName = "";
		// check for current connection
		if (globalEnv.currentConnection == null) {
			return new ObjError(MSG_CONNECTION_NOT_EXISTS);
		}	
		if (useIn.astAlias == null) {
			if (globalEnv.currentAlias.isEmpty()) {
				return NULL;
			} else {
				aliasName = globalEnv.currentAlias; 
			}
		} else {
			// evaluate alias node
			Obj objResult = Eval(useIn.astAlias, env);
			if (isError(objResult)) {
				return objResult;
			}
			if (objResult == null || objResult.type() != ObjType.STRING_OBJ) {
				return new ObjError("Invalid data type for alias name");
			}
			aliasName = objResult.inspect();
		}
		// search for alias name in workarea
		Obj value = globalEnv.workArea.get(aliasName.toLowerCase());
		if (value == null) {
			return new ObjError("Alias '" + aliasName + "' is not found.");
		}
		// delete the alias object
		value = null;
		// delete from symboltable
		env.remove(aliasName);
		// delete the current alias from global environment
		globalEnv.currentAlias = "";
		
		return NULL;
	}

	private Obj evalSelectTable(AstSelectTable astSel, Environment env) {
		// check for current connection
		if (globalEnv.currentConnection == null) {
			return new ObjError(MSG_CONNECTION_NOT_EXISTS);
		}
		
		// evaluate table name
		String tableName = "";
		if (astSel.astName instanceof AstString) {
			tableName = ((AstString)astSel.astName).value;
		} else {			
			Obj objResult = Eval(astSel.astName, env);
			if (isError(objResult)) {
				return objResult;
			}
			if (objResult == null || objResult.type() != ObjType.STRING_OBJ) {
				return new ObjError("invalid data type for alias name.");
			}		
			// search for alias name
			tableName = objResult.inspect();
		}

		Obj value = globalEnv.workArea.get(tableName.toLowerCase());		
		if (value == null || value.type() != ObjType.TABLE_OBJ) {
			return new ObjError("Alias '" + tableName + "' is not found.");
		}
		
		// update the current alias
		globalEnv.currentAlias = tableName;
		
		return TRUE;
	}
	
	private Obj evalBrowse(AstBrowse astBrowse, Environment env) {
		if (globalEnv.currentAlias.isEmpty()) {
			return new ObjError(MSG_NOT_OPEN_TABLE);
		}
		// search for alias name in workarea
		Obj value = globalEnv.workArea.get(globalEnv.currentAlias.toLowerCase());
		if (value == null) {
			return new ObjError("Alias '" + globalEnv.currentAlias + "' is not found.");
		}
		ObjTable objTable = (ObjTable)value;
		if (objTable.isAppending) {
			return new ObjError("Invalid command in APPEND BLANK mode."); 
		}		
		String title = astBrowse.title.isEmpty() ? objTable.alias : astBrowse.title; 
		// hit the browse
		try {
			new BrowseWindow(objTable.cursor, title);			
			objTable.requery(globalEnv.currentConnection);
		} catch (Exception e) {
			return new ObjError("Runtime Error: " + e.getMessage());
		}
		
		return TRUE;
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
		return NULL;
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
			if (leftOp.type() == ObjType.NUMBER_OBJ || rightOp.type() == ObjType.NUMBER_OBJ) {
				return evalArithmeticExpression(leftOp, astBinOp.op, rightOp);
			} 
			else if (leftOp.type() == ObjType.STRING_OBJ || rightOp.type() == ObjType.STRING_OBJ) {
				return evalBinaryStringExpression(leftOp, astBinOp.op, rightOp);
			} else {
				return new ObjError("Invalid data types for binary operation.");
			}
		}
		return NULL;
	}
	private Obj evalBinaryStringExpression(Obj leftObj, TokenType typeOp, Obj rightObj) {
		switch (typeOp) {
		case PLUS:
			return new ObjString(leftObj.inspect() + rightObj.inspect());
		default:
			return new ObjError("unknown operator for binary string expression");
		}
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
			return new ObjError("unknown operator for binary integer expression.");
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
		return NULL;
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
	private ObjError runTimeError() {
		return new ObjError("Runtime Error");
	}
	private boolean isError(Obj result) {
		return result != null && result.type() == ObjType.ERROR_OBJ;
	}
}