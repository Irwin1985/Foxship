package v1;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
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
		if (curToken.type == TokenType.CREATE && peekToken.type == TokenType.CONNECTION) {
			statement = parseConnection();
		}
		else if (curToken.type == TokenType.SET && peekToken.type == TokenType.CONNECTION) {
			statement = parseSetConnection();
		}
		else if (curToken.type == TokenType.CLOSE && peekToken.type == TokenType.CONNECTION) {
			statement = parseCloseConnection();
		}
		else if (curToken.type == TokenType.USE && peekToken.type == TokenType.IDENT) {
			statement = parseUse();
		}
		else if (curToken.type == TokenType.USE && peekToken.type == TokenType.IN) {
			statement = parseUseIn();
		}
		else if (curToken.type == TokenType.SELECT) {
			statement = parseSelectTable();
		}
		else if (curToken.type == TokenType.BROWSE) {
			statement = parseBrowse();
		}
		else if (curToken.type == TokenType.MESSAGEBOX) {
			statement = parseMessagebox();
		} 
		else if (curToken.type == TokenType.GO) {
			statement = parseGo();
		}
		else if (curToken.type == TokenType.EXPORT) {
			statement = parseExportConnection();
		}
		else {
			statement = parseExpression();
		}
		skipNewLine();
		return statement;
	}
	
	private Ast parseConnection() {
		
		match(TokenType.CREATE);
		match(TokenType.CONNECTION);
		if (curToken.type == TokenType.FROM) {
			match(TokenType.FROM);
			match(TokenType.FILE);
			return new AstConnectionFromFile(parseExpression());
		}
		else {
			AstConnection conn = new AstConnection();
			conn.conId = match(TokenType.IDENT).lexeme;
			
			while (curToken.type != TokenType.EOF && curToken.type != TokenType.LBREAK) {			
				if (curToken.type == TokenType.DATABASE) {
					match(TokenType.DATABASE);
					conn.dataBase = parseExpression();
					continue;
				}
				else if (curToken.type == TokenType.ENGINE) {
					match(TokenType.ENGINE);				
					conn.engine = parseExpression();
					continue;
				}
				else if (curToken.type == TokenType.PASSWORD) {
					match(TokenType.PASSWORD);
					conn.password = parseExpression();
					continue;
				}
				else if (curToken.type == TokenType.PORT) {
					match(TokenType.PORT);
					conn.port = parseExpression();
					continue;
				}
				else if (curToken.type == TokenType.SERVER) {
					match(TokenType.SERVER);
					conn.server = parseExpression();
					continue;
				}
				else if (curToken.type == TokenType.USER) {
					match(TokenType.USER);
					conn.user = parseExpression();
					continue;
				}
			}
			return conn;
		}		
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
	
	private Ast parseUse() {
		match(TokenType.USE);
		AstUseTable useTable = new AstUseTable();
		useTable.name = match(TokenType.IDENT).lexeme;
		
		while (curToken.type != TokenType.EOF && curToken.type != TokenType.LBREAK) {
			if (curToken.type == TokenType.ALIAS) {
				match(TokenType.ALIAS);
				useTable.alias = match(TokenType.IDENT).lexeme;
				continue;
			}
			else if (curToken.type == TokenType.NODATA) {
				match(TokenType.NODATA);
				useTable.noData = true;
				continue;
			}
			else if (curToken.type == TokenType.NOUPDATE) {
				match(TokenType.NOUPDATE);
				useTable.noUpdate = true;
				continue;
			}
		}
		// check for no alias (assume name)
		if (useTable.alias.isEmpty()) {
			useTable.alias = useTable.name;
		}
		return useTable;		
	}
	
	private Ast parseUseIn() {
		match(TokenType.USE);
		match(TokenType.IN);

		return new AstUseIn(match(TokenType.IDENT).lexeme);
	}
	
	private Ast parseSelectTable() {
		match(TokenType.SELECT);

		return new AstSelectTable(match(TokenType.IDENT).lexeme);
	}
	
	private Ast parseBrowse() {
		match(TokenType.BROWSE);
		AstBrowse browse = new AstBrowse();

		while (curToken.type != TokenType.EOF && curToken.type != TokenType.LBREAK) {
			if (curToken.type == TokenType.TITLE) {
				match(TokenType.TITLE);
				browse.title = match(TokenType.STRING).lexeme;
				continue;
			}
		}
		return browse;
	}
	private Ast parseMessagebox() {
		AstMessagebox messagebox = new AstMessagebox();

		match(TokenType.MESSAGEBOX);
		match(TokenType.LPAREN);
		messagebox.content = parseExpression();

		// button type
		if (curToken.type == TokenType.COMMA) {
			match(TokenType.COMMA);
			messagebox.buttons = parseExpression();
		}
		// title
		if (curToken.type == TokenType.COMMA) {
			match(TokenType.COMMA);
			messagebox.title = parseExpression();
		}
		// timeout
		if (curToken.type == TokenType.COMMA) {
			match(TokenType.COMMA);
			messagebox.timeout = parseExpression();
		}		
		match(TokenType.RPAREN);
		
		return messagebox;
	}
	
	private Ast parseExpression() {
		return parseEquality();
	}
	
	private Ast parseEquality() {
		Ast expr = parseOr();
		// check for equality token
		if (curToken.type == TokenType.ASSIGN) {
			match(TokenType.ASSIGN);
			return new AstAssignment(expr, parseExpression());
		}
		return expr;
	}
	
	private Ast parseOr() {
		Ast expr = parseAnd();
		while (curToken.type == TokenType.OR) {
			match(TokenType.OR);
			expr = new AstBinOp(expr, TokenType.OR, parseAnd());
		}
		return expr;
	}

	private Ast parseAnd() {
		Ast expr = parseComparison();
		while (curToken.type == TokenType.AND) {
			match(TokenType.AND);
			expr = new AstBinOp(expr, TokenType.AND, parseComparison());
		}
		return expr;
	}
	
	private Ast parseComparison() {
		Ast expr = parseTerm();
		while (inlist(TokenType.LESS, TokenType.LESS_EQ, TokenType.GREATER, TokenType.GREATER_EQ))
		{
			TokenType opType = match(curToken.type).type;
			expr = new AstBinOp(expr, opType, parseTerm());
		}
		return expr;
	}
	
	private Ast parseTerm() {
		Ast expr = parseFactor();
		while (inlist(TokenType.PLUS, TokenType.MINUS))
		{
			TokenType opType = match(curToken.type).type;
			expr = new AstBinOp(expr, opType, parseFactor());
		}
		return expr;
	}
	
	private Ast parseFactor() {
		Ast expr = parseUnary();
		while (inlist(TokenType.MUL, TokenType.DIV))
		{
			TokenType opType = match(curToken.type).type;
			expr = new AstBinOp(expr, opType, parseUnary());
		}
		return expr;
	}
	
	private Ast parseUnary() {
		if (inlist(TokenType.MINUS, TokenType.NOT))
		{
			TokenType opType = match(curToken.type).type;
			return new AstUnary(opType, parseUnary());
		}

		return parseCall();		
	}
	
	private Ast parseCall() {
		Ast expr = parsePrimary();
		// check for function parsing
		return expr;
	}
	
	private Ast parsePrimary() {
		if (inlist(TokenType.TRUE, TokenType.FALSE)) {
			TokenType type = match(curToken.type).type;
			return new AstBoolean(type == TokenType.TRUE);
		}
		else if (curToken.type == TokenType.NULL) {
			match(TokenType.NULL);
			return new AstNull();			
		}
		else if (curToken.type == TokenType.NUMBER) {
			String lexeme = match(TokenType.NUMBER).lexeme;
			return new AstNumber(Double.parseDouble(lexeme));
		}
		else if (curToken.type == TokenType.STRING) {
			String lexeme = match(TokenType.STRING).lexeme;
			return new AstString(lexeme);			
		}
		else if (curToken.type == TokenType.IDENT) {
			String lexeme = match(TokenType.IDENT).lexeme;
			return new AstIdentifier(lexeme);
		}
		else if (curToken.type == TokenType.LPAREN) {
			match(TokenType.LPAREN);
			Ast expr = parseExpression();
			match(TokenType.RPAREN);
			return expr;
		}
		
		return null;
	}
	
	private boolean inlist(TokenType... types) {
		for (TokenType type : types) {
			if (curToken.type == type) {
				return true;
			}
		}
		return false;
	}
	private Ast parseGo() {
		match(TokenType.GO);
		AstGo astGo = new AstGo();
		astGo.astRow = parseExpression();

		if (curToken.type == TokenType.IN) {
			match(TokenType.IN);
			astGo.astAlias = parseExpression();
		}
		return astGo;
	}
	private Ast parseExportConnection() {
		match(TokenType.EXPORT);
		match(TokenType.CONNECTION);
		match(TokenType.TO);
		
		return new AstExportConnection(parseExpression());
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
