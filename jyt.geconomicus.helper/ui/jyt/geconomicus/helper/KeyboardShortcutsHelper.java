package jyt.geconomicus.helper;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import jyt.geconomicus.helper.HelperUI.ActionCommand;

public class KeyboardShortcutsHelper extends JFrame
{
	private static final int DEFAULT_WIDTH = 500;
	private static final int DEFAULT_HEIGHT = 200;
	private HelperUI mHelperUI;

	public KeyboardShortcutsHelper(HelperUI pHelperUI, int pMoneySystem) throws IOException
	{
		super(UIMessages.getString("KeyboardShortcutsHelper.Main.Title")); //$NON-NLS-1$
		setIconImage(ImageIO.read(HelperUI.class.getResourceAsStream("/geconomicus.png"))); //$NON-NLS-1$
		mHelperUI = pHelperUI;
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent pEvent)
			{
				super.windowClosing(pEvent);
				mHelperUI.closedKeyboardShortcutHelper();
			}
		});
		setAlwaysOnTop(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setBounds(pHelperUI.getX() + pHelperUI.getWidth() - DEFAULT_WIDTH - 20, pHelperUI.getY() + pHelperUI.getHeight() - DEFAULT_HEIGHT - 50, DEFAULT_WIDTH, DEFAULT_HEIGHT);

		final StringBuilder sb = new StringBuilder();
		sb.append("<html><ul>"); //$NON-NLS-1$
		final SortedMap<String, String> shortCutToDescr = new TreeMap<>();
		for (ActionCommand action : HelperUI.ActionCommand.values())
			if (action.getMnemo().length() == 1)
				shortCutToDescr.put(action.getMnemo(), action.getDescription());
		for (String shortcut : shortCutToDescr.keySet())
			sb.append("<li>").append(shortcut).append(": ").append(shortCutToDescr.get(shortcut)).append("</li>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sb.append("<li>").append(UIMessages.getString("KeyboardShortcutsHelper.F2PlayerRename")).append("</li>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sb.append("<li>").append(UIMessages.getString("KeyboardShortcutsHelper.F2EventChangeDate")).append("</li>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		sb.append("</ul></html>"); //$NON-NLS-1$
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JScrollPane(new JLabel(sb.toString())));
	}
}
