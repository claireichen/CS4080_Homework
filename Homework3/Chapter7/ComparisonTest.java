package Chapter7;

public final class ComparisonTest {
  private static Expr comparison(
      Object left,
      TokenType operatorType,
      String operatorText,
      Object right
  ) {
    Token operator = new Token(
        operatorType,
        operatorText,
        null,
        1
    );

    return new Expr.Binary(
        new Expr.Literal(left),
        operator,
        new Expr.Literal(right)
    );
  }

  private static void test(
      Interpreter interpreter,
      String label,
      Expr expression
  ) {
    Object result = interpreter.evaluate(expression);

    System.out.println(
        label + " -> " + result
    );
  }

  public static void main(String[] args) {
    Interpreter interpreter = new Interpreter();

    test(
        interpreter,
        "3 < 4",
        comparison(
            3.0,
            TokenType.LESS,
            "<",
            4.0
        )
    );

    test(
        interpreter,
        "apple < banana",
        comparison(
            "apple",
            TokenType.LESS,
            "<",
            "banana"
        )
    );

    test(
        interpreter,
        "cat > car",
        comparison(
            "cat",
            TokenType.GREATER,
            ">",
            "car"
        )
    );

    test(
        interpreter,
        "10 < 2 as strings",
        comparison(
            "10",
            TokenType.LESS,
            "<",
            "2"
        )
    );

    try {
      test(
          interpreter,
          "3 as a string < 4 as a number",
          comparison(
              "3",
              TokenType.LESS,
              "<",
              4.0
          )
      );
    } catch (RuntimeError error) {
      System.out.println(
          "Mixed comparison rejected: "
              + error.getMessage()
      );
    }
  }
}