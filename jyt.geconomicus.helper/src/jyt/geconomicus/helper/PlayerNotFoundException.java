package jyt.geconomicus.helper;

public class PlayerNotFoundException extends Exception
{
	public PlayerNotFoundException(String pPlayerName)
	{
		super(pPlayerName);
	}
}
