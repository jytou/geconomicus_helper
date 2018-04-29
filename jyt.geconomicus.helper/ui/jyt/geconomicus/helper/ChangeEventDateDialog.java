package jyt.geconomicus.helper;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class ChangeEventDateDialog extends JDialog
{
	private static final String ESCAPE_ACTION = "escape"; //$NON-NLS-1$
	private static final String ENTER_ACTION = "enter"; //$NON-NLS-1$

	private class CancelAction extends AbstractAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent pEvent)
		{
			setVisible(false);
		}
	}

	private class AddAction extends AbstractAction implements ActionListener
	{
		private final JTextField mDateTF;

		private AddAction(JTextField pDateTF)
		{
			mDateTF = pDateTF;
		}

		@Override
		public void actionPerformed(ActionEvent pEvent)
		{
			Date date;
			try
			{
				date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(mDateTF.getText()); //$NON-NLS-1$
			}
			catch (ParseException e)
			{
				JOptionPane.showMessageDialog(ChangeEventDateDialog.this, UIMessages.getString("ChangeEventDateDialog.Error.Message.DateMustRespectFormat"), UIMessages.getString("ChangeEventDateDialog.Error.Title.IncorrectDate"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
			mEntityManager.getTransaction().begin();
			mEvent.setTstamp(date);
			mEntityManager.getTransaction().commit();
			setVisible(false);
		}
	}

	private EntityManager mEntityManager;
	private Event mEvent;

	public ChangeEventDateDialog(final JFrame pParent, final EntityManager pEntityManager, final Event pEvent)
	{
		super(pParent, UIMessages.getString("ChangeEventDateDialog.Title.ChangeEventDate")); //$NON-NLS-1$
		mEvent = pEvent;
		mEntityManager = pEntityManager;
		setSize(500, 300);
		setLocation(200, 200);
		setModal(true);
		final JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.add(new JLabel(UIMessages.getString("ChangeEventDateDialog.Label.EventDate")), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0)); //$NON-NLS-1$
		final JTextField dateTF = new JTextField(50);
		dateTF.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(mEvent.getTstamp())); //$NON-NLS-1$
		mainPanel.add(dateTF, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		dateTF.getInputMap().put(KeyStroke.getKeyStroke((char)10), ChangeEventDateDialog.ENTER_ACTION);
		dateTF.getActionMap().put(ChangeEventDateDialog.ENTER_ACTION, new AddAction(dateTF));
		dateTF.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ChangeEventDateDialog.ESCAPE_ACTION);
		dateTF.getActionMap().put(ChangeEventDateDialog.ESCAPE_ACTION, new CancelAction());
		dateTF.requestFocusInWindow();
		final JPanel buttonsPanel = new JPanel(new FlowLayout());
		final JButton applyButton = new JButton(UIMessages.getString("ChangeEventDateDialog.Button.Label.ChangeDate")); //$NON-NLS-1$
		buttonsPanel.add(applyButton);
		applyButton.addActionListener(new AddAction(dateTF));
		final JButton cancelButton = new JButton(UIMessageKeyProvider.DIALOG_BUTTON_CANCEL.getMessage());
		buttonsPanel.add(cancelButton);
		cancelButton.addActionListener(new CancelAction());
		mainPanel.add(buttonsPanel, new GridBagConstraints(0, 10, 2, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		getContentPane().add(mainPanel);
	}
}
