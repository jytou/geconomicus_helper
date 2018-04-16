package jyt.geconomicus.helper;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
				date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(mDateTF.getText());
			}
			catch (ParseException e)
			{
				JOptionPane.showMessageDialog(ChangeEventDateDialog.this, "La date doit être au format 2018/04/15 22:30:27", "Date incorrecte", JOptionPane.ERROR_MESSAGE);
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
		super(pParent, "Changer la date d'un événement");
		mEvent = pEvent;
		mEntityManager = pEntityManager;
		setSize(500, 300);
		setLocation(200, 200);
		setModal(true);
		final JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.add(new JLabel("Date"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		final JTextField dateTF = new JTextField(50);
		dateTF.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(mEvent.getTstamp()));
		mainPanel.add(dateTF, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		dateTF.getInputMap().put(KeyStroke.getKeyStroke((char)10), "enter");
		dateTF.getActionMap().put("enter", new AddAction(dateTF));
		dateTF.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
		dateTF.getActionMap().put("escape", new CancelAction());
		final JPanel buttonsPanel = new JPanel(new FlowLayout());
		final JButton applyButton = new JButton("Changer la date !");
		buttonsPanel.add(applyButton);
		applyButton.addActionListener(new AddAction(dateTF));
		final JButton cancelButton = new JButton("Annuler");
		buttonsPanel.add(cancelButton);
		cancelButton.addActionListener(new CancelAction());
		mainPanel.add(buttonsPanel, new GridBagConstraints(0, 10, 2, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		getContentPane().add(mainPanel);
		addFocusListener(new FocusListener()
		{
			@Override
			public void focusLost(FocusEvent pE)
			{
			}
			
			@Override
			public void focusGained(FocusEvent pE)
			{
				dateTF.requestFocus();
			}
		});
	}
}
