package MVC;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;

import MVC.Model.Scanner;
import MVC.Model.Parser;
import MVC.Model.Token;

public class Controller implements ActionListener {
  private View view;

  public Controller(View view) {
    this.view = view;
    view.setListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == view.getStartButton()) {
      view.resetResultsText();

      String sourceCode = view.getCodingArea().getText(); 

      Scanner scanner = new Scanner(sourceCode);
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
      return;
    }
  }
}
