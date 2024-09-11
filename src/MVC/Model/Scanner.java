package MVC.Model;

import java.util.ArrayList;
import java.util.HashMap;

public class Scanner {
  private static HashMap<String, TokenType> reservedWords;
  private static HashMap<String, TokenType> symbols;

  private ArrayList<Token> tokenStream;
  private String source;

  public Scanner(String source) {
    this.source = source;
    tokenStream = new ArrayList<>();

    initializeStaticValues();
  }

  private void initializeStaticValues() {
    if (reservedWords == null) {
      reservedWords = new HashMap<String,TokenType>();
      reservedWords.put("begin", TokenType.RW_BEGIN);
      reservedWords.put("end", TokenType.RW_END);
      reservedWords.put("read", TokenType.RW_READ);
      reservedWords.put("print", TokenType.RW_PRINT);
      reservedWords.put("if", TokenType.RW_IF);
      reservedWords.put("else", TokenType.RW_ELSE);
      reservedWords.put("while", TokenType.RW_WHILE);
      reservedWords.put("continue", TokenType.RW_CONTINUE);
      reservedWords.put("break", TokenType.RW_BREAK);
      reservedWords.put("exit", TokenType.RW_EXIT);
      reservedWords.put("int", TokenType.RW_INT);
      reservedWords.put("string", TokenType.RW_STRING);
    }

    if (symbols == null) {
      symbols = new HashMap<>();
      symbols.put("(", TokenType.LEFT_PARENTHESES);
      symbols.put(")", TokenType.RIGHT_PARENTHESES);
      symbols.put("{", TokenType.LEFT_CURLY_BRACE);
      symbols.put("}", TokenType.RIGHT_CURLY_BRACE);
      symbols.put("+", TokenType.ADD);
      symbols.put("-", TokenType.SUBSTRACT);
    }
  }
  
  public boolean analyze() {
    tokenStream = new ArrayList<>();
    int i = 0, n = source.length();
    while (i < n) {
      char c = source.charAt(i);
      if (c == ' ' || c == '\t' || c == '\n') { ++i; continue; }
      if (Character.isLowerCase(c)) {
        StringBuilder value = new StringBuilder();
        do {
          value.append(c);
          if (++i == n) break;
          c = source.charAt(i);
        } while (Character.isLowerCase(c) || Character.isDigit(c));
        if (isReservedWord(value.toString())) {
          tokenStream.add(new Token(reservedWords.get(value.toString()), value.toString()));
        } else {
          tokenStream.add(new Token(TokenType.IDENTIFIER, value.toString()));
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
        System.out.println(value.toString());
        if (i < n && c == '"') {
          value.append(c);
          tokenStream.add(new Token(TokenType.STRING, value.toString()));
          ++i;
          continue;
        }
        return false;
      }
      if (Character.isDigit(c)) {
        StringBuilder value = new StringBuilder();
        do {
          value.append(c);
          if (++i == n) break;
          c = source.charAt(i);
        } while (Character.isDigit(c));
        tokenStream.add(new Token(TokenType.INTEGER, value.toString()));
        continue;
      }
      if (c == '<') {
        if (++i == n || source.charAt(i) != '=') {
          tokenStream.add(new Token(TokenType.LESS, "<"));
          continue;
        }
        tokenStream.add(new Token(TokenType.LESS_EQUALS, "<="));
        ++i;
        continue;
      }
      if (c == '>') {
        if (++i == n || source.charAt(i) != '=') {
          tokenStream.add(new Token(TokenType.GREATER, ">"));
          continue;
        }
        tokenStream.add(new Token(TokenType.GREATER_EQUALS, ">="));
        ++i;
        continue;
      }
      if (c == '=') {
        if (++i == n || source.charAt(i) != '=') {
          tokenStream.add(new Token(TokenType.ASSIGNMENT, "="));
          continue;
        }
        tokenStream.add(new Token(TokenType.EQUALS, "=="));
        ++i;
        continue;
      }
      if (c == '!') {
        if (++i == n) return false;
        c = source.charAt(i);
        if (c != '=') return false;
        tokenStream.add(new Token(TokenType.NOT_EQUALS, "!="));
        ++i;
        continue;
      }
      if (symbols.containsKey("" + c)) {
        tokenStream.add(new Token(symbols.get("" + c), "" + c));
        ++i;
        continue;
      }
      return false;
    }
    return true;
  }

  private boolean isReservedWord(String word) {
    return reservedWords.containsKey(word);
  }

  public ArrayList<Token> getTokenStream() {
    return tokenStream;
  }
}
