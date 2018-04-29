package jyt.geconomicus.helper;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jyt.geconomicus.helper.Event.EventType;

/**
 * Used to convert from an EventType to a String that can be saved into the database and back to the EventType.
 * @author jytou
 */
@Converter
public class EventTypeConverter implements AttributeConverter<EventType, String>
{
	// Contains all event String representations
	private final static Map<String, EventType> sChar2Type = new HashMap<>();
	static
	{
		// We take the first character of every EventType as its representation, with the drawback that they all have to start with different characters but it's simple
		for (EventType evtType : EventType.values())
		{
			final String c = evtType.name().substring(0, 1);
			if (sChar2Type.containsKey(c))
				throw new RuntimeException("EventType " + evtType.toString() + " collides with event " + sChar2Type.get(c).toString() + ": they shouldn't start with the same letter for persistence"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sChar2Type.put(c, evtType);
		}
	}

	/**
	 * Check if pEvtType is a valid String representation for an event type.
	 * @param pEvtType
	 * @return
	 */
	public static boolean isValidEventType(final String pEvtType)
	{
		return sChar2Type.containsKey(pEvtType);
	}

	@Override
	public String convertToDatabaseColumn(final EventType pEvtType)
	{
		return pEvtType.name().substring(0, 1);
	}

	@Override
	public EventType convertToEntityAttribute(final String pEvtType)
	{
		return getEventType(pEvtType);
	}

	/**
	 * Returns the EventType corresponding to this String representation.
	 * @param pEvtType
	 * @return
	 */
	public static EventType getEventType(final String pEvtType)
	{
		return sChar2Type.get(pEvtType);
	}

}
