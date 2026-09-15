package Chapter6;

import java.util.List;

public final class CommaParserTest {
  private static Token number(double value) {
    String text = Double.toString(value);

    return new Token(
        TokenType.NUMBER,
        text,
        value,
        1
    );
  }

  private static Token operator(
      TokenType type,
      String text
  ) {
    return new Token(type, text, null, 1);
  }

  public static void main(String[] args) {
    List<Token> tokens = List.of(
        number(1),
        operator(TokenType.PLUS, "+"),
        number(2),

        operator(TokenType.COMMA, ","),

        number(3),
        operator(TokenType.STAR, "*"),
        number(4),

        operator(TokenType.COMMA, ","),

        number(5),

        new Token(
            TokenType.EOF,
            "",
            null,
            1
        )
    );

    Expr expression = new Parser(tokens).parse();

    if (expression == null) {
      System.err.println("Parsing failed.");
      System.exit(1);
    }

    String result =
        new RpnPrinter().print(expression);

    System.out.println(result);
  }
}