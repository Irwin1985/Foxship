package v1;

public class Token {
	public TokenType type;
	public String lexeme;
	
	Token(TokenType type, String lexeme) {
		this.type = type;
		this.lexeme = lexeme;
	}
	
	@Override
	public String toString() {
		return String.format("type: %s, lexeme: '%s'", type, lexeme);
	}
}
