/*
 * I do hereby declare this code to be public domain. 
 * Do whatever the **** you want with it.
 * -CCHall
 */

package edu.prl.kramerlab.script;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import javax.swing.*;
/**
 * Example App to demonstrate how to use the JavascriptEngine. Also demonstrates 
 * how java 8 lambda notation makes Swing programming a lot less painful.
 * @author CCHall
 */
@Deprecated public class JavascriptEngineTest {
	
	
	private JTextArea jsArea;
	
	private JTextArea logArea;
	
	private JButton button;
	
	final JavascriptEngine jsengine;
	
	public JavascriptEngineTest(){
		jsengine = new JavascriptEngine();
	}
	@Deprecated public static void main(String[] args){
		SwingUtilities.invokeLater(() -> {
			try {
				JavascriptEngineTest app = new JavascriptEngineTest();


				JPanel p = app.makeGUI();

				final JFileChooser jfc = new JFileChooser();
				
				// bind java object as variable in the javascript environment
				app.jsengine.bindObject("fileChooser", jfc); 
				// bind java method as a global function in the javascript environment
				app.jsengine.bindMethod(app, "log", Object.class); // note that the class parameters have to match the method declaration EXACTLY
				app.jsengine.bindMethod(app, "sayHello", String.class, Integer.TYPE); 
				
				JFrame frame = new JFrame("Super Easy Nashorn Javascript Engine Test");
				frame.getContentPane().add(p);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
				
			} catch (Exception ex) {
				// unexpected exception
				Logger.getLogger(JavascriptEngineTest.class.getName()).log(Level.SEVERE, null, ex);
			}
		});
	}
	
	
	
	public void log(Object text){
		// IMPORTANT: javascript Strings ARE NOT ALWAYS JAVA STRINGS.
		logArea.append(text.toString()+"\n");
	}
	
	public void sayHello(String text, int n){
		logArea.append("Hi "+text);
		for (int i = 0; i < n; i++){
			logArea.append("!");
		}
		logArea.append("\n");
	}
	
	
	
	private JPanel makeGUI() {
		JPanel vpane = new JPanel();
		vpane.setLayout(new BoxLayout(vpane, BoxLayout.Y_AXIS));
		JLabel l1 = new JLabel("Input javascript here:");
		vpane.add(l1);
		jsArea = new JTextArea(20, 80);
		jsArea.setText("// Super Easy Nashorn Javascript Example App\n" +
				"// bound \"log\" and \"sayHello\" as methods and \"fileChooser\" as a JFileChooser\n" +
				"sayHello(\"Bob\",3);\n" +
				"log(\"Bob is smiling.\");\n" +
				"fileChooser.showOpenDialog(null);\n" +
				"var file = fileChooser.getSelectedFile();\n" +
				"log(\"Sent Bob file '\"+file.getPath()+\"' in an email\");\n" +
				"log(\"Bob is frowning.\");\n");
		JScrollPane jsAreaSP = new JScrollPane(jsArea);
		vpane.add(jsAreaSP);
		JLabel l2 = new JLabel("Output goes here:");
		vpane.add(l2);
		logArea = new JTextArea(20, 80);
		JScrollPane logAreaSP  = new JScrollPane(logArea);
		vpane.add(logAreaSP);
		button = new JButton("eval");
		button.addActionListener((ActionEvent ae)->{
			logArea.setText("");
			// java lambdas FTW!
			Object retval = null;
			try {
				retval = jsengine.eval(jsArea.getText());
			} catch (ScriptException ex) {
				System.err.println("Error on line #"+ex.getLineNumber());
				Logger.getLogger(JavascriptEngineTest.class.getName()).log(Level.SEVERE, null, ex);
				JOptionPane.showMessageDialog(vpane, ex.getMessage());
			} catch (Exception ex) {
				Logger.getLogger(JavascriptEngineTest.class.getName()).log(Level.SEVERE, null, ex);
				JOptionPane.showMessageDialog(vpane, ex.getMessage());
			}
			if(retval != null){
				logArea.append(retval.toString());
			}
		});
		vpane.add(button);
		return vpane;
	}
}
