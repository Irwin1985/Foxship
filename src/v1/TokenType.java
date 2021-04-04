package v1;

public enum TokenType {
	IDENT,
	NUMBER,
	STRING,
	LBREAK,
	
	// Keywords
	CREATE,
	CONNECTION,
	ENGINE,
	SERVER,
	PORT,
	DATABASE,
	USER,
	PASSWORD,
	SET,
	TO,
	USE,
	BROWSE,
	CLOSE,
	
	EOF,
}
