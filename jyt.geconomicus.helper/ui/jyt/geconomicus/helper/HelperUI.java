package jyt.geconomicus.helper;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import jyt.geconomicus.helper.CreditActionDialog.Fields;
import jyt.geconomicus.helper.Event.EventType;

public class HelperUI extends JFrame
{
	private final static Random sRand = new SecureRandom();

	private static final int PLAYER_INACTIVE = -1;
	private static final int PLAYER_OK = 1;
	private static final int PLAYER_NEEDS_BANK = 0;
	private static final int PLAYER_IN_PRISON = 2;

	private class EventTableModelDebtMoney extends AbstractTableModel
	{
		private final String[] COL_NAMES_DEBT_MONEY = new String[] {"Heure", "Type", "Joueur", "Intérêts", "Principal", "Cartes 1", "Cartes 2", "Cartes 4"};
		private final String[] COL_NAMES_LIBRE_MONEY = new String[] {"Heure", "Type", "Joueur", "Monnaie Faible", "Monnaie Moyenne", "Monnaie Forte", "Cartes 1", "Cartes 2", "Cartes 4"};
		private int mMoneySystem;

		public EventTableModelDebtMoney(int pMoneySystem)
		{
			super();
			mMoneySystem = pMoneySystem;
		}

		@Override
		public Object getValueAt(int pRowIndex, int pColumnIndex)
		{
			synchronized (mEventTable)
			{
				Event event = mEvents.get(mEvents.size() - pRowIndex - 1);
				switch (pColumnIndex)
				{
				case 0:
					return new SimpleDateFormat("HH:mm:ss").format(event.getTstamp());
				case 1:
					return event.getEvt().getDescription();
				case 2:
					return event.getPlayer() == null ? "" : event.getPlayer().getName();
				default:
					break;
				}
				if (mMoneySystem == Game.MONEY_DEBT)
					switch (pColumnIndex)
					{
					case 3:
						return event.getInterest();
					case 4:
						return event.getPrincipal();
					case 5:
						return event.getWeakCards();
					case 6:
						return event.getMediumCards();
					case 7:
						return event.getStrongCards();
		
					default:
						return "";
					}
				else
					switch (pColumnIndex)
					{
					case 3:
						return event.getWeakCoins();
					case 4:
						return event.getMediumCoins();
					case 5:
						return event.getStrongCoins();
					case 6:
						return event.getWeakCards();
					case 7:
						return event.getMediumCards();
					case 8:
						return event.getStrongCards();
		
					default:
						return "";
					}
			}
		}

		@Override
		public int getRowCount()
		{
			synchronized (mEventTable)
			{
				return mEvents.size();
			}
		}

		@Override
		public int getColumnCount()
		{
			return mMoneySystem == Game.MONEY_DEBT ? COL_NAMES_DEBT_MONEY.length : COL_NAMES_LIBRE_MONEY.length;
		}

		@Override
		public String getColumnName(int pColumn)
		{
			return mMoneySystem == Game.MONEY_DEBT ? COL_NAMES_DEBT_MONEY[pColumn] : COL_NAMES_LIBRE_MONEY[pColumn];
		}
	}

	private class ColorRenderer extends JLabel implements TableCellRenderer
	{
		private DefaultTableCellRenderer mDefault = new DefaultTableCellRenderer();

		public ColorRenderer()
		{
			super();
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable pTable, Object pValue, boolean pIsSelected, boolean pHasFocus, int pRow, int pColumn)
		{
			if (pColumn != 0)
				return mDefault.getTableCellRendererComponent(pTable, pValue, pIsSelected, pHasFocus, pRow, pColumn);
			int value = ((Integer)pValue).intValue();
			setBackground(value == PLAYER_INACTIVE ? Color.gray : value == PLAYER_NEEDS_BANK ? Color.red : value == PLAYER_IN_PRISON ? Color.lightGray : Color.green);
			return this;
		}
	}

	private class PlayerTableModel extends AbstractTableModel
	{
		private final String[] COL_NAMES_DEBT_MONEY = new String[] {"Statut", "Nom", "Âge", "Principal", "Intérêts", "Historique"};
		private final String[] COL_NAMES_LIBRE_MONEY = new String[] {"Statut", "Nom", "Âge", "Historique"};
		private int mMoneySystem;

		public PlayerTableModel(int pMoneySystem)
		{
			super();
			mMoneySystem = pMoneySystem;
		}

		@Override
		public Object getValueAt(int pRowIndex, int pColumnIndex)
		{
			synchronized (mPlayerTable)
			{
				final Player player = mPlayers.get(pRowIndex);
				if (player == null)
					return "";
				switch (pColumnIndex)
				{
				case 0:
					return player.isActive() ? (player.hasVisitedBank() ? (mPlayersInPrison.contains(player.getId()) ? PLAYER_IN_PRISON : PLAYER_OK) : PLAYER_NEEDS_BANK) : PLAYER_INACTIVE;
				case 1:
					return player.getName();
				case 2:
				{
					final Integer age = mPlayerAges.get(player.getId());
					if (age == null)
						return "";
					else
						return String.valueOf(age);
				}
				case 3:
					return mMoneySystem == Game.MONEY_DEBT ? player.getCurDebt() : mCreditHistory.get(player.getId()).toString();
				case 4:
					return mMoneySystem == Game.MONEY_DEBT ? player.getCurInterest() : "";
				case 5:
					return mMoneySystem == Game.MONEY_DEBT ? mCreditHistory.get(player.getId()).toString() : "";

				default:
					break;
				}
				return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int pColumnIndex)
		{
			return pColumnIndex == 1 || pColumnIndex == (mMoneySystem == Game.MONEY_DEBT ? 4 : 3) ? String.class : Integer.class;
		}

		@Override
		public int getRowCount()
		{
			synchronized (mPlayerTable)
			{
				return mPlayers.size();
			}
		}

		@Override
		public int getColumnCount()
		{
			return mMoneySystem == Game.MONEY_DEBT ? COL_NAMES_DEBT_MONEY.length : COL_NAMES_LIBRE_MONEY.length;
		}

		@Override
		public String getColumnName(int pColumn)
		{
			return mMoneySystem == Game.MONEY_DEBT ? COL_NAMES_DEBT_MONEY[pColumn] : COL_NAMES_LIBRE_MONEY[pColumn];
		}

		@Override
		public boolean isCellEditable(int pRowIndex, int pColumnIndex)
		{
			return pColumnIndex == 1;
		}

		@Override
		public void setValueAt(Object pAValue, int pRowIndex, int pColumnIndex)
		{
			synchronized (mPlayerTable)
			{
				super.setValueAt(pAValue, pRowIndex, pColumnIndex);
				Player player = mPlayers.get(pRowIndex);
				mEntityManager.getTransaction().begin();
				player.setName((String)pAValue);
				mEntityManager.getTransaction().commit();
				sortPlayers();
				fillStatusPanel();
			}
		}
	}

	private Game mGame = null;
	private List<Player> mPlayers = new ArrayList<>();
	private Map<Integer, StringBuilder> mCreditHistory = new HashMap<>();
	private Set<Integer> mPlayersInPrison = new HashSet<>();
	private Map<Integer, Integer> mPlayerAges = new HashMap<>();
	private List<Event> mEvents = new ArrayList<>();
	private EntityManager mEntityManager;
	private Set<Integer> mNonDeadPlayers = new HashSet<>();

	public final static String ACTION_ADDITIONAL_COMMENTS = "a";// addition
	public final static String ACTION_RECOMPUTE = "0";// remise à 0
	public final static String ACTION_INVEST_BANK = "b";// banque d'Affaires
	public final static String ACTION_NEW_CREDIT = "c";// Crédit
	public final static String ACTION_CANNOT_PAY = "d";// Défaut
	public final static String ACTION_ASSESSMENT_BANK = "e";// Évaluation finale
	public final static String ACTION_END_GAME = "f";// Fin
	public final static String ACTION_REIMB_INTEREST = "i";// Intérêt
	public final static String ACTION_JOIN_PLAYER = "j";// Joueur
	public final static String ACTION_TECH_BREAKTROUGH = "k";// tek
	public final static String ACTION_DEATH = "m";// Mort
	public final static String ACTION_IMPORT = "n";// Noms des joueurs
	public final static String ACTION_PRISON = "p";// Prison
	public final static String ACTION_QUIT_PLAYER = "q";// Quitte
	public final static String ACTION_QUIT_APP = "qq";// Quitte l'appli
	public final static String ACTION_REIMB_CREDIT = "r";// Remboursement
	public final static String ACTION_DELETE_PLAYER = "s";// Supprimer
	public final static String ACTION_NEW_TURN = "t";// Tour
	public final static String ACTION_UNEXPECTED_MM_CHANGE = "v";// Vol de la banque 
	public final static String ACTION_EXPORT = "x"; // eXport
	public final static String ACTION_BANKRUPTCY = "y";// y va mal
	public final static String ACTION_UNDO = "z";// ctrl Z

	private JTable mPlayerTable;
	private JTable mEventTable;
	private MyAction mMyAction = new MyAction();
	private List<JButton> mPlayerActionButtons = new ArrayList<>();
	private TableModel mPlayerTableModel;
	private TableModel mEventTableModel;
	private List<Integer> mDeathSchedule = new ArrayList<>();

	private class MyAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent pEvent)
		{
			// First non existing player events
			if (ACTION_JOIN_PLAYER.equals(pEvent.getActionCommand()))
			{
				final AddPlayerDialog dialog = new AddPlayerDialog(HelperUI.this, mGame, mEntityManager);
				dialog.setVisible(true);
				final Player newPlayer = dialog.getNewPlayer();
				if (newPlayer != null)
				{
					mPlayers.add(newPlayer);
					createNewEvent(newPlayer, EventType.JOIN);
				}
			}
			else if (ACTION_NEW_TURN.equals(pEvent.getActionCommand()))
			{
				createNewEvent(null, EventType.TURN);
				if (mValuesHelper != null)
					mValuesHelper.rotateValues();
				suggestDeaths();
			}
			else if (ACTION_END_GAME.equals(pEvent.getActionCommand()))
			{
				if (JOptionPane.showConfirmDialog(HelperUI.this, "Réellement terminer la partie ?", "Fin de partie", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					createNewEvent(null, EventType.END);
			}
			else if (ACTION_RECOMPUTE.equals(pEvent.getActionCommand()))
			{
				mEntityManager.getTransaction().begin();
				mGame.recomputeAll(null);
				mEntityManager.getTransaction().commit();
				refreshUI();
			}
			else if (ACTION_UNDO.equals(pEvent.getActionCommand()))
			{
				if (!mEvents.isEmpty())
				{
					if (JOptionPane.showConfirmDialog(HelperUI.this, "Voulez-vous vraiment annuler la dernière action ?", "Annulation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
					{
						mEntityManager.getTransaction().begin();
						final Event toUndo = mEvents.get(mEvents.size() - 1);
						mEvents.remove(toUndo);
						mGame.removeEvent(toUndo);
						mEntityManager.getTransaction().commit();
						if ((mValuesHelper != null) && EventType.TURN.equals(toUndo.getEvt()))
						{
							// Rotating 3 times is equivalent to rotating backwards
							mValuesHelper.rotateValues();
							mValuesHelper.rotateValues();
							mValuesHelper.rotateValues();
						}
						refreshUI();
					}
				}
			}
			else if (ACTION_UNEXPECTED_MM_CHANGE.equals(pEvent.getActionCommand()))
			{
				final CreditActionDialog dialog = new CreditActionDialog(HelperUI.this, "Changement imprévu de masse monétaire", 0, false, Fields.PRINCIPAL_ONLY);
				dialog.setVisible(true);
				final int principal = dialog.getPrincipal();
				if (principal > 0)
					createNewEventDebtMoney(null, EventType.MM_CHANGE, principal, 0, 0, 0, 0);
			}
			else if (ACTION_INVEST_BANK.equals(pEvent.getActionCommand()))
			{
				final CreditActionDialog dialog = new CreditActionDialog(HelperUI.this, "Investissement de la banque", 0, false, Fields.INTEREST_CARDS);
				dialog.setVisible(true);
				if (dialog.wasApplied())
					createNewEventDebtMoney(null, EventType.SIDE_INVESTMENT, dialog.getPrincipal(), 0, dialog.getWeakCards(), dialog.getMediumCards(), dialog.getStrongCards());
			}
			else if (ACTION_ASSESSMENT_BANK.equals(pEvent.getActionCommand()))
			{
				final CreditActionDialog dialog = new CreditActionDialog(HelperUI.this, "Inventaire final de la banque", 0, false, Fields.INTEREST_CARDS);
				dialog.setVisible(true);
				if (dialog.wasApplied())
					createNewEventDebtMoney(null, EventType.ASSESSMENT_FINAL, dialog.getPrincipal(), 0, dialog.getWeakCards(), dialog.getMediumCards(), dialog.getStrongCards());
			}
			else if (ACTION_ADDITIONAL_COMMENTS.equals(pEvent.getActionCommand()))
				new ChangeDescriptionDialog(HelperUI.this, mGame, mEntityManager).setVisible(true);
			else if (ACTION_QUIT_APP.equals(pEvent.getActionCommand()))
				dispatchEvent(new WindowEvent(HelperUI.this, WindowEvent.WINDOW_CLOSING));
			else if (ACTION_EXPORT.equals(pEvent.getActionCommand()))
			{
				final JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("xml", "xml"));
				if (fc.showSaveDialog(HelperUI.this) == JFileChooser.APPROVE_OPTION)
				{
					File toExport = fc.getSelectedFile();
					try
					{
						JAXBContext jc = JAXBContext.newInstance(Game.class);
						Marshaller marshaller = jc.createMarshaller();
						marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
						marshaller.marshal(mGame, toExport);
					}
					catch (PropertyException e)
					{
						JOptionPane.showMessageDialog(rootPane, "Erreur durant l'export", "Erreur", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
					catch (JAXBException e)
					{
						JOptionPane.showMessageDialog(rootPane, "Erreur durant l'export", "Erreur", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
				}
			}
			else if (ACTION_IMPORT.equals(pEvent.getActionCommand()))
			{
				try
				{
					final ImportDialog importDialog = new ImportDialog(HelperUI.this, mGame, mEntityManager);
					importDialog.setVisible(true);
					if (importDialog.wasApplied())
					{
						mPlayers.addAll(importDialog.getNewPlayers());
						mEvents.addAll(importDialog.getNewEvents());
						refreshUI();
					}
				}
				catch (IOException e)
				{
					// This shouldn't happen
					JOptionPane.showMessageDialog(HelperUI.this, "Une erreur est survenue", "Erreur", JOptionPane.ERROR_MESSAGE);
				}
			}
			else
			{
				// These are all player-related, let's double-check that a player is selected
				final int selectedPlayer = mPlayerTable.getSelectedRow();
				if (selectedPlayer < 0)
					JOptionPane.showMessageDialog(rootPane, "Sélectionnez un joueur pour effectuer cette action", "Joueur requis", JOptionPane.ERROR_MESSAGE);
				else
				{
					final Player player = mPlayers.get(selectedPlayer);
					if (ACTION_NEW_CREDIT.equals(pEvent.getActionCommand()))
					{
						final CreditActionDialog dialog = new CreditActionDialog(HelperUI.this, "Nouveau crédit pour " + player.getName(), 3, false, Fields.PRINCIPAL_ONLY);
						dialog.setVisible(true);
						final int principal = dialog.getPrincipal();
						if (principal > 0)
							createNewEventDebtMoney(player, EventType.NEW_CREDIT, principal, principal / 3, 0, 0, 0);
					}
					else if (ACTION_REIMB_INTEREST.equals(pEvent.getActionCommand()))
						createNewEventDebtMoney(player, EventType.INTEREST_ONLY, 0, player.getCurInterest(), 0, 0, 0);
					else if (ACTION_REIMB_CREDIT.equals(pEvent.getActionCommand()))
					{
						final CreditActionDialog dialog = new CreditActionDialog(HelperUI.this, "Remboursement de crédit pour " + player.getName(), 3, false, Fields.PRINCIPAL_ONLY);
						dialog.setVisible(true);
						final int principal = dialog.getPrincipal();
						if (principal > 0)
							createNewEventDebtMoney(player, EventType.REIMB_CREDIT, principal, player.getCurInterest(), 0, 0, 0);
					}
					else if (ACTION_TECH_BREAKTROUGH.equals(pEvent.getActionCommand()))
						createNewEvent(player, EventType.XTECHNOLOGICAL_BREAKTHROUGH);
					else if (ACTION_DELETE_PLAYER.equals(pEvent.getActionCommand()))
					{
						if (JOptionPane.showConfirmDialog(HelperUI.this, "!!! ATTENTION !!!\nCette action va supprimer définitivement le joueur ainsi que toutes ses actions.\nSi un joueur quitte simplement la partie il suffit de lui faire quitter la partie,\nLa seule utilisation valide est suite à un import de joueurs depuis une autre partie, et le joueur en question ne joue pas dans cette nouvelle partie.\nVoulez-vous vraiment continuer ?", "Suppression définitive de joueur", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
						{
							if (JOptionPane.showConfirmDialog(HelperUI.this, "ATTENTION, CETTE ACTION EST IRRÉVERSIBLE !!!\nÊtes-vous vraiment sûr de vouloir SUPPRIMER totalement ce joueur ?", "Suppression définitive de joueur", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
							{
								synchronized (mPlayerTable)
								{
									mEntityManager.getTransaction().begin();
									for (Event event : new ArrayList<>(mGame.getEvents()))
									{
										if (event.getPlayer() == player)
										{
											mGame.removeEvent(event);
											mEvents.remove(event);
										}
									}
									mPlayers.remove(player);
									mGame.removePlayer(player);
									mGame.recomputeAll(null);
									mEntityManager.getTransaction().commit();
									refreshUI();
								}
							}
						}
					}
					else
					{
						final boolean cannotPay = ACTION_CANNOT_PAY.equals(pEvent.getActionCommand());
						if (cannotPay || ACTION_DEATH.equals(pEvent.getActionCommand()) || ACTION_QUIT_PLAYER.equals(pEvent.getActionCommand()))
						{
							String title;
							if (cannotPay)
								title = "Défaut de remboursement pour " + player.getName();
							else if (ACTION_DEATH.equals(pEvent.getActionCommand()))
							{
								title = "Mort de " + player.getName();
								if (!mSuggestedDeathsLabel.getText().contains(player.getName()))
									if (JOptionPane.showConfirmDialog(HelperUI.this, "Ce joueur ne fait pas partie de la liste des joueurs suggérés. Voulez-vous quand même continuer et le faire renaître ?", "Joueur non suggéré", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
										return;
								// Check if this player was already reborn
								for (Event event : mEvents)
									if (EventType.DEATH.equals(event.getEvt()) && player.getId().equals(event.getPlayer().getId()))
										if (JOptionPane.showConfirmDialog(HelperUI.this, "Ce joueur a déjà subi une renaissance. Voulez-vous quand même continuer et le faire renaître à nouveau ?", "Joueur déjà mort", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
											return;
							}
							else
								title = player.getName() + " qutte la partie";
							if ((player.getCurDebt() > 0) && (ACTION_DEATH.equals(pEvent.getActionCommand()) || ACTION_QUIT_PLAYER.equals(pEvent.getActionCommand())))
								if (JOptionPane.showConfirmDialog(HelperUI.this, "Ce joueur a encore des dettes. Il devrait aller voir la banque avant de mourir. Voulez-vous quand même le faire mourir ?", "Joueur avec dettes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
									return;
							final CreditActionDialog dialog = new CreditActionDialog(HelperUI.this, title, 0, cannotPay, mGame.getMoneySystem() == Game.MONEY_DEBT ? Fields.INTEREST_CARDS : Fields.COINS_CARDS);
							dialog.setVisible(true);
							if (dialog.wasApplied())
							{
								EventType eventType;
								if (cannotPay)
									eventType = dialog.getEventType();
								else if (ACTION_DEATH.equals(pEvent.getActionCommand()))
								{
									eventType = EventType.DEATH;
									mNonDeadPlayers.remove(player.getId());
								}
								else// if (ACTION_QUIT_PLAYER.equals(pEvent.getActionCommand()))
									eventType = EventType.QUIT;
								if (mGame.getMoneySystem() == Game.MONEY_DEBT)
									createNewEventDebtMoney(player, eventType, dialog.getPrincipal(), dialog.getInterest(), dialog.getWeakCards(), dialog.getMediumCards(), dialog.getStrongCards());
								else
									createNewEventLibreMoney(player, eventType, dialog.getWeakCoins(), dialog.getMediumCoins(), dialog.getStrongCoins(), dialog.getWeakCards(), dialog.getMediumCards(), dialog.getStrongCards());
							}
						}
					}

				}
			}
		}

		public void createNewEvent(final Player pPlayer, final EventType pEventType)
		{
			createNewEventDebtMoney(pPlayer, pEventType, 0, 0, 0, 0, 0);
		}

		public void createNewEventDebtMoney(final Player pPlayer, final EventType pEventType, final int principal, final int interest, final int weakCards, final int mediumCards, final int strongCards)
		{
			mEntityManager.getTransaction().begin();
			Event event = new Event(mGame, pEventType, pPlayer);
			if (principal > 0) event.setPrincipal(principal);
			if (interest > 0) event.setInterest(interest);
			if (weakCards > 0) event.setWeakCards(weakCards);
			if (mediumCards > 0) event.setMediumCards(mediumCards);
			if (strongCards > 0) event.setStrongCards(strongCards);
			event.applyEvent();
			mEntityManager.getTransaction().commit();
			mEvents.add(event);
			refreshUI();
		}

		public void createNewEventLibreMoney(final Player pPlayer, final EventType pEventType, final int weakCoins, final int mediumCoins, final int strongCoins, final int weakCards, final int mediumCards, final int strongCards)
		{
			mEntityManager.getTransaction().begin();
			Event event = new Event(mGame, pEventType, pPlayer);
			if (weakCoins > 0) event.setWeakCoins(weakCoins);
			if (mediumCoins > 0) event.setMediumCoins(mediumCoins);
			if (strongCoins > 0) event.setStrongCoins(strongCoins);
			if (weakCards > 0) event.setWeakCards(weakCards);
			if (mediumCards > 0) event.setMediumCards(mediumCards);
			if (strongCards > 0) event.setStrongCards(strongCards);
			event.applyEvent();
			mEntityManager.getTransaction().commit();
			mEvents.add(event);
			refreshUI();
		}
	}

	private void enableButtons()
	{
		final boolean enabled = mPlayerTable.getSelectedRow() != -1;
		for (JButton jButton : mPlayerActionButtons)
			jButton.setEnabled(enabled);
		mMenuPlayer.setEnabled(enabled);
	}

	private void createAction(JToolBar pToolBar, String pButtonImage, JMenu pMenu, String pMenuLabel, String pActionName, boolean pPlayerAction)
	{
		if (pToolBar != null)
		{
			final JButton button = new JButton();
			button.setBorder(BorderFactory.createRaisedBevelBorder());
			try
			{
				final Image img = ImageIO.read(getClass().getResource("/buttons/" + pButtonImage + ".png"));
				final Graphics g = img.getGraphics();
				g.setFont(g.getFont().deriveFont(12f).deriveFont(Font.BOLD));
				final Rectangle2D bounds = g.getFontMetrics().getStringBounds(pActionName.substring(0, 1), g);
				g.setColor(Color.black);
				g.drawString(pActionName.substring(0, 1), 1, (int)(bounds.getHeight() - 2));
				g.dispose();
				button.setIcon(new ImageIcon(img));
			}
			catch (Exception e)
			{
				button.setText(pMenuLabel + " (" + pActionName.charAt(0) + ")");
			}
			button.setActionCommand(pActionName);
			button.addActionListener(mMyAction);
			button.setToolTipText(pMenuLabel + " (" + pActionName.charAt(0) + ")");
			pToolBar.add(button);
			final JPanel separator = new JPanel();
			separator.setMinimumSize(new Dimension(3, 10));
			pToolBar.add(separator);
			mPlayerTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar(pActionName.charAt(0)), 0/*InputEvent.CTRL_DOWN_MASK*/), pActionName);
			mPlayerTable.getActionMap().put(pActionName, mMyAction);
			if (pPlayerAction)
				mPlayerActionButtons.add(button);
		}

		if (pMenu != null)
		{
			final JMenuItem menuItem = new JMenuItem(pMenuLabel);
			menuItem.setMnemonic(pActionName.charAt(0));
			menuItem.addActionListener(mMyAction);
			menuItem.setActionCommand(pActionName);
			pMenu.add(menuItem);
		}
	}

	private JLabel mMoneySystemLabel;
	private JLabel mTurnNumberLabel;
	private JLabel mMoneyMassLabel;
	private JLabel mMoneyPerPlayerLabel;
	private JLabel mNbPlayersLabel;
	private JLabel mGainedInterestLabel;
	private JLabel mSeizedValuesLabel;
	private JLabel mSuggestedDeathsLabel;

	private JMenu mMenuPlayer;

	private JCheckBoxMenuItem mMenuViewMoneyHelper;
	private ValuesHelper mValuesHelper = null;

	public HelperUI(final EntityManager pEntityManager, final Game pGame) throws IOException
	{
		super("Aide Ğeconomicus");
		mEntityManager = pEntityManager;
		setSize(1024, 800);
		setIconImage(ImageIO.read(HelperUI.class.getResourceAsStream("/geconomicus.png")));
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent pEvent)
			{
				super.windowClosing(pEvent);
				mEntityManager.close();
				System.exit(0);
			}
		});
		final JPanel mainPanel = new JPanel(new GridBagLayout());
		final JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildPlayerListPane(pGame.getMoneySystem()), buildEventListPane(pGame.getMoneySystem()));
		mainPanel.add(mainSplitPane, new GridBagConstraints(0, 2, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		mainSplitPane.setResizeWeight(0.3);
		addWindowFocusListener(new WindowFocusListener()
		{
			@Override
			public void windowLostFocus(WindowEvent pE)
			{
			}
			
			@Override
			public void windowGainedFocus(WindowEvent pE)
			{
				mPlayerTable.requestFocus();
			}
		});

		final JMenuBar menuBar = new JMenuBar();
		final JMenu menuGame = new JMenu("Partie");
		menuGame.setMnemonic('p');
		final JToolBar toolbar = new JToolBar();
		createAction(toolbar, "add_player", menuGame, "Nouveau Joueur...", ACTION_JOIN_PLAYER, false);
		createAction(toolbar, "new_turn", menuGame, "Nouveau Tour", ACTION_NEW_TURN, false);
		mMenuPlayer = new JMenu("Joueur");
		mMenuPlayer.setMnemonic('j');
		if (pGame.getMoneySystem() == Game.MONEY_DEBT)
		{
			createAction(toolbar, "new_credit", mMenuPlayer, "Nouveau crédit...", ACTION_NEW_CREDIT, true);
			createAction(toolbar, "pay_interest", mMenuPlayer, "Remboursement des intérêts seuls", ACTION_REIMB_INTEREST, true);
			createAction(toolbar, "pay_credit", mMenuPlayer, "Remboursement de crédit...", ACTION_REIMB_CREDIT, true);
			createAction(toolbar, "cannot_pay", mMenuPlayer, "Défaut de paiement...", ACTION_CANNOT_PAY, true);
			createAction(toolbar, "moneymass_change", menuGame, "Changement de masse monétaire (vol de la banque ?)...", ACTION_UNEXPECTED_MM_CHANGE, false);
			createAction(toolbar, "bank_invest", menuGame, "Investissement de la banque...", ACTION_INVEST_BANK, false);
			createAction(toolbar, "bank_assess", menuGame, "Inventaire de la banque...", ACTION_ASSESSMENT_BANK, false);
		}
		createAction(toolbar, "death", mMenuPlayer, "Mort / renaissance...", ACTION_DEATH, true);
		createAction(toolbar, "leaves", mMenuPlayer, "Le joueur quitte la partie...", ACTION_QUIT_PLAYER, true);
		createAction(null, "", menuGame, "Fin de partie", ACTION_END_GAME, false);
		createAction(null, "", menuGame, "Recalcul des événements (remise à 0)", ACTION_RECOMPUTE, false);
		createAction(null, "", mMenuPlayer, "Rupture tekno", ACTION_TECH_BREAKTROUGH, true);
		createAction(toolbar, "undo", menuGame, "Annuler la dernière action (Ctrl Z)", ACTION_UNDO, false);
		createAction(null, "", menuGame, "Export...", ACTION_EXPORT, false);
		createAction(null, "", menuGame, "Import...", ACTION_IMPORT, false);
		createAction(null, "", menuGame, "Changer la description...", ACTION_ADDITIONAL_COMMENTS, false);
		createAction(null, "", menuGame, "Quitter", ACTION_QUIT_APP, false);
		createAction(null, "", mMenuPlayer, "Supprimer définitivement un joueur", ACTION_DELETE_PLAYER, false);
		menuBar.add(menuGame);
		menuBar.add(mMenuPlayer);
		final JMenu menuView = new JMenu("Vue");
		menuView.setMnemonic('v');
		mMenuViewMoneyHelper = new JCheckBoxMenuItem("Monnaie", false);
		mMenuViewMoneyHelper.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				if (mMenuViewMoneyHelper.isSelected())
				{
					if (mValuesHelper == null)
					{
						mValuesHelper = new ValuesHelper(HelperUI.this, mGame.getMoneySystem());
						for (Event event : mEvents)
							if (EventType.TURN.equals(event.getEvt()))
								mValuesHelper.rotateValues();
						mValuesHelper.setVisible(true);
					}
				}
				else if (mValuesHelper != null)
					mValuesHelper.dispatchEvent(new WindowEvent(mValuesHelper, WindowEvent.WINDOW_CLOSING));
			}
		});
		menuView.add(mMenuViewMoneyHelper);
		menuBar.add(menuView);
		final JMenu menuHelp = new JMenu("Aide");
		final JMenuItem menuItemWebsite = new JMenuItem("Site Web");
		menuItemWebsite.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				try
				{
					Desktop.getDesktop().browse(new URL("https://github.com/jytou/geconomicus_helper/blob/master/README.md").toURI());
				}
				catch (MalformedURLException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				catch (URISyntaxException e)
				{
					e.printStackTrace();
				}
			}
		});
		menuHelp.add(menuItemWebsite);
		final JMenuItem menuItemAbout = new JMenuItem("À propos");
		menuItemAbout.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				JOptionPane.showMessageDialog(HelperUI.this, "Programme d'aide à l'animateur et banquier de Ğeconomicus.\n\nVersion 0.9. 2018/04/12", "À propos", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		menuHelp.add(menuItemAbout);
		menuBar.add(menuHelp);
		mainPanel.add(toolbar, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		setJMenuBar(menuBar);

		mainPanel.add(buildStatusPanel(pGame), new GridBagConstraints(0, 3, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		getContentPane().add(mainPanel);
		setGame(pGame);
		suggestDeaths();
		setVisible(true);
	}

	public JPanel buildPlayerListPane(int pMoneySystem)
	{
		final JPanel playerListPane = new JPanel(new GridBagLayout());
		mPlayerTableModel = new PlayerTableModel(pMoneySystem);
		mPlayerTable = new JTable(mPlayerTableModel);
		mPlayerTable.setDefaultRenderer(Integer.class, new ColorRenderer());
		final TableColumnModel columnModel = mPlayerTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(10);// Status
		columnModel.getColumn(1).setPreferredWidth(100);// Name
		columnModel.getColumn(2).setPreferredWidth(10);// Age
		if (pMoneySystem == Game.MONEY_DEBT)
		{
			columnModel.getColumn(3).setPreferredWidth(30);// Principal
			columnModel.getColumn(4).setPreferredWidth(30);// Interest
			columnModel.getColumn(4).setPreferredWidth(60);// History
		}
		else
			columnModel.getColumn(3).setPreferredWidth(30);// History (essentially rebirth)
		playerListPane.add(new JScrollPane(mPlayerTable), new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		mPlayerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent pEvent)
			{
				enableButtons();
			}
		});
		return playerListPane;
	}

	public JPanel buildEventListPane(int pMoneySystem)
	{
		final JPanel eventListPane = new JPanel(new GridBagLayout());
		eventListPane.add(new JLabel("Morts suggérés :"), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		mSuggestedDeathsLabel = new JLabel();
		eventListPane.add(mSuggestedDeathsLabel, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		mEventTableModel = new EventTableModelDebtMoney(pMoneySystem);
		mEventTable = new JTable(mEventTableModel);
		eventListPane.add(new JScrollPane(mEventTable), new GridBagConstraints(0, 10, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		final TableColumnModel columnModel = mEventTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(70);// date/time
		columnModel.getColumn(1).setPreferredWidth(150);// full text event type
		columnModel.getColumn(2).setPreferredWidth(100);// player name
		// The additional columns are values for coins and cards
		for (int i = 3; i < (pMoneySystem == Game.MONEY_DEBT ? 8 : 9); i++)
			columnModel.getColumn(i).setPreferredWidth(30);
		return eventListPane;
	}

	public JPanel buildStatusPanel(final Game pGame)
	{
		final JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.add(new JLabel("Monnaie : "));
		statusPanel.add(mMoneySystemLabel = new JLabel());
		statusPanel.add(createSeparationPanel());
		statusPanel.add(new JLabel("Tour : "));
		statusPanel.add(mTurnNumberLabel = new JLabel());
		statusPanel.add(createSeparationPanel());
		statusPanel.add(new JLabel("Joueurs : "));
		statusPanel.add(mNbPlayersLabel = new JLabel());
		statusPanel.add(createSeparationPanel());
		if (pGame.getMoneySystem() == Game.MONEY_DEBT)
		{
			statusPanel.add(new JLabel("MM : "));
			statusPanel.add(mMoneyMassLabel = new JLabel());
			statusPanel.add(createSeparationPanel());
			statusPanel.add(new JLabel("Monnaie/joueur : "));
			statusPanel.add(mMoneyPerPlayerLabel = new JLabel());
			statusPanel.add(createSeparationPanel());
			statusPanel.add(new JLabel("Gains : "));
			statusPanel.add(mGainedInterestLabel = new JLabel());
			statusPanel.add(createSeparationPanel());
			statusPanel.add(new JLabel("Saisies : "));
			statusPanel.add(mSeizedValuesLabel = new JLabel());
		}
		fillStatusPanel();
		return statusPanel;
	}

	public JPanel createSeparationPanel()
	{
		final JPanel separationPanel = new JPanel();
		separationPanel.setMinimumSize(new Dimension(10, 5));
		separationPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		return separationPanel;
	}

	private void fillStatusPanel()
	{
		if (mGame != null)
		{
			mMoneySystemLabel.setText(mGame.getMoneySystem() == Game.MONEY_DEBT ? "Monnaie-dette " : "Monnaie libre ");
			mTurnNumberLabel.setText(String.valueOf(mGame.getTurnNumber()) + " / " + String.valueOf(mGame.getNbTurnsPlanned()) + " ");
			mNbPlayersLabel.setText(String.valueOf(mPlayers.size()) + " ");
			if (mGame.getMoneySystem() == Game.MONEY_DEBT)
			{
				mMoneyMassLabel.setText(String.valueOf(mGame.getMoneyMass()) + " ");
				mMoneyPerPlayerLabel.setText(new DecimalFormat(".###").format(1.0 * mGame.getMoneyMass() / mPlayers.size()) + " ");
				mGainedInterestLabel.setText(String.valueOf(mGame.getInterestGained()) + " ");
				mSeizedValuesLabel.setText(String.valueOf(mGame.getSeizedValues()) + " ");
			}
		}
	}

	public EntityManager getEntityManager()
	{
		return mEntityManager;
	}

	public static void main(String[] args) throws IOException
	{
		final EntityManagerFactory factory = Persistence.createEntityManagerFactory("geco");
		final EntityManager entityManager = factory.createEntityManager();
		new ChooseGameDialog(entityManager).setVisible(true);
	}

	public Game getGame()
	{
		return mGame;
	}

	public void setGame(Game pGame)
	{
		mGame = pGame;
		mMenuViewMoneyHelper.setEnabled(mGame.getMoneySystem() == Game.MONEY_LIBRE);
		mPlayers.clear();
		mPlayers.addAll(mGame.getPlayers());
		mEvents.clear();
		mEvents.addAll(mGame.getEvents());
		sortEvents();
		mNonDeadPlayers.clear();
		for (Player player : mPlayers)
			mNonDeadPlayers.add(player.getId());
		for (Event event : mEvents)
			if (EventType.DEATH.equals(event.getEvt()))
				mNonDeadPlayers.remove(event.getPlayer().getId());
		sortPlayers();
		fillStatusPanel();
		createDeathSchedule();
	}

	private double rebornFunction(double pNbPlayers, double pNbTurns, double pReferenceTurn, double pRenewedAtReferenceTurn, double pCurrentTurn)
	{
		return (pCurrentTurn - pReferenceTurn) * (pNbPlayers - pRenewedAtReferenceTurn) / (pNbTurns - pReferenceTurn) + pRenewedAtReferenceTurn;
	}

	private void createDeathSchedule()
	{
		mDeathSchedule.clear();
		// search for the first turn when there was no player movement
		int t0 = 1;// this is the first "reference" turn
		int p0 = 0;// pop regenerated at the "reference" turn
		int pSinceLastReference = 0;// nb players reborn since reference turn
		int t = 1;
		for (int i = 0; i < mEvents.size(); i++)
		{
			Event event = mEvents.get(i);
			if (EventType.TURN.equals(event.getEvt()))
				t++;
			else if (EventType.JOIN.equals(event.getEvt()) || EventType.QUIT.equals(event.getEvt()))
			{
				t0 = t;
				p0 += pSinceLastReference;
				pSinceLastReference = 0;
			}
			else if (EventType.DEATH.equals(event.getEvt()))
			{
				pSinceLastReference++;
				// Fill the entries until the current turn
				while (mDeathSchedule.size() < t)
					mDeathSchedule.add(0);
				// Increment the existing value
				mDeathSchedule.set(t - 1, mDeathSchedule.get(t - 1).intValue() + 1);
			}
		}
		// Now check if everything has been according to the suggestions since the last reference turn
		int nbPlayers = 0;
		for (Player player : mPlayers)
			if (player.isActive())
				nbPlayers++;
		if (mGame.getTurnNumber() > t0)
		// Otherwise there's nothing to check, we're at the reference turn
			if (pSinceLastReference != Math.round(rebornFunction(nbPlayers, mGame.getNbTurnsPlanned(), t0, p0, mGame.getTurnNumber())))
			{
				// We have to make the current turn the reference turn because the animator hasn't been following the plan
				t0 = mGame.getTurnNumber();
				p0 += pSinceLastReference;
				pSinceLastReference = 0;
			}
		int curRenewed = p0;
		for (int i = t0; i < mGame.getNbTurnsPlanned(); i++)
		{
			// Fill the entries until the current turn
			while (mDeathSchedule.size() < i)
				mDeathSchedule.add(0);
			int target = (int)Math.round(rebornFunction(nbPlayers, mGame.getNbTurnsPlanned(), t0, p0, i + 1));
			int diff = target - curRenewed;
			mDeathSchedule.set(i - 1, diff);
			curRenewed += diff;
		}
	}

	private void sortPlayers()
	{
		int selectedPlayerIndex = mPlayerTable.getSelectedRow();
		Player selectedPlayer = null;
		if (selectedPlayerIndex >= 0)
			selectedPlayer = mPlayers.get(selectedPlayerIndex);
		synchronized (mPlayerTable)
		{
			Collections.sort(mPlayers, new Comparator<Player>()
			{
				@Override
				public int compare(Player p1, Player p2)
				{
					if (p1.isActive() != p2.isActive())
						return p1.isActive() ? -1 : 1;
					if (p1.hasVisitedBank() != p2.hasVisitedBank())
						return p2.hasVisitedBank() ? -1 : 1;
					return p1.getName().compareTo(p2.getName());
				}
			});
			mCreditHistory.clear();
			mPlayersInPrison.clear();
			mPlayerAges.clear();
			Map<Integer, Integer> currentCredit = new HashMap<>();
			for (Event event : mEvents)
			{
				if (event.getPlayer() != null)
				{
					final Integer playerId = event.getPlayer().getId();
					StringBuilder sb = mCreditHistory.get(playerId);
					if (sb == null)
						mCreditHistory.put(playerId, sb = new StringBuilder());
					switch (event.getEvt())
					{
					case JOIN:
						mPlayerAges.put(playerId, 1);
						break;
					case NEW_CREDIT:
						sb.append('+').append(String.valueOf(event.getPrincipal()));
						if (currentCredit.containsKey(playerId))
							currentCredit.put(playerId, currentCredit.get(playerId).intValue() + event.getPrincipal());
						else
							currentCredit.put(playerId, event.getPrincipal());
						break;
					case INTEREST_ONLY:
						sb.append("/").append(String.valueOf(event.getInterest()));
						break;
					case REIMB_CREDIT:
						if (event.getPrincipal() == currentCredit.get(playerId).intValue())
						// Credit reimbursed in full
						{
							sb.append("/R.");
							currentCredit.remove(playerId);
						}
						else
						{
							sb.append("/R").append(String.valueOf(event.getPrincipal()));
							currentCredit.put(playerId, currentCredit.get(playerId).intValue() - event.getPrincipal());
						}
						break;
					case BANKRUPT:
					case PRISON:
						// Add to prison for this turn
						mPlayersInPrison.add(playerId);
					case CANNOT_PAY:
						sb.append("/D");
						if (EventType.BANKRUPT.equals(event.getEvt()))
							sb.append("F");
						else if (EventType.PRISON.equals(event.getEvt()))
							sb.append("P");
						sb.append(".");
						currentCredit.remove(playerId);
						break;
					case DEATH:// Death clears everything
						sb.setLength(0);
						sb.append("M ");
						currentCredit.remove(playerId);
						mPlayerAges.put(playerId, 1);
						break;
					default:
						break;
					}
				}
				else if (EventType.TURN.equals(event.getEvt()))
				{
					mPlayersInPrison.clear();
					for (Integer playerId : mPlayerAges.keySet())
						mPlayerAges.put(playerId, mPlayerAges.get(playerId).intValue() + 1);
				}
			}
		}
		mPlayerTable.tableChanged(new TableModelEvent(mPlayerTableModel));
		mPlayerTable.repaint();
		if (selectedPlayer != null)
		{
			int startIndex = 0;
			for (Player player : mPlayers)
			{
				if (player == selectedPlayer)
					break;
				startIndex++;
			}
			mPlayerTable.setRowSelectionInterval(startIndex, startIndex);
		}
	}

	private void sortEvents()
	{
		synchronized (mEventTable)
		{
			Collections.sort(mEvents, new Comparator<Event>()
			{
				@Override
				public int compare(Event e1, Event e2)
				{
					return e1.getTstamp().compareTo(e2.getTstamp());
				}
			});
			mEventTable.tableChanged(new TableModelEvent(mEventTableModel));
			mEventTable.repaint();
		}
	}

	public void suggestDeaths()
	{
		// Suggest new deaths
		Set<Integer> nonDeadPlayers = new HashSet<>();
		nonDeadPlayers.addAll(mNonDeadPlayers);
		SortedSet<String> chosen = new TreeSet<>();
		createDeathSchedule();
		if ((nonDeadPlayers.size() > 0) && (mGame.getTurnNumber() <= mDeathSchedule.size()))
		{
			int curTarget = mDeathSchedule.get(mGame.getTurnNumber() - 1);
			while ((chosen.size() < curTarget) && (nonDeadPlayers.size() > 0))
			{
				if (nonDeadPlayers.size() == 1)
				{
					chosen.add(getPlayerWithId(nonDeadPlayers.iterator().next()).getName());
					nonDeadPlayers.clear();
				}
				else
				{
					int rand = sRand.nextInt(nonDeadPlayers.size() + 1);
					Iterator<Integer> it = nonDeadPlayers.iterator();
					for (int i = 0; i < rand - 1; i++)
						it.next();
					Integer i = it.next();
					nonDeadPlayers.remove(i);
					chosen.add(getPlayerWithId(i).getName());
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		for (String playerName : chosen)
		{
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(playerName);
		}
		mSuggestedDeathsLabel.setText(sb.toString());
	}

	private Player getPlayerWithId(Integer pPlayerId)
	{
		for (Player player : mPlayers)
			if (player.getId().equals(pPlayerId))
				return player;
		return null;
	}

	public void refreshUI()
	{
		sortEvents();
		sortPlayers();
		fillStatusPanel();
	}

	public void closedValueHelper()
	{
		mMenuViewMoneyHelper.setSelected(false);
		mValuesHelper = null;
	}
}
