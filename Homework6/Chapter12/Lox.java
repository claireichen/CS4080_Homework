package Chapter12;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Lox {
  private static final Interpreter INTERPRETER =
      new Interpreter();

  private static boolean hadError;
  private static boolean hadRuntimeError;

  public static void main(String[] args)
      throws IOException {
    if (args.length > 1) {
      System.err.println(
          "Usage: java Chapter12.Lox [script]"
      );

      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path)
      throws IOException {
    String source = Files.readString(
        Path.of(path),
        StandardCharsets.UTF_8
    );

    run(source, false);

    if (hadError) {
      System.exit(65);
    }

    if (hadRuntimeError) {
      System.exit(70);
    }
  }

  private static void runPrompt()
      throws IOException {
    BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(System.in)
        );

    while (true) {
      System.out.print("> ");

      String line = reader.readLine();

      if (line == null) {
        break;
      }

      hadError = false;
      run(line, true);
    }
  }

  private static void run(
      String source,
      boolean repl
  ) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    Parser parser = new Parser(tokens);

    /*
     * When executing a script, parse all
     * statements and resolve them before
     * interpretation begins.
     */
    if (!repl) {
      List<Stmt> statements = parser.parse();

      if (!hadError) {
        Resolver resolver =
            new Resolver(INTERPRETER);

        resolver.resolve(statements);
      }

      if (!hadError) {
        INTERPRETER.interpret(statements);
      }

      return;
    }

    /*
     * REPL input can be either a single
     * expression or a list of statements.
     */
    Object syntax = parser.parseRepl();

    if (hadError) {
      return;
    }

    if (syntax instanceof Expr expression) {
      Resolver resolver =
          new Resolver(INTERPRETER);

      resolver.resolve(expression);

      if (hadError) {
        return;
      }

      String result =
          INTERPRETER.interpret(expression);

      if (result != null) {
        System.out.println("= " + result);
      }
    } else {
      @SuppressWarnings("unchecked")
      List<Stmt> statements =
          (List<Stmt>) syntax;

      Resolver resolver =
          new Resolver(INTERPRETER);

      resolver.resolve(statements);

      if (!hadError) {
        INTERPRETER.interpret(statements);
      }
    }
  }

  public static void error(
      int line,
      String message
  ) {
    report(
        line,
        "",
        message
    );
  }

  public static void error(
      Token token,
      String message
  ) {
    if (token.type == TokenType.EOF) {
      report(
          token.line,
          " at end",
          message
      );
    } else {
      report(
          token.line,
          " at '" + token.lexeme + "'",
          message
      );
    }
  }

  private static void report(
      int line,
      String where,
      String message
  ) {
    System.err.println(
        "[line " + line + "] Error"
            + where + ": " + message
    );

    hadError = true;
  }

  public static void runtimeError(
      RuntimeError error
  ) {
    System.err.println(
        error.getMessage()
            + "\n[line "
            + error.token.line
            + "]"
    );

    hadRuntimeError = true;
  }
}
