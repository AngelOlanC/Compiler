package MVC.Model;

public class SyntaxError {
  private String errorMessage;
  private int line, column;

  public SyntaxError(String errorMessage, int line, int column) {
    this.errorMessage = errorMessage;
    this.line = line;
    this.column = column;
  }

  @Override
  public String toString() {
    return "Error on [" + line + ", " + column + "]\n" +
           "\t" + errorMessage;
  }
}
