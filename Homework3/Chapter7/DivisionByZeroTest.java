package Chapter7;

public final class DivisionByZeroTest {
  private static Expr division(
      double left,
      double right
  ) {
    Token slash = new Token(
        TokenType.SLASH,
        "/",
        null,
        1
    );

    return new Expr.Binary(
        new Expr.Literal(left),
        slash,
        new Expr.Literal(right)
    );
  }

  private static void test(
      Interpreter interpreter,
      double left,
      double right
  ) {
    System.out.println(
        "Testing: " + left + " / " + right
    );

    try {
      Object result = interpreter.evaluate(
          division(left, right)
      );

      System.out.println("Result: " + result);
    } catch (RuntimeError error) {
      System.out.println(
          "[line " + error.token.line
              + "] Runtime error at '"
              + error.token.lexeme
              + "': "
              + error.getMessage()
      );
    }

    System.out.println();
  }

  public static void main(String[] args) {
    Interpreter interpreter = new Interpreter();

    test(interpreter, 10.0, 2.0);
    test(interpreter, 10.0, 0.0);
    test(interpreter, -10.0, -0.0);
    test(interpreter, 0.0, 0.0);
  }
}