package jyt.geconomicus.helper;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import jyt.geconomicus.helper.Event.EventType;

public class GeconomicusHelper
{

	public static void main(String[] args)
	{
		if (args.length == 0)
			usage();

		final EntityManagerFactory factory = Persistence.createEntityManagerFactory("geco"); //$NON-NLS-1$
		final EntityManager em = factory.createEntityManager();
		final String command = args[0].toLowerCase();
		if ("list".equals(command)) //$NON-NLS-1$
		{
			@SuppressWarnings("unchecked")
			final List<Game> games = em.createNamedQuery("Game.findAll").getResultList(); //$NON-NLS-1$
			for (Game game : games)
				System.out.println(game.toString());
		}
		else if ("new".equals(command)) //$NON-NLS-1$
		{
			if (args.length != 3)
			{
				System.err.println("New game needs a date and location"); //$NON-NLS-1$
				usage();
			}
			final String curDate = args[1];
			final String location = args[2];
			em.getTransaction().begin();
			final Game game = new Game(Game.MONEY_DEBT, 10, "jytou", "jytou@jytou.geconomicus", "", curDate, location, 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			em.persist(game);
			em.getTransaction().commit();
			System.out.println("Created new game " + game.toString()); //$NON-NLS-1$
		}
		else if ("use".equals(command)) //$NON-NLS-1$
		{
			if (args.length < 3)
			{
				System.err.println("USE needs more arguments"); //$NON-NLS-1$
				usage();
			}
			final int gameId = Integer.valueOf(args[1]);
			final Game game = em.find(Game.class, gameId);
			final String subCmd = args[2].toLowerCase();
			if ("status".equals(subCmd)) //$NON-NLS-1$
			{
				if (args.length == 4)
				{
					final Player player = em.find(Player.class, Integer.valueOf(args[3]));
					if (player == null)
					{
						System.err.println("Player " + args[3] + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
						usage();
					}
					// history of the player
					for (Event event : game.getEvents())
					{
						if (player == event.getPlayer())
							System.out.println(event.getEvt().getDescription() + " - " + event.details()); //$NON-NLS-1$
					}
					System.out.println(player.toString());
				}
				else
				// simple status of the whole game
				{
					System.out.println(game.toString());
					System.out.println(" ***** PLAYERS ******"); //$NON-NLS-1$
					for (Player player : game.getPlayers())
						System.out.println("\t" + player.toString()); //$NON-NLS-1$
				}
			}
			else if ("history".equals(subCmd)) //$NON-NLS-1$
			{
				for (Event event : game.getEvents())
					System.out.println(event);
			}
			else if ("create".equals(subCmd)) //$NON-NLS-1$
			{
				if (args.length < 4)
				{
					System.err.println("Subcommand CREATE needs a player name"); //$NON-NLS-1$
					usage();
				}
				final StringBuilder sb = new StringBuilder();
				for (int i = 3; i < args.length; i++)
				{
					sb.append(args[i]);
					sb.append(" "); //$NON-NLS-1$
				}
				final String playerName = sb.toString().trim();
				em.getTransaction().begin();
				final Player player = new Player(game, playerName);
				em.persist(player);
				final Event event = new Event(game, EventType.JOIN, player);
				event.applyEvent();
				em.persist(event);
				em.getTransaction().commit();
				System.out.println("Created player " + player.toString()); //$NON-NLS-1$
			}
			else if ("event".equals(subCmd)) //$NON-NLS-1$
			{
				if (args.length < 4)
				{
					System.err.println("Subcommand EVENT needs an event type"); //$NON-NLS-1$
					usage();
				}
				final String evtTypeString = args[3];
				if (!EventTypeConverter.isValidEventType(evtTypeString))
				{
					System.err.println("Event type " + evtTypeString + " is unknown");  //$NON-NLS-1$//$NON-NLS-2$
					usage();
				}
				Player player = null;
				if (!EventType.TURN.name().substring(0, 1).equals(evtTypeString))
				{
					if (args.length < 5)
					{
						System.err.println("Event type " + evtTypeString + " needs a player"); //$NON-NLS-1$ //$NON-NLS-2$
						usage();
					}
					player = em.find(Player.class, Integer.valueOf(args[4]));
					if (player == null)
					{
						System.err.println("Player " + args[4] + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
						usage();
					}
				}
				em.getTransaction().begin();
				final Event event = new Event(game, EventTypeConverter.getEventType(evtTypeString), player);
				if (game.getMoneySystem() == Game.MONEY_DEBT)
				{
					if (args.length > 5)
						event.setInterest(Integer.valueOf(args[5]));
					if (args.length > 6)
						event.setPrincipal(Integer.valueOf(args[6]));
					if (args.length > 7)
						event.setWeakCards(Integer.valueOf(args[7]));
					if (args.length > 8)
						event.setMediumCards(Integer.valueOf(args[8]));
					if (args.length > 9)
						event.setStrongCards(Integer.valueOf(args[9]));
				}
				else
				{
					if (args.length > 5)
						event.setWeakCoins(Integer.valueOf(args[5]));
					if (args.length > 6)
						event.setMediumCoins(Integer.valueOf(args[6]));
					if (args.length > 7)
						event.setStrongCoins(Integer.valueOf(args[7]));
					if (args.length > 8)
						event.setWeakCards(Integer.valueOf(args[8]));
					if (args.length > 9)
						event.setMediumCards(Integer.valueOf(args[9]));
					if (args.length > 10)
						event.setStrongCards(Integer.valueOf(args[10]));
				}
				event.applyEvent();
				em.getTransaction().commit();
				System.out.println("Created event " + event.toString()); //$NON-NLS-1$
			}
			else
			{
				System.err.println("Unknown subcommand " + subCmd + " for command USE"); //$NON-NLS-1$ //$NON-NLS-2$
				usage();
			}
		}
		else if ("del".equals(command)) //$NON-NLS-1$
		{
			if (args.length < 2)
			{
				System.err.println("DEL needs more arguments"); //$NON-NLS-1$
				usage();
			}
			final int gameId = Integer.valueOf(args[1]);
			final Game game = em.find(Game.class, gameId);
			em.getTransaction().begin();
			em.remove(game);;
			em.getTransaction().commit();
			System.out.println("Deleted game #" + args[1]); //$NON-NLS-1$
		}
		else
		{
			System.err.println("Unknown command " + command); //$NON-NLS-1$
			usage();
		}
		em.close();
	}

	private static void usage()
	{
		System.out.println("Usage:"); //$NON-NLS-1$
		System.out.println("list: list all games"); //$NON-NLS-1$
		System.out.println("new <date> <location>: creates a game at the given date and location"); //$NON-NLS-1$
		System.out.println("use <game-id>"); //$NON-NLS-1$
		System.out.println("\tstatus [<player-id>]: the current status of the whole game or of a single player"); //$NON-NLS-1$
		System.out.println("\tcreate <player-name>: create a new player in the game and make him join (the join event is automatically created)"); //$NON-NLS-1$
		System.out.println("\tevent <event-type-code> [<player-id> [[<interest> [<principal>]|[low-medium-high coins] [low - medium - high cards]]]]"); //$NON-NLS-1$
		System.out.println("\thistory: all events in the game"); //$NON-NLS-1$
		System.out.println("\tdel <game-id> to be used only when you know what you are doing!"); //$NON-NLS-1$
		System.out.println();
		System.out.println("List of event codes:"); //$NON-NLS-1$
		for (EventType eventType : EventType.values())
			System.out.println("\t" + eventType.name().toLowerCase().charAt(0) + " : " + eventType.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$
		System.exit(1);
	}

}
