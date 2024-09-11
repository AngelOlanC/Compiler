package MVC.Model;

import java.util.ArrayList;

public class Parser {
  private ArrayList<Token> tokenStream;
  private int i;

  public Parser(ArrayList<Token> tokenStream) {
    this.tokenStream = tokenStream;
  }

  public boolean analyze() {
    i = 0;
    return program();
  }

  private boolean program() {
    if (!match(TokenType.LEFT_CURLY_BRACE)) return false;
    goNext();
    if (!sentences()) return false;
    goNext();
    if (!match(TokenType.RIGHT_CURLY_BRACE)) return false;
    return i == tokenStream.size() - 1;
  }

  private boolean sentences() {
    if (match(TokenType.RIGHT_CURLY_BRACE)) {
      goBack();
      return true;
    }
    if (sentence()) {
      goNext();
      return sentences();
    }
    return false;
  }

  private boolean sentence() {
    if (match(TokenType.RW_READ)) {
      goNext();
      return match(TokenType.IDENTIFIER);
    }
    if (match(TokenType.RW_PRINT)) {
      goNext();
      return data();
    }
    if (dataType()) {
      goNext();
      return match(TokenType.IDENTIFIER);
    }
    if (match(TokenType.IDENTIFIER)) {
      goNext();
      if (!match(TokenType.ASSIGNMENT)) return false;
      goNext();
      return operation();
    }
    if (match(TokenType.RW_WHILE)) {
      goNext();
      if (!condition()) return false;
      goNext();
      if (!match(TokenType.LEFT_CURLY_BRACE)) return false;
      goNext();
      if (!sentences()) return false;
      goNext();
      return match(TokenType.RIGHT_CURLY_BRACE);
    }
    if (match(TokenType.RW_IF)) {
      goNext();
      if (!condition()) return false;
      goNext();
      if (!match(TokenType.LEFT_CURLY_BRACE)) return false;
      goNext();
      if (!sentences()) return false;
      goNext();
      if (!match(TokenType.RIGHT_CURLY_BRACE)) return false;
      goNext();
      if (!match(TokenType.RW_ELSE)) return true;
      goNext();
      if (!match(TokenType.LEFT_CURLY_BRACE)) return false;
      goNext();
      if (!sentence()) return false;
      goNext();
      return match(TokenType.RIGHT_CURLY_BRACE);
    }
    return match(TokenType.RW_BREAK) || match(TokenType.RW_CONTINUE) || match(TokenType.RW_EXIT);
  }

  
  private boolean operation() {
    if (data()) return true;
    if (!match(TokenType.LEFT_PARENTHESES)) return false;
    goNext();
    if (!operation()) return false;
    goNext();
    if (!arithmeticOperator()) return false;
    goNext();
    if (!operation()) return false;
    goNext();
    return match(TokenType.RIGHT_PARENTHESES);
  }
  
  private boolean condition() {
    if (!operation()) return false;
    goNext();
    if (!comparationOperator()) return false;
    goNext();
    return operation();
  }
  
  private boolean data() {
    return literal() || match(TokenType.IDENTIFIER);  
  }
  
  private boolean literal() {
    return match(TokenType.STRING) || match(TokenType.INTEGER);
  }
  
  private boolean dataType() {
    return match(TokenType.RW_INT) || match(TokenType.RW_STRING);
  }

  private boolean arithmeticOperator() {
    return match(TokenType.ADD) || match(TokenType.SUBSTRACT);
  }

  private boolean comparationOperator() {
    return match(TokenType.LESS) || match(TokenType.LESS_EQUALS) ||
           match(TokenType.GREATER) || match(TokenType.GREATER_EQUALS) ||
           match(TokenType.EQUALS) || match(TokenType.NOT_EQUALS);
  }

  private void goNext() { ++i; }

  private void goBack() { --i; }
  
  private boolean match(TokenType tokenType) {
    return i < tokenStream.size() && tokenStream.get(i).getType() == tokenType;
  }
}
