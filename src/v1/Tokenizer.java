package v1;
import java.util.Map;
import java.util.HashMap;

public class Tokenizer {
	private String source;
	private static char NONE = '\0';
	private int pos = 0;
	private char currentChar = NONE;
	private Map<String, TokenType> keywords;
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
			System.out.println("(Lexer Error) unknown character: " + currentChar);
			System.exit(1);
		}
		return newToken(TokenType.EOF, "");
	}
}
