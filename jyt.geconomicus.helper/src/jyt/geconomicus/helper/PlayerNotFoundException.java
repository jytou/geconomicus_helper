package jyt.geconomicus.helper;

/**
 * Thrown when a player with that name was not found.
 * @author jytou
 */
public class PlayerNotFoundException extends Exception
{
	public PlayerNotFoundException(String pPlayerName)
	{
		super(pPlayerName);
	}
}
