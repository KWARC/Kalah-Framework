import java.util.ArrayList;

public class VictoryPlayer extends Agent
{
    ArrayList<String> myStudents;
    String myName;
    Board board;
    boolean playerOne;

    public VictoryPlayer(String name)
    {
        myStudents=new ArrayList<>();
        this.myName=name;
        myStudents=new ArrayList<String>();
        myStudents.add("Max Musterman");
        myStudents.add("Maria Musterfrau");
    }

    @Override
    public void init(Board board, boolean playerOne)
    {
        this.board=board;
        this.playerOne=playerOne;

        // Get my houses and their seeds, index starting at 0
        ArrayList<Integer> myHouses=Converter.getMyHouses(board,playerOne);

        // Do some fancy stuff, e.g. "Calculate the mean" (Hell... It is just a simple example do not expect too much :D)
        int meanSeed=0;
        for (int i=0;i<myHouses.size();i++)
        {
            meanSeed+=myHouses.get(i);
        }
        meanSeed/=myHouses.size();

        // Print mean
        System.out.println("Mean: "+meanSeed);
    }

    @Override
    public int move()
    {
        ArrayList<Integer> myHouses=Converter.getMyHouses(board,playerOne);

        for(int i=0;i<myHouses.size();i++)
        {
            if(myHouses.get(i)>0)
            {
                return i+1;
            }
        }
        return 1;
    }

    @Override
    public String name() {
        return myName;
    }

    @Override
    public Iterable<String> students() {
        return myStudents;
    }
}
