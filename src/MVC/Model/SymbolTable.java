package MVC.Model;

import java.util.ArrayList;

public class SymbolTable {
  ArrayList<Entry> entries;

  public SymbolTable() {
    entries = new ArrayList<>();
  }

  public void install(Lexeme lexeme, int line, int column) {
    entries.add(new Entry(lexeme, line, column));
  }

  @Override
  public String toString() {
    StringBuilder text = new StringBuilder();
    for (Entry e : entries) text.append(e.toString());
    return text.toString();
  }

  private class Entry {
    private Lexeme lexeme;
    private int line, column;
    
    public Entry(Lexeme lexeme, int line, int column) {
      this.lexeme = lexeme;
      this.line = line;
      this.column = column;
    }

    @Override
    public String toString() {
      return lexeme.getType() + " " + lexeme.getValue() + " " + line + " " + column + "\n";
    }
  }
}
