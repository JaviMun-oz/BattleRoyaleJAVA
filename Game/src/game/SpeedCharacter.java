package game;

public class SpeedCharacter extends Character {
    public SpeedCharacter(String name) { super(name, 80, 12, 8, 20); }
    @Override public String getClassName() { return "Speed"; }
}