package jyt.geconomicus.helper;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import jyt.geconomicus.helper.Event.EventType;

public class ImportDialog extends JDialog
{
	private static final String CANCEL_ACTION = "cancel"; //$NON-NLS-1$

	private List<Player> mNewPlayers = new ArrayList<>();
	private List<Event> mNewEvents = new ArrayList<>();
	private boolean mApplied = false;

	private final static Set<EventType> sBankEventTypes = new HashSet<>();
	static
	{
		sBankEventTypes.add(EventType.ASSESSMENT_FINAL);
		sBankEventTypes.add(EventType.BANKRUPT);
		sBankEventTypes.add(EventType.CANNOT_PAY);
		sBankEventTypes.add(EventType.INTEREST_ONLY);
		sBankEventTypes.add(EventType.MM_CHANGE);
		sBankEventTypes.add(EventType.NEW_CREDIT);
		sBankEventTypes.add(EventType.PRISON);
		sBankEventTypes.add(EventType.REIMB_CREDIT);
		sBankEventTypes.add(EventType.SIDE_INVESTMENT);
	}

	public ImportDialog(final JFrame pParentFrame, final Game pCurrentGame, final EntityManager pEntityManager) throws IOException
	{
		super(pParentFrame, UIMessages.getString("ImportDialog.Title.ImportGame")); //$NON-NLS-1$
		setIconImage(ImageIO.read(HelperUI.class.getResourceAsStream("/geconomicus.png"))); //$NON-NLS-1$
		Dimension size = new Dimension(900, 300);
		setSize(size);
		setModal(true);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width / 2 - size.width/2, screenSize.height / 2 - size.height/2);
		final JPanel mainPanel = new JPanel(new GridBagLayout());
		final JPanel sourcePanel = new JPanel(new GridBagLayout());
		sourcePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), UIMessages.getString("ImportDialog.Panel.Label.SourceOfImport"))); //$NON-NLS-1$

		final ButtonGroup sourceGroup = new ButtonGroup();
		final JRadioButton rbFromDB = new JRadioButton(UIMessages.getString("ImportDialog.Label.SourceDatabase")); //$NON-NLS-1$
		sourceGroup.add(rbFromDB);
		sourcePanel.add(rbFromDB, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		@SuppressWarnings("unchecked")
		final List<Game> games = pEntityManager.createNamedQuery("Game.findAll").getResultList(); //$NON-NLS-1$
		for (Iterator<Game> itGame = games.iterator(); itGame.hasNext();)
			if (itGame.next().getId().equals(pCurrentGame.getId()))
				itGame.remove();

		final JComboBox<Game> gameCombo = new JComboBox<>(games.toArray(new Game[games.size()]));
		sourcePanel.add(gameCombo, new GridBagConstraints(1, 0, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		final JRadioButton rbFromXml = new JRadioButton(UIMessages.getString("ImportDialog.Label.SourceXmlFile")); //$NON-NLS-1$
		sourceGroup.add(rbFromXml);
		sourcePanel.add(rbFromXml, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		final JTextField xmlFileTF = new JTextField();
		sourcePanel.add(xmlFileTF, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		final JButton chooseXmlFileButton = new JButton(UIMessages.getString("ImportDialog.Button.Label.Open")); //$NON-NLS-1$
		sourcePanel.add(chooseXmlFileButton, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		sourceGroup.setSelected(rbFromDB.getModel(), true);
		mainPanel.add(sourcePanel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		final JPanel actionPanel = new JPanel();
		actionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), UIMessages.getString("ImportDialog.Panel.Label.ActionToPerform"))); //$NON-NLS-1$
		final ButtonGroup actionGroup = new ButtonGroup();
		final JRadioButton importPlayerNamesAction = new JRadioButton(UIMessages.getString("ImportDialog.Option.Label.ImportPlayerNames")); //$NON-NLS-1$
		actionPanel.add(importPlayerNamesAction, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		actionGroup.add(importPlayerNamesAction);
		final JRadioButton importBankEventsAction = new JRadioButton(UIMessages.getString("ImportDialog.Option.Label.ImportBankEvents")); //$NON-NLS-1$
		actionPanel.add(importBankEventsAction, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		actionGroup.add(importBankEventsAction);
		actionGroup.setSelected(importPlayerNamesAction.getModel(), true);
		mainPanel.add(actionPanel, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
		final JButton importGameButton = new JButton(UIMessages.getString("ImportDialog.Button.Label.Import")); //$NON-NLS-1$
		final Action importGameAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				// import the game
				Game importedGame = null;
				if (sourceGroup.isSelected(rbFromDB.getModel()))
					importedGame = (Game)gameCombo.getSelectedItem();
				else
				{
					// Import from XML
					try
					{
						JAXBContext jc = JAXBContext.newInstance(Game.class);
						Unmarshaller unmarshaller = jc.createUnmarshaller();
						importedGame = (Game)unmarshaller.unmarshal(new File(xmlFileTF.getText()));
					}
					catch (PropertyException e)
					{
						UIUtil.showExceptionMessage(ImportDialog.this, e);
						return;
					}
					catch (JAXBException e)
					{
						UIUtil.showExceptionMessage(ImportDialog.this, e);
						return;
					}
				}
				if (importedGame != null)
				{
					if (actionGroup.isSelected(importPlayerNamesAction.getModel()))
					{
						pEntityManager.getTransaction().begin();
						try
						{
							// Import the player names
							for (Player player : importedGame.getPlayers())
							{
								final Player newPlayer = new Player(pCurrentGame, player.getName());
								mNewPlayers.add(newPlayer);
								final Event joinEvent = new Event(pCurrentGame, EventType.JOIN, newPlayer);
								mNewEvents.add(joinEvent);
								joinEvent.applyEvent();
							}
							pEntityManager.getTransaction().commit();
						}
						catch (Throwable e)
						{
							for (Event event : mNewEvents)
								pCurrentGame.removeEvent(event, false);
							pCurrentGame.recomputeAll(null);
							mNewEvents.clear();;
							for (Player player : mNewPlayers)
								pCurrentGame.removePlayer(player);
							mNewPlayers.clear();
							pEntityManager.getTransaction().rollback();
							UIUtil.showExceptionMessage(ImportDialog.this, e);
						}
					}
					else if (actionGroup.isSelected(importBankEventsAction.getModel()))
					{
						pEntityManager.getTransaction().begin();
						try
						{
							for (Event event : importedGame.getEvents())
								if (sBankEventTypes.contains(event.getEvt()))
									mNewEvents.add(event.cloneFor(pCurrentGame));
							pCurrentGame.recomputeAll(null);
							pEntityManager.getTransaction().commit();
						}
						catch (Throwable e)
						{
							for (Event event : mNewEvents)
								pCurrentGame.removeEvent(event, false);
							pCurrentGame.recomputeAll(null);
							pEntityManager.getTransaction().rollback();
							UIUtil.showExceptionMessage(ImportDialog.this, e);
						}
					}
				}
				mApplied = true;
				setVisible(false);
			}
		};
		importGameButton.setEnabled(!games.isEmpty());
		importGameButton.addActionListener(importGameAction);
		importGameButton.setMnemonic('i');
		buttonsPanel.add(importGameButton);

		chooseXmlFileButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				final JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("xml", "xml"));  //$NON-NLS-1$//$NON-NLS-2$
				if (fc.showOpenDialog(ImportDialog.this) == JFileChooser.APPROVE_OPTION)
				{
					xmlFileTF.setText(fc.getSelectedFile().getAbsolutePath());
					sourceGroup.setSelected(rbFromXml.getModel(), true);
					importGameButton.setEnabled(true);
				}
			}
		});

		final JButton cancelButton = new JButton(UIMessageKeyProvider.DIALOG_BUTTON_CANCEL.getMessage());
		final Action cancelAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				if (JOptionPane.showConfirmDialog(ImportDialog.this, UIMessageKeyProvider.DIALOG_MESSAGE_CANCEL.getMessage(), UIMessageKeyProvider.DIALOG_TITLE_CANCEL.getMessage(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					setVisible(false);
			}
		};
		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ImportDialog.CANCEL_ACTION);
		mainPanel.getActionMap().put(ImportDialog.CANCEL_ACTION, cancelAction);
		cancelButton.addActionListener(cancelAction);
		buttonsPanel.add(cancelButton);
		mainPanel.add(buttonsPanel, new GridBagConstraints(0, 2, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		getContentPane().add(mainPanel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	public boolean wasApplied()
	{
		return mApplied;
	}

	public List<Event> getNewEvents()
	{
		return mNewEvents;
	}

	public List<Player> getNewPlayers()
	{
		return mNewPlayers;
	}
}
