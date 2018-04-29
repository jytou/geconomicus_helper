package jyt.geconomicus.helper;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

/**
 * The first dialog that the user sees: he can choose to open an existing game, choose to import a game from a file or create a new game.<br>
 * It then opens the relevant window: the main window or the statistics game chooser.
 * @author jytou
 */
public class ChooseGameDialog extends JFrame
{
	private static final String OPEN_GAME_ACTION = "openGame"; //$NON-NLS-1$
	private static final String NEW_GAME_ACTION = "newGame"; //$NON-NLS-1$
	private static final String CANCEL_ACTION = "cancel"; //$NON-NLS-1$

	// This boolean is set to true if the dialog wasn't cancelled
	private boolean mChosen = false;

	// The money system
	private JComboBox<Integer> mMoneySystemCB;
	private JTextField mDateTextField;
	private JTextField mLocationTextField;
	private JTextField mAnimatorTextField;
	private JTextField mEmailTextField;
	private JTextField mNbTurnsTextField;
	private JTextField mMoneyCardsFactorTextField;
	private JButton mNewGameButton;
	private JLabel mErrorGameButtonPanel;

	public ChooseGameDialog(final EntityManager pEntityManager, final EntityManagerFactory pEntityManagerFactory) throws IOException
	{
		super(UIMessages.getString("ChooseGameDialog.Title")); //$NON-NLS-1$
		// Global stuff on the dialog
		setIconImage(ImageIO.read(HelperUI.class.getResourceAsStream("/geconomicus.png"))); //$NON-NLS-1$
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent pEvent)
			{
				super.windowClosing(pEvent);
				// If the window is closing, we have to quit if the game has not been set in the main frame
				if (!mChosen)
					System.exit(1);
			}
		});

		final Dimension size = new Dimension(800, 600);
		setSize(size);
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width / 2 - size.width/2, screenSize.height / 2 - size.height/2);

		final JPanel mainPanel = new JPanel(new GridBagLayout());

		/**
		 * Actions on existing games (open, delete)
		 */
		final JPanel existingGamesPanel = new JPanel(new GridBagLayout());
		existingGamesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), UIMessages.getString("ChooseGameDialog.ExistingGames.Panel.Title"))); //$NON-NLS-1$
		final Insets insets = new Insets(2, 2, 2, 2);
		final JLabel openGameLabel = new JLabel(UIMessages.getString("ChooseGameDialog.GameToOpen.Label")); //$NON-NLS-1$
		existingGamesPanel.add(openGameLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		@SuppressWarnings("unchecked")
		final List<Game> games = pEntityManager.createNamedQuery("Game.findAll").getResultList(); //$NON-NLS-1$
		games.add(0, null);
		games.sort(new Comparator<Game>()
		{
			@Override
			public int compare(Game pO1, Game pO2)
			{
				return createLabelForGame(pO1).compareTo(createLabelForGame(pO2));
			}
		});
		final JComboBox<Game> gameCombo = new JComboBox<>(games.toArray(new Game[games.size()]));
		gameCombo.setRenderer(new ListCellRenderer<Game>()
		{
			@Override
			public Component getListCellRendererComponent(JList<? extends Game> pList, Game pGame, int pIndex, boolean pIsSelected, boolean pCellHasFocus)
			{
				return new JLabel(createLabelForGame(pGame));
			}
		});
		existingGamesPanel.add(gameCombo, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		final JPanel existingGamesButtonPanel = new JPanel(new GridBagLayout());
		existingGamesButtonPanel.add(new JPanel(), new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		final JButton iWantStatistics = new JButton(UIMessages.getString("ChooseGameDialog.OpenStatisticsOnly.Button")); //$NON-NLS-1$
		iWantStatistics.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pE)
			{
				mChosen = true;
				try
				{
					new ChooseGamesDialog(pEntityManager, pEntityManagerFactory).setVisible(true);
					setVisible(false);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
		existingGamesButtonPanel.add(iWantStatistics, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		final JButton openGameButton = new JButton(UIMessages.getString("ChooseGameDialog.OpenThisGame.Button")); //$NON-NLS-1$
		existingGamesButtonPanel.add(openGameButton, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		final Action openGameAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				try
				{
					new HelperUI(pEntityManager, pEntityManagerFactory, (Game)gameCombo.getSelectedItem());
				}
				catch (IOException e)
				{
					JOptionPane.showMessageDialog(ChooseGameDialog.this, MessageFormat.format(UIMessages.getString("ChooseGameDialog.CouldNotOpenMainFrame.Error.Message"), e.getMessage()), UIMessageKeyProvider.DIALOG_TITLE_ERROR.getMessage(), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					return;
				}
				mChosen = true;
				setVisible(false);
			}
		};
		openGameButton.setEnabled(false);
		openGameButton.addActionListener(openGameAction);
		openGameButton.setMnemonic(UIMessages.getString("ChooseGameDialog.OpenButton.Mnemonic").charAt(0)); //$NON-NLS-1$
		openGameButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), ChooseGameDialog.OPEN_GAME_ACTION);
		openGameButton.getActionMap().put(ChooseGameDialog.OPEN_GAME_ACTION, openGameAction);
		final JButton deleteGameButton = new JButton(UIMessages.getString("ChooseGameDialog.DeleteThisGame.Button")); //$NON-NLS-1$
		deleteGameButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pE)
			{
				if (JOptionPane.showConfirmDialog(ChooseGameDialog.this, UIMessages.getString("ChooseGameDialog.DeleteGame.Confirm.Label"), UIMessages.getString("ChooseGameDialog.DeleteGame.Confirm.Title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) //$NON-NLS-1$ //$NON-NLS-2$
				{
					pEntityManager.getTransaction().begin();
					final Game game = (Game)gameCombo.getSelectedItem();
					pEntityManager.remove(game);
					gameCombo.removeItem(game);
					pEntityManager.getTransaction().commit();
				}
			}
		});
		deleteGameButton.setEnabled(false);
		existingGamesButtonPanel.add(deleteGameButton, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		existingGamesPanel.add(existingGamesButtonPanel, new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		mainPanel.add(existingGamesPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 5, 5));
		gameCombo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				openGameButton.setEnabled(gameCombo.getSelectedItem() != null);
				deleteGameButton.setEnabled(gameCombo.getSelectedItem() != null);
			}
		});

		/**
		 * Importing a game from an XML file
		 */
		final JPanel importGamesPanel = new JPanel(new GridBagLayout());
		importGamesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), UIMessages.getString("ChooseGameDialog.ImportGame.Panel.Title"))); //$NON-NLS-1$
		final JLabel importXmlFileLabel = new JLabel(UIMessages.getString("ChooseGameDialog.ImportGame.FromXML.Label")); //$NON-NLS-1$
		importGamesPanel.add(importXmlFileLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		final JTextField xmlFileTF = new JTextField();
		importGamesPanel.add(xmlFileTF, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		final JButton chooseXmlFileButton = new JButton(UIMessageKeyProvider.DIALOG_BUTTON_OPEN.getMessage());
		importGamesPanel.add(chooseXmlFileButton, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		final JPanel importGameButtonPanel = new JPanel(new GridBagLayout());
		importGameButtonPanel.add(new JPanel(), new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		final JButton importGameButton = new JButton(UIMessageKeyProvider.DIALOG_BUTTON_IMPORT.getMessage());
		final Action importGameAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				// import the game
				Game importedGame = null;
				// Import from XML
				try
				{
					JAXBContext jc = JAXBContext.newInstance(Game.class);
					Unmarshaller unmarshaller = jc.createUnmarshaller();
					importedGame = (Game)unmarshaller.unmarshal(new File(xmlFileTF.getText()));
				}
				catch (PropertyException e)
				{
					UIUtil.showExceptionMessage(ChooseGameDialog.this, e);
					return;
				}
				catch (JAXBException e)
				{
					UIUtil.showExceptionMessage(ChooseGameDialog.this, e);
					return;
				}
				if (importedGame != null)
				{
					// The game was imported successfully, persist it in the database
					pEntityManager.getTransaction().begin();
					try
					{
						importedGame.recomputeAll(null);
						pEntityManager.persist(importedGame);
						pEntityManager.getTransaction().commit();
						// Open the main window with this new game
						new HelperUI(pEntityManager, pEntityManagerFactory, importedGame);
						setVisible(false);
					}
					catch (Throwable e)
					{
						pEntityManager.getTransaction().rollback();
						UIUtil.showExceptionMessage(ChooseGameDialog.this, e);
					}
				}
			}
		};
		importGameButton.setEnabled(false);
		importGameButton.addActionListener(importGameAction);
		importGameButton.setMnemonic(UIMessages.getString("ChooseGameDialog.ImportGame.Mnemonic").charAt(0)); //$NON-NLS-1$
		chooseXmlFileButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				final JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("xml", "xml")); //$NON-NLS-1$ //$NON-NLS-2$
				if (fc.showOpenDialog(ChooseGameDialog.this) == JFileChooser.APPROVE_OPTION)
				{
					xmlFileTF.setText(fc.getSelectedFile().getAbsolutePath());
					importGameButton.setEnabled(true);
				}
			}
		});
		xmlFileTF.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void removeUpdate(DocumentEvent pE)
			{
				activateImportButton();
			}
			private void activateImportButton()
			{
				importGameButton.setEnabled(new File(xmlFileTF.getText()).exists());
			}
			@Override
			public void insertUpdate(DocumentEvent pE)
			{
				activateImportButton();
			}
			@Override
			public void changedUpdate(DocumentEvent pE)
			{
				activateImportButton();
			}
		});
		importGameButtonPanel.add(importGameButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		importGamesPanel.add(importGameButtonPanel, new GridBagConstraints(0, 1, 3, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		mainPanel.add(importGamesPanel, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 5, 5));

		/**
		 * Creating a new game
		 */
		final JPanel newGamePanel = new JPanel(new GridBagLayout());
		newGamePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), UIMessages.getString("ChooseGameDialog.NewGame.Panel.Title"))); //$NON-NLS-1$
		int y = 0;
		newGamePanel.add(new JLabel(UIMessageKeyProvider.GAME_DATE_LABEL.getMessage()), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		mDateTextField = new JTextField(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").format(new Date())); //$NON-NLS-1$
		newGamePanel.add(mDateTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel(UIMessages.getString("ChooseGameDialog.NewGame.MoneyType.Label")), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0)); //$NON-NLS-1$
		mMoneySystemCB = new JComboBox<>(new Integer[] {-1, 0, 1});
		mMoneySystemCB.setRenderer(new ListCellRenderer<Integer>()
		{
			@Override
			public Component getListCellRendererComponent(JList<? extends Integer> pList, Integer pValue, int pIndex, boolean pIsSelected, boolean pCellHasFocus)
			{
				String text = ""; //$NON-NLS-1$
				switch (pValue.intValue())
				{
				case -1:// this is empty
					break;
				case Game.MONEY_DEBT:
					text = UIMessageKeyProvider.GENERAL_DEBT_MONEY.getMessage();
					break;
				case Game.MONEY_LIBRE:
					text = UIMessageKeyProvider.GENERAL_LIBRE_CURRENCY.getMessage();
					break;
				default:
					// This shouldn't happen
					System.err.println("Unknown value for money type in combobox"); //$NON-NLS-1$
				}
				return new JLabel(text);
			}
		});
		mMoneySystemCB.requestFocusInWindow();
		newGamePanel.add(mMoneySystemCB, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel(UIMessages.getString("ChooseGameDialog.NewGame.Location.Label")), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0)); //$NON-NLS-1$
		mLocationTextField = new JTextField();
		newGamePanel.add(mLocationTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel(UIMessages.getString("ChooseGameDialog.NewGame.AnimatorPseudo.Label")), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0)); //$NON-NLS-1$
		mAnimatorTextField = new JTextField();
		newGamePanel.add(mAnimatorTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel(UIMessages.getString("ChooseGameDialog.NewGame.AnimatorEmail.Label")), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0)); //$NON-NLS-1$
		mEmailTextField = new JTextField();
		newGamePanel.add(mEmailTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel(UIMessages.getString("ChooseGameDialog.NewGame.NbTurns.Label")), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0)); //$NON-NLS-1$
		mNbTurnsTextField = new JTextField(""); //$NON-NLS-1$
		newGamePanel.add(mNbTurnsTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel(UIMessages.getString("ChooseGameDialog.NewGame.CardMoneyFactor.Label")), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0)); //$NON-NLS-1$
		mMoneyCardsFactorTextField = new JTextField(""); //$NON-NLS-1$
		newGamePanel.add(mMoneyCardsFactorTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel(UIMessageKeyProvider.GAME_DESCRIPTION_LABEL.getMessage()), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		final JTextArea descriptionTextArea = new JTextArea();
		final JScrollPane descriptionScrollPane = new JScrollPane(descriptionTextArea);
		newGamePanel.add(descriptionScrollPane, new GridBagConstraints(1, y++, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0));

		final JPanel newGameButtonPanel = new JPanel(new GridBagLayout());
		mErrorGameButtonPanel = new JLabel();
		mErrorGameButtonPanel.setForeground(Color.red);
		newGameButtonPanel.add(mErrorGameButtonPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		mNewGameButton = new JButton(UIMessages.getString("ChooseGameDialog.NewGame.Button")); //$NON-NLS-1$
		newGameButtonPanel.add(mNewGameButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));

		mNewGameButton.setEnabled(false);
		final Action newGameAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				EntityManager em = pEntityManager;
				try
				{
					em.getTransaction().begin();
					final Game newGame = new Game(((Integer)mMoneySystemCB.getSelectedItem()).intValue(), Integer.valueOf(mNbTurnsTextField.getText()), mAnimatorTextField.getText(), mEmailTextField.getText(), descriptionTextArea.getText(), mDateTextField.getText(), mLocationTextField.getText(), Integer.valueOf(mMoneyCardsFactorTextField.getText()));
					em.persist(newGame);
					em.getTransaction().commit();
					new HelperUI(pEntityManager, pEntityManagerFactory, newGame);
				}
				catch (NumberFormatException e)
				{
					em.getTransaction().rollback();
					JOptionPane.showMessageDialog(ChooseGameDialog.this, UIMessages.getString("ChooseGameDialog.ShouldBeANumber.Error.Message"), UIMessageKeyProvider.DIALOG_TITLE_ERROR.getMessage(), JOptionPane.ERROR_MESSAGE);; //$NON-NLS-1$
					return;
				}
				catch (IOException e)
				{
					em.getTransaction().rollback();
					JOptionPane.showMessageDialog(ChooseGameDialog.this, MessageFormat.format(UIMessages.getString("ChooseGameDialog.CouldNotOpenMainFrame.Error.Message"), e.getMessage()), UIMessageKeyProvider.DIALOG_TITLE_ERROR.getMessage(), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					return;
				}
				setVisible(false);
			}
		};
		mNewGameButton.setMnemonic(UIMessages.getString("ChooseGameDialog.NewGame.Button.Mnemonic").charAt(0)); //$NON-NLS-1$
		mNewGameButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), ChooseGameDialog.NEW_GAME_ACTION);
		mNewGameButton.getActionMap().put(ChooseGameDialog.NEW_GAME_ACTION, newGameAction);
		mNewGameButton.addActionListener(newGameAction);
		newGamePanel.add(newGameButtonPanel, new GridBagConstraints(0, y++, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		mainPanel.add(newGamePanel, new GridBagConstraints(0, 10, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 5, 5));

		final Action cancelAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				if (JOptionPane.showConfirmDialog(ChooseGameDialog.this, UIMessages.getString("ChooseGameDialog.ReallyExit.Message"), UIMessages.getString("ChooseGameDialog.ReallyExit.Title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) //$NON-NLS-1$ //$NON-NLS-2$
					System.exit(1);
			}
		};
		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ChooseGameDialog.CANCEL_ACTION);
		mainPanel.getActionMap().put(ChooseGameDialog.CANCEL_ACTION, cancelAction);

		mMoneySystemCB.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pE)
			{
				activateNewGameButton();
			}
		});
		final DocumentListener buttonActivator = new DocumentListener()
		{
			@Override
			public void removeUpdate(DocumentEvent pEvent)
			{
				activateNewGameButton();
			}
			
			@Override
			public void insertUpdate(DocumentEvent pEvent)
			{
				activateNewGameButton();
			}
			
			@Override
			public void changedUpdate(DocumentEvent pEvent)
			{
				activateNewGameButton();
			}
		};
		mDateTextField.getDocument().addDocumentListener(buttonActivator);
		mLocationTextField.getDocument().addDocumentListener(buttonActivator);
		mAnimatorTextField.getDocument().addDocumentListener(buttonActivator);
		mEmailTextField.getDocument().addDocumentListener(buttonActivator);
		mNbTurnsTextField.getDocument().addDocumentListener(buttonActivator);
		mMoneyCardsFactorTextField.getDocument().addDocumentListener(buttonActivator);

		getContentPane().add(mainPanel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Activates the new game button and makes sure that the different fields have been correctly filled beforehand.
	 * Shows a revelant error depending on the fields that have been incorrectly filled.
	 */
	private void activateNewGameButton()
	{
		String error = null;
		if (mMoneySystemCB.getSelectedIndex() == 0)
			error = UIMessages.getString("ChooseGameDialog.ChooseMonetarySystem.ErrorHint"); //$NON-NLS-1$
		else if (mDateTextField.getText().isEmpty())
			error = UIMessages.getString("ChooseGameDialog.DateCannotBeEmpty.ErrorHint"); //$NON-NLS-1$
		else if (mLocationTextField.getText().isEmpty())
			error = UIMessages.getString("ChooseGameDialog.LocationCannotBeEmpty.ErrorHint"); //$NON-NLS-1$
		else if (mAnimatorTextField.getText().isEmpty())
			error = UIMessages.getString("ChooseGameDialog.ChooseAnimator.ErrorHint"); //$NON-NLS-1$
		else if (!mEmailTextField.getText().contains("@")) //$NON-NLS-1$
			error = UIMessages.getString("ChooseGameDialog.EmailMustHaveAt.ErrorHint"); //$NON-NLS-1$
		else if (mNbTurnsTextField.getText().isEmpty())
			error = UIMessages.getString("ChooseGameDialog.NbTurnsMustBeFilled.ErrorHint"); //$NON-NLS-1$
		else if (mMoneyCardsFactorTextField.getText().isEmpty())
			error = UIMessages.getString("ChooseGameDialog.FactorMustBeFilled.ErrorHint"); //$NON-NLS-1$
		if (error == null)
		{
			try
			{
				Integer.parseInt(mNbTurnsTextField.getText());
			}
			catch (NumberFormatException e)
			{
				error = UIMessages.getString("ChooseGameDialog.NbTurnsMustBeInt.ErrorHint"); //$NON-NLS-1$
			}
			try
			{
				Integer.parseInt(mMoneyCardsFactorTextField.getText());
			}
			catch (NumberFormatException e)
			{
				error = UIMessages.getString("ChooseGameDialog.MoneyFactorMustBeInt.ErrorHint"); //$NON-NLS-1$
			}
			try
			{
				new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").parse(mDateTextField.getText()); //$NON-NLS-1$
			}
			catch (ParseException e)
			{
				error = UIMessages.getString("ChooseGameDialog.IncorrectDateFormat.ErrorHint"); //$NON-NLS-1$
			}
		}
		mNewGameButton.setEnabled(error == null);
		mErrorGameButtonPanel.setText(error);
	}

	public String createLabelForGame(Game pGame)
	{
		if (pGame == null)
			return ""; //$NON-NLS-1$
		return MessageFormat.format(UIMessages.getString("ChooseGameDialog.GameCombo.LabelForGame"), new Object[] {pGame.getCurdate(), pGame.getMoneySystem() == Game.MONEY_DEBT ? UIMessageKeyProvider.GENERAL_DEBT_MONEY.getMessage() : UIMessageKeyProvider.GENERAL_LIBRE_CURRENCY.getMessage(), pGame.getLocation(), pGame.getAnimatorPseudo()}); //$NON-NLS-1$
	}
}
