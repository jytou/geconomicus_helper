package jyt.geconomicus.helper;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

@Entity
public class Event implements Serializable
{
	// Use event types with different starting characters for persistence
	public enum EventType
	{
		// A player joins the game
		JOIN(Messages.getString("BaseMessage.Event.NewPlayer")), //$NON-NLS-1$
		// A turn just finished - initiate a new turn
		TURN(Messages.getString("BaseMessage.Event.NewTurn")), //$NON-NLS-1$
		// A player takes a new credit
		NEW_CREDIT(Messages.getString("BaseMessage.Event.NewCredit")), //$NON-NLS-1$
		// A player pays back only the interest
		INTEREST_ONLY(Messages.getString("BaseMessage.Event.ReimburseInterestOnly")), //$NON-NLS-1$
		// A player reimbursed his credit (partially or in full)
		REIMB_CREDIT(Messages.getString("BaseMessage.Event.ReimburseCredit")), //$NON-NLS-1$
		// A player is defaulting on his debt but can still play after having been seized
		CANNOT_PAY(Messages.getString("BaseMessage.Event.DefaultOk")), //$NON-NLS-1$
		// A player cannot pay and doesn't have enough cards to continue playing: he skis a turn
		BANKRUPT(Messages.getString("BaseMessage.Event.DefaultBankrupt")), //$NON-NLS-1$
		// A player doesn't have enough cards to cover his default: he goes to prison
		PRISON(Messages.getString("BaseMessage.Event.DefaultPrison")), //$NON-NLS-1$
		// A player quits the game (can be used in the middle of the game but also used to do the assessment at the end of the game)
		QUIT(Messages.getString("BaseMessage.Event.QuitGame")), //$NON-NLS-1$
		// The money mass changes unexpectedly (should mostly not happen at all - only for very exceptional cases)
		MM_CHANGE(Messages.getString("BaseMessage.Event.MoneyMassChange")), //$NON-NLS-1$
		// End of the game
		END(Messages.getString("BaseMessage.Event.EndGame")), //$NON-NLS-1$
		// A player dies - assessment of his possessions
		DEATH(Messages.getString("BaseMessage.Event.DeathRebirth")), //$NON-NLS-1$
		// A technological breakthrough. Note that this event MUST have a player attached to it: the player that caused the breakthrough
		XTECHNOLOGICAL_BREAKTHROUGH(Messages.getString("BaseMessage.Event.TechnologicalBreakthrough")), //$NON-NLS-1$
		// The bank invests money and/or cards
		SIDE_INVESTMENT(Messages.getString("BaseMessage.Event.BankInvestment")), //$NON-NLS-1$
		// Final assessment of the investments of the bank at the end of the game
		ASSESSMENT_FINAL(Messages.getString("BaseMessage.Event.BankAssessment")); //$NON-NLS-1$

		private String description;
		EventType(String pDescription)
		{
			description = pDescription;
		}

		public String getDescription()
		{
			return description;
		}
	}; 

	// IDs are generated automatically by EclipseLink.
	@TableGenerator(
		name="evtGen",
		table="ID_GEN",
		pkColumnName="GEN_KEY",
		valueColumnName="GEN_VALUE",
		pkColumnValue="EVT_ID",
		allocationSize=1
	)
	@XmlTransient
	@GeneratedValue(strategy=GenerationType.TABLE, generator="evtGen")
	@Id
	private Integer id;

	// The timestamp for this event
	@Temporal(TemporalType.TIMESTAMP)
	private Date tstamp;

	// The event type - translated into a String in the dabatase
	@Column
	@Convert(converter = EventTypeConverter.class)
	private EventType evt;

	// The game in which this event occurred
	@XmlIDREF
	@ManyToOne
	@JoinColumn(nullable=false)
	private Game game;

	// The player that triggered this event. It may be null if it is a game event.
	@XmlIDREF
	@JoinColumn(nullable=true)
	private Player player;

	// The two next are only for the debt-money system
	// The interest due/reimbursed by the player during this event
	private int interest = 0;
	// The principal of the credit due/reimbursed by the player during this event
	private int principal = 0;

	// The three next are only for the Free Currency system: the coins that are left when a player dies/quits the game
	private int weakCoins = 0;
	private int mediumCoins = 0;
	private int strongCoins = 0;
	// The cards that a player has left in his hands, or that are seized by the banker during a default.
	private int weakCards = 0;
	private int mediumCards = 0;
	private int strongCards = 0;

	// Used by EclipseLink to instantiate empty objects.
	@SuppressWarnings("unused")
	private Event()
	{
		super();
	}

	/**
	 * Creates a new event for this game
	 * @param pGame
	 * @param pEventType
	 * @param pPlayer can be <code>null</code> if the event is a global event (new turn, etc.)
	 */
	public Event(Game pGame, EventType pEventType, Player pPlayer)
	{
		super();
		game = pGame;
		evt = pEventType;
		player = pPlayer;
		game.addEvent(this);
		tstamp = new Date();
	}

	public EventType getEvt()
	{
		return evt;
	}

	public void setEvt(EventType pEvt)
	{
		evt = pEvt;
	}

	public Date getTstamp()
	{
		return tstamp;
	}

	public int getInterest()
	{
		return interest;
	}

	public void setInterest(int pInterest)
	{
		interest = pInterest;
	}

	public int getPrincipal()
	{
		return principal;
	}

	public void setPrincipal(int pPrincipal)
	{
		principal = pPrincipal;
	}

	public int getWeakCards()
	{
		return weakCards;
	}

	public void setWeakCards(int pWeakCardrs)
	{
		weakCards = pWeakCardrs;
	}

	public int getMediumCards()
	{
		return mediumCards;
	}

	public void setMediumCards(int pMediumCards)
	{
		mediumCards = pMediumCards;
	}

	public int getStrongCards()
	{
		return strongCards;
	}

	public void setStrongCards(int pStrongCards)
	{
		strongCards = pStrongCards;
	}

	@XmlTransient
	public Integer getId()
	{
		return id;
	}

	public void setPlayer(Player pPlayer)
	{
		player = pPlayer;
	}

	@XmlTransient
	public Player getPlayer()
	{
		return player;
	}

	/**
	 * Applies this event to the current game.<br>
	 * It adds seized values or interest gained, increments or decrements the money owed by a player,
	 * adjusts the current money mass, etc.
	 */
	public void applyEvent()
	{
		switch (evt)
		{
		case BANKRUPT:
		case PRISON:
		case CANNOT_PAY:
		case REIMB_CREDIT:
			// Money and/or cards get taken from a player
			game.seizeValues(weakCards, mediumCards, strongCards);
			game.gainInterest(interest);
			// and then it's just like quitting: don't break here!

		case QUIT:
		case DEATH:
			// A player has finished playing - do an inventory of what he has left
			// take that into account in the total money mass
			game.changeMoneyMass(-interest-principal-(weakCoins + 2 * mediumCoins + 4 * strongCoins) * game.getMoneyCardsFactor());
			if (EventType.REIMB_CREDIT.equals(evt))
			{
				player.setCurDebt(player.getCurDebt() - principal);
				player.setCurInterest(player.getCurInterest() - interest);
			}
			else
			// If it's anything else, the debt is wiped out
			{
				player.setCurDebt(0);
				player.setCurInterest(0);
			}
			player.setVisitedBank(true);
			if ((game.getMoneySystem() == Game.MONEY_LIBRE) && EventType.DEATH.equals(evt))
			// adjust money mass
				game.changeMoneyMass(8 * game.getMoneyCardsFactor());
			break;
		case INTEREST_ONLY:
			// The bank is grabbing some interest only
			game.gainInterest(interest);
			game.changeMoneyMass(-interest);
			player.setVisitedBank(true);
			break;
		case MM_CHANGE:
			game.changeMoneyMass(principal);
			break;
		case NEW_CREDIT:
			player.setCurDebt(player.getCurDebt() + principal);
			player.setCurInterest(player.getCurInterest() + interest);
			player.setVisitedBank(true);
			game.changeMoneyMass(principal);
			break;
		case JOIN:
			player.setActive(true);
			if (game.getMoneySystem() == Game.MONEY_LIBRE)
			// Add player's DU to money mass
				game.changeMoneyMass(7 * game.getMoneyCardsFactor());
			break;
		case TURN:
			// All players that have debt need to go to the bank
			for (Player player : game.getPlayers())
				if (player.getCurDebt() > 0)
					player.setVisitedBank(false);
			game.incTurnNumber();
			if (game.getMoneySystem() == Game.MONEY_LIBRE)
			// The money mass is going towards the average
			// Note that we don't have the actual data of how much money each player is giving away
			// We can deal with an average here.
			{
				int nbPlayers = 0;
				for (Player player : game.getPlayers())
					if (player.isActive())
						nbPlayers++;
				final int target = 7 * game.getMoneyCardsFactor() * nbPlayers;
				final int currentMM = game.getMoneyMass();
				game.changeMoneyMass((target - currentMM) / 2);
			}
			break;
		case END:
		case XTECHNOLOGICAL_BREAKTHROUGH:
			// Nothing to do here
			break;
		case SIDE_INVESTMENT:
			// The bank invests some money and cards
			game.investMoney(interest);
			game.investCards(weakCards + 2 * mediumCards + 4 * strongCards);
			break;
		case ASSESSMENT_FINAL:
			// The final assessment from the bank
			game.gainInterest(interest);
			game.seizeValues(weakCards, mediumCards, strongCards);
			break;
		default:
			// This should not happen
			throw new RuntimeException("Unexpected event " + evt.description); //$NON-NLS-1$
		}
		// This is a special case that cannot go into the switch
		if (EventType.QUIT.equals(evt))
			player.setActive(false);
	}

	@Override
	public String toString()
	{
		// To be used only in the CLI version
		return "#" + getId() + " - " + getTstamp().toString() + " - player " + (getPlayer() == null ? "" : " - player " + getPlayer().getName()) + " - " + getEvt().getDescription() + " - " + details();  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
	}

	/**
	 * This method should only be used in the CLI version.
	 * @return
	 */
	public String details()
	{
		final StringBuilder sb = new StringBuilder();
		if (principal > 0)
			sb.append("principal: ").append(principal); //$NON-NLS-1$

		if (interest > 0)
		{
			if (sb.length() > 0)
				sb.append(" - "); //$NON-NLS-1$
			sb.append("interest: ").append(interest); //$NON-NLS-1$
		}
		if (weakCoins + mediumCoins + strongCoins > 0)
		{
			if (sb.length() > 0)
				sb.append(" - "); //$NON-NLS-1$
			sb.append("had: ").append(weakCoins).append(", ").append(mediumCoins).append(", ").append(strongCoins).append(" (total: ").append(weakCoins + mediumCoins * 2 + strongCoins * 4).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		if (weakCards + mediumCards + strongCards > 0)
		{
			if (sb.length() > 0)
				sb.append(" - "); //$NON-NLS-1$
			sb.append("seized: ").append(weakCards).append(", ").append(mediumCards).append(", ").append(strongCards).append(" (total: ").append(weakCards + mediumCards * 2 + strongCards * 4).append(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		return sb.toString();
	}

	public int getWeakCoins()
	{
		return weakCoins;
	}

	public void setWeakCoins(int pWeakCoins)
	{
		weakCoins = pWeakCoins;
	}

	public int getMediumCoins()
	{
		return mediumCoins;
	}

	public void setMediumCoins(int pMediumCoins)
	{
		mediumCoins = pMediumCoins;
	}

	public int getStrongCoins()
	{
		return strongCoins;
	}

	public void setStrongCoins(int pStrongCoins)
	{
		strongCoins = pStrongCoins;
	}

	// This is for JAXB
	public void setTstamp(Date pTstamp)
	{
		tstamp = pTstamp;
	}

	/**
	 * Clones an event into another game. Useful for importing events into a game.
	 * @param pGame
	 * @return the cloned event for pGame
	 * @throws PlayerNotFoundException if no player with the same name is found
	 */
	public Event cloneFor(Game pGame) throws PlayerNotFoundException
	{
		Player otherPlayer = null;
		if (player != null)
		{
			boolean found = false;
			for (Player gamePlayer : pGame.getPlayers())
			{
				if (gamePlayer.getName().equals(player.getName()))
				{
					otherPlayer = gamePlayer;
					found = true;
					break;
				}
			}
			if (!found)
				throw new PlayerNotFoundException(player.getName());
		}
		final Event event = new Event(pGame, evt, otherPlayer);
		event.setInterest(interest);
		event.setPrincipal(principal);
		event.setTstamp(tstamp);
		event.setWeakCards(weakCards);
		event.setMediumCards(mediumCards);
		event.setStrongCards(strongCards);
		event.setWeakCoins(weakCoins);
		event.setMediumCoins(mediumCoins);
		event.setStrongCoins(strongCoins);
		return event;
	}
}
