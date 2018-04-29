package jyt.geconomicus.helper;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class UIMessages
{
	private static final String BUNDLE_NAME = "jyt.geconomicus.helper.ui-messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private UIMessages()
	{
	}

	public static String getString(String key)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e)
		{
			return '!' + key + '!';
		}
	}
}
