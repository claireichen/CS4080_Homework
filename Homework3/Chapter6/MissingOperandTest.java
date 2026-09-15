package Chapter6;

import java.util.List;

public final class MissingOperandTest {
  private static Token operator(
      TokenType type,
      String lexeme
  ) {
    return new Token(
        type,
        lexeme,
        null,
        1
    );
  }

  private static Token number(double value) {
    return new Token(
        TokenType.NUMBER,
        Double.toString(value),
        value,
        1
    );
  }

  private static Token eof() {
    return new Token(
        TokenType.EOF,
        "",
        null,
        1
    );
  }

  private static void test(
      String source,
      List<Token> tokens
  ) {
    System.out.println("Testing: " + source);

    Parser parser = new Parser(tokens);
    parser.parse();

    System.out.println();
  }

  public static void main(String[] args) {
    test(
        "== 1",
        List.of(
            operator(TokenType.EQUAL_EQUAL, "=="),
            number(1),
            eof()
        )
    );

    test(
        "> 2",
        List.of(
            operator(TokenType.GREATER, ">"),
            number(2),
            eof()
        )
    );

    test(
        "+ 3 * 4",
        List.of(
            operator(TokenType.PLUS, "+"),
            number(3),
            operator(TokenType.STAR, "*"),
            number(4),
            eof()
        )
    );

    test(
        "* 5",
        List.of(
            operator(TokenType.STAR, "*"),
            number(5),
            eof()
        )
    );
  }
}