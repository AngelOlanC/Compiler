package MVC.Model.Parser.Node;

import MVC.Model.Scanner.Token;

public class NodeTerminal extends Node {
  private final Token token;
  
  public NodeTerminal(Token token) {
    super();
    this.token = token;
  }

  public Token getToken() {
    return token;
  }

  @Override
  public String toString() {
    return "TERMINAL " + token.toString();
  }
}
