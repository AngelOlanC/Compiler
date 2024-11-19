package MVC.Model.Constants;

public enum TokenType {
  RW,
  ID,
  LITERAL,
  LP("("),
  RP(")"),
  LCB("{"),
  RCB("}"),
  ASSIGN("="),
  EQUALS("=="),
  NEQ("!="),
  LESS("<"),
  LEQ("<="),
  GREATER(">"),
  GEQ(">="),
  ADD("+"),
  SUB("-"),
  MUL("*"),
  DIV("/"),
  REM("%");
  
  private String value;

  private TokenType() {
    this.value = null;
  }
  private TokenType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static TokenType match(String value) {
    for (TokenType tokenType : TokenType.values()) {
      if (tokenType.getValue() != null && tokenType.getValue().equals(value)) {
        return tokenType;
      }
    }
    return null;
  }
}
