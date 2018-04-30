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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.MessageFormat;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
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

import jyt.geconomicus.helper.CreditActionDialog.Purpose;
import jyt.geconomicus.helper.Event.EventType;

/**
 * The helper's main frame.<br>
 * Note that we don't take too much care of keeping too many caches. The number of players in a Geconomicus game is limited,
 * and the number of events is also limited. Therefore we can afford to recalculate absolutely everything from scratch almost
 * all the time, without much trouble even on an old computer.
 * @author jytou
 */
public class HelperUI extends JFrame
{
	// Increment those at each release.
	protected static final String RELEASE_DATE = "2018/04/30"; //$NON-NLS-1$
	protected static final String VERSION_NUMBER = "1.0.2"; //$NON-NLS-1$

	// Default database name used
	static final String DB_DEFAULT_NAME = "geco"; //$NON-NLS-1$

	private final static Random sRand = new SecureRandom();

	// The different states the player can be
	private static final int PLAYER_INACTIVE = -1;  // the player quit the game
	private static final int PLAYER_OK = 1;         // ok
	private static final int PLAYER_NEEDS_BANK = 0; // needs to go to the bank this turn
	private static final int PLAYER_IN_PRISON = 2;  // is in prison (or personal bankruptcy)
	private static final int PLAYER_IN_WARNING = 3; // warning on the player - name collision

	// Because those are internationalized, we have to check that there is no collision in the different mnemonics
	// Therefore these must be added to the following static verification
	private static final String MNEM_MENU_VIEW = UIMessages.getString("HelperUI.Menu.Mnemonic.View"); //$NON-NLS-1$
	private static final String MNEM_MENU_PLAYER = UIMessages.getString("HelperUI.Menu.Mnemonic.Player"); //$NON-NLS-1$
	private static final String MNEM_MENU_GAME = UIMessages.getString("HelperUI.Menu.Mnemonic.Game"); //$NON-NLS-1$
	// Make sure there is no collision in the mnemonics
	static
	{
		final Set<String> mnemos = new HashSet<>();
		final String[] allMnems = new String[] {MNEM_MENU_VIEW, MNEM_MENU_GAME, MNEM_MENU_PLAYER};
		for (String s : allMnems)
		{
			if (mnemos.contains(s))
				throw new RuntimeException("Collision in menu mnemonics for the current locale for letter " + s + ". Verify there is no collision."); //$NON-NLS-1$ //$NON-NLS-2$
			mnemos.add(s);
		}
	}

	// Colors assigned to the different players
	private Map<String, Color> mPlayerColors = new HashMap<>();

	// Base background color for even rows in the Events table
	private final static Color BG_EVENT_COLOR_EVEN = Color.white;
	// Base background color for odd rows in the Events table
	private final static Color BG_EVENT_COLOR_ODD = new Color(240, 240, 240);

	// Colors for every type of event
	private final static Map<EventType, Color> COLORS = new HashMap<>();
	static
	{
		COLORS.put(EventType.JOIN, new Color(230, 230, 255));
		COLORS.put(EventType.TURN, new Color(210, 235, 200));
		COLORS.put(EventType.ASSESSMENT_FINAL, new Color(255, 190, 253));
		COLORS.put(EventType.SIDE_INVESTMENT, new Color(255, 200, 254));
		COLORS.put(EventType.BANKRUPT, new Color(255, 190, 200));
		COLORS.put(EventType.CANNOT_PAY, new Color(255, 212, 190));
		COLORS.put(EventType.DEATH, new Color(210, 210, 210));
		COLORS.put(EventType.PRISON, new Color(255, 170, 175));
		COLORS.put(EventType.END, new Color(210, 254, 255));
		COLORS.put(EventType.INTEREST_ONLY, new Color(230, 255, 205));
		COLORS.put(EventType.REIMB_CREDIT, new Color(214, 255, 211));
		COLORS.put(EventType.MM_CHANGE, new Color(238, 215, 255));
		COLORS.put(EventType.NEW_CREDIT, new Color(249, 255, 205));
		COLORS.put(EventType.QUIT, new Color(205, 220, 255));
		COLORS.put(EventType.XTECHNOLOGICAL_BREAKTHROUGH, new Color(236, 213, 212));
	}

	// The textual history for each player on the player table
	private static final char TXT_HISTORY_NEWCREDIT = '+';
	private static final String TXT_HISTORY_CREDIT_END = "."; //$NON-NLS-1$
	private static final String TXT_HISTORY_TURN = "/"; //$NON-NLS-1$
	private static final String TXT_HISTORY_SEPARATOR = " "; //$NON-NLS-1$

	/**
	 * The rendering of events: every line has a different grey level and events and players have distinctive colors
	 * @author jytou
	 */
	private class EventColorRenderer extends JLabel implements TableCellRenderer
	{
		public EventColorRenderer()
		{
			super();
			setOpaque(true);
			// For some reason it is bold by default
			setFont(getFont().deriveFont(Font.PLAIN));
		}

		@Override
		public Component getTableCellRendererComponent(JTable pTable, Object pValue, boolean pIsSelected, boolean pHasFocus, int pRow, int pColumn)
		{
			if (pValue != null)
				setText(pValue.toString());
			else
				setText(" "); //$NON-NLS-1$
			if (pColumn == 1)
			// EventType
			{
				Color color = COLORS.get(mEvents.get(mEvents.size() - pRow - 1).getEvt());
				if (color == null)
					color = Color.white;
				setBackground(color);
				setHorizontalAlignment(SwingConstants.LEADING);
			}
			else if (pColumn == 2)
			// Player name - if any
			{
				Color color = (pValue == null) || ((String)pValue).isEmpty() ? Color.white : mPlayerColors.get(pValue);
				setBackground(color == null ? Color.white : color);
				setHorizontalAlignment(SwingConstants.CENTER);
			}
			else
			// All other columns
			{
				setBackground(pRow % 2 == 0 ? BG_EVENT_COLOR_EVEN : BG_EVENT_COLOR_ODD);
				setHorizontalAlignment(pValue instanceof String ? SwingConstants.CENTER : SwingConstants.TRAILING);
				if (pColumn > 2)
				{
					if (new Integer(0).equals(pValue))
						setText(""); //$NON-NLS-1$
				}
			}
			return this;
		}
	}

	/**
	 * The table model for the events
	 * @author jytou
	 */
	private class EventTableModelDebtMoney extends AbstractTableModel
	{
		// All column names in Debt-Money
		private final String[] COL_NAMES_DEBT_MONEY = new String[]
		{
			UIMessageKeyProvider.MAINFRAME_TABLE_EVENT_COLNAME_TIME.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_EVENT_COLNAME_TYPE.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_EVENT_COLNAME_PLAYER.getMessage(),
			UIMessageKeyProvider.GENERAL_CREDIT_INTEREST.getMessage(),
			UIMessageKeyProvider.GENERAL_CREDIT_PRINCIPAL.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_EVENT_COLNAME_CARDS1.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_EVENT_COLNAME_CARDS2.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_EVENT_COLNAME_CARDS4.getMessage()
		};
		// All column names in Libre Currency
		private final String[] COL_NAMES_LIBRE_MONEY = new String[]
		{
			UIMessageKeyProvider.MAINFRAME_TABLE_EVENT_COLNAME_TIME.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_EVENT_COLNAME_TYPE.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_EVENT_COLNAME_PLAYER.getMessage(),
			UIMessageKeyProvider.GENERAL_MONEY_WEAK.getMessage(),
			UIMessageKeyProvider.GENERAL_MONEY_MEDIUM.getMessage(),
			UIMessageKeyProvider.GENERAL_MONEY_STRONG.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_EVENT_COLNAME_CARDS1.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_EVENT_COLNAME_CARDS2.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_EVENT_COLNAME_CARDS4.getMessage()
		};
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
				case 0: // time of event
					return new SimpleDateFormat("HH:mm:ss").format(event.getTstamp()); //$NON-NLS-1$
				case 1: // event type
					return event.getEvt().getDescription();
				case 2: // name of the player, if any
					return event.getPlayer() == null ? "" : event.getPlayer().getName(); //$NON-NLS-1$
				default:
					break;
				}
				if (mMoneySystem == Game.MONEY_DEBT)
				// this is only in debt-money
					switch (pColumnIndex)
					{
					case 3: // interest
						return event.getInterest();
					case 4: // principal
						return event.getPrincipal();
					case 5: // weak cards
						return event.getWeakCards();
					case 6: // medium cards
						return event.getMediumCards();
					case 7: // strong cards
						return event.getStrongCards();
		
					default:
						return ""; //$NON-NLS-1$
					}
				else
				// this is only in libre currency
					switch (pColumnIndex)
					{
					case 3: // weak coins
						return event.getWeakCoins();
					case 4: // medium coins
						return event.getMediumCoins();
					case 5: // strong coins
						return event.getStrongCoins();
					case 6: // weak cards
						return event.getWeakCards();
					case 7: // medium cards
						return event.getMediumCards();
					case 8: // strong cards
						return event.getStrongCards();
		
					default:
						return ""; //$NON-NLS-1$
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

		@Override
		public boolean isCellEditable(int pRowIndex, int pColumnIndex)
		{
			return pColumnIndex >= 3;// only the values are editable
		}

		@Override
		public Class<?> getColumnClass(int pColumnIndex)
		{
			return pColumnIndex >= 3 ? Integer.class : String.class;
		}

		@Override
		public void setValueAt(Object pAValue, int pRowIndex, int pColumnIndex)
		{
			synchronized (mEventTable)
			{
				super.setValueAt(pAValue, pRowIndex, pColumnIndex);
				final Event event = mEvents.get(mEvents.size() - pRowIndex - 1);
				final int value = ((Integer)pAValue).intValue();
				mEntityManager.getTransaction().begin();

				if (mMoneySystem == Game.MONEY_DEBT)
					switch (pColumnIndex)
					{
					case 3:
						event.setInterest(value);
						break;
					case 4:
						event.setPrincipal(value);
						break;
					case 5:
						event.setWeakCards(value);
						break;
					case 6:
						event.setMediumCards(value);
						break;
					case 7:
						event.setStrongCards(value);
						break;
					}
				else
					switch (pColumnIndex)
					{
					case 3:
						event.setWeakCoins(value);
						break;
					case 4:
						event.setMediumCoins(value);
						break;
					case 5:
						event.setStrongCoins(value);
						break;
					case 6:
						event.setWeakCards(value);
						break;
					case 7:
						event.setMediumCards(value);
						break;
					case 8:
						event.setStrongCards(value);
						break;
					}

				// make sure everything is in sync
				mGame.recomputeAll(null);
				// commit to db
				mEntityManager.getTransaction().commit();
				refreshUI();
			}
		}
	}

	/**
	 * Renderer to show the status of the players.
	 * @author jytou
	 */
	private class PlayerColorRenderer extends JLabel implements TableCellRenderer
	{
		private DefaultTableCellRenderer mDefault = new DefaultTableCellRenderer();
		private boolean mDeathCandidate = false;

		public PlayerColorRenderer()
		{
			super();
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable pTable, Object pValue, boolean pIsSelected, boolean pHasFocus, int pRow, int pColumn)
		{
			if (pColumn > 0)
			// we will deal with the first column later since it is special
			{
				DefaultTableCellRenderer c =  (DefaultTableCellRenderer)mDefault.getTableCellRendererComponent(pTable, pValue, pIsSelected, pHasFocus, pRow, pColumn);
				if (pColumn == 1)
				// we are on a player: put its color in the background
				{
					Color color = mPlayerColors.get(pValue);
					c.setBackground(color == null ? Color.white : color);
					c.setForeground(pIsSelected ? pTable.getSelectionForeground() : pTable.getForeground());
				}
				else
				// any other column: do nothing but show if it is selected
				{
					c.setBackground(pIsSelected ? pTable.getSelectionBackground() : pTable.getBackground());
					c.setForeground(pIsSelected ? pTable.getSelectionForeground() : pTable.getForeground());
				}
				return c;
			}
			final int value = ((Integer)pValue).intValue();
			Color color = Color.black;
			setToolTipText(""); //$NON-NLS-1$
			setText(""); //$NON-NLS-1$
			String tooltip = ""; //$NON-NLS-1$
			switch (value)
			{
			case PLAYER_INACTIVE:
				tooltip = UIMessages.getString("HelperUI.PlayerTable.Tooltip.InactivePlayer"); //$NON-NLS-1$
				color = Color.gray;
				break;
			case PLAYER_NEEDS_BANK:
				tooltip = UIMessages.getString("HelperUI.PlayerTable.Tooltip.PlayerMustVisitBank"); //$NON-NLS-1$
				color = Color.red;
				break;
			case PLAYER_IN_WARNING:
				tooltip = mPlayersInWarning.get(mPlayers.get(pRow).getId());
				color = Color.orange;
				break;
			case PLAYER_IN_PRISON:
				tooltip = UIMessages.getString("HelperUI.PlayerTable.Tooltip.PlayerInPrison"); //$NON-NLS-1$
				color = Color.lightGray;
				break;
			case PLAYER_OK:
				tooltip = UIMessages.getString("HelperUI.PlayerTable.Tooltip.PlayerOk"); //$NON-NLS-1$
				color = Color.green;
				break;

			default:
				break;
			}
			setBackground(color);
			mDeathCandidate = mSuggestedDeathsLabel.getText().contains(mPlayers.get(pRow).getName());
			if (mDeathCandidate)
			{
				tooltip = "<html>" + tooltip; //$NON-NLS-1$
				if (tooltip.length() > 0)
					tooltip += "<br>"; //$NON-NLS-1$
				tooltip += UIMessages.getString("HelperUI.Player.Table.Tooltip.SuggestedDeath"); //$NON-NLS-1$
				tooltip = tooltip + "</html>"; //$NON-NLS-1$
			}
			setToolTipText(tooltip);
			return this;
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			if (mDeathCandidate)
			{
				g.setColor(Color.darkGray);
				g.drawLine(0, 0, getWidth(), getHeight());
				g.drawLine(0, getWidth(), 0, getHeight());
			}
		}
	}

	private class PlayerTableModel extends AbstractTableModel
	{
		private final String[] COL_NAMES_DEBT_MONEY = new String[]
		{
			UIMessageKeyProvider.MAINFRAME_TABLE_PLAYER_COLNAME_STATUS.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_PLAYER_COLNAME_NAME.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_PLAYER_COLNAME_AGE.getMessage(),
			UIMessageKeyProvider.GENERAL_CREDIT_PRINCIPAL.getMessage(),
			UIMessageKeyProvider.GENERAL_CREDIT_INTEREST.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_PLAYER_COLNAME_HISTORY.getMessage()
		};
		private final String[] COL_NAMES_LIBRE_MONEY = new String[]
		{
			UIMessageKeyProvider.MAINFRAME_TABLE_PLAYER_COLNAME_STATUS.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_PLAYER_COLNAME_NAME.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_PLAYER_COLNAME_AGE.getMessage(),
			UIMessageKeyProvider.MAINFRAME_TABLE_PLAYER_COLNAME_HISTORY.getMessage()
		};
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
					return ""; //$NON-NLS-1$
				switch (pColumnIndex)
				{
				case 0:
					if (mPlayersInWarning.containsKey(player.getId()))
						return PLAYER_IN_WARNING;
					else
						return player.isActive() ? (player.hasVisitedBank() ? (mPlayersInPrison.contains(player.getId()) ? PLAYER_IN_PRISON : PLAYER_OK) : PLAYER_NEEDS_BANK) : PLAYER_INACTIVE;
				case 1:
					return player.getName();
				case 2:
				{
					final Integer age = mPlayerAges.get(player.getId());
					if (age == null)
						return ""; //$NON-NLS-1$
					else
						return String.valueOf(age);
				}
				case 3:
					return mMoneySystem == Game.MONEY_DEBT ? player.getCurDebt() : mCreditHistory.get(player.getId()).toString();
				case 4:
					return mMoneySystem == Game.MONEY_DEBT ? player.getCurInterest() : ""; //$NON-NLS-1$
				case 5:
					return mMoneySystem == Game.MONEY_DEBT ? mCreditHistory.get(player.getId()).toString() : ""; //$NON-NLS-1$

				default:
					break;
				}
				return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int pColumnIndex)
		{
			// okay, that's an ugly one
			// Depending on the money system, the String representing the player's history is on column 4 (debt money) or column 3 (libre)
			// Besides, column 1 is the player name, so it is a String. All other columns are integers.
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
			return false;
		}
	}

	// The current open game
	private Game mGame = null;
	// All players in the game, sorted in the current best way
	private List<Player> mPlayers = new ArrayList<>();
	// A Map of [player ID, player name] for all players that have a warning
	private Map<Integer, String> mPlayersInWarning = new HashMap<>();
	// Map of [player ID, credit history as a StringBuilder]
	private Map<Integer, StringBuilder> mCreditHistory = new HashMap<>();
	// Set of [player ID] : all players currently in prison
	private Set<Integer> mPlayersInPrison = new HashSet<>();
	// Map of [player ID, age of player]
	private Map<Integer, Integer> mPlayerAges = new HashMap<>();
	// All events sorted in the correct way
	private List<Event> mEvents = new ArrayList<>();
	// All players that have not died in the game yet
	private Set<Integer> mNonDeadPlayers = new HashSet<>();

	private EntityManager mEntityManager;
	private EntityManagerFactory mEntityManagerFactory;
	/**
	 * These are VERY SENSITIVE. Every action that has only one letter is actually a keyboard shortcut
	 * for the action. All others (with more than one letter) don't have a shortcut.
	 * There shouldn't be two equal ones since these are direct keyboard shortcuts.
	 * As these are internationalized, every new shortcut should be added to the verification
	 * just after these declarations.
	 */
	public enum ActionCommand
	{
		 ACTION_ADDITIONAL_COMMENTS(UIMessages.getString("HelperUI.Action.Command.ModifyGameComments")), //$NON-NLS-1$
		 ACTION_RECOMPUTE(UIMessages.getString("HelperUI.Action.Command.RecomputeGameEvents")), //$NON-NLS-1$
		 ACTION_INVEST_BANK(UIMessages.getString("HelperUI.Action.Command.BankInvestsMoneyAndCards")), //$NON-NLS-1$
		 ACTION_NEW_CREDIT(UIMessages.getString("HelperUI.Action.Command.PlayerTakesNewCredit")), //$NON-NLS-1$
		 ACTION_CANNOT_PAY(UIMessages.getString("HelperUI.Action.Command.PlayerDefaultsOnDebts")), //$NON-NLS-1$
		 ACTION_ASSESSMENT_BANK(UIMessages.getString("HelperUI.Action.Command.BankFinalAssessment")), //$NON-NLS-1$
		 ACTION_EVENT_DATE(UIMessages.getString("HelperUI.Action.Command.EventDate")), //$NON-NLS-1$
		 ACTION_END_GAME(UIMessages.getString("HelperUI.Action.Command.EndOfGame")), //$NON-NLS-1$
		 ACTION_REIMB_INTEREST(UIMessages.getString("HelperUI.Action.Command.PlayerPaysInterestOnly")), //$NON-NLS-1$
		 ACTION_JOIN_PLAYER(UIMessages.getString("HelperUI.Action.Command.NewPlayer")), //$NON-NLS-1$
		 ACTION_TECH_BREAKTROUGH(UIMessages.getString("HelperUI.Action.Command.TechnologicalBreakthrough")), //$NON-NLS-1$
		 ACTION_DEATH(UIMessages.getString("HelperUI.Action.Command.PlayerDiesAndReborn")), //$NON-NLS-1$
		 ACTION_IMPORT(UIMessages.getString("HelperUI.Action.Command.ImportPlayerNames")), //$NON-NLS-1$
		 ACTION_PRISON(UIMessages.getString("HelperUI.Action.Command.PlayerGoesToPrison")), //$NON-NLS-1$
		 ACTION_QUIT_PLAYER(UIMessages.getString("HelperUI.Action.Command.PlayerQuitsGame")), //$NON-NLS-1$
		 ACTION_QUIT_APP(UIMessages.getString("HelperUI.Action.Command.QuitApplication")), //$NON-NLS-1$
		 ACTION_REIMB_CREDIT(UIMessages.getString("HelperUI.Action.Command.PlayerPaysBackHisCredit")), //$NON-NLS-1$
		 ACTION_RENAME_PLAYER(UIMessages.getString("HelperUI.Action.Command.PlayerRenamed")), //$NON-NLS-1$
		 ACTION_DELETE_PLAYER(UIMessages.getString("HelperUI.Action.Command.PlayerDeletedCompletely")), //$NON-NLS-1$
		 ACTION_NEW_TURN(UIMessages.getString("HelperUI.Action.Command.NewTurn")), //$NON-NLS-1$
		 ACTION_UNEXPECTED_MM_CHANGE(UIMessages.getString("HelperUI.Action.Command.UnexpectedMoneyMassChange")),  //$NON-NLS-1$
		 ACTION_EXPORT(UIMessages.getString("HelperUI.Action.Command.ExportGame")), //$NON-NLS-1$
		 ACTION_BANKRUPTCY(UIMessages.getString("HelperUI.Action.Command.PlayerGoesBankrupt")), //$NON-NLS-1$
		 ACTION_UNDO(UIMessages.getString("HelperUI.Action.Command.Undo")); //$NON-NLS-1$

		private String mMnemo;
		private String mDescription;

		private ActionCommand(String pFullCommand)
		{
			final int colonPos = pFullCommand.indexOf(':');
			mMnemo = pFullCommand.substring(0, colonPos);
			mDescription = pFullCommand.substring(colonPos + 1);
		}

		public String getMnemo()
		{
			return mMnemo;
		}
		public String getDescription()
		{
			return mDescription;
		}
	}

	// Verify that there is no collision between different action keys
	static
	{
		final Set<String> existing = new HashSet<>();
		for (ActionCommand s : ActionCommand.values())
		{
			if (s.getMnemo().length() == 1)
			{
				if (existing.contains(s.getMnemo()))
					throw new RuntimeException("Collision for action shortcut " + s); //$NON-NLS-1$
				existing.add(s.getMnemo());
			}
		}
	}

	private JTable mPlayerTable;
	private JTable mEventTable;
	private TableModel mPlayerTableModel;
	private TableModel mEventTableModel;
	// All actions go through this action, and are identified by their action command
	private MyAction mMyAction = new MyAction();
	// All buttons that have associated actions and shortcuts
	private List<JButton> mPlayerActionButtons = new ArrayList<>();
	// The forecasted number of deaths at each turn
	private List<Integer> mDeathSchedule = new ArrayList<>();

	private class MyAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent pEvent)
		{
			// First non existing player events
			final String command = pEvent.getActionCommand();
			if (ActionCommand.ACTION_JOIN_PLAYER.getMnemo().equals(command))
			{
				final AddPlayerDialog dialog = new AddPlayerDialog(HelperUI.this, mGame, mEntityManager);
				dialog.setVisible(true);
				final Player newPlayer = dialog.getNewPlayer();
				if (newPlayer != null)
				{
					mPlayers.add(newPlayer);
					createNewEvent(newPlayer, EventType.JOIN);
					mNonDeadPlayers.add(newPlayer.getId());
				}
				generatePlayerColorPalette();
			}
			else if (ActionCommand.ACTION_NEW_TURN.getMnemo().equals(command))
			{
				createNewEvent(null, EventType.TURN);
				if (mValuesHelper != null)
					mValuesHelper.rotateValues();
				suggestDeaths();
			}
			else if (ActionCommand.ACTION_END_GAME.getMnemo().equals(command))
			{
				if (JOptionPane.showConfirmDialog(HelperUI.this, UIMessages.getString("HelperUI.Dialog.Message.ConfirmQuitGame"), UIMessages.getString("HelperUI.Dialog.Title.ConfirmQuitGame"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) //$NON-NLS-1$ //$NON-NLS-2$
					createNewEvent(null, EventType.END);
			}
			else if (ActionCommand.ACTION_RECOMPUTE.getMnemo().equals(command))
			{
				mEntityManager.getTransaction().begin();
				mGame.recomputeAll(null);
				mEntityManager.getTransaction().commit();
				generatePlayerColorPalette();
				refreshUI();
			}
			else if (ActionCommand.ACTION_EVENT_DATE.getMnemo().equals(command))
			{
				if (mEventTable.getSelectedRowCount() == 1)
				{
					new ChangeEventDateDialog(HelperUI.this, mEntityManager, mEvents.get(mEvents.size() - mEventTable.getSelectedRow() - 1)).setVisible(true);;
					refreshUI();
				}
			}
			else if (ActionCommand.ACTION_UNDO.getMnemo().equals(command))
			{
				if (!mEvents.isEmpty())
				{
					if (JOptionPane.showConfirmDialog(HelperUI.this, UIMessages.getString("HelperUI.Dialog.Message.ReallyUndoLastEvent"), UIMessageKeyProvider.DIALOG_TITLE_CANCEL.getMessage(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) //$NON-NLS-1$
					{
						mEntityManager.getTransaction().begin();
						final Event toUndo = mEvents.get(mEvents.size() - 1);
						mEvents.remove(toUndo);
						mGame.removeEvent(toUndo, true);
						mEntityManager.getTransaction().commit();
						if ((mValuesHelper != null) && EventType.TURN.equals(toUndo.getEvt()))
						{
							// Rotating 3 times is equivalent to rotating backwards
							mValuesHelper.rotateValues();
							mValuesHelper.rotateValues();
							mValuesHelper.rotateValues();
						}
						generatePlayerColorPalette();
						refreshUI();
					}
				}
			}
			else if (ActionCommand.ACTION_UNEXPECTED_MM_CHANGE.getMnemo().equals(command))
			{
				final CreditActionDialog dialog = new CreditActionDialog(HelperUI.this, UIMessages.getString("HelperUI.Dialog.Title.UnexpectedMoneyMassChange"), 0, Purpose.MONEY_MASS_CHANGE); //$NON-NLS-1$
				dialog.setVisible(true);
				final int principal = dialog.getPrincipal();
				if (dialog.wasApplied())
					createNewEventDebtMoney(null, EventType.MM_CHANGE, principal, 0, 0, 0, 0);
			}
			else if (ActionCommand.ACTION_INVEST_BANK.getMnemo().equals(command))
			{
				final CreditActionDialog dialog = new CreditActionDialog(HelperUI.this, UIMessages.getString("HelperUI.Dialog.Title.BankInvestment"), 0, Purpose.BANK_INVESTMENT); //$NON-NLS-1$
				dialog.setVisible(true);
				if (dialog.wasApplied())
				// Note that exceptionally, the "principal" in here is really an interest earned by the bank that is reinvested - it has to be counted as "interest" as it is not debt money
					createNewEventDebtMoney(null, EventType.SIDE_INVESTMENT, 0, dialog.getPrincipal(), dialog.getWeakCards(), dialog.getMediumCards(), dialog.getStrongCards());
			}
			else if (ActionCommand.ACTION_ASSESSMENT_BANK.getMnemo().equals(command))
			{
				final CreditActionDialog dialog = new CreditActionDialog(HelperUI.this, UIMessages.getString("HelperUI.Dialog.Title.FinalBankAssessment"), 0, Purpose.BANK_INVESTMENT); //$NON-NLS-1$
				dialog.setVisible(true);
				if (dialog.wasApplied())
					// Note that exceptionally, the "principal" in here is really an interest earned by the bank that is reinvested - it has to be counted as "interest" as it is not debt money
					createNewEventDebtMoney(null, EventType.ASSESSMENT_FINAL, 0, dialog.getPrincipal(), dialog.getWeakCards(), dialog.getMediumCards(), dialog.getStrongCards());
			}
			else if (ActionCommand.ACTION_ADDITIONAL_COMMENTS.getMnemo().equals(command))
				new ChangeDescriptionDialog(HelperUI.this, mGame, mEntityManager).setVisible(true);
			else if (ActionCommand.ACTION_QUIT_APP.getMnemo().equals(command))
				dispatchEvent(new WindowEvent(HelperUI.this, WindowEvent.WINDOW_CLOSING));
			else if (ActionCommand.ACTION_EXPORT.getMnemo().equals(command))
			{
				final JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("xml", "xml")); //$NON-NLS-1$ //$NON-NLS-2$
				if (fc.showSaveDialog(HelperUI.this) == JFileChooser.APPROVE_OPTION)
				{
					File toExport = fc.getSelectedFile();
					if (!toExport.getName().endsWith(".xml")) //$NON-NLS-1$
						toExport = new File(toExport.getAbsolutePath() + ".xml"); //$NON-NLS-1$
					try
					{
						JAXBContext jc = JAXBContext.newInstance(Game.class);
						Marshaller marshaller = jc.createMarshaller();
						marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
						marshaller.marshal(mGame, toExport);
					}
					catch (PropertyException e)
					{
						UIUtil.showExceptionMessage(rootPane, e);
						e.printStackTrace();
					}
					catch (JAXBException e)
					{
						UIUtil.showExceptionMessage(rootPane, e);
						e.printStackTrace();
					}
				}
			}
			else if (ActionCommand.ACTION_IMPORT.getMnemo().equals(command))
			{
				try
				{
					final ImportDialog importDialog = new ImportDialog(HelperUI.this, mGame, mEntityManager);
					importDialog.setVisible(true);
					if (importDialog.wasApplied())
					{
						mPlayers.addAll(importDialog.getNewPlayers());
						for (Player newPlayer : importDialog.getNewPlayers())
							mNonDeadPlayers.add(newPlayer.getId());
						mEvents.addAll(importDialog.getNewEvents());
						generatePlayerColorPalette();
						refreshUI();
					}
				}
				catch (IOException e)
				{
					// This shouldn't happen
					UIUtil.showExceptionMessage(rootPane, e);
				}
			}
			else
			{
				// These are all player-related, let's double-check that a player is selected before applying any of those events
				final int selectedPlayer = mPlayerTable.getSelectedRow();
				if (selectedPlayer < 0)
					JOptionPane.showMessageDialog(rootPane, UIMessages.getString("HelperUI.Dialog.Message.SelectAPlayerForThisAction"), UIMessages.getString("HelperUI.Dialog.Title.SelectAPlayerForThisAction"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
				else
				{
					final Player player = mPlayers.get(selectedPlayer);
					if (ActionCommand.ACTION_NEW_CREDIT.getMnemo().equals(command))
					{
						final CreditActionDialog dialog = new CreditActionDialog(HelperUI.this, MessageFormat.format(UIMessages.getString("HelperUI.Dialog.Title.NewCreditForPlayer"), player.getName()), 3, Purpose.NEW_OR_REIMB_CREDIT); //$NON-NLS-1$
						dialog.setVisible(true);
						final int principal = dialog.getPrincipal();
						if (principal > 0)
							createNewEventDebtMoney(player, EventType.NEW_CREDIT, principal, principal / 3, 0, 0, 0);
					}
					else if (ActionCommand.ACTION_REIMB_INTEREST.getMnemo().equals(command))
						createNewEventDebtMoney(player, EventType.INTEREST_ONLY, 0, player.getCurInterest(), 0, 0, 0);
					else if (ActionCommand.ACTION_REIMB_CREDIT.getMnemo().equals(command))
					{
						final CreditActionDialog dialog = new CreditActionDialog(HelperUI.this, MessageFormat.format(UIMessages.getString("HelperUI.Dialog.Title.PlayerPaysBackCredit"), player.getName()), 3, Purpose.NEW_OR_REIMB_CREDIT); //$NON-NLS-1$
						dialog.setVisible(true);
						final int principal = dialog.getPrincipal();
						if (principal > 0)
							createNewEventDebtMoney(player, EventType.REIMB_CREDIT, principal, player.getCurInterest(), 0, 0, 0);
					}
					else if (ActionCommand.ACTION_RENAME_PLAYER.getMnemo().equals(command))
					{
						new AddPlayerDialog(HelperUI.this, mGame, mEntityManager, player).setVisible(true);
						generatePlayerColorPalette();
						refreshUI();
					}
					else if (ActionCommand.ACTION_TECH_BREAKTROUGH.getMnemo().equals(command))
						createNewEvent(player, EventType.XTECHNOLOGICAL_BREAKTHROUGH);
					else if (ActionCommand.ACTION_DELETE_PLAYER.getMnemo().equals(command))
					{
						if (JOptionPane.showConfirmDialog(HelperUI.this, UIMessages.getString("HelperUI.Dialog.Warning.DeletePlayerIrreversible1"), UIMessages.getString("HelperUI.Dialog.Title.DeletePlayerIrreversible"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) //$NON-NLS-1$ //$NON-NLS-2$
						{
							if (JOptionPane.showConfirmDialog(HelperUI.this, UIMessages.getString("HelperUI.Dialog.Warning.DeletePlayerIrreversible2"), UIMessages.getString("HelperUI.Dialog.Title.DeletePlayerIrreversible"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) //$NON-NLS-1$ //$NON-NLS-2$
							{
								synchronized (mPlayerTable)
								{
									mEntityManager.getTransaction().begin();
									for (Event event : new ArrayList<>(mGame.getEvents()))
									{
										if (event.getPlayer() == player)
										{
											mGame.removeEvent(event, true);
											mEvents.remove(event);
										}
									}
									mPlayers.remove(player);
									mNonDeadPlayers.remove(player.getId());
									mGame.removePlayer(player);
									mGame.recomputeAll(null);
									mEntityManager.getTransaction().commit();
									generatePlayerColorPalette();
									refreshUI();
								}
							}
						}
					}
					else
					{
						final boolean cannotPay = ActionCommand.ACTION_CANNOT_PAY.getMnemo().equals(command);
						if (cannotPay || ActionCommand.ACTION_DEATH.getMnemo().equals(command) || ActionCommand.ACTION_QUIT_PLAYER.getMnemo().equals(command))
						// These events need a collection of the player's assets through a dialog, therefore they are all treated the same way here
						{
							// First compute the title of the dialog - it will be different for each of those cases
							String title;
							if (cannotPay)
								title = MessageFormat.format(UIMessages.getString("HelperUI.Dialog.Title.PlayerDefaultOnDebts"), player.getName()); //$NON-NLS-1$
							else if (ActionCommand.ACTION_DEATH.getMnemo().equals(command))
							{
								title = MessageFormat.format(UIMessages.getString("HelperUI.Dialog.Title.DeathOfPlayer"), player.getName()); //$NON-NLS-1$
								if (!mSuggestedDeathsLabel.getText().contains(player.getName()))
								// The player is not suggested by the engine as a candidate for death, give a warning
									if (JOptionPane.showConfirmDialog(HelperUI.this, UIMessages.getString("HelperUI.Dialog.Warning.PlayerNotInDeathSuggested"), UIMessages.getString("HelperUI.Dialog.Title.PlayerNotInDeathSuggested"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) //$NON-NLS-1$ //$NON-NLS-2$
										return;
								// Check if this player was already reborn
								for (Event event : mEvents)
									if (EventType.DEATH.equals(event.getEvt()) && player.getId().equals(event.getPlayer().getId()))
										if (JOptionPane.showConfirmDialog(HelperUI.this, UIMessages.getString("HelperUI.Dialog.Warning.PlayerAlreadyDied"), UIMessages.getString("HelperUI.Dialog.Title.PlayerAlreadyDied"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) //$NON-NLS-1$ //$NON-NLS-2$
											return;
							}
							else
								title = MessageFormat.format(UIMessages.getString("HelperUI.Dialog.Title.PlayerQuitsGame"), player.getName()); //$NON-NLS-1$

							if ((player.getCurDebt() > 0) && (ActionCommand.ACTION_DEATH.getMnemo().equals(command) || ActionCommand.ACTION_QUIT_PLAYER.getMnemo().equals(command)))
							// If this player still has debts but was shown as "dying" or "quitting", we HAVE to do the bank stuff first - no way we can continue here
								if (JOptionPane.showConfirmDialog(HelperUI.this, UIMessages.getString("HelperUI.Dialog.Warning.PlayerStillHasDebts"), UIMessages.getString("HelperUI.Dialog.Title.PlayerStillHasDebts"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) //$NON-NLS-1$ //$NON-NLS-2$
									return;

							// Now let's see what the purpose of the dialog will be
							Purpose purpose;
							if (cannotPay)
								purpose = Purpose.DEFAULT;
							else if (mGame.getMoneySystem() == Game.MONEY_DEBT)
								purpose = Purpose.PLAYER_ASSESSMENT_DEBT_MONEY;
							else
								purpose = Purpose.PLAYER_ASSESSMENT_LIBRE_MONEY;

							// Okay, we can create the dialog now that we have everything. Note that the default principal here depends only
							// on the amount of money that is held by the player, we cannot assume anything here
							final CreditActionDialog dialog = new CreditActionDialog(HelperUI.this, title, 0, purpose);
							dialog.setVisible(true);
							if (dialog.wasApplied())
							// The used has applied the dialog - do extra stuff here
							{
								// Get the type of event that was created
								EventType eventType;
								if (cannotPay)
									eventType = dialog.getEventType();
								else if (ActionCommand.ACTION_DEATH.getMnemo().equals(command))
								// For death, it is a little special as we have to maintain the death-related schedule and information
								{
									eventType = EventType.DEATH;
									mNonDeadPlayers.remove(player.getId());
									// Remove the player from suggested deaths
									if (mSuggestedDeathsLabel.getText().contains(player.getName() + ", ")) //$NON-NLS-1$
										mSuggestedDeathsLabel.setText(mSuggestedDeathsLabel.getText().replaceAll(player.getName() + ", ", ""));  //$NON-NLS-1$//$NON-NLS-2$
									else if (mSuggestedDeathsLabel.getText().contains(player.getName()))
										mSuggestedDeathsLabel.setText(mSuggestedDeathsLabel.getText().replaceAll(player.getName(), "")); //$NON-NLS-1$
								}
								else// if (ACTION_QUIT_PLAYER.equals(pEvent.getActionCommand()))
									eventType = EventType.QUIT;

								// Create the event from the event type and the information gathered from the dialog
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

		/**
		 * Creates an event of this type with 0's for all values
		 * @param pPlayer
		 * @param pEventType
		 */
		private void createNewEvent(final Player pPlayer, final EventType pEventType)
		{
			createNewEventDebtMoney(pPlayer, pEventType, 0, 0, 0, 0, 0);
		}

		/**
		 * Creates an event in the debt-money system.
		 * @param pPlayer
		 * @param pEventType
		 * @param principal
		 * @param interest
		 * @param weakCards
		 * @param mediumCards
		 * @param strongCards
		 */
		private void createNewEventDebtMoney(final Player pPlayer, final EventType pEventType, final int principal, final int interest, final int weakCards, final int mediumCards, final int strongCards)
		{
			mEntityManager.getTransaction().begin();
			Event event = new Event(mGame, pEventType, pPlayer);
			if (principal != 0) event.setPrincipal(principal);// note that the principal may be negative when the bank invests some money in trading
			if (interest > 0) event.setInterest(interest);
			if (weakCards > 0) event.setWeakCards(weakCards);
			if (mediumCards > 0) event.setMediumCards(mediumCards);
			if (strongCards > 0) event.setStrongCards(strongCards);
			event.applyEvent();
			mEntityManager.getTransaction().commit();
			mEvents.add(event);
			refreshUI();
		}

		/**
		 * Creates an event in the libre currency system.
		 * @param pPlayer
		 * @param pEventType
		 * @param weakCoins
		 * @param mediumCoins
		 * @param strongCoins
		 * @param weakCards
		 * @param mediumCards
		 * @param strongCards
		 */
		private void createNewEventLibreMoney(final Player pPlayer, final EventType pEventType, final int weakCoins, final int mediumCoins, final int strongCoins, final int weakCards, final int mediumCards, final int strongCards)
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

	/**
	 * Enables the buttons when a player is selected, and disables them otherwise.
	 */
	private void enableButtons()
	{
		final boolean enabled = mPlayerTable.getSelectedRow() != -1;
		for (JButton jButton : mPlayerActionButtons)
			jButton.setEnabled(enabled);
		mMenuPlayer.setEnabled(enabled);
	}

	/**
	 * Creates one action with a button in the toolbar and even potentially a menu.
	 * @param pToolBar the main toolbar
	 * @param pButtonImage the image for the button
	 * @param pMenu the main menu this action is part of
	 * @param pMenuLabel the label of the menu for this action
	 * @param pActionCommand the action command
	 * @param pPlayerAction <code>true</code> if it is an action related to a player
	 */
	private void createAction(JToolBar pToolBar, String pButtonImage, JMenu pMenu, String pMenuLabel, ActionCommand pActionCommand, boolean pPlayerAction)
	{
		if (pToolBar != null)
		{
			// Builds the button
			final JButton button = new JButton();
			button.setBorder(BorderFactory.createRaisedBevelBorder());
			try
			{
				final Image img = ImageIO.read(getClass().getResource("/buttons/" + pButtonImage + ".png")); //$NON-NLS-1$ //$NON-NLS-2$
				final Graphics g = img.getGraphics();
				g.setFont(g.getFont().deriveFont(12f).deriveFont(Font.BOLD));
				final Rectangle2D bounds = g.getFontMetrics().getStringBounds(pActionCommand.getMnemo().substring(0, 1), g);
				g.setColor(Color.black);
				g.drawString(pActionCommand.getMnemo().substring(0, 1), 1, (int)(bounds.getHeight() - 2));
				g.dispose();
				button.setIcon(new ImageIcon(img));
			}
			catch (Exception e)
			{
				button.setText(pMenuLabel + " (" + pActionCommand.getMnemo().charAt(0) + ")");  //$NON-NLS-1$//$NON-NLS-2$
			}
			button.setActionCommand(pActionCommand.getMnemo());
			button.addActionListener(mMyAction);
			button.setToolTipText(pMenuLabel + " (" + pActionCommand.getMnemo().charAt(0) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			pToolBar.add(button);
			// Add a separation between the buttons
			final JPanel separator = new JPanel();
			separator.setMinimumSize(new Dimension(3, 10));
			pToolBar.add(separator);
			if (pActionCommand.getMnemo().length() == 1)
			{
				// Do NOT create keyboard shortcuts for names that are not a single char
				mPlayerTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar(pActionCommand.getMnemo().charAt(0)), 0), pActionCommand.getMnemo());
				mPlayerTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.getExtendedKeyCodeForChar(pActionCommand.getMnemo().toUpperCase().charAt(0)), 0), pActionCommand.getMnemo());
				mPlayerTable.getActionMap().put(pActionCommand.getMnemo(), mMyAction);
			}
			if (pPlayerAction)
				mPlayerActionButtons.add(button);
		}

		if (pMenu != null)
		// Add the action in the corresponding menu
		{
			final JMenuItem menuItem = new JMenuItem(pMenuLabel);
			menuItem.setMnemonic(pActionCommand.getMnemo().charAt(0));
			menuItem.addActionListener(mMyAction);
			menuItem.setActionCommand(pActionCommand.getMnemo());
			pMenu.add(menuItem);
		}
	}

	// All labels in the status bar - these get populated with real-time values
	private JLabel mMoneySystemLabel;
	private JLabel mTurnNumberLabel;
	private JLabel mMoneyMassLabel;
	private JLabel mMoneyPerPlayerLabel;
	private JLabel mNbPlayersLabel;
	private JLabel mGainedInterestLabel;
	private JLabel mSeizedValuesLabel;
	private JLabel mSuggestedDeathsLabel;

	// Main Player menu
	private JMenu mMenuPlayer;

	private JCheckBoxMenuItem mMenuViewMoneyHelper;
	private ValuesHelper mValuesHelper = null;
	private JCheckBoxMenuItem mMenuViewKBShortcutsHelper;
	private KeyboardShortcutsHelper mKBShortcutsHelper = null;
	private boolean mSwitchingToStats = false;

	public HelperUI(final EntityManager pEntityManager, final EntityManagerFactory pEntityManagerFactory, final Game pGame) throws IOException
	{
		super(UIMessages.getString("HelperUI.Main.Title")); //$NON-NLS-1$
		mEntityManager = pEntityManager;
		mEntityManagerFactory = pEntityManagerFactory;
		setSize(1024, 800);
		setIconImage(ImageIO.read(HelperUI.class.getResourceAsStream("/geconomicus.png"))); //$NON-NLS-1$
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent pEvent)
			{
				super.windowClosing(pEvent);
				if (!mSwitchingToStats)
				{
					mEntityManager.close();
					System.exit(0);
				}
			}
		});

		// Create the main panel, which is made of a split pane
		final JPanel mainPanel = new JPanel(new GridBagLayout());
		final int moneySystem = pGame.getMoneySystem();
		final JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildPlayerListPane(moneySystem), buildEventListPane(moneySystem));
		mainPanel.add(mainSplitPane, new GridBagConstraints(0, 2, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		// This default seems to work well
		mainSplitPane.setResizeWeight(0.3);

		// Create the menus and toolbars
		createMenuAndToolbar(mainPanel, moneySystem);

		// Finally build the status panel
		mainPanel.add(buildStatusPanel(pGame), new GridBagConstraints(0, 3, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		getContentPane().add(mainPanel);

		// Now set the current game
		setGame(pGame);

		setVisible(true);
		mPlayerTable.requestFocusInWindow();
	}

	private void createMenuAndToolbar(final JPanel pMainPanel, final int pMoneySystem)
	{
		// Create the menus
		final JMenuBar menuBar = new JMenuBar();
		final JMenu menuGame = new JMenu(UIMessages.getString("HelperUI.Main.Menu.Game")); //$NON-NLS-1$
		menuGame.setMnemonic(HelperUI.MNEM_MENU_GAME.charAt(0));
		final JToolBar toolbar = new JToolBar();
		createAction(toolbar, "add_player", menuGame, UIMessages.getString("HelperUI.Main.SubMenu.NewPlayer"), ActionCommand.ACTION_JOIN_PLAYER, false); //$NON-NLS-1$ //$NON-NLS-2$
		createAction(toolbar, "new_turn", menuGame, UIMessages.getString("HelperUI.Main.SubMenu.NewTurn"), ActionCommand.ACTION_NEW_TURN, false); //$NON-NLS-1$ //$NON-NLS-2$
		mMenuPlayer = new JMenu(UIMessages.getString("HelperUI.Main.Menu.Player")); //$NON-NLS-1$
		mMenuPlayer.setMnemonic(HelperUI.MNEM_MENU_PLAYER.charAt(0));
		if (pMoneySystem == Game.MONEY_DEBT)
		{
			createAction(toolbar, "new_credit", mMenuPlayer, UIMessages.getString("HelperUI.Main.SubMenu.NewCredit"), ActionCommand.ACTION_NEW_CREDIT, true); //$NON-NLS-1$ //$NON-NLS-2$
			createAction(toolbar, "pay_interest", mMenuPlayer, UIMessages.getString("HelperUI.Main.SubMenu.PayBackInterestOnly"), ActionCommand.ACTION_REIMB_INTEREST, true); //$NON-NLS-1$ //$NON-NLS-2$
			createAction(toolbar, "pay_credit", mMenuPlayer, UIMessages.getString("HelperUI.Main.SubMenu.PayBackCredit"), ActionCommand.ACTION_REIMB_CREDIT, true); //$NON-NLS-1$ //$NON-NLS-2$
			createAction(toolbar, "cannot_pay", mMenuPlayer, UIMessages.getString("HelperUI.Main.SubMenu.DefaultOnDebts"), ActionCommand.ACTION_CANNOT_PAY, true); //$NON-NLS-1$ //$NON-NLS-2$
			createAction(toolbar, "moneymass_change", menuGame, UIMessages.getString("HelperUI.Main.SubMenu.UnexpectedMoneyMassChange"), ActionCommand.ACTION_UNEXPECTED_MM_CHANGE, false); //$NON-NLS-1$ //$NON-NLS-2$
			createAction(toolbar, "bank_invest", menuGame, UIMessages.getString("HelperUI.Main.SubMenu.BankInvestment"), ActionCommand.ACTION_INVEST_BANK, false); //$NON-NLS-1$ //$NON-NLS-2$
			createAction(toolbar, "bank_assess", menuGame, UIMessages.getString("HelperUI.Main.SubMenu.BankAssessment"), ActionCommand.ACTION_ASSESSMENT_BANK, false); //$NON-NLS-1$ //$NON-NLS-2$
		}
		createAction(null, "", mMenuPlayer, UIMessages.getString("HelperUI.Main.SubMenu.ChangePlayerName"), ActionCommand.ACTION_RENAME_PLAYER, true);//$NON-NLS-1$ //$NON-NLS-2$
		createAction(toolbar, "death", mMenuPlayer, UIMessages.getString("HelperUI.Main.SubMenu.DeathRebirth"), ActionCommand.ACTION_DEATH, true); //$NON-NLS-1$ //$NON-NLS-2$
		createAction(toolbar, "leaves", mMenuPlayer, UIMessages.getString("HelperUI.Main.SubMenu.PlayerLeavesGame"), ActionCommand.ACTION_QUIT_PLAYER, true); //$NON-NLS-1$ //$NON-NLS-2$
		createAction(null, "", menuGame, UIMessages.getString("HelperUI.Main.SubMenu.EndOfGame"), ActionCommand.ACTION_END_GAME, false);//$NON-NLS-1$ //$NON-NLS-2$
		createAction(null, "", menuGame, UIMessages.getString("HelperUI.Main.SubMenu.RecomputeEvents"), ActionCommand.ACTION_RECOMPUTE, false);//$NON-NLS-1$ //$NON-NLS-2$
		createAction(null, "", mMenuPlayer, UIMessages.getString("HelperUI.Main.SubMenu.TechnologicalBreakthrough"), ActionCommand.ACTION_TECH_BREAKTROUGH, true);//$NON-NLS-1$ //$NON-NLS-2$
		createAction(toolbar, "undo", menuGame, UIMessages.getString("HelperUI.Main.SubMenu.UndoLastEvent"), ActionCommand.ACTION_UNDO, false); //$NON-NLS-1$ //$NON-NLS-2$
		createAction(null, "", menuGame, UIMessages.getString("HelperUI.Main.SubMenu.ExportGame"), ActionCommand.ACTION_EXPORT, false);//$NON-NLS-1$ //$NON-NLS-2$
		createAction(null, "", menuGame, UIMessages.getString("HelperUI.Main.SubMenu.ImportGame"), ActionCommand.ACTION_IMPORT, false);//$NON-NLS-1$ //$NON-NLS-2$
		createAction(null, "", menuGame, UIMessages.getString("HelperUI.Main.SubMenu.ChangeGameDescription"), ActionCommand.ACTION_ADDITIONAL_COMMENTS, false);//$NON-NLS-1$ //$NON-NLS-2$
		createAction(null, "", menuGame, UIMessages.getString("HelperUI.Main.SubMenu.QuitApplication"), ActionCommand.ACTION_QUIT_APP, false);//$NON-NLS-1$ //$NON-NLS-2$
		createAction(null, "", mMenuPlayer, UIMessages.getString("HelperUI.Main.SubMenu.TotallyDeletePlayer"), ActionCommand.ACTION_DELETE_PLAYER, false);//$NON-NLS-1$ //$NON-NLS-2$
		menuBar.add(menuGame);
		menuBar.add(mMenuPlayer);
		final JMenu menuView = new JMenu(UIMessages.getString("HelperUI.Main.Menu.View")); //$NON-NLS-1$
		menuView.setMnemonic(HelperUI.MNEM_MENU_VIEW.charAt(0));
		mMenuViewMoneyHelper = new JCheckBoxMenuItem(UIMessages.getString("HelperUI.Main.SubMenu.MoneyValueFrame"), false); //$NON-NLS-1$
		mMenuViewMoneyHelper.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				if (mMenuViewMoneyHelper.isSelected())
				{
					if (mValuesHelper == null)
					{
						try
						{
							mValuesHelper = new ValuesHelper(HelperUI.this, mGame.getMoneySystem());
						}
						catch (IOException e)
						{
							UIUtil.showExceptionMessage(HelperUI.this, e);
						}
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
		mMenuViewKBShortcutsHelper = new JCheckBoxMenuItem(UIMessages.getString("HelperUI.Main.SubMenu.ShowKeyboardShortcutsHelper"), false); //$NON-NLS-1$
		mMenuViewKBShortcutsHelper.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				if (mMenuViewKBShortcutsHelper.isSelected())
				{
					if (mKBShortcutsHelper == null)
					{
						try
						{
							mKBShortcutsHelper = new KeyboardShortcutsHelper(HelperUI.this, mGame.getMoneySystem());
						}
						catch (IOException e)
						{
							UIUtil.showExceptionMessage(HelperUI.this, e);
						}
						mKBShortcutsHelper.setVisible(true);
					}
				}
				else if (mKBShortcutsHelper != null)
					mKBShortcutsHelper.dispatchEvent(new WindowEvent(mKBShortcutsHelper, WindowEvent.WINDOW_CLOSING));
			}
		});
		menuView.add(mMenuViewKBShortcutsHelper);
		final JMenuItem menuViewStats = new JMenuItem(UIMessages.getString("HelperUI.Main.SubMenu.SwitchToStats")); //$NON-NLS-1$
		menuViewStats.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				try
				{
					new ChooseGamesDialog(mEntityManager, mEntityManagerFactory).setVisible(true);
					mSwitchingToStats = true;
					dispatchEvent(new WindowEvent(HelperUI.this, WindowEvent.WINDOW_CLOSING));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		});
		menuView.add(menuViewStats);
		menuBar.add(menuView);
		final JMenu menuHelp = new JMenu(UIMessages.getString("HelperUI.Main.Menu.Help")); //$NON-NLS-1$
		final JMenuItem menuItemWebsite = new JMenuItem(UIMessages.getString("HelperUI.Main.SubMenu.LinkToWebSite")); //$NON-NLS-1$
		menuItemWebsite.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				try
				{
					Desktop.getDesktop().browse(new URL(UIMessages.getString("HelperUI.ReadmeOnline")).toURI()); //$NON-NLS-1$
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
		final JMenuItem menuItemAbout = new JMenuItem(UIMessages.getString("HelperUI.Main.SubMenu.About")); //$NON-NLS-1$
		menuItemAbout.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				JOptionPane.showMessageDialog(HelperUI.this, MessageFormat.format(UIMessages.getString("HelperUI.Dialog.Message.About"), new Object[] {VERSION_NUMBER, RELEASE_DATE}), UIMessages.getString("HelperUI.Dialog.Title.About"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
		menuHelp.add(menuItemAbout);
		menuBar.add(menuHelp);
		pMainPanel.add(toolbar, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		setJMenuBar(menuBar);
	}

	/**
	 * Generates a set of colors for the current players. These are taken with different hues so that they would be quite different from each other
	 */
	private void generatePlayerColorPalette()
	{
		final int n = mPlayers.size();
		if (mPlayerColors.size() != n)
		{
			mPlayerColors.clear();
			final SortedSet<String> names = new TreeSet<>();
			for (Player player : mPlayers)
				names.add(player.getName());
			int i = 0;
			for (String name : names)
				mPlayerColors.put(name, new Color(Color.HSBtoRGB(1.0f * i++ / n, 0.1f, 1f)));
		}
	}

	public JPanel buildPlayerListPane(int pMoneySystem)
	{
		final JPanel playerListPane = new JPanel(new GridBagLayout());
		mPlayerTableModel = new PlayerTableModel(pMoneySystem);
		mPlayerTable = new JTable(mPlayerTableModel);
		mPlayerTable.setDefaultRenderer(Integer.class, new PlayerColorRenderer());
		mPlayerTable.setDefaultRenderer(String.class, new PlayerColorRenderer());
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
		mPlayerTable.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent pEvent)
			{
				super.keyTyped(pEvent);
				if ((pEvent.getKeyCode() == KeyEvent.VK_F2) && (mPlayerTable.getSelectedRow() >= 0))
				{
					mMyAction.actionPerformed(new ActionEvent(mPlayerTable, 0, ActionCommand.ACTION_RENAME_PLAYER.getMnemo()));
					pEvent.consume();
				}
			}
		});
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
		eventListPane.add(new JLabel(UIMessages.getString("HelperUI.Label.SuggestedDeaths")), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0)); //$NON-NLS-1$
		mSuggestedDeathsLabel = new JLabel();
		eventListPane.add(mSuggestedDeathsLabel, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		mEventTableModel = new EventTableModelDebtMoney(pMoneySystem);
		mEventTable = new JTable(mEventTableModel);
		mEventTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		eventListPane.add(new JScrollPane(mEventTable), new GridBagConstraints(0, 10, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		final TableColumnModel columnModel = mEventTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(70);// date/time
		columnModel.getColumn(1).setPreferredWidth(200);// full text event type
		columnModel.getColumn(2).setPreferredWidth(150);// player name
		// The additional columns are values for coins and cards
		for (int i = 3; i < (pMoneySystem == Game.MONEY_DEBT ? 8 : 9); i++)
			columnModel.getColumn(i).setPreferredWidth(30);
		mEventTable.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent pEvent)
			{
				super.keyReleased(pEvent);
				if ((pEvent.getKeyCode() == KeyEvent.VK_DELETE) && (mEventTable.getSelectedRowCount() > 0))
				{
					if (JOptionPane.showConfirmDialog(HelperUI.this, (mEventTable.getSelectedRowCount() > 1 ? UIMessages.getString("HelperUI.Dialog.Message.ConfirmDeleteEvents") : UIMessages.getString("HelperUI.Dialog.Message.ConfirmDeleteOneEvent")) + "\n" + UIMessages.getString("HelperUI.Dialog.Warning.IrreversibleAction"), UIMessages.getString("HelperUI.Dialog.Title.ConfirmDeleteEvents"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
					{
						final int nbEvents = mEvents.size();
						final int[] selectedRows = mEventTable.getSelectedRows();
						mEntityManager.getTransaction().begin();
						for (int i = 0; i < selectedRows.length; i++)
							mGame.removeEvent(mEvents.remove(nbEvents - selectedRows[i] - 1), false);
						mGame.recomputeAll(null);
						mEntityManager.getTransaction().commit();
						refreshUI();
					}
				}
				else if ((pEvent.getKeyCode() == KeyEvent.VK_F2) && (mEventTable.getSelectedRowCount() == 1))
					mMyAction.actionPerformed(new ActionEvent(mEventTable, 0, ActionCommand.ACTION_EVENT_DATE.getMnemo()));
			}
		});

		TableCellRenderer eventRenderer = new EventColorRenderer();
		mEventTable.setDefaultRenderer(String.class, eventRenderer);
		mEventTable.setDefaultRenderer(Integer.class, eventRenderer);
		return eventListPane;
	}

	private JPanel buildStatusPanel(final Game pGame)
	{
		final JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.add(new JLabel(UIMessages.getString("HelperUI.Status.Label.MoneyType") + " ")); //$NON-NLS-1$//$NON-NLS-2$
		statusPanel.add(mMoneySystemLabel = new JLabel());
		statusPanel.add(createSeparationPanel());
		statusPanel.add(new JLabel(UIMessages.getString("HelperUI.Status.Label.CurrentTurn") + " ")); //$NON-NLS-1$//$NON-NLS-2$
		statusPanel.add(mTurnNumberLabel = new JLabel());
		statusPanel.add(createSeparationPanel());
		statusPanel.add(new JLabel(UIMessages.getString("HelperUI.Status.Label.NumberOfPlayers") + " ")); //$NON-NLS-1$//$NON-NLS-2$
		statusPanel.add(mNbPlayersLabel = new JLabel());
		statusPanel.add(createSeparationPanel());
		if (pGame.getMoneySystem() == Game.MONEY_DEBT)
		{
			statusPanel.add(new JLabel(UIMessages.getString("HelperUI.Status.Label.CurrentMoneyMass") + " ")); //$NON-NLS-1$//$NON-NLS-2$
			statusPanel.add(mMoneyMassLabel = new JLabel());
			statusPanel.add(createSeparationPanel());
			statusPanel.add(new JLabel(UIMessages.getString("HelperUI.Status.Label.AmountOfMoneyPerPlayer") + " ")); //$NON-NLS-1$//$NON-NLS-2$
			statusPanel.add(mMoneyPerPlayerLabel = new JLabel());
			statusPanel.add(createSeparationPanel());
			statusPanel.add(new JLabel(UIMessages.getString("HelperUI.Status.Label.CurrentBankGains") + " ")); //$NON-NLS-1$//$NON-NLS-2$
			statusPanel.add(mGainedInterestLabel = new JLabel());
			statusPanel.add(createSeparationPanel());
			statusPanel.add(new JLabel(UIMessages.getString("HelperUI.Status.Label.CurrentBankSeized") + " ")); //$NON-NLS-1$//$NON-NLS-2$
			statusPanel.add(mSeizedValuesLabel = new JLabel());
		}
		// Initialize the correct current values
		fillStatusPanel();
		return statusPanel;
	}

	/**
	 * @return a separation panel for the status bas
	 */
	private JPanel createSeparationPanel()
	{
		final JPanel separationPanel = new JPanel();
		separationPanel.setMinimumSize(new Dimension(10, 5));
		separationPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		return separationPanel;
	}

	/**
	 * Sets the correct values for all elements in the status panel
	 */
	private void fillStatusPanel()
	{
		if (mGame != null)
		{
			mMoneySystemLabel.setText((mGame.getMoneySystem() == Game.MONEY_DEBT ? UIMessageKeyProvider.GENERAL_DEBT_MONEY.getMessage() : UIMessageKeyProvider.GENERAL_LIBRE_CURRENCY.getMessage()) + " ");//$NON-NLS-1$
			mTurnNumberLabel.setText(String.valueOf(mGame.getTurnNumber()) + " / " + String.valueOf(mGame.getNbTurnsPlanned()) + " ");//$NON-NLS-1$//$NON-NLS-2$
			mNbPlayersLabel.setText(String.valueOf(mPlayers.size()) + " ");//$NON-NLS-1$
			if (mGame.getMoneySystem() == Game.MONEY_DEBT)
			{
				mMoneyMassLabel.setText(String.valueOf(mGame.getMoneyMass()) + " ");//$NON-NLS-1$
				mMoneyPerPlayerLabel.setText(new DecimalFormat("#.###").format(mPlayers.size() > 0 ? 1.0 * mGame.getMoneyMass() / mPlayers.size() : 0.0) + " ");//$NON-NLS-1$//$NON-NLS-2$
				mGainedInterestLabel.setText(String.valueOf(mGame.getInterestGained()) + " ");//$NON-NLS-1$
				mSeizedValuesLabel.setText(String.valueOf(mGame.getSeizedValues()) + " ");//$NON-NLS-1$
			}
		}
	}

	public EntityManager getEntityManager()
	{
		return mEntityManager;
	}

	public static void main(String[] args) throws IOException
	{
		final EntityManagerFactory factory = Persistence.createEntityManagerFactory(DB_DEFAULT_NAME);
		final EntityManager entityManager = factory.createEntityManager();
		// Start up with the game chooser
		new ChooseGameDialog(entityManager, factory).setVisible(true);
	}

	public Game getGame()
	{
		return mGame;
	}

	/**
	 * Sets the current game and initializes mostly everything
	 * @param pGame
	 */
	private void setGame(Game pGame)
	{
		mGame = pGame;
		mMenuViewMoneyHelper.setEnabled(mGame.getMoneySystem() == Game.MONEY_LIBRE);

		// Fill in the players and events
		mPlayers.clear();
		mPlayers.addAll(mGame.getPlayers());
		mEvents.clear();
		mEvents.addAll(mGame.getEvents());

		// Initialize everything for this game
		sortEvents();

		// Compute players that have already died in the game
		mNonDeadPlayers.clear();
		for (Player player : mPlayers)
			if (player.isActive())
				mNonDeadPlayers.add(player.getId());

		for (Event event : mEvents)
			if (EventType.DEATH.equals(event.getEvt()))
				mNonDeadPlayers.remove(event.getPlayer().getId());

		sortPlayers();

		fillStatusPanel();
		generatePlayerColorPalette();
		suggestDeaths();
		enableButtons();
	}

	/**
	 * This function computes the number of dead players we should have at this turn.<br>
	 * It is actually a simple linear function.<br>
	 * The base point is at pReferenceTurn with pRenewedAtReferenceTurn players that have been dead at that point.<br>
	 * The last point is at the pNbTurns of the game with pNbPlayers.<br>
	 * And we return the position on the line at position pCurrentTurn.
	 * 
	 * @param pNbPlayers
	 * @param pNbTurns
	 * @param pReferenceTurn
	 * @param pRenewedAtReferenceTurn
	 * @param pCurrentTurn
	 * @return
	 */
	private int rebornFunction(double pNbPlayers, double pNbTurns, double pReferenceTurn, double pRenewedAtReferenceTurn, double pCurrentTurn)
	{
		return (int)Math.round((pCurrentTurn - pReferenceTurn) * (pNbPlayers - pRenewedAtReferenceTurn) / (pNbTurns - pReferenceTurn) + pRenewedAtReferenceTurn);
	}

	/**
	 * Computes the current "ideal" death schedule in mDeathSchedule.
	 * It is probably the trickiest algorithm of this program.
	 */
	private void createDeathSchedule()
	{
		mDeathSchedule.clear();
		// search for the first turn when there was no player movement
		int t0 = 1;// this is the first "reference" turn
		int p0 = 0;// pop regenerated at the "reference" turn
		int pSinceLastReference = 0;// nb players reborn since reference turn
		int t = 1;// the current turn in the loop
		// Search for the "reference" turn
		// It is the last turn where no player has joined or quit, and the last turn where the animator has not followed the initial plan
		for (int i = 0; i < mEvents.size(); i++)
		{
			Event event = mEvents.get(i);
			if (EventType.TURN.equals(event.getEvt()))
			// We have a new turn
				t++;
			else if (EventType.JOIN.equals(event.getEvt()) || EventType.QUIT.equals(event.getEvt()))
			// Not this turn
			{
				t0 = t;
				p0 += pSinceLastReference;
				pSinceLastReference = 0;
			}
			else if (EventType.DEATH.equals(event.getEvt()))
			{
				// Increment the number of deaths since last reference turn
				pSinceLastReference++;
				// Fill the entries until the current turn
				while (mDeathSchedule.size() < t)
					mDeathSchedule.add(0);
				// Increment the existing value
				mDeathSchedule.set(t - 1, mDeathSchedule.get(t - 1).intValue() + 1);
			}
		}
		// Now check if everything has been according to the suggestions since the last reference turn
		// Count the number of active players
		int nbPlayers = 0;
		for (Player player : mPlayers)
			if (player.isActive())
				nbPlayers++;
		if (mGame.getTurnNumber() > t0)
		// Otherwise there's nothing to check, we're already at the reference turn
			if (pSinceLastReference != rebornFunction(nbPlayers, mGame.getNbTurnsPlanned(), t0, p0, mGame.getTurnNumber()))
			{
				// We have to make the current turn the reference turn because the animator hasn't been following the plan
				t0 = mGame.getTurnNumber();
				p0 += pSinceLastReference;
				pSinceLastReference = 0;
			}
		int curRenewed = p0;
		// Now that we know which turn is the reference turn, we can compute the plan since then and until the end of the game
		for (int i = t0; i < mGame.getNbTurnsPlanned(); i++)
		{
			// Fill the entries until the current turn
			while (mDeathSchedule.size() < i)
				mDeathSchedule.add(0);
			final int target = rebornFunction(nbPlayers, mGame.getNbTurnsPlanned(), t0, p0, i + 1);
			final int diff = target - curRenewed;
			mDeathSchedule.set(i - 1, diff);
			curRenewed += diff;
		}
	}

	/**
	 * The sorting of the players is mainly alphabetical, except in debt-money when we want to put players who
	 * require some attention (they need to visit the bank) before the others.<br>
	 * Inactive players are also thrown at the end of the list.
	 */
	private void sortPlayers()
	{
		// Remember which player was selected
		final int selectedPlayerIndex = mPlayerTable.getSelectedRow();
		final Player selectedPlayer = selectedPlayerIndex >= 0 ? mPlayers.get(selectedPlayerIndex) : null;

		synchronized (mPlayerTable)
		{
			Collections.sort(mPlayers, new Comparator<Player>()
			{
				@Override
				public int compare(Player p1, Player p2)
				{
					// First eliminate inactive players
					if (p1.isActive() != p2.isActive())
						return p1.isActive() ? -1 : 1;
					// Throw players who need to visit the bank first
					if (p1.hasVisitedBank() != p2.hasVisitedBank())
						return p2.hasVisitedBank() ? -1 : 1;
					// Always sort with the player name
					return p1.getName().compareTo(p2.getName());
				}
			});

			// Need to recompute everything related to the players
			mCreditHistory.clear();
			mPlayersInPrison.clear();
			mPlayerAges.clear();
			mPlayersInWarning.clear();
			// Map of [player ID, current credit]
			final Map<Integer, Integer> currentCredit = new HashMap<>();
			for (Event event : mEvents)
				if (event.getPlayer() != null)
				// We have a player, compute his debts and his history
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
						sb.append(HelperUI.TXT_HISTORY_NEWCREDIT).append(String.valueOf(event.getPrincipal()));
						if (currentCredit.containsKey(playerId))
							currentCredit.put(playerId, currentCredit.get(playerId).intValue() + event.getPrincipal());
						else
							currentCredit.put(playerId, event.getPrincipal());
						break;
					case INTEREST_ONLY:
						sb.append(HelperUI.TXT_HISTORY_TURN).append(String.valueOf(event.getInterest()));
						break;
					case REIMB_CREDIT:
						if (event.getPrincipal() == currentCredit.get(playerId).intValue())
						// Credit reimbursed in full
						{
							sb.append(HelperUI.TXT_HISTORY_TURN).append(UIMessages.getString("HelperUI.Player.Table.History.Reimbursement")).append(HelperUI.TXT_HISTORY_CREDIT_END); //$NON-NLS-1$
							currentCredit.remove(playerId);
						}
						else
						{
							sb.append(HelperUI.TXT_HISTORY_TURN).append(UIMessages.getString("HelperUI.Player.Table.History.Reimbursement")).append(String.valueOf(event.getPrincipal())); //$NON-NLS-1$
							currentCredit.put(playerId, currentCredit.get(playerId).intValue() - event.getPrincipal());
						}
						break;
					case BANKRUPT:
					case PRISON:
						// Add to prison for this turn (bankruptcy is actually the same)
						mPlayersInPrison.add(playerId);
					case CANNOT_PAY:
						sb.append(HelperUI.TXT_HISTORY_TURN).append(UIMessages.getString("HelperUI.Player.Table.History.Default")); //$NON-NLS-1$
						if (EventType.BANKRUPT.equals(event.getEvt()))
							sb.append(UIMessages.getString("HelperUI.Player.Table.History.Bankruptcy")); //$NON-NLS-1$
						else if (EventType.PRISON.equals(event.getEvt()))
							sb.append(UIMessages.getString("HelperUI.Player.Table.History.Prison")); //$NON-NLS-1$
						sb.append(HelperUI.TXT_HISTORY_CREDIT_END);
						currentCredit.remove(playerId);
						break;
					case DEATH:// Death clears everything - we start afresh
						sb.setLength(0);
						sb.append(UIMessages.getString("HelperUI.Player.Table.History.DeathRebirth")).append(HelperUI.TXT_HISTORY_SEPARATOR); //$NON-NLS-1$
						currentCredit.remove(playerId);
						mPlayerAges.put(playerId, 1);
						break;
					default:
						break;
					}
				}
				else if (EventType.TURN.equals(event.getEvt()))
				{
					// This is a new turn
					mPlayersInPrison.clear();
					for (Integer playerId : mPlayerAges.keySet())
						mPlayerAges.put(playerId, mPlayerAges.get(playerId).intValue() + 1);
				}
		}

		// Check all players by alphabetical order to detect possible collisions
		final List<Player> byAlpha = new ArrayList<>();
		byAlpha.addAll(mPlayers);
		byAlpha.sort(new Comparator<Player>()
		{
			@Override
			public int compare(Player pO1, Player pO2)
			{
				return pO1.getName().compareTo(pO2.getName());
			}
		});
		Player previousPlayer = null;
		for (Player player : byAlpha)
		{
			if ((previousPlayer != null) && (player.getName().contains(previousPlayer.getName())))
			// The previous player and this player have similar names - put a warning on both
			{
				mPlayersInWarning.put(player.getId(), MessageFormat.format(UIMessageKeyProvider.MAINFRAME_TABLE_PLAYER_HINT_AMBIGUOUS_NAME.getMessage(), previousPlayer.getName()));
				mPlayersInWarning.put(previousPlayer.getId(), MessageFormat.format(UIMessageKeyProvider.MAINFRAME_TABLE_PLAYER_HINT_AMBIGUOUS_NAME.getMessage(), player.getName()));
			}
			previousPlayer = player;
		}
		mPlayerTable.tableChanged(new TableModelEvent(mPlayerTableModel));
		mPlayerTable.repaint();

		// Restore the selected player if possible
		if (selectedPlayer != null)
		{
			int startIndex = 0;
			for (Player player : mPlayers)
			{
				if (player == selectedPlayer)
					break;
				startIndex++;
			}
			if (startIndex < mPlayerTable.getModel().getRowCount())
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

	private void suggestDeaths()
	{
		if (mGame.getTurnNumber() == 0)
		// Because we don't want to suggest deaths before the players start playing!
		{
			mSuggestedDeathsLabel.setText("");//$NON-NLS-1$
			return;
		}
		// Suggest new deaths
		final Set<Integer> nonDeadPlayers = new HashSet<>();
		nonDeadPlayers.addAll(mNonDeadPlayers);
		createDeathSchedule();

		// Get as many suggested players as needed by the schedule for this turn. This will automatically be sorted.
		final SortedSet<String> chosen = new TreeSet<>();
		if ((nonDeadPlayers.size() > 0) && (mGame.getTurnNumber() <= mDeathSchedule.size()))
		{
			// The target number of players to be reborn this turn
			final int curTarget = mDeathSchedule.get(mGame.getTurnNumber() - 1);
			// As long as we don't have the correct count or there is no one left to die, continue
			while ((chosen.size() < curTarget) && (nonDeadPlayers.size() > 0))
			{
				if (nonDeadPlayers.size() == 1)
				// That's the last player to die, pick him, there is no choice
				{
					chosen.add(getPlayerWithId(nonDeadPlayers.iterator().next()).getName());
					nonDeadPlayers.clear();
				}
				else
				// Pick a player randomly in the list of players that have not experienced death yet
				{
					final int rand = sRand.nextInt(nonDeadPlayers.size() + 1);
					final Iterator<Integer> it = nonDeadPlayers.iterator();
					for (int i = 0; i < rand - 1; i++)
						it.next();
					final Integer i = it.next();
					nonDeadPlayers.remove(i);
					chosen.add(getPlayerWithId(i).getName());
				}
			}
		}
		// Build the final string
		final StringBuilder sb = new StringBuilder();
		for (String playerName : chosen)
		{
			if (sb.length() > 0)
				sb.append(", ");//$NON-NLS-1$
			sb.append(playerName);
		}
		mSuggestedDeathsLabel.setText(sb.toString());
	}

	/**
	 * @param pPlayerId
	 * @return the player with ID pPlayerId
	 */
	private Player getPlayerWithId(Integer pPlayerId)
	{
		for (Player player : mPlayers)
			if (player.getId().equals(pPlayerId))
				return player;
		return null;
	}

	private void refreshUI()
	{
		sortEvents();
		sortPlayers();
		fillStatusPanel();
	}

	/**
	 * Triggered to change the menu checkbox when that window is closed.
	 */
	public void closedValueHelper()
	{
		mMenuViewMoneyHelper.setSelected(false);
		mValuesHelper = null;
	}

	public void closedKeyboardShortcutHelper()
	{
		mMenuViewKBShortcutsHelper.setSelected(false);
		mKBShortcutsHelper = null;
	}
}
