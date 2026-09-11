import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Lox {
  private static boolean hadError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.err.println("Usage: java Lox [script.lox]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String fileName)
      throws IOException {
    String source = Files.readString(
        Path.of(fileName),
        StandardCharsets.UTF_8
    );

    run(source);

    if (hadError) {
      System.exit(65);
    }
  }

  private static void runPrompt() throws IOException {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(System.in)
    );

    while (true) {
      System.out.print("> ");

      String line = reader.readLine();

      if (line == null) {
        break;
      }

      run(line);
      hadError = false;
    }
  }

  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    for (Token token : tokens) {
      System.out.println(token);
    }
  }

  public static void error(int line, String message) {
    System.err.println(
        "[line " + line + "] Error: " + message
    );

    hadError = true;
  }
}