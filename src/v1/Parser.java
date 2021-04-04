package v1;
import java.util.List;
import java.util.ArrayList;

public class Parser {
	private Token curToken;
	private Token peekToken;
	private Tokenizer tokenizer;
	
	Parser(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
		nextTokens();
		nextTokens();
	}
	
	private void nextTokens() {
		curToken = peekToken;
		peekToken = tokenizer.getNextToken();
	}
	
	public List<Ast> parseCommand() {
		List<Ast> commands = new ArrayList<Ast>();
		
		while (curToken.type != TokenType.EOF) {
			Ast command = parseStatement();
			if (command != null) {
				commands.add(command);
			}
		}
		return commands;
	}

	private Ast parseStatement() {
		Ast statement = null;
		if (curToken.type == TokenType.CREATE) {
			statement = parseConnection();
		}
		else if (curToken.type == TokenType.SET && peekToken.type == TokenType.CONNECTION) {
			statement = parseSetConnection();
		}
		else if (curToken.type == TokenType.CLOSE && peekToken.type == TokenType.CONNECTION) {
			statement = parseCloseConnection();
		}
		skipNewLine();
		return statement;
	}
	
	private Ast parseConnection() {
		AstConnection conn = new AstConnection();
		
		match(TokenType.CREATE);
		match(TokenType.CONNECTION);
		
		conn.conId = match(TokenType.IDENT).lexeme;
		
		while (curToken.type != TokenType.LBREAK) {			
			if (curToken.type == TokenType.DATABASE) {
				match(TokenType.DATABASE);
				conn.dataBase = match(TokenType.STRING).lexeme;
				continue;
			}
			if (curToken.type == TokenType.ENGINE) {
				match(TokenType.ENGINE);
				
				String engine = match(TokenType.STRING).lexeme.toLowerCase();
				if (engine.equals("mysql")) {
					conn.engine = EngineType.MYSQL;				
				}
				else if (engine.equals("mariadb")) {
					conn.engine = EngineType.MARIADB;
				}
				else if (engine.equals("postgre")) {
					conn.engine = EngineType.POSTGRE;
				} else {
					System.out.println("Unknown engine type");
					System.exit(1);
				}
				continue;
			}
			if (curToken.type == TokenType.PASSWORD) {
				match(TokenType.PASSWORD);
				conn.password = match(TokenType.STRING).lexeme;
				continue;
			}
			if (curToken.type == TokenType.PORT) {
				match(TokenType.PORT);
				conn.port = match(TokenType.STRING).lexeme;
				continue;
			}
			if (curToken.type == TokenType.SERVER) {
				match(TokenType.SERVER);
				conn.server = match(TokenType.STRING).lexeme;
				continue;
			}
			if (curToken.type == TokenType.USER) {
				match(TokenType.USER);
				conn.user = match(TokenType.STRING).lexeme;
				continue;
			}		
		}
		
		return conn;
	}
	
	private Ast parseCloseConnection() {
		match(TokenType.CLOSE);
		match(TokenType.CONNECTION);
		
		return new AstCloseConnection(match(TokenType.IDENT).lexeme);		
	}
	
	private Ast parseSetConnection() {
		
		match(TokenType.SET);
		match(TokenType.CONNECTION);
		match(TokenType.TO);
		
		return new AstSetConnection(match(TokenType.IDENT).lexeme);
	}
	
	private void skipNewLine() {
		if (curToken.type == TokenType.LBREAK) {
			match(TokenType.LBREAK);
		}
	}
	private Token match(TokenType type) {
		Token prevToken = curToken;
		if (curToken.type == type) {
			nextTokens();
		} else {
			System.out.println("Unexpected token " + curToken.type + ", want = " + type);
			System.exit(1);			
		}
		return prevToken;
	}
}
