package MVC.Model.Parser;

import MVC.Model.Constants.Production;
import MVC.Model.Parser.Node.Node;
import MVC.Model.Parser.Node.NodeNonTerminal;

public class ParseTree {
  private Node root;

  public ParseTree() {
    root = new NodeNonTerminal(Production.PROGRAM);
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
}
