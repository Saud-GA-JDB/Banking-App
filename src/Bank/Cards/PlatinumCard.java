package Bank.Cards;

public class PlatinumCard extends Card {

    public PlatinumCard(String hashedCode) {
        super(hashedCode, CardTypes.PLATINUMCARD, 20_000, 40_000, 100_000, 80_000, 200_000);
    }
}

