package jyt.geconomicus.helper;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ChooseGameDialog extends JFrame
{
	private boolean mChosen = false;
	public ChooseGameDialog(final EntityManager pEntityManager) throws IOException
	{
		super("Choisir une partie");
		setIconImage(ImageIO.read(HelperUI.class.getResourceAsStream("/geconomicus.png")));
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent pEvent)
			{
				super.windowClosing(pEvent);
				// If the window is closing, we have to quit if the game has not been set in the main frame
				if (!mChosen)
					System.exit(1);
			}
		});
		Dimension size = new Dimension(800, 600);
		setSize(size);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(screenSize.width / 2 - size.width/2, screenSize.height / 2 - size.height/2);
		final JPanel mainPanel = new JPanel(new GridBagLayout());
		final JPanel existingGamesPanel = new JPanel(new GridBagLayout());
		existingGamesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), "Parties existantes"));
		final Insets insets = new Insets(0, 0, 0, 0);
		final JButton openGameButton = new JButton("Ouvrir cette partie");
		existingGamesPanel.add(openGameButton, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		@SuppressWarnings("unchecked")
		final List<Game> games = pEntityManager.createNamedQuery("Game.findAll").getResultList();
		final JComboBox<Game> gameCombo = new JComboBox<>(games.toArray(new Game[games.size()]));
		existingGamesPanel.add(gameCombo, new GridBagConstraints(0, 0, 2, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		final Action openGameAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				try
				{
					new HelperUI(pEntityManager, (Game)gameCombo.getSelectedItem());
				}
				catch (IOException e)
				{
					JOptionPane.showMessageDialog(ChooseGameDialog.this, "Could not open main frame because of exception " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				mChosen = true;
				setVisible(false);
			}
		};
		openGameButton.setEnabled(!games.isEmpty());
		openGameButton.addActionListener(openGameAction);
		openGameButton.setMnemonic('O');
		openGameButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), "openGame");
		openGameButton.getActionMap().put("openGame", openGameAction);
		final JButton deleteGameButton = new JButton("Supprimer cette partie");
		deleteGameButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent pE)
			{
				if (JOptionPane.showConfirmDialog(ChooseGameDialog.this, "Voulez-vous vraiment supprimer cette partie ?", "Suppression de partie", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
				{
					pEntityManager.getTransaction().begin();
					final Game game = (Game)gameCombo.getSelectedItem();
					pEntityManager.remove(game);
					gameCombo.removeItem(game);
					pEntityManager.getTransaction().commit();
				}
			}
		});
		existingGamesPanel.add(deleteGameButton, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		mainPanel.add(existingGamesPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		final JPanel newGamePanel = new JPanel(new GridBagLayout());
		newGamePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), "Nouvelle partie"));
		int y = 0;
		newGamePanel.add(new JLabel("Type de monnaie"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		final JComboBox<Integer> moneySystemCB = new JComboBox<>(new Integer[] {0, 1});
		moneySystemCB.setRenderer(new ListCellRenderer<Integer>()
		{
			@Override
			public Component getListCellRendererComponent(JList<? extends Integer> pList, Integer pValue, int pIndex, boolean pIsSelected, boolean pCellHasFocus)
			{
				return new JLabel(pValue.intValue() == Game.MONEY_DEBT ? "Monnaie-dette" : "Monnaie libre");
			}
		});
		newGamePanel.add(moneySystemCB, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel("Nb tours"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		final JTextField nbTurnsTextField = new JTextField("10");
		newGamePanel.add(nbTurnsTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel("Date"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		final JTextField dateTextField = new JTextField(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").format(new Date()));
		newGamePanel.add(dateTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel("Lieu"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		final JTextField locationTextField = new JTextField();
		newGamePanel.add(locationTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel("Animateur"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		final JTextField animatorTextField = new JTextField();
		newGamePanel.add(animatorTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel("Courriel"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		final JTextField emailTextField = new JTextField();
		newGamePanel.add(emailTextField, new GridBagConstraints(1, y++, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		newGamePanel.add(new JLabel("Commentaires"), new GridBagConstraints(0, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		final JTextArea descriptionTextArea = new JTextArea();
		final JScrollPane descriptionScrollPane = new JScrollPane(descriptionTextArea);
		newGamePanel.add(descriptionScrollPane, new GridBagConstraints(1, y++, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, insets, 0, 0));

		final JButton newGameButton = new JButton("Nouvelle partie");
		newGamePanel.add(newGameButton, new GridBagConstraints(1, y, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));

		newGameButton.setEnabled(false);
		final Action newGameAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				EntityManager em = pEntityManager;
				try
				{
					em.getTransaction().begin();
					final Game newGame = new Game(((Integer)moneySystemCB.getSelectedItem()).intValue(), Integer.valueOf(nbTurnsTextField.getText()), animatorTextField.getText(), emailTextField.getText(), descriptionTextArea.getText(), dateTextField.getText(), locationTextField.getText());
					em.persist(newGame);
					em.getTransaction().commit();
					new HelperUI(pEntityManager, newGame);
				}
				catch (NumberFormatException e)
				{
					em.getTransaction().rollback();
					JOptionPane.showMessageDialog(ChooseGameDialog.this, "Some field should be a number and isn't", "Error", JOptionPane.ERROR_MESSAGE);;
					return;
				}
				catch (IOException e)
				{
					em.getTransaction().rollback();
					JOptionPane.showMessageDialog(ChooseGameDialog.this, "Could not open main frame because of exception " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				setVisible(false);
			}
		};
		newGameButton.setMnemonic('N');
		newGameButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "newGame");
		newGameButton.getActionMap().put("newGame", newGameAction);
		newGameButton.addActionListener(newGameAction);
		mainPanel.add(newGamePanel, new GridBagConstraints(0, 10, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));

		final Action cancelAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent pEvent)
			{
				if (JOptionPane.showConfirmDialog(ChooseGameDialog.this, "Voulez-vous vraiment quitter ?", "Quitter ?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
					System.exit(1);
			}
		};
		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		mainPanel.getActionMap().put("cancel", cancelAction);


		final DocumentListener buttonActivator = new DocumentListener()
		{
			@Override
			public void removeUpdate(DocumentEvent pEvent)
			{
				activateButton();
			}
			
			@Override
			public void insertUpdate(DocumentEvent pEvent)
			{
				activateButton();
			}
			
			@Override
			public void changedUpdate(DocumentEvent pEvent)
			{
				activateButton();
			}
			private void activateButton()
			{
				newGameButton.setEnabled(!(dateTextField.getText().isEmpty() || locationTextField.getText().isEmpty() || animatorTextField.getText().isEmpty() || emailTextField.getText().isEmpty()));
			}
		};
		dateTextField.getDocument().addDocumentListener(buttonActivator);
		locationTextField.getDocument().addDocumentListener(buttonActivator);
		animatorTextField.getDocument().addDocumentListener(buttonActivator);
		emailTextField.getDocument().addDocumentListener(buttonActivator);
		descriptionTextArea.getDocument().addDocumentListener(buttonActivator);

		addWindowFocusListener(new WindowFocusListener()
		{
			@Override
			public void windowLostFocus(WindowEvent pE)
			{
			}
			
			@Override
			public void windowGainedFocus(WindowEvent pE)
			{
				locationTextField.requestFocus();
			}
		});
		getContentPane().add(mainPanel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
}
