package jyt.geconomicus.helper;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.persistence.oxm.annotations.XmlInverseReference;

@Entity
public class Player implements Serializable
{
	@TableGenerator(
		name="playerGen",
		table="ID_GEN",
		pkColumnName="GEN_KEY",
		valueColumnName="GEN_VALUE",
		pkColumnValue="PLAYER_ID",
		allocationSize=1
	)
	@XmlTransient
	@GeneratedValue(strategy=GenerationType.TABLE, generator="playerGen")
	//@GeneratedValue(strategy=GenerationType.AUTO)
	@Id
	private Integer id;
	//@XmlInverseReference(mappedBy="players")
	@XmlIDREF
	@ManyToOne
	@JoinColumn(nullable=false)
	private Game game;
	@XmlID
	private String name;
	private boolean active;
	private int curDebt;
	private int curInterest;
	private boolean visitedBank;

	@SuppressWarnings("unused")
	private Player()
	{
		super();
	}

	public Player(Game pGame, String pName)
	{
		super();
		game = pGame;
		game.addPlayer(this);
		name = pName;
		active = false;
		curDebt = 0;
		curInterest = 0;
		visitedBank = true;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean pActive)
	{
		active = pActive;
	}

	public int getCurDebt()
	{
		return curDebt;
	}

	public void setCurDebt(int pCurDebt)
	{
		curDebt = pCurDebt;
	}

	public int getCurInterest()
	{
		return curInterest;
	}

	public void setCurInterest(int pCurInterest)
	{
		curInterest = pCurInterest;
	}

	@XmlTransient
	public String getName()
	{
		return name;
	}

	public Integer getId()
	{
		return id;
	}

	public boolean hasVisitedBank()
	{
		return visitedBank;
	}

	public boolean isVisitedBank()
	{
		return visitedBank;
	}

	public void setVisitedBank(boolean pVisitedBank)
	{
		visitedBank = pVisitedBank;
	}

	@Override
	public String toString()
	{
		return "#" + getId() + " - " + getName() + (isActive() ? (getCurDebt() > 0 ? " - debt " + getCurDebt() + " - interest " + getCurInterest() : "") : " INACTIVE");
	}

	public void setName(String pName)
	{
		name = pName;
	}
}
