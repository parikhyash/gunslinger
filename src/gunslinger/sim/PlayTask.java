package gunslinger.sim;

import gunslinger.sim.Player;

class PlayTask extends Thread
{
    // the player
    Player player;
    // arguments
    int[] prev;
    boolean[] alive;
    // result
    int target = -1;
    // save exception
    Exception exception;

    public PlayTask(Player player, int[] prev, boolean[] alive) {
        this.player = player;
        this.prev = prev;
        this.alive = alive;
    }

    public void run() {
        try {
            target = player.shoot(prev, alive);
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }
    }
}
