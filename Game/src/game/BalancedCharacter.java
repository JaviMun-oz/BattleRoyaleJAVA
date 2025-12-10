package game;

public class BalancedCharacter extends Character {
    public BalancedCharacter(String name) { super(name, 100, 15, 10, 10); }
    @Override public String getClassName() { return "Balanced"; }
}