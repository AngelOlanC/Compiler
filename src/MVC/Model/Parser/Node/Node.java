package MVC.Model.Parser.Node;

import java.util.ArrayList;

public class Node {
  private final ArrayList<Node> children;

  public Node() {
    children = new ArrayList<>();
  }
  
  public void addChild(Node node) {
    children.add(node);
  }

  public ArrayList<Node> getChildren() {
    return children;
  }
}