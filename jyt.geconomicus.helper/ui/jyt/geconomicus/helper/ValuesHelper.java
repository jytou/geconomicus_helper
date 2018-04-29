package jyt.geconomicus.helper;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;

import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ValuesHelper extends JFrame
{
	private HelperUI mHelperUI;

	private Color mWeakValueColor = new Color(78, 190, 130);// green
	private Color mMediumValueColor = new Color(102, 62, 184);// purple
	private Color mStrongValueColor = new Color(247, 229, 31);// yellow
	private Color mWaitingColor = Color.white;

	// to draw a nice curved arrow, fill a V shape rather than stroking it
	// with lines
	public void drawArrow(Graphics2D g, float pSize, float pStrokeSize)
	{
		Stroke oldStroke = g.getStroke();
        g.setStroke ( new BasicStroke ( pStrokeSize, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER ) );

        // as we're filling rather than stroking, control point is at the
		// apex,

		float arrowRatio = 0.5f;
		float arrowLength = pStrokeSize * 5;

		BasicStroke stroke = (BasicStroke)g.getStroke();

		float endX = pSize;

		float veeX = endX - stroke.getLineWidth() * 0.5f / arrowRatio;

		// vee
		Path2D.Float path = new Path2D.Float();

		float waisting = 0.5f;

		float waistX = endX - arrowLength * 0.5f;
		float waistY = arrowRatio * arrowLength * 0.5f * waisting;
		float arrowWidth = arrowRatio * arrowLength;

		path.moveTo(veeX - arrowLength, -arrowWidth);
		path.quadTo(waistX, -waistY, endX, 0.0f);
		path.quadTo(waistX, waistY, veeX - arrowLength, arrowWidth);

		// end of arrow is pinched in
		path.lineTo(veeX - arrowLength * 0.75f, 0.0f);
		path.lineTo(veeX - arrowLength, -arrowWidth);

		g.fill(path);

		// move stem back a bit
		g.draw(new Line2D.Float(0.0f, 0.0f, veeX - arrowLength * 0.5f, 0.0f));

		g.setStroke(oldStroke);
	}

	private class MoneyHelperPanel extends JPanel
	{
		private final static int SPACE_PERCENT_HORIZ = 25;
		private final static int SPACE_PERCENT_VERT = 50;
		private final static int TEXT_DISTANCE = 5;

		public MoneyHelperPanel()
		{
			super();
			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent pEvent)
				{
					super.mouseClicked(pEvent);
					final int w = getWidth();
					final int h = getHeight();
					final int spacew = (int)(0.01 * w * SPACE_PERCENT_HORIZ / 4);
					final int cardw = (w - spacew * 4) / 3;
					final int spaceh = (int)(0.01 * h * SPACE_PERCENT_VERT / 3);
					final int cardh = (h - spaceh * 3) / 2;
					final int cx = pEvent.getX();
					final int cy = pEvent.getY();
					if ((cy >= spaceh) && (cy <= spaceh + cardh))
					// On the first row
					{
						if ((cx >= spacew) && (cx <= spacew + cardw))
						{
							mWeakValueColor = chooseNewColor(UIMessages.getString("Frame.ValuesHelper.WeakCardsColor.Label"), mWeakValueColor); //$NON-NLS-1$
							repaint();
							return;
						}
						else if ((cx >= spacew * 2 + cardw) && (cx <= spacew * 2 + cardw * 2))
						{
							mMediumValueColor = chooseNewColor(UIMessages.getString("Frame.ValuesHelper.MediumCardsColor.Label"), mMediumValueColor); //$NON-NLS-1$
							repaint();
							return;
						}
						else if ((cx >= spacew * 3 + cardw * 2) && (cx <= spacew * 3 + cardw * 3))
						{
							mStrongValueColor = chooseNewColor(UIMessages.getString("Frame.ValuesHelper.StrongCardsColor.Label"), mStrongValueColor); //$NON-NLS-1$
							repaint();
							return;
						}
					}
					else if ((cy >= spaceh * 2 + cardh) && (cy <= spaceh * 2 + cardh * 2))
					// On the second row
					{
						if ((cx >= spacew * 2 + cardw) && (cx <= spacew * 2 + cardw * 2))
						{
							mWaitingColor = chooseNewColor(UIMessages.getString("Frame.ValuesHelper.WaitingCardsColor.Label"), mWaitingColor); //$NON-NLS-1$
							repaint();
							return;
						}
					}
					rotateValues();
				}

				public Color chooseNewColor(final String pValueType, final Color pOriginalColor)
				{
					final Color color = JColorChooser.showDialog(ValuesHelper.this, MessageFormat.format(UIMessages.getString("Frame.ValuesHelper.Title.ChooseAColorForValue"), pValueType), pOriginalColor); //$NON-NLS-1$
					if (color != null)
						return color;
					else
						return pOriginalColor;
				}
			});
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			final Graphics2D g2 = (Graphics2D)g;
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        final int w = getWidth();
			final int h = getHeight();
			final int spacew = (int)(0.01 * w * SPACE_PERCENT_HORIZ / 4);
			final int cardw = (w - spacew * 4) / 3;
			final int spaceh = (int)(0.01 * h * SPACE_PERCENT_VERT / 3);
			final int cardh = (h - spaceh * 3) / 2;
			final double fontSize = 0.02 * w;
			g.setFont(getFont().deriveFont((float)fontSize));
			g.setColor(Color.white);
			g.fillRect(0, 0, w, h);
			g.setColor(mWeakValueColor);
			g.fillRect(spacew, spaceh, cardw, cardh);
			g.setColor(mMediumValueColor);
			g.fillRect(spacew * 2 + cardw, spaceh, cardw, cardh);
			g.setColor(mStrongValueColor);
			g.fillRect(spacew * 3 + cardw * 2, spaceh, cardw, cardh);
			g.setColor(mWaitingColor);
			g.fillRect(spacew * 2 + cardw, spaceh * 2 + cardh, cardw, cardh);
			g.setColor(Color.black);
			g.drawRect(spacew, spaceh, cardw, cardh);
			g.drawRect(spacew * 2 + cardw, spaceh, cardw, cardh);
			g.drawRect(spacew * 3 + cardw * 2, spaceh, cardw, cardh);
			g.drawRect(spacew * 2 + cardw, spaceh * 2 + cardh, cardw, cardh);
			drawString(g2, UIMessages.getString("General.Cards.Weak"), spacew + cardw / 2, spaceh, 0, -1); //$NON-NLS-1$
			drawString(g2, UIMessages.getString("Frame.ValuesHelper.WeakCardValue.Label"), spacew + cardw / 2, spaceh + cardh / 2, 0, 0); //$NON-NLS-1$
			drawString(g2, UIMessages.getString("General.Cards.Medium"), spacew * 2 + 3 * cardw / 2, spaceh, 0, -1); //$NON-NLS-1$
			drawString(g2, UIMessages.getString("Frame.ValuesHelper.MediumCardValue.Label"), spacew * 2 + 3 * cardw / 2, spaceh + cardh / 2, 0, 0); //$NON-NLS-1$
			drawString(g2, UIMessages.getString("General.Cards.Strong"), spacew * 3 + 5 * cardw / 2, spaceh, 0, -1); //$NON-NLS-1$
			drawString(g2, UIMessages.getString("Frame.ValuesHelper.StrongCardValue.Label"), spacew * 3 + 5 * cardw / 2, spaceh + cardh / 2, 0, 0); //$NON-NLS-1$
			drawString(g2, UIMessages.getString("Frame.ValuesHelper.WaitingMoney.Label"), spacew * 2 + 3 * cardw / 2, spaceh * 2 + cardh, 0, -1); //$NON-NLS-1$
			drawString(g2, UIMessages.getString("Frame.ValuesHelper.WaitingCardValue.Label"), spacew * 2 + 3 * cardw / 2, spaceh * 2 + 3 * cardh / 2, 0, 0); //$NON-NLS-1$
			AffineTransform oldTransform = g2.getTransform();
			AffineTransform turn = new AffineTransform();
			turn.rotate(Math.PI);
			turn.translate(-spacew*2-cardw, -spaceh-cardh/2);
			g2.transform(turn);
			drawArrow(g2, spacew, spacew/10);

			turn = new AffineTransform();
			turn.translate(-cardw-spacew, 0);
			g2.transform(turn);
			drawArrow(g2, spacew, spacew/10);

			turn = new AffineTransform();
			turn.translate(spacew+5*cardw/3, -spaceh/2-cardh/2);
			turn.rotate(6*Math.PI/5);
			g2.transform(turn);
			drawArrow(g2, spacew*3, spacew/10);

			turn = new AffineTransform();
			turn.rotate(-Math.PI/5);
			turn.translate(spacew/2+5*cardw/3, cardh/2);
			turn.rotate(-Math.PI/5);
			g2.transform(turn);
			drawArrow(g2, spacew*3, spacew/10);

			g2.setTransform(oldTransform);
		}

		private void drawString(Graphics2D g2, String pString, int pXpos, int pYpos, int pLocX, int pLocY)
		{
			final Rectangle2D stringBounds = g2.getFontMetrics().getStringBounds(pString, g2);
			int x = pXpos;
			int y = pYpos;
			if (pLocX == 0)
				// Centered
				x -= stringBounds.getWidth() / 2;
			else if (pLocX == 1)
				// To the right
				x += stringBounds.getWidth() + TEXT_DISTANCE;
			else
				// To the left
				x -= TEXT_DISTANCE;
			if (pLocY == 0)
				// Centered
				y += stringBounds.getHeight() / 2;
			else if (pLocY == 1)
				// Under
				y += stringBounds.getHeight() + TEXT_DISTANCE;
			else
				// Over
				y -= TEXT_DISTANCE;
			g2.drawString(pString, x, y);
		}
	}

	public ValuesHelper(HelperUI pHelperUI, int pMoneySystem)
	{
		super(UIMessages.getString("Frame.ValuesHelper.Frame.Title")); //$NON-NLS-1$
		mHelperUI = pHelperUI;
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent pEvent)
			{
				super.windowClosing(pEvent);
				mHelperUI.closedValueHelper();
			}
		});
		setSize(Toolkit.getDefaultToolkit().getScreenSize());
		MoneyHelperPanel moneyHelperPanel = new MoneyHelperPanel();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(moneyHelperPanel);
	}

	public void rotateValues()
	{
		Color oldWaitingValue = mWaitingColor;
		mWaitingColor = mWeakValueColor;
		mWeakValueColor = mMediumValueColor;
		mMediumValueColor = mStrongValueColor;
		mStrongValueColor = oldWaitingValue;
		repaint();
	}
}
