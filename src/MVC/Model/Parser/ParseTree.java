package MVC.Model.Parser;

import java.util.ArrayList;

import MVC.Model.Constants.Production;
import MVC.Model.Scanner.Token;

public class ParseTree {
  private Node root;

  public ParseTree() {
    root = new Node(Production.PROGRAM, null);
  }

  public Node getRoot() {
    return root;
  }

  private StringBuilder traverse(Node node, int spacing) {
    StringBuilder text = new StringBuilder();
    text.append(" ".repeat(spacing) + node.toString() + "\n");
    for (Node child : node.getChildren()) text.append(traverse(child, spacing + 4));
    return text;
  }

  @Override
  public String toString() {
    return traverse(root, 0).toString();
  }

  public static class Node {
    private Production production;
    private Token token;
    private ArrayList<Node> children;

    public Node(Production production, Token token) {
      this.production = production;
      this.token = token;
      children = new ArrayList<>();
    }
    
    public void addChild(Node node) {
      children.add(node);
    }

    public Production getProduction() {
      return production;
    }

    public Token getToken() {
      return token;
    }

    public ArrayList<Node> getChildren() {
      return children;
    }

    @Override
    public String toString() {
      String productionName = production != null ? production.name() : "XXXXX";
      String tokenTypeName = token != null ? token.getTokenType().name() : "XXXXX";
      String tokenValue = token != null ? token.getValue() : "XXXXX";
      return productionName + " " + tokenTypeName + " " + tokenValue;
    }
  }
}
