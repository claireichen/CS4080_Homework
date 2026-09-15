package Chapter6;

import java.util.List;

public final class ConditionalParserTest {
  private static Token token(
      TokenType type,
      String text
  ) {
    return new Token(type, text, null, 1);
  }

  private static Token number(double value) {
    return new Token(
        TokenType.NUMBER,
        Double.toString(value),
        value,
        1
    );
  }

  public static void main(String[] args) {
    // true ? 1 : false ? 2 : 3
    List<Token> tokens = List.of(
        token(TokenType.TRUE, "true"),
        token(TokenType.QUESTION, "?"),
        number(1),
        token(TokenType.COLON, ":"),

        token(TokenType.FALSE, "false"),
        token(TokenType.QUESTION, "?"),
        number(2),
        token(TokenType.COLON, ":"),
        number(3),

        token(TokenType.EOF, "")
    );

    Expr expression = new Parser(tokens).parse();

    if (expression == null) {
      System.err.println("Parsing failed.");
      System.exit(1);
    }

    System.out.println(
        new RpnPrinter().print(expression)
    );
  }
}