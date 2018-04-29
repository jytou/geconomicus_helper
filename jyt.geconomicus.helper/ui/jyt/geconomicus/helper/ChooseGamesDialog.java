package jyt.geconomicus.helper;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

public class ChooseGamesDialog extends JFrame
{
	private static final String OPEN_GAME_ACTION = "openGame"; //$NON-NLS-1$
	private boolean mChosen = false;
	private List<Game> mGames;
	private class GamesTableModel extends AbstractTableModel
	{
		private final String[] COL_NAMES = new String[]
		{
			UIMessageKeyProvider.GAME_LOCATION_LABEL.getMessage(),
			UIMessageKeyProvider.GAME_DATE_LABEL.getMessage(),
			UIMessageKeyProvider.GAME_ANIMATOR_PSEUDO_LABEL.getMessage(),
			UIMessageKeyProvider.GAME_MONEY_TYPE_LABEL.getMessage(),
			UIMessageKeyProvider.GAME_DESCRIPTION_LABEL.getMessage()
		};

		@Override
		public int getRowCount()
		{
			return mGames.size();
		}

		@Override
		public int getColumnCount()
		{
			return COL_NAMES.length;
		}

		@Override
		public String getColumnName(int pColumn)
		{
			return COL_NAMES[pColumn];
		}

		@Override
		public Object getValueAt(int pRowIndex, int pColumnIndex)
		{
			Game game = mGames.get(pRowIndex);
			switch (pColumnIndex)
			{
			case 0:
				return game.getLocation();
			case 1:
				return game.getCurdate();
			case 2:
				return game.getAnimatorPseudo();
			case 3:
				return game.getMoneySystem() == Game.MONEY_DEBT ? UIMessageKeyProvider.GENERAL_DEBT_MONEY.getMessage() : UIMessageKeyProvider.GENERAL_LIBRE_CURRENCY.getMessage();
			case 4:
				return game.getDescription();
			}
			// This should never happen
			return "ERROR"; //$NON-NLS-1$
		}
	}

	public static void main(String[] args) throws IOException
	{
		final EntityManagerFactory factory = Persistence.createEntityManagerFactory(HelperUI.DB_DEFAULT_NAME);
		final EntityManager entityManager = factory.createEntityManager();
		new ChooseGamesDialog(entityManager, factory).setVisible(true);
	}

	public ChooseGamesDialog(final EntityManager pEntityManager, final EntityManagerFactory pFactory) throws IOException
	{
		super(UIMessages.getString("ChooseGamesDialog.Title.ChooseGamesToCompare")); //$NON-NLS-1$
		setIconImage(ImageIO.read(HelperUI.class.getResourceAsStream("/geconomicus_stats.png"))); //$NON-NLS-1$
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
		Dimension size = new Dimension(800, 600);
		setSize(size);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width / 2 - size.width/2, screenSize.height / 2 - size.height/2);
		final JPanel mainPanel = new JPanel(new GridBagLayout());
		final Insets insets = new Insets(0, 0, 0, 0);
		final JButton openGamesButton = new JButton(UIMessages.getString("ChooseGamesDialog.Button.Label.OpenGames")); //$NON-NLS-1$
		mainPanel.add(openGamesButton, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		feedGames(pEntityManager);
		final JTable gamesTable = new JTable(new GamesTableModel());
		gamesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent pEvent)
			{
				openGamesButton.setEnabled(gamesTable.getSelectedRows().length > 0);
			}
		});
		mainPanel.add(new JScrollPane(gamesTable), new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
		final Action openGameAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				final List<Game> selectedGames = new ArrayList<>();
				final int[] selectedRows = gamesTable.getSelectedRows();
				for (int i = 0; i < selectedRows.length; i++)
					selectedGames.add(mGames.get(selectedRows[i]));
				try
				{
					new StatsFrame(selectedGames).setVisible(true);
				}
				catch (IOException e)
				{
					return;
				}
				pEntityManager.close();
				pFactory.close();
				mChosen = true;
				setVisible(false);
			}
		};
		openGamesButton.setEnabled(false);
		openGamesButton.addActionListener(openGameAction);
		openGamesButton.setMnemonic('O');
		openGamesButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), ChooseGamesDialog.OPEN_GAME_ACTION);
		openGamesButton.getActionMap().put(ChooseGamesDialog.OPEN_GAME_ACTION, openGameAction);
		getContentPane().add(mainPanel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	@SuppressWarnings("unchecked")
	public void feedGames(final EntityManager pEntityManager)
	{
		mGames = pEntityManager.createNamedQuery("Game.findAll").getResultList(); //$NON-NLS-1$
	}
}
