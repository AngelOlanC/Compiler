package MVC;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import MVC.Model.SymbolTable;
import MVC.Model.SyntaxAnalyzer;

public class Controller implements ActionListener {
  private SyntaxAnalyzer syntaxAnalyzer;
  private View view;

  public Controller(SymbolTable symbolTable, View view) {
    syntaxAnalyzer = new SyntaxAnalyzer(symbolTable);
    this.view = view;
    view.setListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == view.getStartButton()) {
      String sourceCode = view.getCodingArea().getText();
      syntaxAnalyzer.analyze(sourceCode);

      view.getSymbolTablePane().setText(syntaxAnalyzer.getSymbolTable().toString());

      String errorsText = syntaxAnalyzer.getErrorsText();
      if (errorsText.isEmpty()) errorsText = "No errors found";
      view.getErrorsPane().setText(errorsText);
      
      return;
    }
  }
}
