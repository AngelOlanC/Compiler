package MVC.Model;

import java.util.ArrayList;

public class SyntaxAnalyzer {
  private final String[] reservedWords = {"begin", "end", "read", "print", "if", "else",
      "while", "continue", "break", "exit", "int", "string"};
  private SymbolTable symbolTable;
  private ArrayList<SyntaxError> errors;
  
  public SyntaxAnalyzer(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
    errors = new ArrayList<>();
  }

  public boolean analyze(String source) {
    symbolTable.entries.clear();
    errors.clear();
    
    int i = 0, n = source.length(), line = 1, last_new_line = -1;
    while (i < n) {
      char c = source.charAt(i);
      if (Character.isLowerCase(c)) {
        StringBuilder value = new StringBuilder();
        do {
          value.append(c);
          if (++i == n) break;
          c = source.charAt(i);
        } while (Character.isLowerCase(c) || Character.isDigit(c));
        if (isReservedWord(value.toString())) {
          symbolTable.install(new Lexeme(Token.RESERVED_WORD, value.toString()), line, i - last_new_line);
        } else {
          symbolTable.install(new Lexeme(Token.IDENTIFIER, value.toString()), line, i - last_new_line);
        }
        continue;
      }
      if (c == '"') {
        StringBuilder value = new StringBuilder();
        do {
          value.append(c);
          if (++i == n) break;
          c = source.charAt(i);
        } while (Character.isLowerCase(c) || Character.isDigit(c) || c == ' ');
        if (i < n && c == '"') {
          symbolTable.install(new Lexeme(Token.STRING, value.toString()), line, i - last_new_line);
          continue;
        }
        String errorMessage;
        if (i == n) errorMessage = "Expected \", but found end of file";
        else        errorMessage = "Expected \", but found " + Character.toString(c);
        errors.add(new SyntaxError(errorMessage, line, i - last_new_line));
        continue;
      }
      if (Character.isDigit(c)) {
        StringBuilder value = new StringBuilder();
        do {
          value.append(c);
          if (++i == n) break;
          c = source.charAt(i);
        } while (Character.isDigit(c));
        symbolTable.install(new Lexeme(Token.INTEGER, value.toString()), line, i - last_new_line);
      }
      if (c == ' ' || c == '\t') {
        ++i;
        continue;
      }
      if (c == '\n') {
        last_new_line = i++;
        ++line;
        continue;
      }
      if (c == '(') {
        symbolTable.install(new Lexeme(Token.LEFT_PARENTHESES, "("), line, i - last_new_line);
        ++i;
        continue;
      }
      if (c == ')') {
        symbolTable.install(new Lexeme(Token.RIGHT_PARENTHESES, ")"), line, i - last_new_line);
        ++i;
        continue;
      }
      if (c == '{') {
        symbolTable.install(new Lexeme(Token.LEFT_CURLY_BRACE, "{"), line, i - last_new_line);
        ++i;
        continue;
      }
      if (c == '}') {
        symbolTable.install(new Lexeme(Token.RIGHT_CURLY_BRACE, "}"), line, i - last_new_line);
        ++i;
        continue;
      }
      if (c == '+') {
        symbolTable.install(new Lexeme(Token.ADD, "+"), line, i - last_new_line);
        ++i;
        continue;
      }
      if (c == '-') {
        symbolTable.install(new Lexeme(Token.SUBSTRACT, "-"), line, i - last_new_line);
        ++i;
        continue;
      }
      if (c == '<') {
        if (++i == n || source.charAt(i) != '=') {
          symbolTable.install(new Lexeme(Token.LESS, "<"), line, i - last_new_line);
          continue;
        }
        symbolTable.install(new Lexeme(Token.LESS_EQUALS, "<="), line, i - last_new_line);
        ++i;
        continue;
      }
      if (c == '>') {
        if (++i == n || source.charAt(i) != '=') {
          symbolTable.install(new Lexeme(Token.GREATER, ">"), line, i - last_new_line);
          continue;
        }
        symbolTable.install(new Lexeme(Token.GREATER_EQUALS, ">="), line, i - last_new_line);
        ++i;
        continue;
      }
      if (c == '=') {
        if (++i == n || source.charAt(i) != '=') {
          symbolTable.install(new Lexeme(Token.ASSIGNMENT, "="), line, i - last_new_line);
          continue;
        }
        symbolTable.install(new Lexeme(Token.EQUALS, "=="), line, i - last_new_line);
        ++i;
        continue;
      }
      if (c == '!') {
        if (++i == n) {
          errors.add(new SyntaxError("Expected =, but found end of file", line, i - last_new_line));
          continue;
        }
        c = source.charAt(i);
        if (c != '=') {
          errors.add(new SyntaxError("Expected =, but found " + c, line, i - last_new_line));
          continue;
        }
        symbolTable.install(new Lexeme(Token.NOT_EQUALS, "!="), line, i - last_new_line);
        ++i;
        continue;
      }
      errors.add(new SyntaxError("Character " + Character.toString(c) + " not recognized", line, i - last_new_line));
      ++i;
    }
    return true;
  }

  private boolean isReservedWord(String word) {
    for (String reservedWord : reservedWords) {
      if (word.equals(reservedWord)) return true;
    }
    return false;
  }

  public SymbolTable getSymbolTable() {
    return symbolTable;
  }

  public ArrayList<SyntaxError> getErrors() {
    return errors;
  }

  public String getErrorsText() {
    StringBuilder text = new StringBuilder();
    for (SyntaxError e : errors) text.append(e.toString());
    return text.toString();
  }
}
