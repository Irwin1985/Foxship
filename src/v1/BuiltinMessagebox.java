package v1;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class BuiltinMessagebox implements BuiltinBase {
	public Obj execute(List<Obj> objArgs, GlobalEnv globalEnv) {
		int argSize = objArgs.size();		
		Obj objContent = null, objTitle = null, objButtons = null, objTimeout = null;
		
		if (argSize == 0) {
			return new ObjError("Too few arguments.");
		}
		String content = "", title = "Foxship";
		int buttonType = 0; 
		int timeout = 0;
		
		// content
		if (argSize >= 1) {
			objContent = objArgs.get(0);
			content = objContent.inspect();
		}
		// buttons
		if (argSize >= 2) {			
			objButtons = objArgs.get(1);
			if (objButtons.type() != ObjType.NUMBER_OBJ) {
				return new ObjError("button type in messagebox must be an INTEGER.");
			}
			buttonType = (int)((ObjNumber)objButtons).value;
		}
		// title
		if (argSize >= 3) {			
			objTitle = objArgs.get(2);
			title = objTitle.inspect();
		}
		// timeout
		if (argSize >= 4) {
			objTimeout = objArgs.get(3);
			if (objTimeout.type() != ObjType.NUMBER_OBJ) {
				return new ObjError("timeout parameter in messagebox must be an INTEGER.");
			}
			timeout = (int)((ObjNumber)objTimeout).value;
		}		

		// parse buttons and show message
		int result = 0, nReturn = 1;
		switch(buttonType) {
		case 16:
		{
			String[] opt = {"Aceptar"};
			result = showMessagebox(content, title, opt, JOptionPane.ERROR_MESSAGE);
		}
			break;
		case 17:{			
			String[] opt = {"Aceptar", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.ERROR_MESSAGE);
		}
			break;
		case 18:{
			String[] opt = {"Abortar", "Reintentar", "Ignorar"};
			result = showMessagebox(content, title, opt, JOptionPane.ERROR_MESSAGE);			
		}
			break;
		case 19:{
			String[] opt = {"Sí", "No", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.ERROR_MESSAGE);			
		}
			break;
		case 20:{
			String[] opt = {"Sí", "No"};
			result = showMessagebox(content, title, opt, JOptionPane.ERROR_MESSAGE);
		}
			break;
		case 21:{
			String[] opt = {"Reintentar", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.ERROR_MESSAGE);			
		}
			break;
		case 32:{
			String[] opt = {"Aceptar"};
			result = showMessagebox(content, title, opt, JOptionPane.QUESTION_MESSAGE);
		}
		break;
		case 33:{
			String[] opt = {"Aceptar", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.QUESTION_MESSAGE);
		}
		break;
		case 34:{
			String[] opt = {"Abortar", "Reintentar", "Ignorar"};
			result = showMessagebox(content, title, opt, JOptionPane.QUESTION_MESSAGE);
		}
		break;
		case 35:{
			String[] opt = {"Sí", "No", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.QUESTION_MESSAGE);
		}
		break;
		case 36:{
			String[] opt = {"Sí", "No"};
			result = showMessagebox(content, title, opt, JOptionPane.QUESTION_MESSAGE);
		}
		break;
		case 37:{
			String[] opt = {"Reintentar", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.QUESTION_MESSAGE);
		}		
		break;
		case 48:{
			String[] opt = {"Aceptar"};
			result = showMessagebox(content, title, opt, JOptionPane.WARNING_MESSAGE);
		}
		break;
		case 49:{
			String[] opt = {"Aceptar", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.WARNING_MESSAGE);
		}
		break;
		case 50:{
			String[] opt = {"Abortar", "Reintentar", "Ignorar"};
			result = showMessagebox(content, title, opt, JOptionPane.WARNING_MESSAGE);
		}
		break;
		case 51:{
			String[] opt = {"Sí", "No", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.WARNING_MESSAGE);
		}
		break;
		case 52:{
			String[] opt = {"Sí", "No"};
			result = showMessagebox(content, title, opt, JOptionPane.WARNING_MESSAGE);
		}
		break;
		case 53:{
			String[] opt = {"Reintentar", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.WARNING_MESSAGE);
		}
		break;		
		case 64:{
			String[] opt = {"Aceptar"};
			result = showMessagebox(content, title, opt, JOptionPane.INFORMATION_MESSAGE);
		}
		break;
		case 65:{
			String[] opt = {"Aceptar", "Calcelar"};
			result = showMessagebox(content, title, opt, JOptionPane.INFORMATION_MESSAGE);
		}
		break;
		case 66:{
			String[] opt = {"Abortar", "Reintentar", "Ignorar"};
			result = showMessagebox(content, title, opt, JOptionPane.INFORMATION_MESSAGE);
		}
		break;
		case 67:{
			String[] opt = {"Sí", "No", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.INFORMATION_MESSAGE);
		}
		break;
		case 68:{
			String[] opt = {"Sí", "No"};
			result = showMessagebox(content, title, opt, JOptionPane.INFORMATION_MESSAGE);
		}
		break;
		case 69:{
			String[] opt = {"Reintentar", "Cancelar"};
			result = showMessagebox(content, title, opt, JOptionPane.INFORMATION_MESSAGE);
		}
		break;
		default:
			String[] opt = {"Aceptar"};
			result = showMessagebox(content, title, opt, JOptionPane.INFORMATION_MESSAGE);			
		}
		int[] opt1 = {0,16,32,48,64};
		int[] opt2 = {17, 33, 49, 65, (1 + 16 + 256), (1 + 32 + 256), (1 + 48 + 256), (1 + 64 + 256)};
		int[] opt3 = {18, 34, 50, 66, (2 + 16 + 256), (2 + 16 + 512), (2 + 32 + 256), (2 + 32 + 512), (2 + 48 + 256), (2 + 48 + 512), (2 + 64 + 256), (2 + 64 + 512)};
		int[] opt4 = {19, 35, 51, 67, (3 + 16 + 256), (3 + 16 + 512), (3 + 32 + 256), (3 + 32 + 512), (3 + 48 + 256), (3 + 48 + 512), (3 + 64 + 256), (3 + 64 + 512)};
		int[] opt5 = {20, 36, 52, 68, (4 + 16 + 256), (4 + 16 + 512), (4 + 32 + 256), (4 + 32 + 512), (4 + 48 + 256), (4 + 48 + 512), (4 + 64 + 256), (4 + 64 + 512)};
		int[] opt6 = {21, 37, 53, 69, (5 + 16 + 256), (5 + 16 + 512), (5 + 32 + 256), (5 + 32 + 512), (5 + 48 + 256), (5 + 48 + 512), (5 + 64 + 256), (5 + 64 + 512)};

		if (isInIntegerList(buttonType, opt1)) {
			return new ObjNumber(1);
		}
		if (isInIntegerList(buttonType, opt2)) {
			nReturn = result == 1 ? 1 : 2;
		}
		if (isInIntegerList(buttonType, opt3)) {
			if (result == 0) {
				nReturn = 3;
			} else if (result == 1) {
				nReturn = 4;
			} else if (result == 2) {
				nReturn = 5;
			}
		}
		if (isInIntegerList(buttonType, opt4)) {
			if (result == 0) {
				nReturn = 6;
			} else if (result == 1) {
				nReturn = 7;
			} else if (result == 2) {
				nReturn = 2;
			}
		}
		if (isInIntegerList(buttonType, opt5)) {
			if (result == 0) {
				nReturn = 6;
			} else if (result == 1) {
				nReturn = 7;
			}
		}
		if (isInIntegerList(buttonType, opt6)) {
			if (result == 0) {
				nReturn = 4;
			} else if (result == 1) {
				nReturn = 2;
			}
		}
		return new ObjNumber(nReturn);
	}
	private boolean isInIntegerList(int compareFrom, int...numbers) {
		for (int number : numbers) {
			if (number == compareFrom) {
				return true;
			}
		}
		return false;
	}
	private int showMessagebox(String content, String title, String[] choices, int buttons) {
		JFrame jf=new JFrame();
		jf.setAlwaysOnTop(true);
		
		int response = JOptionPane.showOptionDialog(
				jf,
                content,
                title,
                JOptionPane.CANCEL_OPTION,
                buttons,
                null,
                choices,
                "None");	
		return response;
	}
}
