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
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
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

public class ChooseGameDialog extends JFrame
{
	private boolean mChosen = false;
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
		super("Choisir une partie");
		// Global stuff on the dialog
		setIconImage(ImageIO.read(HelperUI.class.getResourceAsStream("/geconomicus.png")));
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
		existingGamesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), "Parties existantes"));
		final Insets insets = new Insets(2, 2, 2, 2);
		final JLabel openGameLabel = new JLabel("Partie à ouvrir");
		existingGamesPanel.add(openGameLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		@SuppressWarnings("unchecked")
		final List<Game> games = pEntityManager.createNamedQuery("Game.findAll").getResultList();
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

		final JButton iWantStatistics = new JButton("Je veux juste les statistiques !");
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
		final JButton openGameButton = new JButton("Ouvrir cette partie");
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
					JOptionPane.showMessageDialog(ChooseGameDialog.this, "Could not open main frame because of exception " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				mChosen = true;
				setVisible(false);
			}
		};
		openGameButton.setEnabled(false);
		openGameButton.addActionListener(openGameAction);
		openGameButton.setMnemonic('O');
		openGameButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), "openGame");
		openGameButton.getActionMap().put("openGame", openGameAction);
		final JButton deleteGameButton = new JButton("Supprimer cette partie");
		deleteGameButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pE)
			{
				if (JOptionPane.showConfirmDialog(ChooseGameDialog.this, "Voulez-vous vraiment supprimer cette partie ?", "Suppression de partie", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
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
		importGamesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), "Importer une partie"));
		final JLabel importXmlFileLabel = new JLabel("Importer depuis un fichier XML");
		importGamesPanel.add(importXmlFileLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		final JTextField xmlFileTF = new JTextField();
		importGamesPanel.add(xmlFileTF, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		final JButton chooseXmlFileButton = new JButton("Ouvrir...");
		importGamesPanel.add(chooseXmlFileButton, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		final JPanel importGameButtonPanel = new JPanel(new GridBagLayout());
		importGameButtonPanel.add(new JPanel(), new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		final JButton importGameButton = new JButton("Importer !");
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
					JOptionPane.showMessageDialog(rootPane, "Erreur durant l'import : " + e.getClass().getName() + " (" + e.getMessage() + ")", "Erreur", JOptionPane.ERROR_MESSAGE);
					return;
				}
				catch (JAXBException e)
				{
					JOptionPane.showMessageDialog(rootPane, "Erreur durant l'import : " + e.getClass().getName() + " (" + e.getMessage() + ")", "Erreur", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (importedGame != null)
				{
					pEntityManager.getTransaction().begin();
					try
					{
						importedGame.recomputeAll(null);
						pEntityManager.persist(importedGame);
						pEntityManager.getTransaction().commit();
						new HelperUI(pEntityManager, pEntityManagerFactory, importedGame);
						setVisible(false);
					}
					catch (Throwable e)
					{
						pEntityManager.getTransaction().rollback();
						JOptionPane.showMessageDialog(ChooseGameDialog.this, "Une erreur est survenue : " + e.getClass().getName() + "(" + e.getMessage() + ")", "", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		importGameButton.setEnabled(false);
		importGameButton.addActionListener(importGameAction);
		importGameButton.setMnemonic('i');
		chooseXmlFileButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				final JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("xml", "xml"));
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
		newGamePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), "Nouvelle partie"));
		int y = 0;
		newGamePanel.add(new JLabel("Date"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		mDateTextField = new JTextField(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").format(new Date()));
		newGamePanel.add(mDateTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel("Type de monnaie"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		mMoneySystemCB = new JComboBox<>(new Integer[] {-1, 0, 1});
		mMoneySystemCB.setRenderer(new ListCellRenderer<Integer>()
		{
			@Override
			public Component getListCellRendererComponent(JList<? extends Integer> pList, Integer pValue, int pIndex, boolean pIsSelected, boolean pCellHasFocus)
			{
				String text = "";
				switch (pValue.intValue())
				{
				case -1:// this is empty
					break;
				case Game.MONEY_DEBT:
					text = "Monnaie-dette";
					break;
				case Game.MONEY_LIBRE:
					text = "Monnaie libre";
					break;
				default:
					System.err.println("Unknown value for money type in combobox");
				}
				return new JLabel(text);
			}
		});
		newGamePanel.add(mMoneySystemCB, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel("Lieu"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		mLocationTextField = new JTextField();
		newGamePanel.add(mLocationTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel("Animateur"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		mAnimatorTextField = new JTextField();
		newGamePanel.add(mAnimatorTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel("Courriel"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		mEmailTextField = new JTextField();
		newGamePanel.add(mEmailTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel("Nb tours"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		mNbTurnsTextField = new JTextField("");
		newGamePanel.add(mNbTurnsTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel("Facteur carte/monnaie"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		mMoneyCardsFactorTextField = new JTextField("");
		newGamePanel.add(mMoneyCardsFactorTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel("Commentaires"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		final JTextArea descriptionTextArea = new JTextArea();
		final JScrollPane descriptionScrollPane = new JScrollPane(descriptionTextArea);
		newGamePanel.add(descriptionScrollPane, new GridBagConstraints(1, y++, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0));

		final JPanel newGameButtonPanel = new JPanel(new GridBagLayout());
		mErrorGameButtonPanel = new JLabel();
		mErrorGameButtonPanel.setForeground(Color.red);
		newGameButtonPanel.add(mErrorGameButtonPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		mNewGameButton = new JButton("Nouvelle partie");
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
					JOptionPane.showMessageDialog(ChooseGameDialog.this, "Some field should be a number and isn't", "Error", JOptionPane.ERROR_MESSAGE);;
					return;
				}
				catch (IOException e)
				{
					em.getTransaction().rollback();
					JOptionPane.showMessageDialog(ChooseGameDialog.this, "Could not open main frame because of exception " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				setVisible(false);
			}
		};
		mNewGameButton.setMnemonic('N');
		mNewGameButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "newGame");
		mNewGameButton.getActionMap().put("newGame", newGameAction);
		mNewGameButton.addActionListener(newGameAction);
		newGamePanel.add(newGameButtonPanel, new GridBagConstraints(0, y++, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		mainPanel.add(newGamePanel, new GridBagConstraints(0, 10, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 5, 5));

		final Action cancelAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				if (JOptionPane.showConfirmDialog(ChooseGameDialog.this, "Voulez-vous vraiment quitter ?", "Quitter ?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					System.exit(1);
			}
		};
		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		mainPanel.getActionMap().put("cancel", cancelAction);

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

		addWindowFocusListener(new WindowFocusListener()
		{
			@Override
			public void windowLostFocus(WindowEvent pE)
			{
			}
			
			@Override
			public void windowGainedFocus(WindowEvent pE)
			{
				mMoneySystemCB.requestFocus();
			}
		});
		getContentPane().add(mainPanel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * Activates the new game button and makes sure that the different fields have been correctly filled beforehand.
	 */
	private void activateNewGameButton()
	{
		String error = null;
		if (mMoneySystemCB.getSelectedIndex() == 0)
			error = "Sélectionner le système monétaire de la partie.";
		else if (mDateTextField.getText().isEmpty())
			error = "La date ne peut être vide.";
		else if (mLocationTextField.getText().isEmpty())
			error = "Le lieu ne peut être vide.";
		else if (mAnimatorTextField.getText().isEmpty())
			error = "Saisir un animateur pour la partie.";
		else if (!mEmailTextField.getText().contains("@"))
			error = "L'adresse de courriel doit contenir un arobase.";
		else if (mNbTurnsTextField.getText().isEmpty())
			error = "Le nombre de tours ne peut pas être vide.";
		else if (mMoneyCardsFactorTextField.getText().isEmpty())
			error = "Le facteur de la monnaie par rapport aux cartes ne doit pas être vide.";
		if (error == null)
		{
			try
			{
				Integer.parseInt(mNbTurnsTextField.getText());
			}
			catch (NumberFormatException e)
			{
				error = "Le nombre de tours doit être un nombre.";
			}
			try
			{
				Integer.parseInt(mMoneyCardsFactorTextField.getText());
			}
			catch (NumberFormatException e)
			{
				error = "Le facteur de la monnaie par rapport aux cartes doit être un nombre.";
			}
			try
			{
				new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").parse(mDateTextField.getText());
			}
			catch (ParseException e)
			{
				error = "Saisir une date de type « 2018.04.15 20:39:39 CEST ».";
			}
		}
		mNewGameButton.setEnabled(error == null);
		mErrorGameButtonPanel.setText(error);
	}

	public String createLabelForGame(Game pGame)
	{
		if (pGame == null)
			return "";
		return pGame.getCurdate() + " / " + (pGame.getMoneySystem() == Game.MONEY_DEBT ? "Monnaie Dette" : "Monnaie Libre") + " / " + pGame.getLocation() + " - " + pGame.getAnimatorPseudo();
	}
}
