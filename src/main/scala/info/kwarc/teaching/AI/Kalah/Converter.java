import scala.Tuple4;
import java.util.ArrayList;

public class Converter
{
    public Converter() {}

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

    static Integer getMyStoreSeeds(Tuple4<Iterable<Object>, Iterable<Object>, Object, Object> obj, boolean playerOne)
    {
        if(playerOne==true)
        {
            return (Integer) obj._3();
        }
        return (Integer) obj._4();
    }

    static Integer getEnemyStoreSeeds(Tuple4<Iterable<Object>, Iterable<Object>, Object, Object> obj, boolean playerOne)
    {
        if(playerOne==true)
        {
            return (Integer) obj._4();
        }
        return (Integer) obj._3();
    }

    static ArrayList<Integer> getMyHouses(Board board, boolean playerOne)
    {
        return getMyHouses(board.getState(),playerOne);
    }

    static ArrayList<Integer> getEnemyHouses(Board board, boolean playerOne)
    {
        return getEnemyHouses(board.getState(),playerOne);
    }

    static Integer getMyStoreSeeds(Board board, boolean playerOne)
    {
        return getMyStoreSeeds(board.getState(),playerOne);
    }

    static Integer getEnemyStoreSeeds(Board board, boolean playerOne)
    {
        return getEnemyStoreSeeds(board.getState(),playerOne);
    }
}
