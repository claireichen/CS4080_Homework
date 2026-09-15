package Chapter7;

public final class ConcatenationTest {
  private static Expr addition(
      Object left,
      Object right
  ) {
    Token plus = new Token(
        TokenType.PLUS,
        "+",
        null,
        1
    );

    return new Expr.Binary(
        new Expr.Literal(left),
        plus,
        new Expr.Literal(right)
    );
  }

  private static void test(
      Interpreter interpreter,
      Object left,
      Object right
  ) {
    Object result = interpreter.evaluate(
        addition(left, right)
    );

    System.out.println(result);
  }

  public static void main(String[] args) {
    Interpreter interpreter = new Interpreter();

    test(interpreter, "scone", 4.0);
    test(interpreter, 4.0, " scones");
    test(interpreter, "value: ", true);
    test(interpreter, "value: ", null);
    test(interpreter, 2.0, 3.0);
  }
}