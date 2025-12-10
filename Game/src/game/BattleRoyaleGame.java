package game;

// This file contains the main entry point to run the game.

public class BattleRoyaleGame {
    public static void main(String[] args) {
        System.out.println("Welcome to Console Battle Royale!");
        GameEngine engine = new GameEngine();
        
        // 1. Setup the game (collect players, assign roles, place on map)
        engine.setupGame(); 
        
        // 2. Start the main loop if enough players are ready
        if (engine.getAliveCount() > 1) {
            engine.startGame();
        } else {
            System.out.println("Not enough players to start the game.");
        }
    }
}