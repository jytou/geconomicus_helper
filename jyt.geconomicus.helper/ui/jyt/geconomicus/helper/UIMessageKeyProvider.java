package jyt.geconomicus.helper;

public enum UIMessageKeyProvider implements MessageKeyProvider
{
	GENERAL_DEBT_MONEY("General.Debt.Money"), //$NON-NLS-1$
	GENERAL_LIBRE_CURRENCY("General.Libre.Currency"), //$NON-NLS-1$
	GENERAL_CARDS_WEAK("General.Cards.Weak"), //$NON-NLS-1$
	GENERAL_CARDS_MEDIUM("General.Cards.Medium"), //$NON-NLS-1$
	GENERAL_CARDS_STRONG("General.Cards.Strong"), //$NON-NLS-1$
	GENERAL_MONEY_WEAK("General.Money.Weak"), //$NON-NLS-1$
	GENERAL_MONEY_MEDIUM("General.Money.Medium"), //$NON-NLS-1$
	GENERAL_MONEY_STRONG("General.Money.Strong"), //$NON-NLS-1$
	GENERAL_CREDIT_INTEREST("General.Credit.Interest"), //$NON-NLS-1$
	GENERAL_CREDIT_PRINCIPAL("General.Credit.Principal"), //$NON-NLS-1$

	MAINFRAME_TABLE_PLAYER_COLNAME_STATUS("HelperUI.Player.Table.ColumnTitle.Status"), //$NON-NLS-1$
	MAINFRAME_TABLE_PLAYER_COLNAME_NAME("HelperUI.Player.Table.ColumnTitle.Name"), //$NON-NLS-1$
	MAINFRAME_TABLE_PLAYER_COLNAME_AGE("HelperUI.Player.Table.ColumnTitle.Age"), //$NON-NLS-1$
	MAINFRAME_TABLE_PLAYER_COLNAME_HISTORY("HelperUI.Player.Table.ColumnTitle.History"), //$NON-NLS-1$
	MAINFRAME_TABLE_PLAYER_HINT_AMBIGUOUS_NAME("HelperUI.Player.Table.Hint.AmbiguousPlayerName"), //$NON-NLS-1$
	MAINFRAME_TABLE_EVENT_COLNAME_TIME("HelperUI.Event.Table.ColumnTitle.Time"), //$NON-NLS-1$
	MAINFRAME_TABLE_EVENT_COLNAME_TYPE("HelperUI.Event.Table.ColumnTitle.Type"), //$NON-NLS-1$
	MAINFRAME_TABLE_EVENT_COLNAME_PLAYER("HelperUI.Event.Table.ColumnTitle.Player"), //$NON-NLS-1$
	MAINFRAME_TABLE_EVENT_COLNAME_CARDS1("HelperUI.Event.Table.ColumnTitle.Cards1"), //$NON-NLS-1$
	MAINFRAME_TABLE_EVENT_COLNAME_CARDS2("HelperUI.Event.Table.ColumnTitle.Cards2"), //$NON-NLS-1$
	MAINFRAME_TABLE_EVENT_COLNAME_CARDS4("HelperUI.Event.Table.ColumnTitle.Cards4"), //$NON-NLS-1$
	MAINFRAME_TABLE_EVENT_COLNAME_MONEY1("HelperUI.Event.Table.ColumnTitle.Money1"), //$NON-NLS-1$
	MAINFRAME_TABLE_EVENT_COLNAME_MONEY2("HelperUI.Event.Table.ColumnTitle.Money2"), //$NON-NLS-1$
	MAINFRAME_TABLE_EVENT_COLNAME_MONEY4("HelperUI.Event.Table.ColumnTitle.Money4"), //$NON-NLS-1$

	DIALOG_TITLE_ERROR("Dialog.Title.Error"), //$NON-NLS-1$
	DIALOG_TITLE_CANCEL("Dialog.Title.Cancel"), //$NON-NLS-1$
	DIALOG_MESSAGE_CANCEL("Dialog.Message.Cancel"), //$NON-NLS-1$
	DIALOG_ERROR_MESSAGE("Dialog.Error.Message"), //$NON-NLS-1$
	DIALOG_BUTTON_CANCEL("Dialog.Button.Cancel"), //$NON-NLS-1$
	DIALOG_BUTTON_APPLY("Dialog.Button.Apply"), //$NON-NLS-1$
	DIALOG_BUTTON_ADD("Dialog.Button.Add"), //$NON-NLS-1$
	DIALOG_BUTTON_IMPORT("Dialog.Button.Import"), //$NON-NLS-1$
	DIALOG_BUTTON_OPEN("Dialog.Button.Open"), //$NON-NLS-1$
	DIALOG_PLAYER_NAME_LABEL("Dialog.Player.Name.Label"), //$NON-NLS-1$
	DIALOG_BUTTON_RENAME("Dialog.Button.Rename"), //$NON-NLS-1$
	GAME_LOCATION_LABEL("ChooseGameDialog.NewGame.Location.Label"), //$NON-NLS-1$
	GAME_DESCRIPTION_LABEL("ChooseGameDialog.NewGame.Description.Label"), //$NON-NLS-1$
	GAME_DATE_LABEL("ChooseGameDialog.NewGame.Date.Label"), //$NON-NLS-1$
	GAME_MONEY_TYPE_LABEL("ChooseGameDialog.NewGame.MoneyType.Label"), //$NON-NLS-1$
	GAME_ANIMATOR_EMAIL_LABEL("ChooseGameDialog.NewGame.AnimatorEmail.Label"), //$NON-NLS-1$
	GAME_ANIMATOR_PSEUDO_LABEL("ChooseGameDialog.NewGame.AnimatorPseudo.Label"); //$NON-NLS-1$

	private String key;

	private UIMessageKeyProvider(String key)
	{
		this.key = key;
	}

	@Override
	public String getKey()
	{
		return key;
	}

	public String getMessage()
	{
		return UIMessages.getString(key);
	}
}
