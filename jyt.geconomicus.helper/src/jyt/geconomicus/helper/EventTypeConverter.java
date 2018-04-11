package jyt.geconomicus.helper;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import jyt.geconomicus.helper.Event.EventType;

@Converter
public class EventTypeConverter implements AttributeConverter<EventType, String>
{
	private final static Map<String, EventType> sChar2Type = new HashMap<>();
	static
	{
		for (EventType evtType : EventType.values())
		{
			final String c = evtType.name().substring(0, 1);
			if (sChar2Type.containsKey(c))
				throw new RuntimeException("EventType " + evtType.toString() + " collides with event " + sChar2Type.get(c).toString() + ": they shouldn't start with the same letter for persistence");
			sChar2Type.put(c, evtType);
		}
	}

	public static boolean isValidEventType(String pEvtType)
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

	public static EventType getEventType(final String pEvtType)
	{
		return sChar2Type.get(pEvtType);
	}

}
