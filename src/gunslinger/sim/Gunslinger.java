package gunslinger.sim;

// general utilities
import java.io.*;
import java.util.List;
import java.util.*;
import javax.tools.*;
import java.net.URL;

import java.util.concurrent.*;

// gui utilities
import static java.awt.geom.AffineTransform.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import javax.swing.*;

public class Gunslinger
{

	public static boolean debug=false;
	
    // Default parameters
    private static String ROOT_DIR = "gunslinger";
    private static int DEFAULT_FRIENDS = 2;
    private static int DEFAULT_ENEMIES = 2;
    private static String DEFAULT_PLAYERLIST = "players.list";
    private static int DEFAULT_GAMES = 1;
    private static int DEFAULT_TIMEOUT = 1000; // 1000 milliseconds
    
    // recompile .class file?
    private static boolean recompile = true;
    
    // print more details?
    private static boolean verbose = true;

    // Step by step trace
    private static boolean trace = true;

    // enable gui
    private static boolean gui = false;

    // time limit per move
    private static int timeout = DEFAULT_TIMEOUT;
    
	// list files below a certain directory
	// can filter those having a specific extension constraint
    //
	private static List <File> directoryFiles(String path, String extension) {
		List <File> allFiles = new ArrayList <File> ();
		allFiles.add(new File(path));
		int index = 0;
		while (index != allFiles.size()) {
			File currentFile = allFiles.get(index);
			if (currentFile.isDirectory()) {
				allFiles.remove(index);
				for (File newFile : currentFile.listFiles())
					allFiles.add(newFile);
			} else if (!currentFile.getPath().endsWith(extension))
				allFiles.remove(index);
			else index++;
		}
		return allFiles;
	}

    private static String[] loadPlayerNames(String txtPath) {
        ArrayList<String> namelist = new ArrayList<String>();
        try {
            // get file of players
            BufferedReader in = new BufferedReader(new FileReader(new File(txtPath)));
            String line;
            while ( (line = in.readLine()) != null)
                if (line.trim() != "")
                    namelist.add(line);
        } catch (Exception e) {
            e.printStackTrace();        
            System.exit(1);
        }
        return namelist.toArray(new String[0]);
    }

  	// compile and load players dynamically
    //
	private static Player[] loadPlayers(String txtPath) {
        //        List<Class> playerClasses = new LinkedList<Class>();

		// list of players
        List <Player> playersList = new LinkedList <Player> ();

        try {
            // get file of players
            BufferedReader in = new BufferedReader(new FileReader(new File(txtPath)));
            // get tools

            // The default class loader sucks!
            // ClassLoader loader = Gunslinger.class.getClassLoader();

            URL url = Gunslinger.class.getProtectionDomain().getCodeSource().getLocation();
            // Create a new class loader to load the players between each game
            // so that no static fields will be carried to the next game
            ClassLoader loader = new ClassReloader(url, Gunslinger.class.getClassLoader());

            if (loader == null) throw new Exception("Cannot load class loader");
            JavaCompiler compiler = null;
            StandardJavaFileManager fileManager = null;
            // get separator
            String sep = File.separator;
            // load players
            String group;
            while ((group = in.readLine()) != null) {
                System.err.println("Group: " + group);
                // search for compiled files
                File classFile = new File(ROOT_DIR + sep + group + sep + "Player.class");
                System.err.println(classFile.getAbsolutePath());
                if (!classFile.exists() || recompile) {
                    // delete all class files
                    List <File> classFiles = directoryFiles(ROOT_DIR + sep + group, ".class");
                    System.err.print("Deleting " + classFiles.size() + " class files...   ");
                    for (File file : classFiles)
                        file.delete();
                    System.err.println("OK");
                    if (compiler == null) compiler = ToolProvider.getSystemJavaCompiler();
                    if (compiler == null) throw new Exception("Cannot load compiler");
                    if (fileManager == null) fileManager = compiler.getStandardFileManager(null, null, null);
                    if (fileManager == null) throw new Exception("Cannot load file manager");
                    // compile all files
                    List <File> javaFiles = directoryFiles(ROOT_DIR + sep + group, ".java");
                    System.err.print("Compiling " + javaFiles.size() + " source files...   ");
                    Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(javaFiles);
                    boolean ok = compiler.getTask(null, fileManager, null, null, null, units).call();
                    if (!ok) throw new Exception("Compile error");
                    System.err.println("OK");
                }
                // load class
                System.err.print("Loading player class...   ");
                String className = ROOT_DIR + "." + group + ".Player";
                Class playerClass = loader.loadClass(className);
                System.err.println("OK");
                // set name of player and append on list

                // add a small delay to avoid random seed collision
                Thread.sleep(5);                
                Player player = (Player) playerClass.newInstance();
                if (player == null)
                    throw new Exception("Load error");
                playersList.add(player);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }

		return playersList.toArray(new Player[0]);
	}
    
    // Gunslinger <playerlist> <num of enermies> <num of friends> <gui> <recompile> <verbose> <trace> <games> <timeout>
    //
	public static void main(String[] args) throws Exception
	{
        int games = DEFAULT_GAMES;
        int nenemies = 0, nfriends = 0;
        String playerPath = null;

        try {
            String sep = File.separator;
            // players path
            if (args.length > 0)
                playerPath = args[0];
            else
                playerPath = ROOT_DIR + sep + DEFAULT_PLAYERLIST;

            // number of enermies
            if (args.length > 1)
                nenemies = Integer.parseInt(args[1]);
            else
                nenemies = DEFAULT_ENEMIES;
      
            // number of friends
            if (args.length > 2)
                nfriends = Integer.parseInt(args[2]);
            else
                nfriends = DEFAULT_FRIENDS;

            // gui
            if (args.length > 3)
                gui = Boolean.parseBoolean(args[3]);

            // recompile
            if (args.length > 4)
                recompile = Boolean.parseBoolean(args[4]);

            // verbose
            if (args.length > 5)
                verbose = Boolean.parseBoolean(args[5]);
        
            // trace
            if (args.length > 6)
                trace = Boolean.parseBoolean(args[6]);

            // turns
            if (args.length > 7)
                games = Integer.parseInt(args[7]);

            // timeout
            if (args.length > 8)
                timeout = Integer.parseInt(args[8]);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Usage: java Gunslinger <playerlist> <num of enermies> <num of friends> <gui> <recompile> <verbose> <trace> <games> <timeout>");
            System.exit(1);
        }
            
        playerNames = loadPlayerNames(playerPath);
        int nplayers = playerNames.length;
        
        // check parameters
        if (nenemies + nfriends >= nplayers) {
            System.err.println("[Error] Invalid parameters: e+f<N");
            System.exit(1);
        }
        
        // additional constraint
        if ((nplayers * nfriends) % 2 == 1) {
            System.err.println("[Error] Invalid parameter: f and N cannot be odd together, no valid relationship is possible.");
            System.exit(1);
        }

        // play the game with gui
        // can only run one game
        if (gui) {
            // Create and initialize the game
            Player[] players = loadPlayers(playerPath);
            // reshuffle the players
            shufflePlayers(players);

            Gunslinger game = new Gunslinger(nenemies, nfriends, players);
            game.init();
            game.playgui();
        }
        else {
            // Aggregate results
            int[][] ranks = new int[nplayers][nplayers];
            int[] totalScores = new int[nplayers];
            int[][] scoreCounts = new int[nplayers][nfriends+nenemies+1+1];

            for (int g = 0; g != games; ++g) {
                Player.globalIndex = 0;

                Player[] players = loadPlayers(playerPath);
                shufflePlayers(players);
                
                // Create and initialize the game
                Gunslinger game = new Gunslinger(nenemies, nfriends, players);
                game.init();

                game.printConfig();

                game.play();

                // ranks
                int[] rank = game.rank();
                for (int i = 0; i != rank.length; ++i)
                    ranks[i][rank[i]]++;

                // average scores
                for (int i = 0; i < players.length; i++) {
                    int score = game.scores[i];
                    totalScores[players[i].index] += score;
                    scoreCounts[players[i].index][score]++;
                }

            }

            printStats(ranks, totalScores, scoreCounts, games);

            // Force stopping all pending threads
            System.exit(0);
        }
    }        

    // shuffle the players to decide their index
    private static void shufflePlayers(Player[] players)
    {
        for (int i = 0; i != players.length; ++i) {
            int j = gen.nextInt(players.length - i) + i;
            Player t = players[i];
            players[i] = players[j];
            players[j] = t;
            players[i].id = i;
        }
    }

    private void printConfig()
    {
        // print configuration
        System.err.println("##### Configurations #####");
        System.err.println("# players: " + players.length);
        System.err.println("# friends/player: " + nfriends);
        System.err.println("# enemies/player: " + nenemies);
        printRelationship();
    }
    
    private static void printStats(int[][] ranks, int[] totalScores,
                                   int[][] scoreCounts, int games)
    {
        System.err.println("##### Tournament ranking #####");
        
        // print header
        System.err.printf("%5s", " ");
        for (int i = 1; i <= ranks.length; ++i)
            System.err.printf("%3d ", i);
        System.err.println();

        for (int i = 0; i != ranks.length; ++i) {
            System.err.printf("%-5s", playerNames[i]); 
            for (int j = 0; j != ranks.length; ++j) {
                System.err.printf("%3d ", ranks[i][j]);
            }
            System.err.println();
        }

        System.err.println("##### Average score #####");

        for (int i = 0; i < totalScores.length; i++)
            System.err.printf("%s: %.2f\n", playerNames[i], 1.0*totalScores[i]/games);

        System.err.println("##### Score board #####");
        
        // print header
        System.err.printf("%5s", " ");
        for (int i = 0; i < scoreCounts[0].length; ++i)
            System.err.printf("%3d ", i);
        System.err.println();

        for (int i = 0; i != scoreCounts.length; ++i) {
            System.err.printf("%-5s", playerNames[i]); 
            for (int j = 0; j != scoreCounts[0].length; ++j) {
                System.err.printf("%3d ", scoreCounts[i][j]);
            }
            System.err.println();
        }


    }

    // constructor
    //
    public Gunslinger(int nenemies, int nfriends, Player[] players)
    {
        this.nenemies = nenemies;
        this.nfriends = nfriends;
        this.players = players;
    }

    // Initialize the game   
    //
    private void init()
    {
        nplayers = players.length;

        // generate relationships
        relationship = RelationGenerator.generate(nplayers, nfriends, nenemies);
        friendship = RelationGenerator.getFriendList(relationship, nplayers, nfriends);
        enmityship = RelationGenerator.getEnemyList(relationship, nplayers, nenemies);

        // generate a random permutation for each player
        playerToHost = new int[nplayers][nplayers];
        hostToPlayer = new int[nplayers][nplayers];
        for (int i = 0; i < nplayers; i++)
            generateTranslation(playerToHost[i], hostToPlayer[i]);


        // initialize the players
        for (int p = 0; p != nplayers; ++p) {
            try {
                Thread.sleep(5);
            } catch (Exception e) {}

            // change the player's id to its internal index
            players[p].id = hostToPlayer[p][p];
            int[] friendlocal = mapIntToPlayer(p, friendship[p]);
            int[] enemylocal = mapIntToPlayer(p, enmityship[p]);
            players[p].init(nplayers, friendlocal, enemylocal);
        }

        // initialize all players as alive
        alive = new boolean[nplayers];
        for (int p = 0; p != nplayers; ++p)
            alive[p] = true;

        // initialize violation to false
        violation = new boolean[nplayers];

        // initialize current actions
        current = new int[nplayers];


    }

    static void shuffle(int[] a) {
        for (int i = 0; i != a.length; ++i) {
            int j = gen.nextInt(a.length - i) + i;
            int t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
    }

    static void generateTranslation(int[] playerToHost, int[] hostToPlayer) {
        for (int i = 0; i < playerToHost.length; i++)
            playerToHost[i] = i;

        shuffle(playerToHost);

        for (int i = 0; i < playerToHost.length; i++)
            hostToPlayer[playerToHost[i]] = i;
    }

    public static Color getColor(int i) {
        return ColorGenerator.getColor(i);
    }

    // GUI
    //
    public class GunslingerUI extends JPanel implements ActionListener {
        private static final int ArrowSize = 10;
        private static final int ImageSize = 500;
        private static final int CanvasSize = 150;
        private static final int PlayerSize = 35;

        private final Color[] PlayerColors = {Color.BLUE, Color.CYAN,
                                              Color.GREEN, Color.MAGENTA,
                                              Color.ORANGE, Color.PINK,
                                              Color.BLACK, Color.YELLOW,
                                              Color.darkGray, Color.lightGray};

        public GunslingerUI() {
            // compute player positions
            initPlayerPositions();

            // generate the relation graph in advance
            generateRelationGraph();
            
            setPreferredSize(new Dimension(1000, 600));
            setOpaque(true);
            setBackground(Color.WHITE);
        }

        // compute the positions of each player
        private void initPlayerPositions() {
            xx = new int[nplayers];
            yy = new int[nplayers];
            double x0 = CanvasSize;
            double y0 = CanvasSize;

            double offset = 2 * 3.14 / nplayers;

            for (int i = 0; i != nplayers; ++i) {
                double angle = offset * i;
                double xoffset = CanvasSize * Math.sin(angle);
                double yoffset = CanvasSize * Math.cos(angle);
                xx[i] = (int)(x0 + xoffset);
                yy[i] = (int)(y0 + yoffset);
            }
        }

        // draw an directed arrow from player i to player j
        //
        void drawArrow(Graphics g, int i, int j) {
            int r = PlayerSize / 2;
            drawArrow(g, xx[i]+r, yy[i]+r, xx[j]+r, yy[j]+r);
        }

        // draw arrow from (x1,y1) to (x2,y2)
        //
        void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
            Graphics2D g = (Graphics2D) g1.create();
            int smallOffset = 10;
            
            double dx = x2 - x1, dy = y2 - y1;
            double angle = Math.atan2(dy, dx);
            int len = (int) Math.sqrt(dx*dx + dy*dy) - smallOffset;
            AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
            at.concatenate(AffineTransform.getRotateInstance(angle));
            g.transform(at);

            // Draw horizontal arrow starting in (0, 0)
            g.setStroke(new BasicStroke(3));
            g.drawLine(smallOffset, 0, len-smallOffset, 0);
            g.fillPolygon(new int[] {len, len-ArrowSize, len-ArrowSize, len},
                          new int[] {0, -ArrowSize, ArrowSize, 0}, 4);
        }

        // generate the player graph
        //
        void drawPlayers() {
            playerImg = new BufferedImage(ImageSize, ImageSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = playerImg.createGraphics();

            Font font = new Font("Arial", Font.PLAIN, 15);
            g.setFont(font);

            for (int i = 0; i != nplayers; ++i) {
                String displayName = alive[i] ? players[i].name() : players[i].name() + "(R.I.P)";
                if (scores != null)
                    displayName = players[i].name() + "(" + scores[i] + ")";
                if (alive[i] && violation[i])
                    displayName = players[i].name() + "(Disqualifed)";

                //                Color c = alive[i] ? PlayerColors[i] : Color.GRAY;
                Color c = alive[i] ? getColor(i) : Color.GRAY;

                g.setColor(c);
                g.fillOval(xx[i], yy[i], PlayerSize, PlayerSize);
                g.drawString(displayName, xx[i], yy[i]);
            }
        }


        // generate the relation graph
        //
        void generateRelationGraph() {
            relationImg = new BufferedImage(ImageSize, ImageSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = relationImg.createGraphics();

            Font font = new Font("Arial", Font.PLAIN, 15);
            g.setFont(font);

            // draw players
            for (int i = 0; i != nplayers; ++i) {
                //                g.setColor(PlayerColors[i]);
                g.setColor(getColor(i));

                g.fillOval(xx[i], yy[i], PlayerSize, PlayerSize);
                g.drawString(players[i].name(), xx[i], yy[i]);
            }
            
            // draw friendship
            for (int i = 0; i != nplayers; ++i) {
                for (int j = 0; j != nplayers; ++j) {
                    if (relationship[i][j] == 0)
                        continue;
                    if (relationship[i][j] > 0)
                        g.setColor(Color.GREEN);
                    else if (relationship[i][j] < 0)
                        g.setColor(Color.RED);
                    drawArrow(g, i, j);
                }
            }
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            drawPlayers();

            g.drawImage(playerImg, 50, 150, null);

            if (showRelation)
                g.drawImage(relationImg, 550, 150, null);

            if (actionImg != null)
                g.drawImage(actionImg, 50, 150, null);
        }
        
        // paint each step
        //
        private void drawStep() {
            actionImg = new BufferedImage(ImageSize, ImageSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = actionImg.createGraphics();
            g2.setColor(Color.RED);
            for (int i = 0; i < nplayers; ++i) {
                int target = current[i];
                if (target >= 0)
                    drawArrow(g2, i, target);
            }

            repaint();
        }


        // Actions for buttons
        //
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == nextButton) {
                // if the game continues
                if (npeace != 10) {
                    // play a single step
                    playstep();
                    // update the screen
                    drawStep();
                }
                else {
                    // compute the scores
                    computeScores();
                    
                    printScores();

                    // Disable the Next Button
                    nextButton.setEnabled(false);
                    
                }
            }
            else if (e.getSource() == showHideButton)
                showRelation = !showRelation;

            repaint();
        }

        // entry point for the UI
        public void createAndShowGUI()
        {
            this.setLayout(null);

            // Create a frame
            mainFrame = new JFrame();
           
            nextButton = new JButton("Next"); 
            nextButton.addActionListener(this);
            nextButton.setBounds(0, 0, 100, 50);

            showHideButton = new JButton("Show/hide");
            showHideButton.addActionListener(this);
            showHideButton.setBounds(100, 0, 150, 50);

            this.add(nextButton);
            this.add(showHideButton);

            mainFrame.add(this);

            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.pack();
            mainFrame.setVisible(true);
        }
        
        // display position of each player
        private int[] xx;
        private int[] yy;

        private BufferedImage playerImg;
        private BufferedImage relationImg;
        private BufferedImage actionImg;
        private JFrame mainFrame;
        private JButton nextButton;
        private JButton showHideButton;

        private boolean showRelation;
    }

    // for friend
    private int[] mapIntToPlayer(int p, int[] a) {
        if (a == null)
            return null;
        int[] b = new int[a.length];
        for (int i = 0; i < a.length; i++)
            b[i] = hostToPlayer[p][a[i]];
        return b;
    }
    
    // for prev
    // order matters
    private int[] mapPrevToPlayer(int p, int[] a) {
        if (a == null)
            return null;
        int[] b = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            if (a[i] == -1)
                b[hostToPlayer[p][i]] = -1;
            else
                b[hostToPlayer[p][i]] = hostToPlayer[p][a[i]];
        }
        return b;
    }

    // for boolean
    private boolean[] mapAliveToPlayer(int p, boolean[] a) {
        if (a == null)
            return null;
        boolean[] b = new boolean[a.length];
        for (int i = 0; i < a.length; i++)
            b[hostToPlayer[p][i]] = a[i];
        return b;
    }

    // Play a single step
    //
    private void playstep()
    {
        round++;

        // anyone is killed in this round?
        boolean killed = false;            
        // bullets each player got
        int[] bullets = new int[nplayers];

        // create a new array
        current = new int[nplayers];

        // reset shoots
        for (int i = 0; i != nplayers; ++i)
            current[i] = -1;

        // print who is alive
        if (verbose) {
            System.err.print("Alive players: ");
            for (int p = 0; p != players.length; ++p)
                if (alive[p])
                    System.err.print(players[p].name() + "  ");
            System.err.println();
        }

        for (int p = 0; p != players.length; ++p) {
            // initialize the player's action to shoot nothing
            current[p] = -1;
            
            // skip dead player or buggy player
            if (!alive[p] || violation[p])
                continue;

            // create a local copy of the previous info
            // in case the player change the array
            // for the first round, prev = null
            int[] prev = previous == null ? null : previous.clone();
            boolean[] alivecopy = alive.clone();

            // translate the index to the player's index
            prev = mapPrevToPlayer(p, prev);
            alivecopy = mapAliveToPlayer(p, alivecopy);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            PlayTask task = new PlayTask(players[p], prev, alivecopy);
            executor.execute(task);
            
            executor.shutdownNow();

            try {
                if(!executor.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                    task.interrupt();
                    // Be generous, give the player some extra time
                    // to respond to the interrupt
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                System.err.println("Unexpected interruption in main thread");
            }


            // check if the thread has thrown an exception
            // or if the player time out
            if (!executor.isTerminated()) {
                System.err.println(players[p].name() + " is time out!");
                // disqualify the player
                violation[p] = true;
                continue;
            }
            if (task.exception != null) {
                System.err.println(players[p].name() + " has malfunctional gun, no longer shoots thereafter.");
                // disqualify the player
                violation[p] = true;
                continue;
            }

            // get the target
            int target = task.target;
            boolean valid = true;

            // map to the host index
            if (target != -1) {
                if (target >= 0 && target < nplayers)
                    target = playerToHost[p][target];
                else
                    // index out of range
                    valid = false;
            }

            valid = valid && !violation[p] && validate(p, target);

            if (valid) {
                // the player stays
                if (target < 0) {
                    current[p] = -1;
                    if (verbose)
                        System.err.println("Round " + round + ": " + players[p].name() + " did not shoot");
                }
                // the player shoots
                else {
                    bullets[target]++;
                    current[p] = target;
                        
                    if (verbose)
                        System.err.println("Round " + round + ": " + players[p].name() + " shoots " + players[target].name());

                }
            }
            else {
                System.err.println(players[p].name() + " shoots an invalid target.");
            }
        }
            
        if (verbose)
            System.err.println("-------------------------");
 
        // update game stats
        for (int p = 0; p != nplayers; ++p) {
            if (alive[p] && bullets[p] > 1) {
                alive[p] = false;
                killed = true;

                if (verbose)
                    System.err.println("Round " + round +": " + players[p].name() + " is killed");
            }
        }
            
        if (killed)
            npeace = 0;
        else
            npeace++;

        previous = current;
    }


    // play with gui
    //
    private void playgui()
    {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    GunslingerUI ui  = new GunslingerUI();
                    ui.createAndShowGUI();
                }
            });
    }

    // play with command line
    //
    private void play() 
    {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));

        // Console is incompatiable with Eclipse
        //        Console console = System.console();

        while (npeace < 10) {
            // step by step trace
            if (trace) {

                try {
                    System.err.print("$");
                    buffer.readLine();
                } catch (Exception e) {}
                // console.format("\nPress ENTER to proceed.\n");
                // console.readLine();
            }

            playstep();
        }

        computeScores();

        printScores();
    }

    
    // validate a player's shoot
    //
    private boolean validate(int p, int target)
    {
        boolean valid = false;
        String msg = null;

        // validate the shoot
        if (target == p)
            msg = "Shoot yourself";
        else if (target >= nplayers)
            msg = "Invalid target";
        else if (target > 0 && !alive[target])
            msg = "Target already dead";
        else
            valid = true;

        if (!valid) {
            System.err.println(players[p].name() + " attempted to shoot " + players[target].name() + " <validation fails>: " + msg);            
        }

        return valid;
    }

    // compute final scores
    //
    private void computeScores()
    {
        scores = new int[nplayers];

        for (int p = 0; p != nplayers; ++p) {
            if (alive[p])
                scores[p]++;
            for (int i = 0; i != nplayers; ++i)
                if ((relationship[p][i] > 0 && alive[i]) || 
                    (relationship[p][i] < 0 && !alive[i]))
                    scores[p]++;
        }            
    }

    // Print the relationshp matrix
    //
    private void printRelationship()
    {
        for (int i = 0; i != nplayers; ++i) {
            for (int j = 0; j != nplayers; ++j)
                System.err.printf("%4d", relationship[i][j]);
            System.err.println();
        }
    }

    // print game result
    //
    private void printScores()
    {
        System.err.println("##### Game result #####");

        int[] teamNo = new int[nplayers];
        int[] sortedScores = new int[nplayers];
        for (int i = 0; i != nplayers; ++i) {
            teamNo[i] = i;
            sortedScores[i] = scores[i];
        }

        // sort by highest score
        sortByScore(teamNo, sortedScores);

        // print result
        for (int i = 0; i != scores.length; ++i)
            System.err.println("Player " + players[teamNo[i]].name() + ": " + sortedScores[i]);
    }

    // sort teams by score
    //
	private static void sortByScore(int[] teams, int[] points)
	{
		for (int i = 0 ; i != teams.length ; ++i) {
			// find max
			int max = i;
			for (int j = i + 1 ; j != teams.length ; ++j)
				if (points[j] > points[max])
					max = j;
			// swap team
			int tempTeam = teams[i];
			teams[i] = teams[max];
			teams[max] = tempTeam;
			// swap points
			int teamPoints = points[i];
			points[i] = points[max];
			points[max] = teamPoints;
		}
	}

    // return the rank of each player
    private int[] rank()
    {
        int[] rank = new int[nplayers];
        int[] teamNo = new int[nplayers];
        int[] sortedScores = new int[nplayers];
        for (int i = 0; i != nplayers; ++i) {
            teamNo[i] = i;
            sortedScores[i] = scores[i];
        }
        
        // sort by highest score
        sortByScore(teamNo, sortedScores);

        int prevrank = 0;
        for (int i = 0; i != nplayers; ++i) {
            if (i > 0 && sortedScores[i] == sortedScores[i-1])
                rank[players[teamNo[i]].index] = prevrank;
            else {
                rank[players[teamNo[i]].index] = i;
                prevrank = i;
            }
        }

        return rank;
    }
    
    // random generator
    private static Random gen = new Random();
    private static String[] playerNames;

    // game configurations
    private int nenemies;
    private int nfriends;
    private int nplayers;

    // relationship
    private int[][] relationship;
    private int[][] friendship;
    private int[][] enmityship;

    private Player[] players;
    private boolean[] alive;
    private boolean[] violation;
    private int[] scores;

    
    // translation map
    private int[][] playerToHost;
    private int[][] hostToPlayer;


    // round num
    private int round = 0;
    // number of consecutive peaceful days
    private int npeace = 0;
    // prevous round info
    private int[] previous = null;
    // current round info
    private int[] current;
}
