package MVC.Model.Scanner;

import MVC.Model.Constants.TokenType;

public class Token {
  private final TokenType tokenType;
  private final String value;
  private final int symbolTableID;

  public Token(TokenType tokenType, String value, Integer symbolTableID) {
    this.tokenType = tokenType;
    this.value = value;
    this.symbolTableID = symbolTableID;
  }
  
  public TokenType getTokenType() {
    return tokenType;
  }

  public String getValue() {
    return value;
  }

  public int getSymbolTableID() {
    return symbolTableID;
  }

  @Override
  public String toString() {
    return "<" + tokenType.name() + ", " + value + ", " + symbolTableID + ">";
  }
}
