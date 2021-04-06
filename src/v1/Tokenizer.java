package v1;
import java.util.Map;
import java.util.HashMap;

public class Tokenizer {
	private String source;
	private static char NONE = '\0';
	private int pos = 0;
	private char currentChar = NONE;
	private Map<String, TokenType> keywords;
	private Map<String, TokenType> singleChars;
	private TokenType lastType = TokenType.EOF;
	
	Tokenizer(String source) {
		this.source = source;
		this.currentChar = source.charAt(pos);
		// fill keywords
		keywords = new HashMap<String, TokenType>();
		// Keywords
		keywords.put("CREATE", TokenType.CREATE);
		keywords.put("CONNECTION", TokenType.CONNECTION);
		keywords.put("ENGINE", TokenType.ENGINE);
		keywords.put("SERVER", TokenType.SERVER);
		keywords.put("PORT", TokenType.PORT);
		keywords.put("DATABASE", TokenType.DATABASE);
		keywords.put("USER", TokenType.USER);
		keywords.put("PASSWORD", TokenType.PASSWORD);
		keywords.put("SET", TokenType.SET);
		keywords.put("TO", TokenType.TO);
		keywords.put("USE", TokenType.USE);
		keywords.put("BROWSE", TokenType.BROWSE);
		keywords.put("CLOSE", TokenType.CLOSE);
		keywords.put("ALIAS", TokenType.ALIAS);
		keywords.put("NODATA", TokenType.NODATA);
		keywords.put("NOUPDATE", TokenType.NOUPDATE);
		keywords.put("FILTER", TokenType.FILTER);
		keywords.put("IN", TokenType.IN);
		keywords.put("SELECT", TokenType.SELECT);
		keywords.put("TITLE", TokenType.TITLE);
		keywords.put("MESSAGEBOX", TokenType.MESSAGEBOX);
		keywords.put("AND", TokenType.AND);
		keywords.put("OR", TokenType.OR);
		keywords.put("TRUE", TokenType.TRUE);
		keywords.put("FALSE", TokenType.FALSE);
		keywords.put("NULL", TokenType.NULL);
		keywords.put("GO", TokenType.GO);
		keywords.put("RECNO", TokenType.RECNO);
		keywords.put("SKIP", TokenType.SKIP);
		// Single digit characters
		singleChars = new HashMap<String, TokenType>();
		singleChars.put(".", TokenType.DOT);
		singleChars.put(",", TokenType.COMMA);
		singleChars.put("(", TokenType.LPAREN);
		singleChars.put(")", TokenType.RPAREN);
		singleChars.put("+", TokenType.PLUS);
		singleChars.put("-", TokenType.MINUS);
		singleChars.put("*", TokenType.MUL);
		singleChars.put("/", TokenType.DIV);
		singleChars.put("=", TokenType.ASSIGN);
	}
	
	private Token newToken(TokenType type, String lexeme) {
		Token tok = new Token(type, lexeme);
		lastType = type;
		return tok;
	}
	
	
	private void advance() {
		pos++;
		if (pos >= source.length()) {
			currentChar = NONE;
		} else {
			currentChar = source.charAt(pos);
		}
	}
	
	private char peek() {
		int peekPos = pos + 1;
		if (peekPos >= source.length()) {
			return NONE;
		} else {
			return source.charAt(peekPos);
		}
	}
	
	private boolean isSpace(char chr) {
		return chr == '\t' || chr == '\r' || chr == ' ';
	}
	
	private void skipWhitespace() {
		while (currentChar != NONE && isSpace(currentChar)) {
			advance(); // skip white spaces
		}
	}
	
	private void skipComment() {
		advance(); // skip first '/'
		advance(); // skip last '/'
		while (currentChar != NONE && currentChar != '\n') {
			advance();
		}
	}
	
	private boolean isComment() {
		return currentChar == '/' && peek() == '/';
	}
	
	private boolean isLetter(char chr) {
		return Character.isLetterOrDigit(chr) || chr == '_';
	}
	
	private boolean isNumber(char chr) {
		return Character.isDigit(chr) || chr == '.';
	}
	
	private Token getIdentifier() {
		String lexeme = "";
		while (currentChar != NONE && isLetter(currentChar)) {
			lexeme += currentChar;
			advance();
		}
		return newToken(keywords.getOrDefault(lexeme.toUpperCase(), TokenType.IDENT), lexeme);
	}
	
	private Token getString() {
		String lexeme = "";
		advance(); // skip the opening '"'
		while (currentChar != NONE && currentChar != '"') {
			lexeme += currentChar;
			advance();
		}
		advance(); // skip the closing '"'
		return newToken(TokenType.STRING, lexeme);		
	}
	
	private Token getNumber() {
		String lexeme = "";
		while (currentChar != NONE && isNumber(currentChar)) {
			lexeme += currentChar;
			advance();
		}
		return newToken(TokenType.NUMBER, lexeme);
	}
	
	public Token getNextToken() {
		while (currentChar != NONE) {
			if (isSpace(currentChar)) {
				skipWhitespace();
				continue;
			}
			if (isComment()) {
				skipComment();
				continue;
			}
			if (currentChar == '\n') {
				if (lastType != TokenType.EOF && lastType != TokenType.LBREAK) {
					advance();
					return newToken(TokenType.LBREAK, "NEW LINE");				
				} else {
					advance(); // skip repeated token
					continue;
				}
			}
			if (Character.isDigit(currentChar)) {
				return getNumber();
			}
			if (isLetter(currentChar)) {
				return getIdentifier();
			}
			if (currentChar == '"') {
				return getString();
			}
			//< double digit length characters
			if (currentChar == '<') {
				advance();
				if (currentChar == '=') {
					advance();
					return newToken(TokenType.LESS_EQ, "<=");
				}
				return newToken(TokenType.LESS, "<");
			}
			if (currentChar == '>') {
				advance();
				if (currentChar == '=') {
					advance();
					return newToken(TokenType.GREATER_EQ, ">=");
				}
				return newToken(TokenType.GREATER, ">");
			}
			if (currentChar == '!') {
				advance();
				if (currentChar == '=') {
					advance();
					return newToken(TokenType.NOT_EQ, "!=");
				}
				return newToken(TokenType.NOT, "!");
			}
			//< double digit length characters
			TokenType tokenType = singleChars.get(currentChar + "");
			if (tokenType != null) {
				advance();
				return newToken(tokenType, currentChar + "");
			}
			
			System.out.println("(Lexer Error) unknown character: " + currentChar);
			System.exit(1);
		}
		return newToken(TokenType.EOF, "");
	}
}
