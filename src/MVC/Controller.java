package MVC;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import MVC.Model.Parser.Parser;
import MVC.Model.Scanner.Scanner;
import MVC.Model.Scanner.Token;
import MVC.Model.SemanticAnalysis.SemanticAnalyzer;
import MVC.Model.SymbolTable.SymbolTable;

public class Controller implements ActionListener, KeyListener {
  private View view;
  private SymbolTable symbolTable;
  private Scanner scanner;
  private Parser parser;
  private SemanticAnalyzer semanticAnalyzer;

  public Controller(View view) {
    this.view = view;
    view.setListener(this);
    view.getStartParserButton().setEnabled(false);
    view.getStartSemanticButton().setEnabled(false);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == view.getStartScannerButton()) {
      symbolTable = new SymbolTable();

      String sourceCode = view.getCodingArea().getText(); 
      view.getStartSemanticButton().setEnabled(false);

      scanner = new Scanner(symbolTable, sourceCode);
      if (!scanner.analyze()) {
        view
          .getTokensPane()
          .setText("ERROR"); 
        view.getStartParserButton().setEnabled(false);
        view.getStartSemanticButton().setEnabled(false);
        return;
      }
      ArrayList<Token> tokenStream = scanner.getTokenStream();
      view.getTokensPane()
        .setText(
          tokenStream
            .stream()
            .reduce("", (partialString, token) -> partialString + token.toString() + "\n", String::concat)
        );
      view.getStartScannerButton().setEnabled(false);
      view.getStartParserButton().setEnabled(true);
      return;
    }

    if (e.getSource() == view.getStartParserButton()) {
      ArrayList<Token> tokenStream = scanner.getTokenStream();
      parser = new Parser(tokenStream);
      if (!parser.analyze()) {
        view
          .getParserPane()
          .setText("ERROR");
          view.getStartSemanticButton().setEnabled(false);
          return;
      }
      view
        .getParserPane()
        .setText("CORRECT"); 
      System.out.println(parser.getParseTree());
      view.getStartParserButton().setEnabled(false);
      view.getStartSemanticButton().setEnabled(true);
      return;
    }

    if (e.getSource() == view.getStartSemanticButton()) {
      semanticAnalyzer = new SemanticAnalyzer(symbolTable, parser.getParseTree());
      if (!semanticAnalyzer.analyze()) {
        view
          .getSemanticPane()
          .setText("ERROR");
          return;
      }
      view
      .getSemanticPane()
      .setText("CORRECT"); 
      view.getStartSemanticButton().setEnabled(false);
      return;
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {}

  @Override
  public void keyReleased(KeyEvent e) {}

  @Override
  public void keyTyped(KeyEvent e) {
    view.resetResultsText();
    view.getStartScannerButton().setEnabled(true);
    view.getStartParserButton().setEnabled(false);
    view.getStartSemanticButton().setEnabled(false);
  }
}
