package game;

public class MeleeWeapon extends Tool {
    public MeleeWeapon(String name) { super(name, 30); }
    @Override public String getType() { return "Melee"; }
}