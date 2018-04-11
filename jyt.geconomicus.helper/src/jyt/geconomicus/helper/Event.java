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
		JOIN("Nouveau joueur"),
		TURN("Nouveau tour"),
		NEW_CREDIT("Nouveau crédit"),
		INTEREST_ONLY("Remboursement des intérêts seuls"),
		REIMB_CREDIT("Remboursement de crédit"),
		CANNOT_PAY("Défaut de paiement : saisie de cartes"),
		BANKRUPT("Faillite personnelle : ne reste pas assez de cartes"),
		PRISON("Prison : pas assez de valeurs à saisir"),
		QUIT("Départ d'un joueur"),
		MM_CHANGE("Changement inattendu de masse monétaire"),
		END("Fin de partie"),
		DEATH("Mort/renaissance"),
		XTECHNOLOGICAL_BREAKTHROUGH("Rupture technologique"),
		SIDE_INVESTMENT("Investissement de banque"),
		ASSESSMENT_FINAL("Décompte final des avoirs de la banque");

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
	//@GeneratedValue(strategy=GenerationType.AUTO)
	@Id
	private Integer id;
	@Temporal(TemporalType.TIMESTAMP)
	private Date tstamp;
	@Column
	@Convert(converter = EventTypeConverter.class)
	private EventType evt;
//	@XmlInverseReference(mappedBy="events")
	@XmlIDREF
	@ManyToOne
	@JoinColumn(nullable=false)
	private Game game;
	@XmlIDREF
	@JoinColumn(nullable=true)
	private Player player;
	private int interest = 0;
	private int principal = 0;
	private int weakCoins = 0;
	private int mediumCoins = 0;
	private int strongCoins = 0;
	private int weakCards = 0;
	private int mediumCards = 0;
	private int strongCards = 0;

	@SuppressWarnings("unused")
	private Event()
	{
		super();
	}

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

	public void applyEvent()
	{
		switch (evt)
		{
		case BANKRUPT:
		case PRISON:
		case CANNOT_PAY:
		case REIMB_CREDIT:
			game.seizeValues(weakCards, mediumCards, strongCards);
			game.gainInterest(interest);
			// and then it's just like quitting
		case QUIT:
		case DEATH:
			game.changeMoneyMass(-interest-principal);
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
			break;
		case INTEREST_ONLY:
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
			break;
		case TURN:
			for (Player player : game.getPlayers())
				if (player.getCurDebt() > 0)
					player.setVisitedBank(false);
			game.incTurnNumber();
			break;
		case END:
		case XTECHNOLOGICAL_BREAKTHROUGH:
			// Nothing to do here
			break;
		case SIDE_INVESTMENT:
			game.investMoney(principal);
			game.investCards(weakCards + 2 * mediumCards + 4 * strongCards);
			break;
		case ASSESSMENT_FINAL:
			game.gainInterest(principal);
			game.seizeValues(weakCards, mediumCards, strongCards);
			break;
		default:
			throw new RuntimeException("Unexpected event " + evt.description);
		}
		if (EventType.QUIT.equals(evt))
			player.setActive(false);
	}

	@Override
	public String toString()
	{
		return "#" + getId() + " - " + getTstamp().toString() + " - player " + (getPlayer() == null ? "" : " - player " + getPlayer().getName()) + " - " + getEvt().getDescription() + " - " + details();
	}

	public String details()
	{
		final StringBuilder sb = new StringBuilder();
		if (principal > 0)
			sb.append("principal: ").append(principal);

		if (interest > 0)
		{
			if (sb.length() > 0)
				sb.append(" - ");
			sb.append("interest: ").append(interest);
		}
		if (weakCoins + mediumCoins + strongCoins > 0)
		{
			if (sb.length() > 0)
				sb.append(" - ");
			sb.append("had: ").append(weakCoins).append(", ").append(mediumCoins).append(", ").append(strongCoins).append(" (total: ").append(weakCoins + mediumCoins * 2 + strongCoins * 4).append(")");
		}
		if (weakCards + mediumCards + strongCards > 0)
		{
			if (sb.length() > 0)
				sb.append(" - ");
			sb.append("seized: ").append(weakCards).append(", ").append(mediumCards).append(", ").append(strongCards).append(" (total: ").append(weakCards + mediumCards * 2 + strongCards * 4).append(")");
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
