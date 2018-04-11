package jyt.geconomicus.helper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import jyt.geconomicus.helper.Event.EventType;

public class StatsFrame extends JFrame
{
	private static final String SORTER_FOR_BANK = "zzz";
	private static final String BANK_NAME = "banque";
	private static final Color STANDARD_DEV_COLOR = new Color(0, 100, 0);
	public static final Color HISTOGRAM_COLOR = new Color(0, 150, 0);

	private List<String> mMoneyTypes = new ArrayList<>();
	// [Player name, [achievement]]
	private SortedMap<String, List<Integer>> mAggregatedAchievements = new TreeMap<>();
	private Color[] mAchievementColors = new Color[] {new Color(200, 120, 60), new Color(0, 150, 0)};
	private int mAggregatedMax = 0;
	private List<Double> mAverages = new ArrayList<>();
	private List<Double> mStdDevs = new ArrayList<>();

	interface IStatsPanel
	{
		void setMaxValue(int pMaxValue);
	}
	private List<IStatsPanel> mStatsPanels = new ArrayList<>();

	class ValuesStats extends JPanel implements IStatsPanel
	{
		private SortedMap<String, Integer> mPlayerAchievements = new TreeMap<>();
		private int mMaxValue;
		private double mAverage;
		private double mStandardDeviation;

		// Percentages
		private final static int BAR_WIDTH = 80;
		private final static int FRAME_SPACE_HORIZ = 5;
		private final static int FRAME_SPACE_VERT = 10;

		public void setPlayerAchievements(SortedMap<String, Integer> pPlayerAchievements)
		{
			if (pPlayerAchievements.size() <= 1)
				return;

			mMaxValue = 0;
			int total = 0;
			for (int value : pPlayerAchievements.values())
			{
				if (value > mMaxValue)
					mMaxValue = value;
				total += value;
			}
			mAverage = total / pPlayerAchievements.size();
			double totalSquare = 0;
			for (int value : pPlayerAchievements.values())
				totalSquare += sqr(mAverage - value);
			// We want the standard deviation relative to the average
			mStandardDeviation = Math.sqrt(totalSquare / (pPlayerAchievements.size() - 1)) / mAverage;
			mPlayerAchievements = pPlayerAchievements;
			repaint();
		}

		public double getAverage()
		{
			return mAverage;
		}

		public double getStandardDeviation()
		{
			return mStandardDeviation;
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			final Graphics2D g2 = (Graphics2D)g;
			final Stroke originalStroke = g2.getStroke();
			if (mPlayerAchievements.isEmpty())
				return;
			// Some initialization
			final int w = getWidth();
			final int h = getHeight();
			final int frameWidth = (int)(1.0 * FRAME_SPACE_HORIZ * w / 100);
			final int frameHeight = (int)(1.0 * FRAME_SPACE_VERT * h / 100);
			final int usableWidth = w - 2 * frameWidth;
			final int usableHeight = h - 2 * frameHeight;
			int x = 0;
			final int nbPlayers = mPlayerAchievements.size();
			final int barWidth = (int)Math.round(1.0 * usableWidth / nbPlayers * BAR_WIDTH / 100);
			final int barPos = usableWidth / nbPlayers - barWidth;
			final double fontSize = 0.4 * frameWidth;
			g.setFont(getFont().deriveFont((float)fontSize));
			g.setColor(Color.white);
			g.fillRect(0, 0, w, h);
			// Show histograms for every player
			for (String playerName : mPlayerAchievements.keySet())
			{
				g.setColor(HISTOGRAM_COLOR);
				final int pos = (int)((1.0 * usableWidth * x) / nbPlayers);
				final int value = usableHeight * mPlayerAchievements.get(playerName).intValue() / mMaxValue;
				g.fillRect(frameWidth + pos + barPos, h - frameHeight - value, barWidth, value);
				g.setColor(Color.black);
				String name = playerName;
				if (name.startsWith(StatsFrame.SORTER_FOR_BANK))
					name = name.substring(SORTER_FOR_BANK.length());
				final int stringWidth = g.getFontMetrics().stringWidth(name);
				g.drawString(name, (int)(frameWidth + pos + (1.0 * usableWidth / nbPlayers / 2) - stringWidth / 2), (int)(h - frameHeight + (int)fontSize + (x%2 * fontSize)));
				x++;
			}
			g.setColor(Color.black);
			// Print Y axis labels
			for (int i = 0; i < mMaxValue + 20; i += 20)
			{
				if (i > mMaxValue)
					i = mMaxValue;
				final int yPos = frameHeight + usableHeight - i * usableHeight / mMaxValue;
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
			g.setColor(STANDARD_DEV_COLOR);
			setDottedLines(g2);
			int stdY = h - frameHeight - (int)(mStandardDeviation * usableHeight);
			g.drawLine(frameWidth, stdY, w - frameWidth, stdY);
			g2.setStroke(originalStroke);
			g.drawString("Écart-type", frameWidth + 5, stdY - 5);
			// std dev axis
			g.drawString("0 %", w - frameWidth + 10, h - frameHeight);
			g.drawString("50 %", w - frameWidth + 10, h - frameHeight - usableHeight / 2);
			g.drawString("100 %", w - frameWidth + 10, frameHeight);
			g.drawLine(w - frameWidth - 5, frameHeight + usableHeight / 2, w - frameWidth + 5, frameHeight + usableHeight / 2);
			g.drawLine(w - frameWidth - 5, frameHeight, w - frameWidth + 5, frameHeight);
			// average
			g.setColor(Color.red);
			stdY = h - frameHeight - (int)(mAverage * usableHeight);
			g.drawLine(frameWidth, stdY, w - frameWidth, stdY);
			g.setColor(Color.blue);
			final int avgY = h - frameHeight - (int)(mAverage * usableHeight / mMaxValue);
			g.drawLine(frameWidth, avgY, w - frameWidth, avgY);
			g.drawString("Moyenne", frameWidth + 5, avgY - 5);
		}

		@Override
		public void setMaxValue(int pMaxValue)
		{
			mMaxValue = pMaxValue;
		}
	}

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
	}

	private class HistoryStats extends JPanel
	{
		private final static int FRAME_SPACE_HORIZ = 5;
		private final static int FRAME_SPACE_VERT = 10;

		private Game mGame;
		private int mMaxMoneyMass = 0;
		private int mNbEvents = 0;

		public HistoryStats(Game pGame)
		{
			super();
			mGame = pGame;
			mGame.recomputeAll(new IEventListener()
			{
				@Override
				public void eventApplied(Event pEvent)
				{
					if (mGame.getMoneyMass() > mMaxMoneyMass)
						mMaxMoneyMass = mGame.getMoneyMass();
					if (mIncludeEvents.contains(pEvent.getEvt()))
						mNbEvents++;
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
			mGame.recomputeAll(new IEventListener()
			{
				private int mPreviousValue = usableHeight;
				private int mPreviousPos = 0;
				private int mCurEvent = 0;
				private int mCurTurn = 0;
				@Override
				public void eventApplied(Event pEvent)
				{
					if (mIncludeEvents.contains(pEvent.getEvt()))
					{
						mCurEvent++;
						final int pos = usableWidth * mCurEvent / mNbEvents;
						final int curValue = usableHeight - usableHeight * mGame.getMoneyMass() / mMaxMoneyMass;
						g.setColor(new Color(0, 150, 0));
						g.drawLine(frameWidth + mPreviousPos, frameHeight + mPreviousValue, frameWidth + pos, frameHeight + curValue);
						mPreviousPos = pos;
						mPreviousValue = curValue;
						if (mGame.getTurnNumber() > mCurTurn)
						{
							g.setColor(Color.black);
							g.drawLine(frameWidth + pos, frameHeight + usableHeight - 5, frameWidth + pos, frameHeight + usableHeight + 5);
							mCurTurn = mGame.getTurnNumber();
							g.drawString(String.valueOf(mCurTurn), frameWidth + pos, (int)(frameHeight + usableHeight + fontSize));
						}
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

	public void setDottedLines(Graphics2D g2)
	{
		g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {9}, 0));
	}

	class AggregatedStats extends JPanel
	{
		// Percentages
		private final static int BAR_WIDTH = 80;
		private final static int FRAME_SPACE_HORIZ = 5;
		private final static int FRAME_SPACE_VERT = 10;

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			final Graphics2D g2 = (Graphics2D)g;
			final Stroke originalStroke = g2.getStroke();
			if (mAggregatedAchievements.isEmpty())
				return;
			final int w = getWidth();
			final int h = getHeight();
			final int frameWidth = (int)(1.0 * FRAME_SPACE_HORIZ * w / 100);
			final int frameHeight = (int)(1.0 * FRAME_SPACE_VERT * h / 100);
			final int usableWidth = w - 2 * frameWidth;
			final int usableHeight = h - 2 * frameHeight;
			final int nbPlayers = mAggregatedAchievements.size();
			final int barWidth = (int)Math.round(1.0 * usableWidth / nbPlayers / mAchievementColors.length * BAR_WIDTH / 100);
			final int barPos = 3;
			final double fontSize = 0.02 * w;
			g.setFont(getFont().deriveFont((float)fontSize));
			g.setColor(Color.white);
			g.fillRect(0, 0, w, h);

			for (int i = 0; i < mAverages.size(); i++)
			{
				g.setColor(mAchievementColors[i]);
				g.drawString(mMoneyTypes.get(i), frameWidth + 10, (int)(frameHeight + i * fontSize));
				// player histograms
				int x = 0;
				for (String playerName : mAggregatedAchievements.keySet())
				{
					int pos = (int)((1.0 * usableWidth * x) / nbPlayers) + (barWidth + 1) * i;
					int value = usableHeight * mAggregatedAchievements.get(playerName).get(i).intValue() / mAggregatedMax;
					g.fillRect(frameWidth + pos + barPos, h - frameHeight - value, barWidth, value);
					x++;
				}
				// standard deviation
				setDottedLines(g2);
				final int stdY = h - frameHeight - (int)(mStdDevs.get(i) * usableHeight);
				g.drawLine(frameWidth, stdY, w - frameWidth, stdY);
				// average
				g2.setStroke(originalStroke);
				final int avgY = h - frameHeight - (int)(mAverages.get(i) * usableHeight / mAggregatedMax);
				g.drawLine(frameWidth, avgY, w - frameWidth, avgY);
			}
			int x = 0;
			g.setColor(Color.black);
			for (String playerName : mAggregatedAchievements.keySet())
			{
				final int pos = (int)((1.0 * usableWidth * x) / nbPlayers);
				String name = playerName;
				if (name.startsWith(StatsFrame.SORTER_FOR_BANK))
					name = name.substring(SORTER_FOR_BANK.length());
				final int stringWidth = g.getFontMetrics().stringWidth(name);
				g.drawString(name, (int)(frameWidth + pos + (1.0 * usableWidth / nbPlayers / 2) - stringWidth / 2), (int)(h - frameHeight + (int)fontSize + (x%2 * fontSize)));
				x++;
			}
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
			g.drawString("0 %", w - frameWidth + 5, h - frameHeight);
			g.drawString("50 %", w - frameWidth + 5, h - frameHeight - usableHeight / 2);
			g.drawString("100 %", w - frameWidth + 5, frameHeight);
			g.drawLine(w - frameWidth - 5, frameHeight + usableHeight / 2, w - frameWidth + 5, frameHeight + usableHeight / 2);
			g.drawLine(w - frameWidth - 5, frameHeight, w - frameWidth + 5, frameHeight);
		}
	}

	private double sqr(double d)
	{
		return d*d;
	}

	public StatsFrame(List<Game> pGames) throws IOException
	{
		super("Statistiques");
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent pEvent)
			{
				System.exit(0);
			}
		});
		setIconImage(ImageIO.read(HelperUI.class.getResourceAsStream("/geconomicus_stats.png")));
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		final JTabbedPane tabbedPane = new JTabbedPane();
		final String bankName = SORTER_FOR_BANK + StatsFrame.BANK_NAME;
		List<Integer> list = new ArrayList<>();
		mAggregatedAchievements.put(bankName, list);
		for (Color color : mAchievementColors)
		{
			list.add(0);
			mAverages.add(0.0);
			mStdDevs.add(0.0);
		}
		pGames.sort(new Comparator<Game>()
		{
			@Override
			public int compare(Game g1, Game g2)
			{
				return g1.getMoneySystem() < g2.getMoneySystem() ? 1 : -1;
			}
		});
		for (Game game : pGames)
		{
			mMoneyTypes.add(game.getMoneySystem() == Game.MONEY_DEBT ? "Monnaie-Dette" : "Monnaie Libre");
			for (Player player : game.getPlayers())
			{
				if (!mAggregatedAchievements.containsKey(player.getName()))
				{
					list = new ArrayList<>();
					mAggregatedAchievements.put(player.getName(), list);
					for (Color color : mAchievementColors)
						list.add(0);
				}
			}
			final ValuesStats panelWOBank = new ValuesStats();
			computeValues(game, panelWOBank, false, game.getMoneySystem() == Game.MONEY_DEBT ? -1 : 1);
			tabbedPane.add(game.getMoneySystem() == Game.MONEY_DEBT ? "Monnaie-Dette sans banque" : "Monnaie Libre", panelWOBank);
			if (game.getMoneySystem() == Game.MONEY_DEBT)
			{
				final ValuesStats panelWithBank = new ValuesStats();
				computeValues(game, panelWithBank, true, 0);
				tabbedPane.add("Avec la banque", panelWithBank);
				final HistoryStats historyStats = new HistoryStats(game);
				tabbedPane.add("Historique masse monétaire", historyStats);
			}
		}
		if (pGames.size() > 1)
		{
			final AggregatedStats aggrStats = new AggregatedStats();
			tabbedPane.addTab("Aggrégés", aggrStats);
		}

		tabbedPane.addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(KeyEvent pEvent)
			{
				if (pEvent.getKeyChar() == 'm')
				{
					final String value = JOptionPane.showInputDialog(StatsFrame.this, "Renseigner la valeur maxi", "Valeur max", JOptionPane.QUESTION_MESSAGE);
					try
					{
						int intVal = Integer.valueOf(value);
						mStatsPanels.get(tabbedPane.getSelectedIndex()).setMaxValue(intVal);
						repaint();
					}
					catch (NumberFormatException e)
					{
						JOptionPane.showMessageDialog(StatsFrame.this, "Valeur saisie non correcte", "Erreur", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			
			@Override
			public void keyReleased(KeyEvent pE)
			{
			}
			
			@Override
			public void keyPressed(KeyEvent pE)
			{
			}
		});
		getContentPane().add(tabbedPane);
	}

	private void computeValues(Game pGame, ValuesStats pValuesPanel, boolean pAddBank, int pFeedAggregate)
	{
		final String bankName = SORTER_FOR_BANK + BANK_NAME;
		final List<Event> events = new ArrayList<>();
		events.addAll(pGame.getEvents());
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
		int currentFactor = 1;
		for (Event event : events)
		{
			switch (event.getEvt())
			{
			case JOIN:
			case TURN:
			case NEW_CREDIT:
			case MM_CHANGE:
			case END:
				// Nothing to do here
				break;
			case CANNOT_PAY:
			case BANKRUPT:
			case INTEREST_ONLY:
			case PRISON:
			case REIMB_CREDIT:
				if (pAddBank)
				// All this goes to the bank
					addFromEvent(event, bankName, playerAchievements, currentFactor);
				break;
			case DEATH:
			case QUIT:
				addFromEvent(event, event.getPlayer().getName(), playerAchievements, currentFactor);
				break;
			case XTECHNOLOGICAL_BREAKTHROUGH:
				currentFactor *= 2;
				break;

			default:
				System.err.println("Unknown event type " + event.getEvt().toString());
				break;
			}
		}
		pValuesPanel.setPlayerAchievements(playerAchievements);
		if (pFeedAggregate != -1)
		{
			for (String playerName : playerAchievements.keySet())
			{
				final Integer value = playerAchievements.get(playerName);
				mAggregatedAchievements.get(playerName).set(pFeedAggregate, value);
				if (value > mAggregatedMax)
					mAggregatedMax = value;
			}
			mAverages.set(pFeedAggregate, pValuesPanel.getAverage());
			mStdDevs.set(pFeedAggregate, pValuesPanel.getStandardDeviation());
		}
	}

	public void addFromEvent(Event pEvent, final String pPlayerName, SortedMap<String, Integer> pPlayerAchievements, int pCurrentFactor)
	{
		final Integer integerValue = pPlayerAchievements.get(pPlayerName);
		final int currentValue = integerValue == null ? 0 : integerValue.intValue();
		pPlayerAchievements.put(pPlayerName, currentValue + pEvent.getPrincipal() + pEvent.getInterest() + (pEvent.getWeakCards() + 2 * pEvent.getMediumCards() + 4 * pEvent.getStrongCards()) * pCurrentFactor);
	}
}
