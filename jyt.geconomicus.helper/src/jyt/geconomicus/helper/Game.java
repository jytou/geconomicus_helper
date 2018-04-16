package jyt.geconomicus.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
@Entity
@NamedQuery(name="Game.findAll", query="SELECT c FROM Game c order by c.id") 
public class Game implements Serializable
{
	@TableGenerator(
		name="gameGen",
		table="ID_GEN",
		pkColumnName="GEN_KEY",
		valueColumnName="GEN_VALUE",
		pkColumnValue="GAME_ID",
		allocationSize=1
	)
	@XmlTransient
	@GeneratedValue(strategy=GenerationType.TABLE, generator="gameGen")
	//@GeneratedValue(strategy=GenerationType.AUTO)
	@Id
	private Integer id;
	private String animatorPseudo;
	private String animatorEmail;
	@Lob
	private String description;
	public final static int MONEY_LIBRE = 0;
	public final static int MONEY_DEBT = 1;
	private int moneySystem;
	private int nbTurnsPlanned;
	private int turnNumber;
	private int moneyMass;
	private int seizedValues;
	private int interestGained;
	private int cardsInvestBank;
	private int moneyInvestBank;
	private Integer moneyCardsFactor;
	@XmlID
	private String curdate;
	private String location;
	@XmlElementWrapper
	@XmlElement(name="player")
	@OneToMany(mappedBy="game",cascade={CascadeType.PERSIST, CascadeType.REMOVE},orphanRemoval=true)
	@OrderBy("active,name")
	private Collection<Player> players = new ArrayList<Player>();
	@XmlElementWrapper
	@XmlElement(name="event")
	@OneToMany(mappedBy="game",cascade={CascadeType.PERSIST, CascadeType.REMOVE},orphanRemoval=true)
	@OrderBy("tstamp")
	private Collection<Event> events = new ArrayList<Event>();

	@SuppressWarnings("unused")
	private Game()
	{
		super();
	}

	public Game(int pMoneySystem, int pNbTurnsPlanned, String pAnimatorPseudo, String pAnimatorEmail, String pDescription, String pCurDate, String pLocation, int pMoneyCardsFactor)
	{
		super();
		moneySystem = pMoneySystem;
		nbTurnsPlanned = pNbTurnsPlanned;
		animatorPseudo = pAnimatorPseudo;
		animatorEmail = pAnimatorEmail;
		description = pDescription;
		curdate = pCurDate;
		location = pLocation;
		turnNumber = 0;
		moneyCardsFactor = pMoneyCardsFactor;
	}

	@XmlTransient
	public Integer getId()
	{
		return id;
	}

	public int getTurnNumber()
	{
		return turnNumber;
	}

	public void incTurnNumber()
	{
		turnNumber++;
	}

	public void setId(Integer pId)
	{
		id = pId;
	}

	@XmlTransient
	public String getCurdate()
	{
		return curdate;
	}

	public void setCurdate(String pCurdate)
	{
		curdate = pCurdate;
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String pLocation)
	{
		location = pLocation;
	}

	public void addPlayer(Player pPlayer)
	{
		players.add(pPlayer);
	}

	public void addEvent(Event pEvent)
	{
		events.add(pEvent);
	}

	public Collection<Player> getPlayers()
	{
		return players;
	}

	public Collection<Event> getEvents()
	{
		return events;
	}

	public int getMoneyMass()
	{
		return moneyMass;
	}

	public int getInterestGained()
	{
		return interestGained;
	}

	public void gainInterest(int pInterest)
	{
		interestGained += pInterest;
	}

	public void seizeValues(int pLow, int pMedium, int pStrong)
	{
		seizedValues += pLow + 2 * pMedium + 4 * pStrong;
	}

	public int getSeizedValues()
	{
		return seizedValues;
	}

	public void changeMoneyMass(int pChange)
	{
		moneyMass += pChange;
	}

	public String getAnimatorPseudo()
	{
		return animatorPseudo;
	}

	public String getAnimatorEmail()
	{
		return animatorEmail;
	}

	public String getDescription()
	{
		return description;
	}

	@Override
	public String toString()
	{
		return "#" + getId() +
		       " - " + getCurdate() +
		       " - " + getLocation() +
		       " \nanimated by " + getAnimatorPseudo() + " (" + getAnimatorEmail() + ")" +
		       " in " + (moneySystem == MONEY_DEBT ? "Debt-Money" : "Libre Money") +
		       " \nturn " + getTurnNumber() + " / " + getNbTurnsPlanned() +
		       " - MM " + getMoneyMass() +
		       " - gained " + getInterestGained() +
		       " - seized " + getSeizedValues();
	}

	public void recomputeAll(IEventListener pEventListener)
	{
		for (Player player : players)
		{
			player.setCurDebt(0);
			player.setCurInterest(0);
			player.setActive(false);
			player.setVisitedBank(true);
		}
		interestGained = 0;
		moneyMass = 0;
		seizedValues = 0;
		turnNumber = 0;
		for (Event event : events)
		{
			event.applyEvent();
			if (pEventListener != null)
				pEventListener.eventApplied(event);
		}
	}

	public int getMoneySystem()
	{
		return moneySystem;
	}

	public int getNbTurnsPlanned()
	{
		return nbTurnsPlanned;
	}

	public int getCardsInvestBank()
	{
		return cardsInvestBank;
	}

	public void setCardsInvestBank(int pCardsInvestBank)
	{
		cardsInvestBank = pCardsInvestBank;
	}

	public int getMoneyInvestBank()
	{
		return moneyInvestBank;
	}

	public void setMoneyInvestBank(int pMoneyInvestBank)
	{
		moneyInvestBank = pMoneyInvestBank;
	}

	public void removeEvent(Event pToUndo, boolean pRecompute)
	{
		events.remove(pToUndo);
		if (pRecompute)
			recomputeAll(null);
	}

	public void investMoney(int pPrincipal)
	{
		moneyMass += pPrincipal;
		moneyInvestBank += pPrincipal;
		interestGained -= pPrincipal;
	}

	public void investCards(int pTotal)
	{
		cardsInvestBank += pTotal;
		seizedValues -= pTotal;
	}

	public int getMoneyCardsFactor()
	{
		if (moneyCardsFactor == null)
			moneyCardsFactor = 1;// default
		return moneyCardsFactor;
	}

	// These are only for JAXB
	public void setAnimatorEmail(String pAnimatorEmail)
	{
		animatorEmail = pAnimatorEmail;
	}
	public void setAnimatorPseudo(String pAnimatorPseudo)
	{
		animatorPseudo = pAnimatorPseudo;
	}
	public void setDescription(String pDescription)
	{
		description = pDescription;
	}
	public void setInterestGained(int pInterestGained)
	{
		interestGained = pInterestGained;
	}
	public void setMoneyMass(int pMoneyMass)
	{
		moneyMass = pMoneyMass;
	}
	public void setMoneySystem(int pMoneySystem)
	{
		moneySystem = pMoneySystem;
	}
	public void setNbTurnsPlanned(int pNbTurnsPlanned)
	{
		nbTurnsPlanned = pNbTurnsPlanned;
	}
	public void setSeizedValues(int pSeizedValues)
	{
		seizedValues = pSeizedValues;
	}
	public void setTurnNumber(int pTurnNumber)
	{
		turnNumber = pTurnNumber;
	}

	// Should normally not be called
	public void removePlayer(Player pPlayer)
	{
		players.remove(pPlayer);
	}
	public void setMoneyCardsFactor(int pMoneyCardsFactor)
	{
		moneyCardsFactor = pMoneyCardsFactor;
	}
}
