import scala.Tuple4;
import java.util.ArrayList;

/**
 * A static class to make communication between scala and Java code easier
 */

public class Converter
{
    public Converter() {}

    /**
     * Returns a Java-ArrayList containing your houses and their current seed number
     * @return The list of numbers of seeds in the houses of you
     */
    static ArrayList<Integer> getMyHouses(Tuple4<Iterable<Object>, Iterable<Object>, Object, Object> obj, boolean playerOne)
    {
        Iterable<Object> houses;
        if(playerOne==true)
        {
            houses = obj._1();
        }
        else
        {
            houses = obj._2();
        }

        ArrayList<Integer> housesJava=new ArrayList<>();
        houses.forEach(item->housesJava.add((Integer)item));

        return housesJava;
    }

    /**
     * Returns a Java-ArrayList containing enemy houses and their current seed number
     * @return The list of numbers of seeds in the houses of enemy
     */
    static ArrayList<Integer> getEnemyHouses(Tuple4<Iterable<Object>, Iterable<Object>, Object, Object> obj, boolean playerOne)
    {
        Iterable<Object> houses;
        if(playerOne==true)
        {
            houses = obj._2();
        }
        else
        {
            houses = obj._1();
        }

        ArrayList<Integer> housesJava=new ArrayList<>();
        houses.forEach(item->housesJava.add((Integer)item));

        return housesJava;
    }

    /**
     * Returns a Java-Integer representing the seeds in your store
     * @return The list of numbers of seeds in the your store
     */
    static Integer getMyStoreSeeds(Tuple4<Iterable<Object>, Iterable<Object>, Object, Object> obj, boolean playerOne)
    {
        if(playerOne==true)
        {
            return (Integer) obj._3();
        }
        return (Integer) obj._4();
    }

    /**
     * Returns a Java-Integer representing the seeds in enemy store
     * @return The list of numbers of seeds in the enemy store
     */
    static Integer getEnemyStoreSeeds(Tuple4<Iterable<Object>, Iterable<Object>, Object, Object> obj, boolean playerOne)
    {
        if(playerOne==true)
        {
            return (Integer) obj._4();
        }
        return (Integer) obj._3();
    }

    /**
     * Returns a Java-ArrayList containing your houses and their current seed number
     * @return The list of numbers of seeds in the houses of you
     */
    static ArrayList<Integer> getMyHouses(Board board, boolean playerOne)
    {
        return getMyHouses(board.getState(),playerOne);
    }

    /**
     * Returns a Java-ArrayList containing enemy houses and their current seed number
     * @return The list of numbers of seeds in the houses of enemy
     */
    static ArrayList<Integer> getEnemyHouses(Board board, boolean playerOne)
    {
        return getEnemyHouses(board.getState(),playerOne);
    }

    /**
     * Returns a Java-Integer representing the seeds in your store
     * @return The list of numbers of seeds in the your store
     */
    static Integer getMyStoreSeeds(Board board, boolean playerOne)
    {
        return getMyStoreSeeds(board.getState(),playerOne);
    }

    /**
     * Returns a Java-Integer representing the seeds in enemy store
     * @return The list of numbers of seeds in the enemy store
     */
    static Integer getEnemyStoreSeeds(Board board, boolean playerOne)
    {
        return getEnemyStoreSeeds(board.getState(),playerOne);
    }
}
