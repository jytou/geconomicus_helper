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
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import jyt.geconomicus.helper.Event.EventType;

public class CreditActionDialog extends JDialog
{
	private class CancelAction extends AbstractAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent pEvent)
		{
			setVisible(false);
		}
	}

	public enum Purpose { MONEY_MASS_CHANGE, BANK_INVESTMENT, NEW_OR_REIMB_CREDIT, DEFAULT, PLAYER_ASSESSMENT_DEBT_MONEY, PLAYER_ASSESSMENT_LIBRE_MONEY };
	private final static Set<Purpose> SHOW_CARDS_PURPOSE = new HashSet<>();
	static
	{
		SHOW_CARDS_PURPOSE.add(Purpose.BANK_INVESTMENT);
		SHOW_CARDS_PURPOSE.add(Purpose.DEFAULT);
		SHOW_CARDS_PURPOSE.add(Purpose.PLAYER_ASSESSMENT_DEBT_MONEY);
		SHOW_CARDS_PURPOSE.add(Purpose.PLAYER_ASSESSMENT_LIBRE_MONEY);
	}

	// Text fields
	private JTextField mPrincipalTF;
	private JTextField mInterestTF;
	private JTextField mWeakCoinsTF;
	private JTextField mMediumCoinsTF;
	private JTextField mStrongCoinsTF;
	private JTextField mWeakCardsTF;
	private JTextField mMediumCardsTF;
	private JTextField mStrongCardsTF;
	// Return values
	private EventType mEventType = null;
	private String mPrincipalLabel;
	private int mPrincipal = 0;
	private int mInterest = 0;
	private int mWeakCoins = 0;
	private int mMediumCoins = 0;
	private int mStrongCoins = 0;
	private int mWeakCards = 0;
	private int mMediumCards = 0;
	private int mStrongCards = 0;
	private boolean mApplied = false;


	private class ApplyAction extends AbstractAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent pEvent)
		{
			if (mPrincipalTF != null)
				try
				{
					mPrincipal = Integer.valueOf(mPrincipalTF.getText());
				}
				catch (NumberFormatException e)
				{
					showErrorMessage("principal");
					return;
				}
			if (mInterestTF != null)
			{
				try
				{
					mInterest = Integer.valueOf(mInterestTF.getText());
				}
				catch (NumberFormatException e)
				{
					showErrorMessage("intérêts");
					return;
				}
			}
			if (mWeakCardsTF != null)
			{
				try
				{
					mWeakCards = Integer.valueOf(mWeakCardsTF.getText());
				}
				catch (NumberFormatException e)
				{
					showErrorMessage("cartes faibles");
					return;
				}
				try
				{
					mMediumCards = Integer.valueOf(mMediumCardsTF.getText());
				}
				catch (NumberFormatException e)
				{
					showErrorMessage("cartes moyennes");
					return;
				}
				try
				{
					mStrongCards = Integer.valueOf(mStrongCardsTF.getText());
				}
				catch (NumberFormatException e)
				{
					showErrorMessage("cartes fortes");
					return;
				}
			}
			if (mWeakCoinsTF != null)
			{
				try
				{
					mWeakCoins = Integer.valueOf(mWeakCoinsTF.getText());
				}
				catch (NumberFormatException e)
				{
					showErrorMessage("monnaie faible");
					return;
				}
				try
				{
					mMediumCoins = Integer.valueOf(mMediumCoinsTF.getText());
				}
				catch (NumberFormatException e)
				{
					showErrorMessage("monnaie moyenne");
					return;
				}
				try
				{
					mStrongCoins = Integer.valueOf(mStrongCoinsTF.getText());
				}
				catch (NumberFormatException e)
				{
					showErrorMessage("monnaie forte");
					return;
				}
			}
			mApplied = true;
			setVisible(false);
		}

		public void showErrorMessage(String pField)
		{
			JOptionPane.showMessageDialog(mPrincipalTF, "La valeur saisie pour les " + pField + " n'est pas un nombre.\nMerci de saisir un nombre entier.", "Saisir un nombre", JOptionPane.ERROR_MESSAGE);
		}
	}

	public CreditActionDialog(final JFrame pParent, final String pTitle, final int pDefaultPrincipal, final Purpose pPurpose)
	{
		super(pParent, pTitle);
		setSize(600, 250);
		setLocation(200, 200);
		setModal(true);
		final JPanel mainPanel = new JPanel(new GridBagLayout());
		switch (pPurpose)
		{
		case NEW_OR_REIMB_CREDIT:
		case DEFAULT:
			mPrincipalLabel = "Principal";
			break;
		case MONEY_MASS_CHANGE:
			mPrincipalLabel = "Delta";
			break;
		case BANK_INVESTMENT:
			mPrincipalLabel = "Monnaie investie";
			break;
		case PLAYER_ASSESSMENT_DEBT_MONEY:
			mPrincipalLabel = "Monnaie restante";
			break;
		default:
			break;
		}
		int y = 0;
		if (!Purpose.PLAYER_ASSESSMENT_LIBRE_MONEY.equals(pPurpose))
		{
			mPrincipalTF = createField(mainPanel, y++, mPrincipalLabel);
			mPrincipalTF.setText(String.valueOf(pDefaultPrincipal));
		}
		if (Purpose.DEFAULT.equals(pPurpose))
		{
			mInterestTF = createField(mainPanel, y++, "Intérêts");
			mInterestTF.setText(String.valueOf(pDefaultPrincipal / 3));
		}
		if (Purpose.PLAYER_ASSESSMENT_LIBRE_MONEY.equals(pPurpose))
		{
			mWeakCoinsTF = createField(mainPanel, y++, "Monnaie faible");
			mMediumCoinsTF = createField(mainPanel, y++, "Monnaie moyenne");
			mStrongCoinsTF = createField(mainPanel, y++, "Monnaie forte");
		}
		if (SHOW_CARDS_PURPOSE.contains(pPurpose))
		{
			mWeakCardsTF = createField(mainPanel, y++, "Cartes faibles");
			mMediumCardsTF = createField(mainPanel, y++, "Cartes moyennes");
			mStrongCardsTF = createField(mainPanel, y++, "Cartes fortes");
		}
		if (Purpose.DEFAULT.equals(pPurpose))
		{
			final ActionListener rbActionListener = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent pEvent)
				{
					if (HelperUI.ACTION_CANNOT_PAY.equals(pEvent.getActionCommand()))
						mEventType = EventType.CANNOT_PAY;
					else if (HelperUI.ACTION_BANKRUPTCY.equals(pEvent.getActionCommand()))
						mEventType = EventType.BANKRUPT;
					else if (HelperUI.ACTION_PRISON.equals(pEvent.getActionCommand()))
						mEventType = EventType.PRISON;
				}
			};
			final JRadioButton cannotPayRB = createRadio(mainPanel, y++, "Le joueur est saisi mais peut continuer à jouer", HelperUI.ACTION_CANNOT_PAY, rbActionListener);
			final JRadioButton bankruptRB = createRadio(mainPanel, y++, "Saisie. Il reste moins de 4 cartes au joueur. Faillite personnelle et passe un tour.", HelperUI.ACTION_BANKRUPTCY, rbActionListener);
			final JRadioButton prisonRB = createRadio(mainPanel, y++, "Le joueur est saisi mais n'a pas assez de cartes pour couvrir la saisie. Prison.", HelperUI.ACTION_PRISON, rbActionListener);
			ButtonGroup actionButtonGroup = new ButtonGroup();
			actionButtonGroup.add(cannotPayRB);
			actionButtonGroup.add(bankruptRB);
			actionButtonGroup.add(prisonRB);
			cannotPayRB.setSelected(true);
			mEventType = EventType.CANNOT_PAY;
		}
		final JPanel buttonsPanel = new JPanel(new FlowLayout());
		final JButton addButton = new JButton("OK");
		buttonsPanel.add(addButton);
		addButton.addActionListener(new ApplyAction());
		final JButton cancelButton = new JButton("Annuler");
		buttonsPanel.add(cancelButton);
		cancelButton.addActionListener(new CancelAction());
		mainPanel.add(buttonsPanel, new GridBagConstraints(0, 10, 2, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		getContentPane().add(mainPanel);
		addFocusListener(new FocusListener()
		{
			@Override
			public void focusLost(FocusEvent pEvent) {}
			@Override
			public void focusGained(FocusEvent pEvent)
			{
				mPrincipalTF.requestFocus();
			}
		});
	}

	private JRadioButton createRadio(final JPanel pMainPanel, final int pGridy, final String pMessage, final String pActionCommand, final ActionListener pActionListener)
	{
		final JRadioButton radioButton = new JRadioButton(pMessage);
		radioButton.setActionCommand(pActionCommand);
		radioButton.addActionListener(pActionListener);
		pMainPanel.add(radioButton, new GridBagConstraints(0, pGridy, 2, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		radioButton.getInputMap().put(KeyStroke.getKeyStroke((char)10), "enter");
		radioButton.getActionMap().put("enter", new ApplyAction());
		radioButton.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
		radioButton.getActionMap().put("escape", new CancelAction());
		return radioButton;
	}

	public JTextField createField(final JPanel pMainPanel, final int pGridy, String pLabel)
	{
		pMainPanel.add(new JLabel(pLabel), new GridBagConstraints(0, pGridy, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		final JTextField jTextField = new JTextField(50);
		jTextField.setText("0");
		pMainPanel.add(jTextField, new GridBagConstraints(1, pGridy, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jTextField.getInputMap().put(KeyStroke.getKeyStroke((char)10), "enter");
		jTextField.getActionMap().put("enter", new ApplyAction());
		jTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
		jTextField.getActionMap().put("escape", new CancelAction());
		jTextField.addFocusListener(new FocusListener()
		{
			@Override
			public void focusLost(FocusEvent pEvent)
			{
			}
			
			@Override
			public void focusGained(FocusEvent pEvent)
			{
				jTextField.setSelectionStart(0);
				jTextField.setSelectionEnd(jTextField.getText().length());
			}
		});
		return jTextField;
	}

	public int getPrincipal()
	{
		return mPrincipal;
	}

	public int getInterest()
	{
		return mInterest;
	}

	public int getWeakCards()
	{
		return mWeakCards;
	}

	public int getMediumCards()
	{
		return mMediumCards;
	}

	public int getStrongCards()
	{
		return mStrongCards;
	}

	public int getWeakCoins()
	{
		return mWeakCoins;
	}

	public int getMediumCoins()
	{
		return mMediumCoins;
	}

	public int getStrongCoins()
	{
		return mStrongCoins;
	}

	public boolean wasApplied()
	{
		return mApplied;
	}

	public EventType getEventType()
	{
		return mEventType;
	}
}
