package Bank.Cards;

public class TitaniumCard extends Card{
    public TitaniumCard(String hashedCode) {
        super(hashedCode, CardTypes.TITANIUMCARD, 10_000, 20000, 100_000, 40_000, 200_000);
    }
}
