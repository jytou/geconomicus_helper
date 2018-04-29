package jyt.geconomicus.helper;

import java.awt.Component;
import java.text.MessageFormat;

import javax.swing.JOptionPane;

public class UIUtil
{
	public static void showExceptionMessage(Component pComponent, Throwable pThrowable)
	{
		JOptionPane.showMessageDialog(pComponent, MessageFormat.format(UIMessageKeyProvider.DIALOG_ERROR_MESSAGE.getMessage(), new Object[] { pThrowable.getClass().getName(), pThrowable.getMessage()}), UIMessageKeyProvider.DIALOG_TITLE_ERROR.getMessage(), JOptionPane.ERROR_MESSAGE);
	}
}
