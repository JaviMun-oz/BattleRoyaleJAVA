package game;

public abstract class Character implements Modifiable {
    protected String name;
    protected int baseHealth;
    protected int baseAttack;
    protected int baseDefense;
    protected int baseSpeed;

    public Character(String name, int h, int a, int d, int s) {
        this.name = name;
        this.baseHealth = h;
        this.baseAttack = a;
        this.baseDefense = d;
        this.baseSpeed = s;
    }

    public abstract String getClassName();

    // Default Modifiable methods (base character offers no stat mods)
    @Override public int getAttackModifier() { return 0; }
    @Override public int getDefenseModifier() { return 0; }
    @Override public int getSpeedModifier() { return 0; }
    @Override public int getDamageReduction() { return 0; }
}