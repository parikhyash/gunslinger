package gunslinger.g1;

import java.util.*;
import gunslinger.g1.Player;

public class Test
{
    private static gunslinger.g1.Player player;
    public static void main(String[] args){
        player = new gunslinger.g1.Player();
        player.init(5, new int[]{2,3}, new int[]{4});

        System.out.println("Testing player " + player.name());

        boolean fail = false;

        String out = shootsEnemyOnFirstRound();
        if (out != "") {
          fail = true;
          System.out.println(out);
        }
        out = containsForArrays();
        if (out != "") {
          fail = true;
          System.out.println(out);
        }

        if (fail) {
            System.out.println("FAIL");
            return;
        }

        System.out.println("Passes");
    }

    private static String shootsEnemyOnFirstRound(){
        int target = player.shoot(new int[]{0,0,0,0,0}, new boolean[]{true, true, true, true, true});
        if (target != 4)
            return "Shot player " + target + " instead of player 4";
        return "";
    }

    private static String containsForArrays(){
        boolean contains = player.contains(2, new int[]{1,2,3});
        if (!contains) return "said [1,2,3] doesnt contain 2";
        contains = player.contains(4, new int[]{1,2,3});
        if (contains) return "said [1,2,3] contains 4";
        return "";
    }
}
