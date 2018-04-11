package jyt.geconomicus.helper;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.persistence.EntityManager;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

public class ChangeDescriptionDialog extends JDialog
{
	private class CancelAction extends AbstractAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent pE)
		{
			setVisible(false);
		}
	}

	private class AddAction extends AbstractAction implements ActionListener
	{
		private JTextArea mDescriptionTA;

		private AddAction(JTextArea pDescriptionTA)
		{
			mDescriptionTA = pDescriptionTA;
		}

		@Override
		public void actionPerformed(ActionEvent pEvent)
		{
			mEntityManager.getTransaction().begin();
			mGame.setDescription(mDescriptionTA.getText());
			mEntityManager.getTransaction().commit();
			setVisible(false);
		}
	}

	private EntityManager mEntityManager;
	private Game mGame;

	public ChangeDescriptionDialog(final JFrame pParent, final Game pGame, final EntityManager pEntityManager)
	{
		super(pParent, "Description / commentaires");
		mGame = pGame;
		mEntityManager = pEntityManager;
		setSize(500, 400);
		setLocation(200, 200);
		setModal(true);
		final JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.add(new JLabel("Description"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		final JTextArea descriptionTA = new JTextArea(mGame.getDescription());
		mainPanel.add(new JScrollPane(descriptionTA), new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		descriptionTA.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "enter");
		descriptionTA.getActionMap().put("enter", new AddAction(descriptionTA));
		descriptionTA.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
		descriptionTA.getActionMap().put("escape", new CancelAction());
		final JPanel buttonsPanel = new JPanel(new FlowLayout());
		final JButton addButton = new JButton("Appliquer");
		buttonsPanel.add(addButton);
		addButton.addActionListener(new AddAction(descriptionTA));
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
				descriptionTA.requestFocus();
			}
		});
	}
}
