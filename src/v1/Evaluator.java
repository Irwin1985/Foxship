package v1;

public class Evaluator {
	public Obj Eval(Ast node, Environment env) {
		if (node instanceof AstProgram) {
			return evalProgram((AstProgram)node, env);
		}
		else if (node instanceof AstConnection) {
			AstConnection astConn = (AstConnection)node;
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
		else if (node instanceof AstSetConnection) {
			AstSetConnection astSetConn = (AstSetConnection)node;
			// Find the symbol
			Obj value = env.get(astSetConn.name);
			
			if (value == null) {
				return new ObjError("connection not found: " + astSetConn.name);
			}
			
			// Create the connection
			ObjConnection objConn = (ObjConnection)value;
			if (!objConn.createConnection()) {
				return new ObjError("Runtime Error");
			}
			// Return the updated objConn to SymbolTable
			env.set(astSetConn.name, objConn);
			
			return objConn;
		}
		else if (node instanceof AstCloseConnection) {
			AstCloseConnection astCloseConn = (AstCloseConnection)node;
			// Find the symbol
			Obj value = env.get(astCloseConn.name);
			
			if (value == null) {
				return new ObjError("connection not found: " + astCloseConn.name);
			}
			ObjConnection objConn = (ObjConnection)value;
			objConn.conn = null;
			// finally remove from environment
			env.remove(astCloseConn.name);

			return null; // nothing to return
		}
		return null;
	}
	
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
}
