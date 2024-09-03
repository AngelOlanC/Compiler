package MVC;


import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

public class View extends JFrame {
  private JTextArea codingArea;
  private JTextPane symbolTablePane, errorsPane;
  private JButton startButton;

  public View() {
    setSize(1000, 600);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(null);
    setVisible(true);
    
    Font outerTextFont = new Font("New Courier", Font.PLAIN, 30);
    Font innerTextFont = new Font("New Courier", Font.PLAIN, 15);

    JLabel textCodingArea = new JLabel("CODING AREA");
    textCodingArea.setFont(outerTextFont);
    textCodingArea.setBounds(50, 10, 1000, 50);
    add(textCodingArea);

    codingArea = new JTextArea();
    codingArea.setBackground(Color.WHITE);
    codingArea.setOpaque(true);
    codingArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    codingArea.setFont(innerTextFont);

    JScrollPane scrollCode = new JScrollPane(codingArea);
    scrollCode.setBounds(50, 50, 600, 400);
    add(scrollCode);

    JLabel textSymbolTablePane = new JLabel("TOKENS");
    textSymbolTablePane.setFont(outerTextFont);
    textSymbolTablePane.setBounds(700, 10, 1000, 50);
    add(textSymbolTablePane);

    symbolTablePane = new JTextPane();
    symbolTablePane.setEnabled(false);
    symbolTablePane.setBackground(Color.WHITE);
    symbolTablePane.setOpaque(true);
    symbolTablePane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    symbolTablePane.setDisabledTextColor(Color.BLACK);
    symbolTablePane.setFont(innerTextFont);

    JScrollPane scrollSymbol = new JScrollPane(symbolTablePane);
    scrollSymbol.setBounds(700, 50, 250, 300);
    add(scrollSymbol);

    JLabel textErrorsPane = new JLabel("ERRORS");
    textErrorsPane.setFont(outerTextFont);
    textErrorsPane.setBounds(700, 350, 1000, 50);
    add(textErrorsPane);
    
    errorsPane = new JTextPane();
    errorsPane.setEnabled(false);
    errorsPane.setBackground(Color.WHITE);
    errorsPane.setOpaque(true);
    errorsPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    errorsPane.setDisabledTextColor(Color.BLACK);
    errorsPane.setFont(innerTextFont);

    JScrollPane scrollErrors = new JScrollPane(errorsPane);
    scrollErrors.setBounds(700, 400, 250, 150);
    add(scrollErrors);

    add(startButton = new JButton());
    startButton.setBounds(300, 500, 150, 50);
    startButton.setText("Scan");
    revalidate();
    repaint();
  }

  public void setListener(Controller controller) {
    startButton.addActionListener(controller);
  }

  public JTextArea getCodingArea() {
    return codingArea;
  }

  public JTextPane getSymbolTablePane() {
    return symbolTablePane;
  }

  public JTextPane getErrorsPane() {
    return errorsPane;
  }

  public JButton getStartButton() {
    return startButton;
  }
}
