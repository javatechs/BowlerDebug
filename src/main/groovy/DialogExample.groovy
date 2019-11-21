// Example ad hoc dialog
import javax.swing.*

def prompt = {
  JFrame jframe = new JFrame()
  String answer = JOptionPane.showInputDialog(jframe, it)
  jframe.dispose()
  answer
}

def first = prompt("Enter a number")
def second = prompt("Enter another number")
System.out.println(first + second)