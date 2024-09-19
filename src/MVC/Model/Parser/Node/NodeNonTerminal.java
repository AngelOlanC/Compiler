package MVC.Model.Parser.Node;

import MVC.Model.Constants.Production;

public class NodeNonTerminal extends Node {
  private final Production production;
  
  public NodeNonTerminal(Production production) {
    super();
    this.production = production;
  }

  public Production getProduction() {
    return production;
  }

  @Override
  public String toString() {
    return "NON TERMINAL " + production.name();
  }
}
