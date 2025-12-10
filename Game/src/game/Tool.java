package game;

public abstract class Tool implements Modifiable {
    protected String name;
    protected int level;
    protected int baseDamage;

    public Tool(String name, int baseDamage) {
        this.name = name;
        this.baseDamage = baseDamage;
        this.level = 1;
    }

    public void levelUp() { level++; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    // Damage increases by 5 per level
    public int getCurrentDamage() { return baseDamage + (level * 5); }
    public abstract String getType();

    @Override public int getAttackModifier() { return level * 2; }
    @Override public int getDefenseModifier() { return 0; }
    @Override public int getSpeedModifier() { return 0; }
    @Override public int getDamageReduction() { return 0; }
}