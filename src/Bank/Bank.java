package Bank;

public class Bank {
    private String name;
    enum CardTypes {PlatinumCard, TitaniumCard, MasterCard};

    public Bank(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
