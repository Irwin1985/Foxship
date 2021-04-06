package v1;

import javax.swing.JOptionPane;

public class Tests {
	public static void main(String[] args) {
		String[] choices = {"Java", "C++", "JavaScript", "COBOL"};
		int response = JOptionPane.showOptionDialog(
		                               null                    
		                             , "Which is your favourite programming language?"    
		                             , "Language Poll"            
		                             , JOptionPane.CANCEL_OPTION 
		                             , 2
		                             , null                    
		                             , choices                   
		                             , "None of your business"  
		                           );	
		System.out.println(response);
	}
}
