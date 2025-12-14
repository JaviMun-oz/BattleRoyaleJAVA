package game;

public abstract class Character implements Modifiable {
    private String name;
    private int baseHealth;
    private int baseAttack;
    private int baseDefense;
    private int baseSpeed;
    private int baseDamageReduction;

    public Character(String name, int h, int a, int d, int s) {
        this(name, h, a, d, s, 0); // default DR = 0
    }

    public Character(String name, int h, int a, int d, int s, int dr) {
        this.name = name;
        this.baseHealth = h;
        this.baseAttack = a;
        this.baseDefense = d;
        this.baseSpeed = s;
        this.baseDamageReduction = dr;
    }


    public abstract String getClassName();
    
    public String getName() { return name; }
    public int getBaseHealth() { return baseHealth; }
    public int getBaseAttack() { return baseAttack; }
    public int getBaseDefense() { return baseDefense; }
    public int getBaseSpeed() { return baseSpeed; }
    public int getBaseDamageReduction() { return baseDamageReduction; }


    // Default Modifiable methods (base character offers no stat mods)
    @Override public int getAttackModifier() { return 0; }
    @Override public int getDefenseModifier() { return 0; }
    @Override public int getSpeedModifier() { return 0; }
    @Override public int getDamageReduction() { return 0; }
}