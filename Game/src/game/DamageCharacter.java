package game;

public class DamageCharacter extends Character {
    public DamageCharacter(String name) { super(name, 90, 25, 5, 8); }
    @Override public String getClassName() { return "Damage"; }
}