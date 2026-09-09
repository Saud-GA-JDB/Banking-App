package Bank.Cards;

public class MasterCard extends Card {
    public MasterCard(String hashedCode) {
        super(hashedCode, CardTypes.MASTERCARD, 5000, 10000, 100_000, 20_000, 200_000);
    }
}
