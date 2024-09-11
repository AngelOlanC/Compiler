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
  private JTextPane tokensPane, parserPane;
  private JButton startButton;

  public View() {
    setSize(1300, 600);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(null);
    setVisible(true);

    makeInterface();

    revalidate();
    repaint();
  }

  private void makeInterface() {
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

    JLabel textTokensPane = new JLabel("TOKENS");
    textTokensPane.setFont(outerTextFont);
    textTokensPane.setBounds(700, 10, 1000, 50);
    add(textTokensPane);

    tokensPane = new JTextPane();
    tokensPane.setEnabled(false);
    tokensPane.setBackground(Color.WHITE);
    tokensPane.setOpaque(true);
    tokensPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    tokensPane.setDisabledTextColor(Color.BLACK);
    tokensPane.setFont(innerTextFont);

    JScrollPane scrollSymbol = new JScrollPane(tokensPane);
    scrollSymbol.setBounds(700, 50, 250, 400);
    add(scrollSymbol);

    JLabel textParserPane = new JLabel("PARSER");
    textParserPane.setFont(outerTextFont);
    textParserPane.setBounds(1000, 10, 1000, 50);
    add(textParserPane);

    parserPane = new JTextPane();
    parserPane.setEnabled(false);
    parserPane.setBackground(Color.WHITE);
    parserPane.setOpaque(true);
    parserPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    parserPane.setDisabledTextColor(Color.BLACK);
    parserPane.setFont(innerTextFont);

    JScrollPane scrollParser = new JScrollPane(parserPane);
    scrollParser.setBounds(1000, 50, 250, 400);
    add(scrollParser);

    add(startButton = new JButton());
    startButton.setBounds(600, 500, 150, 50);
    startButton.setText("Scan");
  }

  public void setListener(Controller controller) {
    startButton.addActionListener(controller);
  }

  public void resetResultsText() {
    tokensPane.setText("");
    parserPane.setText("");
  }

  public JTextArea getCodingArea() {
    return codingArea;
  }

  public JTextPane getTokensPane() {
    return tokensPane;
  }

  public JTextPane getParserPane() {
    return parserPane;
  }

  public JButton getStartButton() {
    return startButton;
  }
}
