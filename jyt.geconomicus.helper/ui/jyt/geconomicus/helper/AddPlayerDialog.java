package jyt.geconomicus.helper;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.persistence.EntityManager;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * A dialog to add a new player or rename an existing one. It has only one field for the player name.
 * @author jytou
 */
public class AddPlayerDialog extends JDialog
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
		private final JTextField mNameTF;

		private AddAction(JTextField pNameTF)
		{
			mNameTF = pNameTF;
		}

		@Override
		public void actionPerformed(ActionEvent pEvent)
		{
			mEntityManager.getTransaction().begin();
			if (mPlayer == null)
				mPlayer = new Player(mGame, mNameTF.getText());
			else
				mPlayer.setName(mNameTF.getText());
			mEntityManager.getTransaction().commit();
			setVisible(false);
		}
	}

	private EntityManager mEntityManager;
	private Game mGame;
	private Player mPlayer = null;

	/**
	 * Renames an existing player.
	 * @param pParent
	 * @param pGame
	 * @param pEntityManager
	 * @param pExistingPlayer
	 */
	public AddPlayerDialog(final JFrame pParent, final Game pGame, final EntityManager pEntityManager, Player pExistingPlayer)
	{
		super(pParent, UIMessages.getString("Dialog.Player.Title.Rename")); //$NON-NLS-1$
		mPlayer = pExistingPlayer;
		initWindow(pGame, pEntityManager);
	}

	/**
	 * Creates a new player.
	 * @param pParent
	 * @param pGame
	 * @param pEntityManager
	 */
	public AddPlayerDialog(final JFrame pParent, final Game pGame, final EntityManager pEntityManager)
	{
		super(pParent, UIMessages.getString("Dialog.Player.Title.Add")); //$NON-NLS-1$
		initWindow(pGame, pEntityManager);
	}

	/**
	 * Creates the window contents.
	 * @param pGame
	 * @param pEntityManager
	 */
	public void initWindow(final Game pGame, final EntityManager pEntityManager)
	{
		mGame = pGame;
		mEntityManager = pEntityManager;
		setSize(500, 200);
		setLocation(200, 200);
		setModal(true);

		final JPanel mainPanel = new JPanel(new GridBagLayout());
		final Insets insets = new Insets(4, 4, 4, 4);

		final JLabel warningText = new JLabel(UIMessages.getString("Dialog.Player.Warning")); //$NON-NLS-1$
		mainPanel.add(warningText, new GridBagConstraints(0, 0, 2, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		// Label, TextField
		mainPanel.add(new JLabel(UIMessageKeyProvider.DIALOG_PLAYER_NAME_LABEL.getMessage()), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		final JTextField nameTF = new JTextField(50);
		if (mPlayer != null)
			nameTF.setText(mPlayer.getName());
		mainPanel.add(nameTF, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		nameTF.getInputMap().put(KeyStroke.getKeyStroke((char)10), AddPlayerDialog.ENTER_ACTION);
		nameTF.getActionMap().put(AddPlayerDialog.ENTER_ACTION, new AddAction(nameTF));
		nameTF.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), AddPlayerDialog.ESCAPE_ACTION);
		nameTF.getActionMap().put(AddPlayerDialog.ESCAPE_ACTION, new CancelAction());
		nameTF.requestFocusInWindow();

		// Buttons
		final JPanel buttonsPanel = new JPanel(new FlowLayout());
		final JButton addButton = new JButton(mPlayer != null ? UIMessageKeyProvider.DIALOG_BUTTON_RENAME.getMessage() : UIMessageKeyProvider.DIALOG_BUTTON_ADD.getMessage());
		buttonsPanel.add(addButton);
		addButton.addActionListener(new AddAction(nameTF));
		final JButton cancelButton = new JButton(UIMessageKeyProvider.DIALOG_BUTTON_CANCEL.getMessage());
		buttonsPanel.add(cancelButton);
		cancelButton.addActionListener(new CancelAction());
		mainPanel.add(buttonsPanel, new GridBagConstraints(0, 10, 2, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		getContentPane().add(mainPanel);
	}

	public Player getNewPlayer()
	{
		return mPlayer;
	}
}
