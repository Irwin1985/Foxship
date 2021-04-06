package v1;

public enum TokenType {
	IDENT,
	NUMBER,
	STRING,
	LBREAK,		
	
	// relational operators
	LESS,
	GREATER,
	LESS_EQ,
	GREATER_EQ,
	EQUAL,
	NOT_EQ,
	ASSIGN,
	
	// arithmetic operators
	PLUS,
	MINUS,
	MUL,
	DIV,
	
	// boolean operators
	AND,
	OR,
	NOT,
	
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
	TITLE,
	MESSAGEBOX,
	
	// table commands
	USE,
	BROWSE,
	CLOSE,
	ALIAS,
	NODATA,
	NOUPDATE,
	FILTER,	
	GO,
	RECNO,
	SKIP,
	IN,	
	SELECT,
	
	TRUE,
	FALSE,
	NULL,
	
	// special characters
	DOT,
	LPAREN,
	RPAREN,
	COMMA,
	
	EOF,
}
