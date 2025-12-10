package game;

import java.util.*;
import java.util.stream.Collectors;

public class GameEngine {
    private final int MAP_SIZE = 10;
    private int mapLimit = MAP_SIZE - 1; // inclusive upper bound (0 to 9)
    private final List<Player> players = new ArrayList<>();
    private final Random rnd = new Random();
    private final List<String> log = new ArrayList<>();
    // Use an instance of Scanner for consistency
    private final Scanner scanner = new Scanner(System.in); 
    private int round = 0;
    private int difficulty = 1;

    public int getAliveCount() { return (int) players.stream().filter(Player::isAlive).count(); }

    // --- Setup Methods (FIXED LOGIC) ---
    public void setupGame() {
        System.out.println("=== Battle Royale (Console) ===");
        int numHuman = askInt("Number of human players (1-4): ", 1, 4);
        int numAI = askInt("Number of AI players (1-8): ", 1, 8);
        difficulty = askInt("Difficulty (1=Easy,2=Med,3=Hard): ", 1, 3);

        // Character and Tool TEMPLATES are created here. We use them for cloning, not removing.
        List<Character> characterTemplates = createCharacters(); 
        List<Tool> toolTemplates = createTools();

        // Human players
        for (int i = 0; i < numHuman; i++) {
            System.out.println("\n--- Human Player " + (i+1) + " ---");
            System.out.print("Enter name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = "Player" + (i+1);

            // 1. Human chooses a type, we clone a new instance for them
            Character chosenTemplate = chooseCharacter(characterTemplates);
            Character playerCharacter = cloneCharacter(chosenTemplate.getClassName()); // Clone new object

            Tool chosenTool = chooseTool(toolTemplates);
            Tool playerTool = cloneToolByName(chosenTool.getName());

            players.add(new Player(name, true, playerCharacter, playerTool));
        }

        // AI players (The fix for the crash is here)
        for (int i = 0; i < numAI; i++) {
            String name = "AI-" + (i+1);
            
            // 1. Choose a random character TEMPLATE from the full list
            Character template = characterTemplates.get(rnd.nextInt(characterTemplates.size()));
            
            // 2. Clone a fresh instance of that character type for the AI
            Character playerCharacter = cloneCharacter(template.getClassName());
            
            // 3. Choose a random tool
            Tool t = toolTemplates.get(rnd.nextInt(toolTemplates.size()));
            Tool playerTool = cloneToolByName(t.getName());
            
            // 4. Add the new player
            players.add(new Player(name, false, playerCharacter, playerTool));
        }

        placePlayers();
        System.out.println("\nSetup complete. Players:\n");
        players.forEach(p -> System.out.println(p));
    }

    // NEW HELPER METHOD TO CLONE CHARACTERS
    private Character cloneCharacter(String className) {
        switch (className) {
            case "Balanced": return new BalancedCharacter("AI-Balanced");
            case "Speed": return new SpeedCharacter("AI-Speed");
            case "Damage": return new DamageCharacter("AI-Damage");
            default: return new BalancedCharacter("AI-Fallback");
        }
    }

    private int askInt(String prompt, int min, int max) {
        int v = -1;
        while (v < min || v > max) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            if (line.isEmpty()) {continue;}
            try { v = Integer.parseInt(line.trim()); }
            catch (Exception e) { System.out.println("Invalid number"); }
        }
        return v;
    }

    private List<Character> createCharacters() {
        List<Character> list = new ArrayList<>();
        list.add(new BalancedCharacter("Bal-1"));
        list.add(new SpeedCharacter("Spd-1"));
        list.add(new DamageCharacter("Dmg-1"));

        return list;
    }

    private List<Tool> createTools() {
        List<Tool> t = new ArrayList<>();
        t.add(new MeleeWeapon("Sword"));
        t.add(new RangedWeapon("Bow"));
        t.add(new DefenseTool("Shield"));
        return t;
    }

    private Character chooseCharacter(List<Character> candidates) {
        System.out.println("Choose a character:");
        for (int i = 0; i < candidates.size(); i++) {
            Character c = candidates.get(i);
            System.out.printf("[%d] %s (Atk:%d Def:%d Spd:%d HP:%d)\n", i+1, c.getClassName(), c.baseAttack, c.baseDefense, c.baseSpeed, c.baseHealth);
        }
        int choice = askInt("Select (number): ", 1, candidates.size());
        return candidates.get(choice-1);
    }

    private Tool chooseTool(List<Tool> available) {
        System.out.println("Choose starting tool (all Lvl1):");
        for (int i = 0; i < available.size(); i++) {
            Tool t = available.get(i);
            System.out.printf("[%d] %s (%s)\n", i+1, t.getName(), t.getType());
        }
        int choice = askInt("Select (number): ", 1, available.size());
        return available.get(choice-1);
    }

    private Tool cloneToolByName(String name) {
        switch (name) {
            case "Sword": return new MeleeWeapon("Sword");
            case "Bow": return new RangedWeapon("Bow");
            case "Shield": return new DefenseTool("Shield");
            default: return new MeleeWeapon("Fist");
        }
    }

    private void placePlayers() {
        Set<String> used = new HashSet<>();
        for (Player p : players) {
            int x, y;
            do { x = rnd.nextInt(MAP_SIZE); y = rnd.nextInt(MAP_SIZE); } while (used.contains(x+","+y));
            used.add(x+","+y);
            p.assignPos(x, y);
            log(String.format("Placed %s at (%d,%d)", p.getName(), x, y));
        }
    }

    // --- Game Loop Methods ---
    public void startGame() {
        while (getAliveCount() > 1) {
            round++;
            System.out.println("\n========================");
            System.out.println(" Round " + round + " | Map area: 0.." + mapLimit);
            System.out.println("========================");

            if (round % 3 == 0) shrinkMap();

            drawMapSimple();

            // order by speed
            List<Player> turnOrder = players.stream()
                .filter(Player::isAlive)
                .sorted((a,b) -> Integer.compare(b.getTotalSpeed(), a.getTotalSpeed()))
                .collect(Collectors.toList());

            for (Player p : turnOrder) {
                if (!p.isAlive()) continue;
                System.out.println("\n-- " + p.getName() + "'s turn --");
                System.out.println(p);

                int[] newPos = (p.isHuman()) ? humanMove(p) : aiMove(p);
                p.moveTo(newPos[0], newPos[1]);
                log(p.getName() + " moved to (" + newPos[0] + "," + newPos[1] + ")");

                // encounter check
                Player other = players.stream()
                    .filter(o -> o.isAlive() && o != p && o.getX() == p.getX() && o.getY() == p.getY())
                    .findFirst().orElse(null);

                if (other != null) {
                    System.out.println("Battle! " + p.getName() + " vs " + other.getName());
                    resolveBattle(p, other);
                    if (getAliveCount() <= 1) break;
                } else {
                    // chance to find loot (5% chance)
                    if (rnd.nextDouble() < 0.05) {
                        Tool loot = getLoot();
                        System.out.println(p.getName() + " found loot: " + loot.getName() + " (L" + loot.getLevel() + ")");
                        if (loot.getLevel() > p.getWeapon().getLevel()) {
                            p.equipWeapon(loot);
                            System.out.println("Equipped new weapon.");
                            log(p.getName() + " equipped " + loot.getName() + " L" + loot.getLevel());
                        } else {
                            System.out.println("Loot ignored (worse than current).");
                        }
                    }
                }
            }

            displayStatus();

            if (players.stream().anyMatch(Player::isHuman) && getAliveCount() > 1) {
                System.out.print("Press Enter to continue...");
                scanner.nextLine();
            }
        }

        endGame();
    }

    private void drawMapSimple() {
        char[][] grid = new char[MAP_SIZE][MAP_SIZE];
        for (int y = 0; y < MAP_SIZE; y++) for (int x = 0; x < MAP_SIZE; x++) grid[y][x] = ' ';

        // Mark players (only alive)
        for (Player p : players) {
            if (!p.isAlive()) continue;
            int x = p.getX(); int y = p.getY();
            // Check if player is within the current map limits
            if (x < 0 || y < 0 || x > mapLimit || y > mapLimit) {
                grid[y][x] = '*'; // Mark out-of-bounds area briefly
                continue;
            } 
            
            // use single-letter token: H/A
            char token = p.isHuman() ? 'H' : 'A';
            
            // if multiple at same tile, mark 'M'
            if (grid[y][x] == ' ' ) grid[y][x] = token;
            else grid[y][x] = 'M';
        }

        // Fill empty spots and OOB spots with a visual marker
        for (int y = 0; y < MAP_SIZE; y++) {
            for (int x = 0; x < MAP_SIZE; x++) {
                if (grid[y][x] == ' ') {
                    if (x > mapLimit || y > mapLimit) {
                        grid[y][x] = 'X'; // Out-of-bounds
                    } else {
                        grid[y][x] = '.'; // In-bounds empty
                    }
                }
            }
        }
        
        // print grid with (0,0) in bottom-left
        System.out.println("Map (simple) - legend: H=Human A=AI M=Multiple .=empty X=Shrunk Zone");
        for (int row = MAP_SIZE - 1; row >= 0; row--) {
            System.out.printf("%2d ", row);
            for (int col = 0; col < MAP_SIZE; col++) System.out.print(grid[row][col] + " ");
            System.out.println();
        }
        // x axis labels
        System.out.print("   ");
        for (int col = 0; col < MAP_SIZE; col++) System.out.printf("%d ", col);
        System.out.println();
    }

    private int[] humanMove(Player p) {
        int x = p.getX(), y = p.getY();
        List<String> opts = Arrays.asList("UP","DOWN","LEFT","RIGHT","STAY");
        String choice = "";
        while(true) {
            System.out.print("Move (UP/DOWN/LEFT/RIGHT/STAY): ");
            choice = scanner.nextLine().trim().toUpperCase();
            if (opts.contains(choice)) break;
            System.out.println("Invalid move.");
        }
        int nx = x, ny = y;
        switch(choice) {
            case "UP": ny = y+1; break;
            case "DOWN": ny = y-1; break;
            case "LEFT": nx = x-1; break;
            case "RIGHT": nx = x+1; break;
            case "STAY": default: break;
        }
        
        // Clamp to map boundaries
        nx = Math.min(Math.max(nx, 0), mapLimit);
        ny = Math.min(Math.max(ny, 0), mapLimit);
        
        return new int[]{nx, ny};
    }

    private int[] aiMove(Player p) {
        int x = p.getX(), y = p.getY();
        List<int[]> moves = new ArrayList<>();
        moves.add(new int[]{x, y}); // stay
        
        // Potential moves, clamped to boundaries
        moves.add(new int[]{Math.min(Math.max(x-1, 0), mapLimit), y});
        moves.add(new int[]{Math.min(Math.max(x+1, 0), mapLimit), y});
        moves.add(new int[]{x, Math.min(Math.max(y-1, 0), mapLimit)});
        moves.add(new int[]{x, Math.min(Math.max(y+1, 0), mapLimit)});
        
        // prefer moves toward center (for simple AI)
        int center = mapLimit / 2;
        moves.sort(Comparator.comparingInt(m -> Math.abs(m[0]-center)+Math.abs(m[1]-center)));
        
        // choose among top 3
        int choice = rnd.nextInt(Math.min(3, moves.size()));
        return moves.get(choice);
    }

    private void resolveBattle(Player a, Player b) {
        // [Damage that A deals to B]
        int aBase = a.getWeapon().getCurrentDamage() + a.getTotalAttack();
        int aTaken = 0; 
        if (a.getWeapon() instanceof MeleeWeapon) {
            aTaken = Math.max(1, aBase - b.getTotalDefense());
        } else if (a.getWeapon() instanceof RangedWeapon) {
            aTaken = Math.max(1, aBase - b.getRangedReduction());
        } else { 
            aTaken = 1; // Defense tool deals minimal damage
        }

        // [Damage that B deals to A]
        int bBase = b.getWeapon().getCurrentDamage() + b.getTotalAttack();
        int bTaken = 0;
        if (b.getWeapon() instanceof MeleeWeapon) {
            bTaken = Math.max(1, bBase - a.getTotalDefense());
        } else if (b.getWeapon() instanceof RangedWeapon) {
            bTaken = Math.max(1, bBase - a.getRangedReduction());
        } else {
            bTaken = 1;
        }

        Player winner, loser; int damage;
        if (aTaken > bTaken) { winner = a; loser = b; damage = aTaken; }
        else if (bTaken > aTaken) { winner = b; loser = a; damage = bTaken; }
        else {
            // Tie-breaker: higher weapon level wins
            if (a.getWeapon().getLevel() > b.getWeapon().getLevel()) { winner=a; loser=b; damage=aTaken; }
            else { winner=b; loser=a; damage=bTaken; }
        }

        loser.takeDamage(damage);
        System.out.printf("%s wins and deals %d damage to %s (HP left: %d)\n", winner.getName(), damage, loser.getName(), loser.getCurrentHealth());
        log(String.format("%s beat %s for %d dmg", winner.getName(), loser.getName(), damage));

        if (!loser.isAlive()) {
            System.out.println(winner.getName() + " eliminated " + loser.getName());
            winner.levelUpWeapon();
            log(winner.getName() + " eliminated " + loser.getName() + " and leveled up.");
        }
    }

    private Tool getLoot() {
        Tool base = createTools().get(rnd.nextInt(3));
        Tool t = cloneToolByName(base.getName());
        int lvl = rnd.nextInt(4) + 2; // Level 2 to 5
        for (int i=1;i<lvl;i++) t.levelUp();
        return t;
    }

    private void shrinkMap() {
        if (mapLimit > 3) {
            mapLimit -= 1;
            log("Map shrunk to 0.." + mapLimit);
            System.out.println("*** MAP SHRINKS! New area 0.." + mapLimit + " ***");
            
            // Eliminate players outside the new boundary
            List<Player> eliminatedByMap = players.stream()
                .filter(Player::isAlive)
                .filter(p -> p.getX() >= mapLimit || p.getY() >= mapLimit)
                .collect(Collectors.toList());
                
            for (Player p : eliminatedByMap) {
                // instant death by shrink
                p.takeDamage(p.getCurrentHealth());
                log(p.getName() + " died to shrink");
                System.out.println("💀 " + p.getName() + " was eliminated by the shrinking map.");
            }
        }
    }

    private void displayStatus() {
        System.out.println("\n-- Status --");
        players.stream().filter(Player::isAlive).forEach(p -> System.out.println(p));
        System.out.println("Alive: " + getAliveCount());
    }

    private void endGame() {
        Player winner = players.stream().filter(Player::isAlive).findFirst().orElse(null);
        System.out.println("\n=== GAME OVER ===");
        if (winner != null) System.out.println("Winner: " + winner.getName() + " -- " + winner);
        else System.out.println("No winner (all dead)");

        System.out.print("Print operations log? (Y/N): ");
        String c = scanner.nextLine().trim();
        if (c.equalsIgnoreCase("Y")) {
            System.out.println("-- Operations Log (Last 1000 entries) --");
            log.forEach(System.out::println);
            System.out.println("-- End of Log --");
        }
        
        // Important: close the scanner to prevent resource leaks
        scanner.close(); 
    }

    private void log(String s) {
        logAdd(String.format("R%d: %s", round, s));
    }

    private void logAdd(String s) {
        log.add(s);
        // Keep log size managed
        if (log.size() > 1000) log.remove(0); 
    }
}