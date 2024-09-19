package MVC;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;

import MVC.Model.Parser.Parser;
import MVC.Model.Scanner.Scanner;
import MVC.Model.Scanner.Token;
import MVC.Model.SemanticAnalysis.SemanticAnalyzer;
import MVC.Model.SymbolTable.SymbolTable;

public class Controller implements ActionListener {
  private View view;
  private SymbolTable symbolTable;

  public Controller(View view) {
    this.view = view;
    view.setListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == view.getStartButton()) {
      view.resetResultsText();
      symbolTable = new SymbolTable();

      String sourceCode = view.getCodingArea().getText(); 

      Scanner scanner = new Scanner(symbolTable, sourceCode);
      if (!scanner.analyze()) {
        view
          .getTokensPane()
          .setText("ERROR"); 
        return;
      }
      ArrayList<Token> tokenStream = scanner.getTokenStream();
      view.getTokensPane()
        .setText(
          tokenStream
            .stream()
            .reduce("", (partialString, token) -> partialString + token.toString() + "\n", String::concat)
        );

      Parser parser = new Parser(tokenStream);
      if (!parser.analyze()) {
        view
          .getParserPane()
          .setText("ERROR");
          return;
      }
      view
        .getParserPane()
        .setText("CORRECT"); 
      System.out.println(parser.getParseTree());

      SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(symbolTable, parser.getParseTree());
      if (!semanticAnalyzer.analyze()) {
        view
          .getSemanticPane()
          .setText("ERROR");
          return;
      }
      view
      .getSemanticPane()
      .setText("CORRECT"); 
      return;
    }
  }
}
