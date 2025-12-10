package game;

public class Player {
    private static int nextId = 1;
    private final int id;
    private final String name;
    private final boolean human;
    private final Character character;
    private Tool weapon;
    private int currentHealth;
    private int x, y;
    private boolean alive;

    public Player(String name, boolean human, Character character, Tool weapon) {
        this.id = nextId++;
        this.name = name;
        this.human = human;
        this.character = character;
        this.weapon = weapon;
        this.currentHealth = getMaxHealth();
        this.alive = true;
        this.x = -1; this.y = -1;
    }

    // --- Stat Calculation Methods ---
    public int getMaxHealth() { return character.baseHealth; }
    public int getTotalAttack() { return character.baseAttack + weapon.getAttackModifier(); }
    public int getTotalDefense() { return character.baseDefense + weapon.getDefenseModifier(); }
    public int getTotalSpeed() { return character.baseSpeed + weapon.getSpeedModifier(); }
    public int getRangedReduction() { return weapon.getDamageReduction(); }

    // --- Action Methods ---
    public void equipWeapon(Tool t) { this.weapon = t; }
    public void assignPos(int x, int y) { this.x = x; this.y = y; }
    public void moveTo(int x, int y) { this.x = x; this.y = y; }

    public void takeDamage(int d) {
        currentHealth -= d;
        if (currentHealth <= 0) { currentHealth = 0; alive = false; }
    }

    public void levelUpWeapon() { weapon.levelUp(); }

    // --- Getters ---
    public Tool getWeapon() { return weapon; }
    public boolean isAlive() { return alive; }
    public boolean isHuman() { return human; }
    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public Character getCharacter() { return character; }
    public int getCurrentHealth() { return currentHealth; }

    @Override
    public String toString() {
        return String.format("%s%s (HP:%d/%d, Class:%s, Wpn:%s L%d, Atk:%d, Def:%d, Spd:%d) @(%d,%d)",
            human?"H":"A", id, currentHealth, getMaxHealth(), character.getClassName(), weapon.getName(), weapon.getLevel(),
            getTotalAttack(), getTotalDefense(), getTotalSpeed(), x, y);
    }
}