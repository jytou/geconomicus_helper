package jyt.geconomicus.helper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import jyt.geconomicus.helper.Event.EventType;

/**
 * The frame that shows all the statistics on games with histograms and history.
 * @author jytou
 */
public class StatsFrame extends JFrame
{
	// This is used to add the bank at the end of all players - hopefully nobody will have a name starting with ZZZZ
	private static final String SORTER_FOR_BANK = "zzz"; //$NON-NLS-1$
	private static final String BANK_NAME = UIMessages.getString("StatsFrame.BankName"); //$NON-NLS-1$
	// The key used in the sorted structures - will automatically go at the end of the list
	private static final String BANK_KEY = SORTER_FOR_BANK + BANK_NAME;

	// The different money types that are supported
	private List<String> mMoneyTypes = new ArrayList<>();

	// Colors for the different money systems
	private static final Color COLOR_DEBT_MONEY = new Color(200, 120, 60);
	private static final Color COLOR_LIBRE_MONEY = new Color(0, 150, 0);

	// Events to be included in the history graphs
	private final static Set<EventType> mIncludeEvents = new HashSet<>();
	static
	{
		mIncludeEvents.add(EventType.NEW_CREDIT);
		mIncludeEvents.add(EventType.INTEREST_ONLY);
		mIncludeEvents.add(EventType.REIMB_CREDIT);
		mIncludeEvents.add(EventType.BANKRUPT);
		mIncludeEvents.add(EventType.CANNOT_PAY);
		mIncludeEvents.add(EventType.PRISON);
		mIncludeEvents.add(EventType.QUIT);
		mIncludeEvents.add(EventType.DEATH);
		mIncludeEvents.add(EventType.MM_CHANGE);
		mIncludeEvents.add(EventType.SIDE_INVESTMENT);
	}

	/**
	 * A panel to show the history of the money mass during the game.
	 * @author jytou
	 */
	private class HistoryStats extends JPanel
	{
		// The spaces on the sides and on the top and bottom
		private final static int FRAME_SPACE_HORIZ = 5;
		private final static int FRAME_SPACE_VERT = 10;

		private Game mGame;
		private int mMaxMoneyMass = 0;
		private int mNbEvents = 0;
		private int mCurTurn = 0;

		public HistoryStats(Game pGame)
		{
			super();
			mGame = pGame;
			// Compute the extent of the graph
			mGame.recomputeAll(new IEventListener()
			{
				@Override
				public void eventApplied(Event pEvent)
				{
					if (mGame.getMoneyMass() > mMaxMoneyMass)
						mMaxMoneyMass = mGame.getMoneyMass();
					if (mIncludeEvents.contains(pEvent.getEvt()) && (mCurTurn < mGame.getNbTurnsPlanned()))
						mNbEvents++;
					if (EventType.TURN.equals(pEvent.getEvt()))
						mCurTurn++;
				}
			});
		}

		@Override
		protected void paintComponent(final Graphics g)
		{
			super.paintComponent(g);
			// Some initialization
			final int w = getWidth();
			final int h = getHeight();
			final int frameWidth = (int)(1.0 * FRAME_SPACE_HORIZ * w / 100);
			final int frameHeight = (int)(1.0 * FRAME_SPACE_VERT * h / 100);
			final int usableWidth = w - 2 * frameWidth;
			final int usableHeight = h - 2 * frameHeight;
			final double fontSize = 0.4 * frameWidth;
			g.setFont(getFont().deriveFont((float)fontSize));
			g.setColor(Color.white);
			g.fillRect(0, 0, w, h);
			mCurTurn = 0;

			// Iterate through events
			mGame.recomputeAll(new IEventListener()
			{
				private int mPreviousValue = usableHeight;
				private int mPreviousPos = 0;
				private int mCurEvent = mGame.getMoneySystem() == Game.MONEY_DEBT ? 0 : -1;
				@Override
				public void eventApplied(Event pEvent)
				{
					if (mIncludeEvents.contains(pEvent.getEvt()) && (mCurTurn < mGame.getNbTurnsPlanned()))
					{
						mCurEvent++;
						if (mCurEvent == 0)
						// Because in the libre money system we want to ignore the first new turn event - there are no credits before that anyway
							return;
						final int pos = usableWidth * mCurEvent / mNbEvents;
						final int curValue = usableHeight - usableHeight * mGame.getMoneyMass() / mMaxMoneyMass;
						g.setColor(new Color(0, 150, 0));
						if ((mGame.getMoneySystem() == Game.MONEY_LIBRE) && (mCurEvent == 1))
						// Because we don't want to start at 0 in libre currencies
							mPreviousValue = curValue;
						g.drawLine(frameWidth + mPreviousPos, frameHeight + mPreviousValue, frameWidth + pos, frameHeight + curValue);
						mPreviousPos = pos;
						mPreviousValue = curValue;
						if (mGame.getTurnNumber() > mCurTurn)
						// draw the x axis turn changes
						{
							g.setColor(Color.black);
							g.drawLine(frameWidth + pos, frameHeight + usableHeight - 5, frameWidth + pos, frameHeight + usableHeight + 5);
							mCurTurn = mGame.getTurnNumber();
							g.drawString(String.valueOf(mCurTurn), frameWidth + pos, (int)(frameHeight + usableHeight + fontSize));
						}
						// Add major events (credits)
						boolean show = false;
						switch (pEvent.getEvt())
						{
						case NEW_CREDIT:
							show = true;
							g.setColor(new Color(0, 200, 0));
							break;
						case BANKRUPT:
						case CANNOT_PAY:
						case PRISON:
							show = true;
							g.setColor(Color.red);
							break;
						default:
							break;
						}
						if (show)
							g.drawLine(frameWidth + pos, frameHeight + usableHeight, frameWidth + pos, frameHeight + curValue);
					}
				}
			});
			g.setColor(Color.black);
			// Print Y axis labels
			for (int i = 0; i < mMaxMoneyMass + 20; i += 20)
			{
				if (i > mMaxMoneyMass)
					i = mMaxMoneyMass;
				final int yPos = frameHeight + usableHeight - i * usableHeight / mMaxMoneyMass;
				g.drawString(String.valueOf(i), (int)(frameWidth - fontSize * 2), yPos);
				g.drawLine(frameWidth - 5, yPos, frameWidth + 5, yPos);
			}
			// y axis
			g.drawLine(frameWidth, frameHeight, frameWidth, h - frameHeight);
			// x axis
			g.drawLine(frameWidth, h - frameHeight, w - frameWidth, h - frameHeight);
		}
	}

	public static void setDashedLines(Graphics2D g2)
	{
		g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {11}, 0));
	}

	public static void setDottedLines(Graphics2D g2)
	{
		g2.setStroke (new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1, new float[] {2, 9}, 0));
	}

	/**
	 * Show aggregated stats of both games.
	 * @author jytou
	 */
	class AggregatedStats extends JPanel
	{
		// Percentages
		private final static int BAR_WIDTH = 80;
		private final static int FRAME_SPACE_HORIZ = 5;
		private final static int FRAME_SPACE_VERT = 10;

		// The achievements of the players. Map of [playername, [value for each game]]
		private SortedMap<String, List<Integer>> mAchievements = new TreeMap<>();
		// The max to know where to stop
		private int mAggregatedMax = 0;
		// Different statistics for every game
		private List<Double> mAverages = new ArrayList<>();
		private List<Double> mStdDevs = new ArrayList<>();
		private List<Double> mPoors = new ArrayList<>();
		private List<Color> mColors = new ArrayList<>();

		/**
		 * Sets the achievement for all players and for a single game (represented by a color).
		 * Basically, using this method we use the panel to show only one game instead of several of them.
		 * @param pAchievements Map of [playername, achievement in this game]
		 * @param pColor the color of the game
		 */
		public synchronized void setPlayerAchievements(final SortedMap<String, Integer> pAchievements, Color pColor)
		{
			final SortedMap<String, List<Integer>> achievements = new TreeMap<>();
			for (String playerName : pAchievements.keySet())
				achievements.put(playerName, Arrays.asList(new Integer[] {pAchievements.get(playerName)}));
			setAchievements(achievements, Arrays.asList(new Color[] {pColor}));
		}

		/**
		 * Set player achievements for several games.
		 * @param pAchievements Map of [playername, list of achievements: one per game]
		 * @param pColors List of colors, one per game
		 */
		public synchronized void setAchievements(final SortedMap<String, List<Integer>> pAchievements, List<Color> pColors)
		{
			mColors = pColors;
			mAchievements.clear();
			for (String playerName : pAchievements.keySet())
				mAchievements.put(playerName, new ArrayList<>(pAchievements.get(playerName)));

			// Compute the max of the whole graphic
			mAggregatedMax = 0;
			final List<Integer> nbPlayersForGame = new ArrayList<>();
			int nbGames = 0;
			for (List<Integer> values : pAchievements.values())
			{
				final int size = values.size();
				if (size > nbGames)
					nbGames = size;
			}
			// Compute stats - average, standard deviation and poverty line
			mAverages.clear();
			mStdDevs.clear();
			mPoors.clear();
			final List<List<Integer>> allValues = new ArrayList<>();
			for (int i = 0; i < nbGames; i++)
			{
				mAverages.add((double)0);
				mStdDevs.add((double)0);
				mPoors.add((double)0);
				nbPlayersForGame.add(0);
				allValues.add(new ArrayList<>());
			}
			for (String playerName : pAchievements.keySet())
			{
				final List<Integer> playerAchievements = pAchievements.get(playerName);
				for (int i = 0; i < playerAchievements.size(); i++)
				{
					final Integer valueInteger = playerAchievements.get(i);
					if (valueInteger != null)
					// A null value indicated that the player has not participated in the game so it shouldn't be counted
					{
						allValues.get(i).add(valueInteger);
						final int value = valueInteger.intValue();
						if (value > mAggregatedMax)
							mAggregatedMax = value;
						nbPlayersForGame.set(i, nbPlayersForGame.get(i) + 1);
						mAverages.set(i, mAverages.get(i).doubleValue() + value);
					}
				}
			}
			// Delete any games that has no players - what would be the goal of that anyway?
			// Besides, compute the average, standard dev and poverty line for the other games
			// We have to navigate backwards in the list of games because we are deleting the empty ones
			for (int i = nbPlayersForGame.size() - 1; i >= 0 ; i--)
			{
				final int nbPlayers = nbPlayersForGame.get(i);
				if (nbPlayers == 0)
				{
					mAverages.remove(i);
					mStdDevs.remove(i);
					mPoors.remove(i);
					allValues.remove(i);
					for (String playerName : pAchievements.keySet())
						pAchievements.get(playerName).remove(i);
				}
				else
				{
					final double average = mAverages.get(i).doubleValue() / nbPlayers;
					mAverages.set(i, average);
					for (List<Integer> values : pAchievements.values())
					{
						Integer value = values.get(i);
						if (value != null)
							mStdDevs.set(i, mStdDevs.get(i) + sqr(average - value.doubleValue()));
					}
					// We want the standard deviation relative to the average
					mStdDevs.set(i, Math.sqrt(mStdDevs.get(i) / nbPlayers) / average);

					// Now compute the median
					final List<Integer> gameValues = allValues.get(i);
					gameValues.sort(new Comparator<Integer>()
					{
						@Override
						public int compare(Integer i1, Integer i2)
						{
							return i1.compareTo(i2);
						}
					});
					final double median = gameValues.size() % 2 == 0 ? (gameValues.get(gameValues.size() / 2) + gameValues.get(gameValues.size() / 2 + 1)) / 2 : gameValues.get(gameValues.size() / 2);
					mPoors.set(i, median * 0.6);
				}
			}
		}

		@Override
		protected synchronized void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			// Initializations
			final Graphics2D g2 = (Graphics2D)g;
			final Stroke originalStroke = g2.getStroke();
			if (mAchievements.isEmpty())
				return;
			final int w = getWidth();
			final int h = getHeight();
			final int frameWidth = (int)(1.0 * FRAME_SPACE_HORIZ * w / 100);
			final int frameHeight = (int)(1.0 * FRAME_SPACE_VERT * h / 100);
			final int usableWidth = w - 2 * frameWidth;
			final int usableHeight = h - 2 * frameHeight;
			final int nbPlayers = mAchievements.size();
			final int barWidth = (int)Math.round(1.0 * usableWidth / nbPlayers / mAverages.size() * BAR_WIDTH / 100);
			final int barPos = 3;
			final double fontSize = 0.02 * w;
			g.setFont(getFont().deriveFont((float)fontSize));
			g.setColor(Color.white);
			g.fillRect(0, 0, w, h);

			int labelsYpos = frameHeight;

			// show averages and the histograms of the players
			for (int i = 0; i < mAverages.size(); i++)
			{
				g.setColor(mColors.get(i));
				if (mAverages.size() > 1)
				// Print the type of money only on the aggregated graphs - eg if there are more than one game
				{
					g.drawString(mMoneyTypes.get(i), frameWidth + 10, labelsYpos);
					labelsYpos += fontSize;
				}
				// player histograms
				int x = 0;
				for (String playerName : mAchievements.keySet())
				{
					final int pos = (int)((1.0 * usableWidth * x) / nbPlayers) + (barWidth + 1) * i;
					final Integer achievement = mAchievements.get(playerName).get(i);
					final int value = achievement == null ? 0 : usableHeight * achievement.intValue() / mAggregatedMax;
					if (value > 0)
						g.fillRect(frameWidth + pos + barPos, h - frameHeight - value, barWidth, value);
					else
						g.fillRect(frameWidth + pos + barPos, h - frameHeight, barWidth, -value);
					x++;
				}
			}
			// Show the other stats
			for (int i = 0; i < mAverages.size(); i++)
			{
				g.setColor(mColors.get(i).darker());
				// standard deviation
				setDashedLines(g2);
				final int stdY = h - frameHeight - (int)(mStdDevs.get(i) * usableHeight / 1.5);// because we go up to 150% in the graph
				g.drawLine(frameWidth, stdY, w - frameWidth, stdY);
				// poors
				setDottedLines(g2);
				final int poorY = h - frameHeight - (int)(mPoors.get(i) * usableHeight / mAggregatedMax);
				g.drawLine(frameWidth, poorY, w - frameWidth, poorY);
				// average
				g2.setStroke(originalStroke);
				final int avgY = h - frameHeight - (int)(mAverages.get(i) * usableHeight / mAggregatedMax);
				g.drawLine(frameWidth, avgY, w - frameWidth, avgY);
			}
			g.setColor(Color.black);
			// Print the different types of lines: average, std dev and poor
			g.drawLine(frameWidth + 10, (int)(labelsYpos - fontSize / 3), frameWidth + 40, (int)(labelsYpos - fontSize / 3));
			g.drawString(UIMessages.getString("StatsFrame.AverageLabel"), frameWidth + 45, labelsYpos); //$NON-NLS-1$
			labelsYpos += fontSize;
			setDashedLines(g2);
			g.drawLine(frameWidth + 10, (int)(labelsYpos - fontSize / 3), frameWidth + 40, (int)(labelsYpos - fontSize / 3));
			g2.setStroke(originalStroke);
			g.drawString(UIMessages.getString("StatsFrame.StandardDeviationLabel"), frameWidth + 45, labelsYpos); //$NON-NLS-1$
			labelsYpos += fontSize;
			setDottedLines(g2);
			g.drawLine(frameWidth + 10, (int)(labelsYpos - fontSize / 3), frameWidth + 40, (int)(labelsYpos - fontSize / 3));
			g2.setStroke(originalStroke);
			g.drawString(UIMessages.getString("StatsFrame.PovertyLineLabel"), frameWidth + 45, labelsYpos); //$NON-NLS-1$

			// Draw the player names
			int x = 0;
			for (String playerName : mAchievements.keySet())
			{
				final int pos = (int)((1.0 * usableWidth * x) / nbPlayers);
				String name = playerName;
				if (name.startsWith(StatsFrame.SORTER_FOR_BANK))
					name = name.substring(SORTER_FOR_BANK.length());
				final int stringWidth = g.getFontMetrics().stringWidth(name);
				g.drawString(name, (int)(frameWidth + pos + (1.0 * usableWidth / nbPlayers / 2) - stringWidth / 2), (int)(h - frameHeight + (int)fontSize + (x%2 * fontSize)));
				x++;
			}
			// Show the y axis values
			for (int i = 0; i < mAggregatedMax + 20; i += 20)
			{
				if (i > mAggregatedMax)
					i = mAggregatedMax;
				final int yPos = frameHeight + usableHeight - i * usableHeight / mAggregatedMax;
				g.drawString(String.valueOf(i), (int)(frameWidth - fontSize * 2), yPos);
				g.drawLine(frameWidth - 5, yPos, frameWidth + 5, yPos);
			}
			// y axis
			g.drawLine(frameWidth, frameHeight, frameWidth, h - frameHeight);
			// x axis
			g.drawLine(frameWidth, h - frameHeight, w - frameWidth, h - frameHeight);
			// standard deviation axis
			g.drawLine(w - frameWidth, frameHeight, w - frameWidth, h - frameHeight);
			// standard deviation
			g.drawString("0 %", w - frameWidth + 10, h - frameHeight); //$NON-NLS-1$
			g.drawString("50 %", w - frameWidth + 10, h - frameHeight - usableHeight / 3); //$NON-NLS-1$
			g.drawString("100 %", w - frameWidth + 10, h - frameHeight - 2 * usableHeight / 3); //$NON-NLS-1$
			g.drawString("150 %", w - frameWidth + 10, frameHeight); //$NON-NLS-1$
			g.drawLine(w - frameWidth - 5, frameHeight + usableHeight / 3, w - frameWidth + 5, frameHeight + usableHeight / 3);
			g.drawLine(w - frameWidth - 5, frameHeight + 2 * usableHeight / 3, w - frameWidth + 5, frameHeight + 2 * usableHeight / 3);
			g.drawLine(w - frameWidth - 5, frameHeight, w - frameWidth + 5, frameHeight);
		}
	}

	private double sqr(double d)
	{
		return d*d;
	}

	public StatsFrame(List<Game> pGames) throws IOException
	{
		super(UIMessages.getString("StatsFrame.FrameTitle.Statistics")); //$NON-NLS-1$
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent pEvent)
			{
				System.exit(0);
			}
		});
		setIconImage(ImageIO.read(HelperUI.class.getResourceAsStream("/geconomicus_stats.png"))); //$NON-NLS-1$
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		final JTabbedPane tabbedPane = new JTabbedPane();
		final String bankName = SORTER_FOR_BANK + StatsFrame.BANK_NAME;
		// The list of achievements - we will reuse this variable for each player
		List<Integer> list = new ArrayList<>();
		final SortedMap<String, List<Integer>> aggregatedAchievements = new TreeMap<>();
		aggregatedAchievements.put(bankName, list);
		for (int i = 0; i < pGames.size(); i++)
			list.add(null);
		// Make sure we have the debt-money game first
		pGames.sort(new Comparator<Game>()
		{
			@Override
			public int compare(Game g1, Game g2)
			{
				return g1.getMoneySystem() < g2.getMoneySystem() ? 1 : -1;
			}
		});
		int pos = 0;
		final List<Color> aggregatedColors = new ArrayList<>();
		// Add every game to the statistics
		for (Game game : pGames)
		{
			aggregatedColors.add(game.getMoneySystem() == Game.MONEY_DEBT ? COLOR_DEBT_MONEY : COLOR_LIBRE_MONEY);
			mMoneyTypes.add(game.getMoneySystem() == Game.MONEY_DEBT ? UIMessageKeyProvider.GENERAL_DEBT_MONEY.getMessage() : UIMessageKeyProvider.GENERAL_LIBRE_CURRENCY.getMessage());
			for (Player player : game.getPlayers())
			{
				if (!aggregatedAchievements.containsKey(player.getName()))
				{
					list = new ArrayList<>();
					aggregatedAchievements.put(player.getName(), list);
					for (int i = 0; i < pGames.size(); i++)
						list.add(null);
				}
			}
			final AggregatedStats panelWOBank = new AggregatedStats();
			computeValues(game, panelWOBank, false, aggregatedAchievements, game.getMoneySystem() == Game.MONEY_DEBT ? -1 : pos++, game.getMoneySystem() == Game.MONEY_DEBT ? COLOR_DEBT_MONEY : COLOR_LIBRE_MONEY);
			tabbedPane.add(game.getMoneySystem() == Game.MONEY_DEBT ? UIMessages.getString("StatsFrame.TabTitle.DebtMoneyWithoutBank") : UIMessageKeyProvider.GENERAL_LIBRE_CURRENCY.getMessage(), panelWOBank); //$NON-NLS-1$
			if (game.getMoneySystem() == Game.MONEY_DEBT)
			// With the debt-money system, we have to add the other graph with the bank
			{
				final AggregatedStats panelWithBank = new AggregatedStats();
				computeValues(game, panelWithBank, true, aggregatedAchievements, pos++, COLOR_DEBT_MONEY);
				tabbedPane.add(UIMessages.getString("StatsFrame.TabTitle.DebtMoneyWithBank"), panelWithBank); //$NON-NLS-1$
			}
			final HistoryStats historyStats = new HistoryStats(game);
			tabbedPane.add(UIMessages.getString("StatsFrame.TabTitle.MoneyMassHistory"), historyStats); //$NON-NLS-1$
		}

		// Now create the aggregated statistics
		final AggregatedStats aggrStatsStd = new AggregatedStats();
		aggrStatsStd.setAchievements(aggregatedAchievements, aggregatedColors);
		tabbedPane.addTab(UIMessages.getString("StatsFrame.TabTitle.AggregatedStandard"), aggrStatsStd); //$NON-NLS-1$

		final SortedMap<String, List<Integer>> aggregatedAchievementsFixed = new TreeMap<>();
		for (String playerName : aggregatedAchievements.keySet())
		{
			List<Integer> achievements = new ArrayList<>();
			achievements.addAll(aggregatedAchievements.get(playerName));
			if (!BANK_KEY.equals(playerName))
			// the bank doesn't need any fixing
				for (int i = 0; i < achievements.size(); i++)
				{
					final Integer achievement = achievements.get(i);
					if (achievement != null)
					{
						// For all games, take away the 8 cards that the player got in his hands for free
						int adjustment = 8;
						if (pGames.get(i).getMoneySystem() == Game.MONEY_LIBRE)
						// Fix the Libre Money part
						// Take away the 7 coins on average that a player gets for each turn - once before his rebirth, once at the end of the game = 14
						// But we counted only 1/3 of the value so that's -2x2=4
							adjustment += 4 * pGames.get(i).getMoneyCardsFactor();
						achievements.set(i, achievement.intValue() - adjustment);
					}
				}
			aggregatedAchievementsFixed.put(playerName, achievements);
		}
		// TODO ideally, we should also adjust in debt money
		// every time we do an assessment on a player, we should take away the equivalent of the average money mass per player at the current turn
		// iterate on the events, for each turn save the current money mass before taking the players into account, then do the assessment for every player
		final AggregatedStats aggrStatsFixed = new AggregatedStats();
		aggrStatsFixed.setAchievements(aggregatedAchievementsFixed, aggregatedColors);
		tabbedPane.addTab(UIMessages.getString("StatsFrame.TabTitle.AggregatedCorrected"), aggrStatsFixed); //$NON-NLS-1$
		getContentPane().add(tabbedPane);
	}

	/**
	 * Computes all values for all players for this game
	 * @param pGame
	 * @param pValuesPanel
	 * @param pAddBank if <code>true</code>, add the bank
	 * @param pAggregatedAchievements
	 * @param pAggregateIndex the index (position) in the aggregated graphs
	 * @param pColor the color to use for this game
	 */
	private void computeValues(final Game pGame, final AggregatedStats pValuesPanel, final boolean pAddBank, SortedMap<String, List<Integer>> pAggregatedAchievements, final int pAggregateIndex, final Color pColor)
	{
		final String bankName = StatsFrame.BANK_KEY;
		final List<Event> events = new ArrayList<>();
		events.addAll(pGame.getEvents());
		// make sure events are sorted
		events.sort(new Comparator<Event>()
		{
			@Override
			public int compare(Event e1, Event e2)
			{
				return e1.getTstamp().compareTo(e2.getTstamp());
			}
		});
		final SortedMap<String, Integer> playerAchievements = new TreeMap<>();
		if (pAddBank)
			if (pGame.getMoneySystem() == Game.MONEY_DEBT)
				playerAchievements.put(bankName, 0);
		// This factor is multiplied by 2 when there is a technological breakthrough
		int currentFactor = 1;
		final Map<String, Integer> playerDebts = new HashMap<>();
		// Iterate through events and accumulate the debts and values created by the players
		for (Event event : events)
		{
			switch (event.getEvt())
			{
			case NEW_CREDIT:
			{
				final String playerName = event.getPlayer().getName();
				if (playerDebts.get(playerName) != null)
					playerDebts.put(playerName, playerDebts.get(playerName).intValue() + event.getPrincipal());
				else
					playerDebts.put(playerName, event.getPrincipal());
				break;
			}
			case JOIN:
			case TURN:
			case MM_CHANGE:
			case END:
				// Nothing to do here - but we take them into account for the default - make sure we don't skip events
				break;
			case INTEREST_ONLY:
				if (pAddBank)
					addFromEvent(pGame, event, bankName, null, playerAchievements, currentFactor);
				break;
			case CANNOT_PAY:
			case BANKRUPT:
			case PRISON:
			case REIMB_CREDIT:
				if (pAddBank)
				// All this goes to the bank, except the principal of the credit
				{
					addFromEvent(pGame, event, bankName, playerDebts.get(event.getPlayer().getName()), playerAchievements, currentFactor);
					playerDebts.remove(event.getPlayer().getName());
				}
				break;
			case DEATH:
			case QUIT:
				addFromEvent(pGame, event, event.getPlayer().getName(), null, playerAchievements, currentFactor);
				break;
			case XTECHNOLOGICAL_BREAKTHROUGH:
				currentFactor *= 2;
				break;
			case SIDE_INVESTMENT:
				if (pAddBank)
				// remove that from the bank's earnings - we'll get them back later.
					addFromEvent(pGame, event, bankName, null, playerAchievements, currentFactor, true);
				break;
			case ASSESSMENT_FINAL:
				if (pAddBank)
				// Add what the bank owns during assessment
					addFromEvent(pGame, event, bankName, null, playerAchievements, currentFactor);
				break;
			default:
				System.err.println(MessageFormat.format(UIMessages.getString("StatsFrame.ErrorMessage.UnknownEventType"), event.getEvt().toString())); //$NON-NLS-1$
				break;
			}
		}
		pValuesPanel.setPlayerAchievements(playerAchievements, pColor);
		if (pAggregateIndex != -1)
			for (String playerName : playerAchievements.keySet())
				pAggregatedAchievements.get(playerName).set(pAggregateIndex, playerAchievements.get(playerName));
	}

	private void addFromEvent(Game pGame, Event pEvent, final String pPlayerName, final Integer pOwedByPlayer, SortedMap<String, Integer> pPlayerAchievements, int pCurrentFactor)
	{
		addFromEvent(pGame, pEvent, pPlayerName, pOwedByPlayer, pPlayerAchievements, pCurrentFactor, false);
	}

	/**
	 * Increments the player's achievements from the event.
	 * @param pGame
	 * @param pEvent
	 * @param pPlayerName
	 * @param pOwedByPlayer this is the principal owed by the player - the one that the bank has to destroy
	 * @param pPlayerAchievements
	 * @param pCurrentFactor
	 * @param pSubtract if <code>true</code> we <b>subtract</b> the gained values instead of adding them: this is used for the bank when it invests cards and money
	 */
	private void addFromEvent(Game pGame, Event pEvent, final String pPlayerName, final Integer pOwedByPlayer, SortedMap<String, Integer> pPlayerAchievements, int pCurrentFactor, boolean pSubtract)
	{
		final Integer integerValue = pPlayerAchievements.get(pPlayerName);
		final int currentValue = integerValue == null ? 0 : integerValue.intValue();
		int gained = 0;
		if (pGame.getMoneySystem() == Game.MONEY_DEBT)
			gained += pEvent.getPrincipal() + pEvent.getInterest();
		else
			gained += (pEvent.getWeakCoins() + 2 * pEvent.getMediumCoins() + 4 * pEvent.getStrongCoins()) / 3;
		gained += (pEvent.getWeakCards() + 2 * pEvent.getMediumCards() + 4 * pEvent.getStrongCards()) * pCurrentFactor * pGame.getMoneyCardsFactor();
		if ((pOwedByPlayer != null) && (EventType.CANNOT_PAY.equals(pEvent.getEvt()) || EventType.BANKRUPT.equals(pEvent.getEvt()) || EventType.PRISON.equals(pEvent.getEvt())))
		{
			// The bank seized some values, but it should really destroy the principal (if possible)
			if (gained > pOwedByPlayer.intValue())
				gained -= pOwedByPlayer.intValue();
			else
			// If the bank couldn't seize enough value for the principal, it didn't earn anything
				gained = 0;
		}
		pPlayerAchievements.put(pPlayerName, currentValue + (pSubtract ? -gained : gained));
	}
}
