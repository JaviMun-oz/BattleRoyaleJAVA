package game;

public class DefenseTool extends Tool {
    public DefenseTool(String name) { super(name, 0); }
    @Override public String getType() { return "Defense"; }
    
    // Defense tool provides a direct Defense bonus and Ranged Reduction
    @Override public int getAttackModifier() { return 0; }
    @Override public int getDefenseModifier() { return level * 3; }
    @Override public int getDamageReduction() { return level * 2; }
}