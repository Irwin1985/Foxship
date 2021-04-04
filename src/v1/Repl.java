package v1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Repl {
	public static void main(String[] args) {
		if (args.length > 0) {
			try {
				runFile(args[0]);
			} catch (Exception e) {
				System.out.println("Error: " + e);
			}
		} else {
			startRepl();
		}
	}
	
	private static void runFile(String filePath) throws IOException {
		String script = Files.readString(Path.of(filePath));
		Tokenizer tokenizer = new Tokenizer(script);
		Parser parser = new Parser(tokenizer);
		
		AstProgram program = new AstProgram();
		program.commands = parser.parseCommand();
		
		Evaluator evaluator = new Evaluator();
		Environment env = new Environment();
		Obj evaluated = evaluator.Eval(program, env);
		
		if (evaluated != null) {
			System.out.println(evaluated.inspect());
		}
		
		
		
		/*
		Token tok = tokenizer.getNextToken();
		while (tok.type != TokenType.EOF) {
			System.out.println(tok.toString());
			tok = tokenizer.getNextToken();
		}
		System.out.println(tok.toString());
		*/
	}
	
	private static void startRepl() {

	}
}
