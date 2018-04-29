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
import java.text.MessageFormat;
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
import jyt.geconomicus.helper.HelperUI.ActionCommand;

/**
 * A multi-purpose dialog to gather information on the assets of a player - money or cards.
 * @author jytou
 */
public class CreditActionDialog extends JDialog
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

	// The different purposes that this dialog can serve
	public enum Purpose
	{
		MONEY_MASS_CHANGE,            // money mass change only
		BANK_INVESTMENT,              // the bank invests some of its seized assets (interest gained or cards seized)
		NEW_OR_REIMB_CREDIT,          // a player takes or pays back a credit
		DEFAULT,                      // a player is defaulting on his debts
		PLAYER_ASSESSMENT_DEBT_MONEY, // assessment of a player in the debt-money system
		PLAYER_ASSESSMENT_LIBRE_MONEY // assessment of a player in the libre currency system
	};

	// A helper to tell for which purposes we need to show cards information in the dialog
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
					showErrorMessage(UIMessageKeyProvider.GENERAL_CREDIT_PRINCIPAL.getMessage());
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
					showErrorMessage(UIMessageKeyProvider.GENERAL_CREDIT_INTEREST.getMessage());
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
					showErrorMessage(UIMessageKeyProvider.GENERAL_CARDS_WEAK.getMessage());
					return;
				}
				try
				{
					mMediumCards = Integer.valueOf(mMediumCardsTF.getText());
				}
				catch (NumberFormatException e)
				{
					showErrorMessage(UIMessageKeyProvider.GENERAL_CARDS_MEDIUM.getMessage());
					return;
				}
				try
				{
					mStrongCards = Integer.valueOf(mStrongCardsTF.getText());
				}
				catch (NumberFormatException e)
				{
					showErrorMessage(UIMessageKeyProvider.GENERAL_CARDS_STRONG.getMessage());
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
					showErrorMessage(UIMessageKeyProvider.GENERAL_MONEY_WEAK.getMessage());
					return;
				}
				try
				{
					mMediumCoins = Integer.valueOf(mMediumCoinsTF.getText());
				}
				catch (NumberFormatException e)
				{
					showErrorMessage(UIMessageKeyProvider.GENERAL_MONEY_MEDIUM.getMessage());
					return;
				}
				try
				{
					mStrongCoins = Integer.valueOf(mStrongCoinsTF.getText());
				}
				catch (NumberFormatException e)
				{
					showErrorMessage(UIMessageKeyProvider.GENERAL_MONEY_STRONG.getMessage());
					return;
				}
			}
			mApplied = true;
			setVisible(false);
		}

		public void showErrorMessage(String pField)
		{
			JOptionPane.showMessageDialog(mPrincipalTF, MessageFormat.format(UIMessages.getString("CreditActionDialog.Error.Message.ValueForFieldNotANumber"), pField), UIMessages.getString("CreditActionDialog.Error.Title.InputANumber"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
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
			mPrincipalLabel = UIMessageKeyProvider.GENERAL_CREDIT_PRINCIPAL.getMessage();
			break;
		case MONEY_MASS_CHANGE:
			mPrincipalLabel = UIMessages.getString("CreditActionDialog.Label.MoneyMassDelta"); //$NON-NLS-1$
			break;
		case BANK_INVESTMENT:
			mPrincipalLabel = UIMessages.getString("CreditActionDialog.Label.InvestedMoney"); //$NON-NLS-1$
			break;
		case PLAYER_ASSESSMENT_DEBT_MONEY:
			mPrincipalLabel = UIMessages.getString("CreditActionDialog.Label.RemainingMoney"); //$NON-NLS-1$
			break;
		default:
			break;
		}
		int y = 0;
		if (!Purpose.PLAYER_ASSESSMENT_LIBRE_MONEY.equals(pPurpose))
		{
			mPrincipalTF = createField(mainPanel, y++, mPrincipalLabel);
			mPrincipalTF.setText(String.valueOf(pDefaultPrincipal));
			mPrincipalTF.requestFocusInWindow();
		}
		if (Purpose.DEFAULT.equals(pPurpose))
		{
			mInterestTF = createField(mainPanel, y++, UIMessageKeyProvider.GENERAL_CREDIT_INTEREST.getMessage());
			mInterestTF.setText(String.valueOf(pDefaultPrincipal / 3));
		}
		if (Purpose.PLAYER_ASSESSMENT_LIBRE_MONEY.equals(pPurpose))
		{
			mWeakCoinsTF = createField(mainPanel, y++, UIMessageKeyProvider.GENERAL_MONEY_WEAK.getMessage());
			mWeakCoinsTF.requestFocusInWindow();
			mMediumCoinsTF = createField(mainPanel, y++, UIMessageKeyProvider.GENERAL_MONEY_MEDIUM.getMessage());
			mStrongCoinsTF = createField(mainPanel, y++, UIMessageKeyProvider.GENERAL_MONEY_STRONG.getMessage());
		}
		if (SHOW_CARDS_PURPOSE.contains(pPurpose))
		{
			mWeakCardsTF = createField(mainPanel, y++, UIMessageKeyProvider.GENERAL_CARDS_WEAK.getMessage());
			mMediumCardsTF = createField(mainPanel, y++, UIMessageKeyProvider.GENERAL_CARDS_MEDIUM.getMessage());
			mStrongCardsTF = createField(mainPanel, y++, UIMessageKeyProvider.GENERAL_CARDS_STRONG.getMessage());
		}
		if (Purpose.DEFAULT.equals(pPurpose))
		{
			final ActionListener rbActionListener = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent pEvent)
				{
					if (ActionCommand.ACTION_CANNOT_PAY.getMnemo().equals(pEvent.getActionCommand()))
						mEventType = EventType.CANNOT_PAY;
					else if (ActionCommand.ACTION_BANKRUPTCY.getMnemo().equals(pEvent.getActionCommand()))
						mEventType = EventType.BANKRUPT;
					else if (ActionCommand.ACTION_PRISON.getMnemo().equals(pEvent.getActionCommand()))
						mEventType = EventType.PRISON;
				}
			};
			final JRadioButton cannotPayRB = createRadio(mainPanel, y++, UIMessages.getString("CreditActionDialog.Option.Label.SeizedOk"), ActionCommand.ACTION_CANNOT_PAY.getMnemo(), rbActionListener); //$NON-NLS-1$
			final JRadioButton bankruptRB = createRadio(mainPanel, y++, UIMessages.getString("CreditActionDialog.Option.Label.SeizedBankrupt"), ActionCommand.ACTION_BANKRUPTCY.getMnemo(), rbActionListener); //$NON-NLS-1$
			final JRadioButton prisonRB = createRadio(mainPanel, y++, UIMessages.getString("CreditActionDialog.Option.Label.SeizedPrison"), ActionCommand.ACTION_PRISON.getMnemo(), rbActionListener); //$NON-NLS-1$
			ButtonGroup actionButtonGroup = new ButtonGroup();
			actionButtonGroup.add(cannotPayRB);
			actionButtonGroup.add(bankruptRB);
			actionButtonGroup.add(prisonRB);
			cannotPayRB.setSelected(true);
			mEventType = EventType.CANNOT_PAY;
		}
		final JPanel buttonsPanel = new JPanel(new FlowLayout());
		final JButton addButton = new JButton(UIMessages.getString("CreditActionDialog.Button.Label.Ok")); //$NON-NLS-1$
		buttonsPanel.add(addButton);
		addButton.addActionListener(new ApplyAction());
		final JButton cancelButton = new JButton(UIMessageKeyProvider.DIALOG_BUTTON_CANCEL.getMessage());
		buttonsPanel.add(cancelButton);
		cancelButton.addActionListener(new CancelAction());
		mainPanel.add(buttonsPanel, new GridBagConstraints(0, 10, 2, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		getContentPane().add(mainPanel);
	}

	private JRadioButton createRadio(final JPanel pMainPanel, final int pGridy, final String pMessage, final String pActionCommand, final ActionListener pActionListener)
	{
		final JRadioButton radioButton = new JRadioButton(pMessage);
		radioButton.setActionCommand(pActionCommand);
		radioButton.addActionListener(pActionListener);
		pMainPanel.add(radioButton, new GridBagConstraints(0, pGridy, 2, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		radioButton.getInputMap().put(KeyStroke.getKeyStroke((char)10), CreditActionDialog.ENTER_ACTION);
		radioButton.getActionMap().put(CreditActionDialog.ENTER_ACTION, new ApplyAction());
		radioButton.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CreditActionDialog.ESCAPE_ACTION);
		radioButton.getActionMap().put(CreditActionDialog.ESCAPE_ACTION, new CancelAction());
		return radioButton;
	}

	public JTextField createField(final JPanel pMainPanel, final int pGridy, String pLabel)
	{
		pMainPanel.add(new JLabel(pLabel), new GridBagConstraints(0, pGridy, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		final JTextField jTextField = new JTextField(50);
		jTextField.setText("0"); //$NON-NLS-1$
		pMainPanel.add(jTextField, new GridBagConstraints(1, pGridy, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jTextField.getInputMap().put(KeyStroke.getKeyStroke((char)10), CreditActionDialog.ENTER_ACTION);
		jTextField.getActionMap().put(CreditActionDialog.ENTER_ACTION, new ApplyAction());
		jTextField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CreditActionDialog.ESCAPE_ACTION);
		jTextField.getActionMap().put(CreditActionDialog.ESCAPE_ACTION, new CancelAction());
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
