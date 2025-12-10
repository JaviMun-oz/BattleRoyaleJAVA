package game;

public class RangedWeapon extends Tool {
    public RangedWeapon(String name) { super(name, 20); }
    @Override public String getType() { return "Ranged"; }
}