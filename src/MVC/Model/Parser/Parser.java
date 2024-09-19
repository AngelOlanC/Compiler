package MVC.Model.Parser;

import java.util.ArrayList;

import MVC.Model.Constants.*;
import MVC.Model.Parser.ParseTree;
import MVC.Model.Parser.Node.*;
import MVC.Model.Scanner.Token;

public class Parser {
  private ArrayList<Token> tokenStream;
  private ParseTree parseTree;
  private int i;

  public Parser(ArrayList<Token> tokenStream) {
    this.tokenStream = tokenStream;
    parseTree = new ParseTree();
  }

  public boolean analyze() {
    i = 0;
    return program();
  }

  private boolean program() {
    Node node = parseTree.getRoot();
    if (!match(TokenType.LCB, node)) return false;
    goNext();
    if (!sentences(null, node)) return false;
    goNext();
    return match(TokenType.RCB, node) && i == tokenStream.size() - 1;
  }

  private boolean sentences(Node node, Node parent) {
    if (node == null) {
      node = new NodeNonTerminal(Production.SENTENCES);
      if (!sentences(node, parent)) return false;
      goBack();
      parent.addChild(node);
      return true;
    }
    if (match(TokenType.RCB)) return true;
    if (!sentence(node)) return false;
    goNext();
    return sentences(node, parent);
  }

  private boolean sentence(Node parent) {
    Node node = new NodeNonTerminal(Production.SENTENCE);
    if (matchRW(ReservedWord.READ, node)) {
      goNext();
      if (!match(TokenType.ID, node)) return false;
      parent.addChild(node);
      return true;
    }
    if (matchRW(ReservedWord.PRINT, node)) {
      goNext();
      if (!operation(node)) return false;
      parent.addChild(node);
      return true;
    }
    if (dataType(node)) {
      goNext();
      if (!match(TokenType.ID, node)) return false;
      parent.addChild(node);
      return true;
    }
    if (match(TokenType.ID, node)) {
      goNext();
      if (!match(TokenType.ASSIGN, node)) return false;
      goNext();
      if (!operation(node)) return false;
      parent.addChild(node);
      return true;
    }
    if (matchRW(ReservedWord.WHILE, node)) {
      goNext();
      if (!condition(node)) return false;
      goNext();
      if (!match(TokenType.LCB, node)) return false;
      goNext();
      if (!sentences(null, node)) return false;
      goNext();
      if (!match(TokenType.RCB, node)) return false;
      parent.addChild(node);
      return true;
    }
    if (matchRW(ReservedWord.IF, node)) {
      goNext();
      if (!condition(node)) return false;
      goNext();
      if (!match(TokenType.LCB, node)) return false;
      goNext();
      if (!sentences(null, node)) return false;
      goNext();
      if (!match(TokenType.RCB, node)) return false;
      goNext();
      if (!matchRW(ReservedWord.ELSE, node)) {
        goBack();
        parent.addChild(node);
        return true;
      }
      goNext();
      if (!match(TokenType.LCB, node)) return false;
      goNext();
      if (!sentences(null, node)) return false;
      goNext();
      if (!match(TokenType.RCB, node)) return false;
      parent.addChild(node);
      return true;
    }
    if (!matchRW(ReservedWord.BREAK, node) && 
        !matchRW(ReservedWord.CONTINUE, node) && 
        !matchRW(ReservedWord.EXIT, node)) return false;
    parent.addChild(node);
    return true;
  }

  private boolean operation(Node parent) {
    Node node = new NodeNonTerminal(Production.OPERATION);
    if (data(node)) {
      parent.addChild(node);
      return true;
    }
    if (!match(TokenType.LP, node)) return false;
    goNext();
    if (!operation(node)) return false;
    goNext();
    if (!arithmeticOperator(node)) return false;
    goNext();
    if (!operation(node)) return false;
    goNext();
    if (!match(TokenType.RP, node)) return false;
    parent.addChild(node);
    return true;
  }
  
  private boolean condition(Node parent) {
    Node node = new NodeNonTerminal(Production.CONDITION);
    if (!operation(node)) return false;
    goNext();
    if (!comparationOperator(node)) return false;
    goNext();
    if (!operation(node)) return false;
    parent.addChild(node);
    return true;
  }
  
  private boolean data(Node parent) {
    Node node = new NodeNonTerminal(Production.DATA);
    if (!match(TokenType.LITERAL, node) && !match(TokenType.ID, node)) return false;
    parent.addChild(node);
    return true;
  }
  
  private boolean dataType(Node parent) {
    Node node = new NodeNonTerminal(Production.DATA_TYPE);
    if (!matchRW(ReservedWord.INT, node) && !matchRW(ReservedWord.STRING, node)) return false;
    parent.addChild(node);
    return true;
  }

  private boolean arithmeticOperator(Node parent) {
    Node node = new NodeNonTerminal(Production.ARITHMETIC_OPERATOR);
    if (!match(TokenType.ADD, node) && !match(TokenType.SUB, node)) return false;
    parent.addChild(node);
    return true;
  }

  private boolean comparationOperator(Node parent) {
    Node node = new NodeNonTerminal(Production.COMPARATION_OPERATOR);
    if (!match(TokenType.LESS, node) && !match(TokenType.LEQ, node) &&
        !match(TokenType.GREATER, node) && !match(TokenType.GEQ, node) &&
        !match(TokenType.EQUALS, node) && !match(TokenType.NEQ, node)) return false;
    parent.addChild(node);
    return true;
  }

  private void goNext() { ++i; }
  
  private void goBack() { --i; }
  
  private boolean match(TokenType tokenType) {
    if (i >= tokenStream.size()) return false;
    Token token = tokenStream.get(i);
    if (token.getTokenType() != tokenType) return false;
    return true;
  }

  private boolean match(TokenType tokenType, Node parent) {
    if (i >= tokenStream.size()) return false;
    Token token = tokenStream.get(i);
    if (token.getTokenType() != tokenType) return false;
    parent.addChild(new NodeTerminal(token));
    return true;
  }

  private boolean matchRW(ReservedWord reservedWord, Node parent) {
    if (i >= tokenStream.size()) return false;
    Token token = tokenStream.get(i);
    if (token.getSymbolTableID() != reservedWord.ordinal()) return false;
    parent.addChild(new NodeTerminal(token));
    return true;
  }

  public ParseTree getParseTree() {
    return parseTree;
  }
}