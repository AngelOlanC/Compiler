import MVC.Controller;
import MVC.View;
import MVC.Model.SymbolTable;

public class Main {
  public static void main(String[] a) {
    new Controller(new SymbolTable(), new View());
  }
}