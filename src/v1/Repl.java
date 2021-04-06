package v1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

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
		GlobalEnv globalEnv = new GlobalEnv();
		Evaluator evaluator = new Evaluator();
		evaluator.globalEnv = globalEnv;
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
		Scanner scanner = new Scanner(System.in);
		String input = "";
		
		GlobalEnv globalEnv = new GlobalEnv();
		Environment env = new Environment();
		Evaluator evaluator = new Evaluator();
		evaluator.globalEnv = globalEnv;

		System.out.println("Wellcome to Foxship, the Command Based Language!");
		while (true) {
			System.out.print(">> ");
			input = scanner.nextLine();
			if (!input.isEmpty()) {				
				if (input.equals("quit")) {
					break;
				}

				Tokenizer tokenizer = new Tokenizer(input);
				Parser parser = new Parser(tokenizer);
				
				AstProgram program = new AstProgram();
				program.commands = parser.parseCommand();
				Obj evaluated = evaluator.Eval(program, env);
				
				if (evaluated != null) {
					System.out.println(evaluated.inspect());
				}			
			}
		}
		scanner.close();
	}
}
