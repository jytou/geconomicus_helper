package jyt.geconomicus.helper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.TestCase;
import jyt.geconomicus.helper.Event.EventType;

public class CreateGameTestCase extends TestCase
{
	public void testCreateGame()
	{
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("geco");
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		Game game = new Game(Game.MONEY_DEBT, 10, "toto", "toto@titi.com", "", "today", "Here", 1);
		Player player1 = new Player(game, "Player1");
		Player player2 = new Player(game, "Player2");
		Event event1 = new Event(game, EventType.JOIN, player1);
		event1.applyEvent();
		Event event2 = new Event(game, EventType.JOIN, player2);
		event2.applyEvent();
		Event event3 = new Event(game, EventType.TURN, null);
		event3.applyEvent();
		Event event4 = new Event(game, EventType.NEW_CREDIT, player1);
		event4.setInterest(1);
		event4.setPrincipal(3);
		event4.applyEvent();
		em.persist(game);
		em.getTransaction().commit();
		em.close();
	}
}
