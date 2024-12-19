import java.util.*;


public class ProtocolMP{

    public Session session;

    ProtocolMP(Session s){
        this.session = s;
    }

    public int TryWS(int x, int y){

        int choice = (x*7) + y;

        if(session.getIsInitialized() == false){
            Initialisation(choice);
            session.setIsInitialized(true);
        }

        char value = session.getGameState().get(choice)[0];
        List<char[]> game = session.getGameState();
        if(value == 'B')
        {
            for(int i = 0; i<49; i++){
                game.get(i)[1] = 'S';
            }
            session.setGameState(game);
            return 1;
        }
        else if(game.get(choice)[1] != 'S'){

            if(value == '0'){
                revealZeroCell(game, x, y);
                session.setGameState(game);
                if(session.getUnrevealedCell() <= 7)
                    return 2;
                else
                    return 0;
            }
            else{
                session.getGameState().get(choice)[1] = 'S';
                session.setUnrevealedCell(session.getUnrevealedCell() - 1);
                if(session.getUnrevealedCell() <= 7)
                    return 2;
                else
                    return 0;
            }
        }
        else
            return 0;

        
    }

    public void FlagWS(int x, int y){
        int choice = x * 7 + y;

        List<char[]> game = session.getGameState();

        if(game.get(choice)[1] == 'S')
            {
                return ;
            } 
            else if(game.get(choice)[1] == 'H') {
                game.get(choice)[1] = 'F';
                
            }
            else{
                game.get(choice)[1] = 'H';
                
            }
            session.setGameState(game);
    }


    private void Initialisation(int choice){

        try{
            List<char[]> game = new ArrayList<>();

            //initialisation of the game
            char[] emplacement;
            //generation of the bomb
            Random random = new Random();
            int i = 0, nombre, BombNumber;
            while (i<7) {
                nombre = random.nextInt(49);

                //we  put the bomb on the game board
                if(nombre != choice && game.get(nombre)[0] != 'B'){
                    emplacement = game.get(nombre);
                    emplacement[0] = 'B';
                    i++;

                }
            }
            //we put the right number of every emplacement

            for(int j = 0; j<49; j++)
            {
                emplacement = game.get(j);
                BombNumber = 0;

                if(emplacement[0] != 'B')
                {
                    //we check if there is a bomb up of the emplacement

                    //we check up
                    if(j>6 && game.get(j-7)[0] == 'B')
                        BombNumber++;
                        
                    //we check up right
                    if((j%7+1) !=7 && j>6 && game.get(j-6)[0] == 'B')
                        BombNumber++;
                    //we check up left
                    if(j>7 && (j%7) != 0 && game.get(j-8)[0] == 'B')
                        BombNumber++;

                    //we check left
                    if((j%7) != 0 && game.get(j-1)[0] == 'B' )
                        BombNumber++;

                    //we check right
                    if((j%7+1) != 7 && game.get(j+1)[0] == 'B')
                        BombNumber++;

                    //we check down left
                    if(j < 42 && (j%7) != 0  && game.get(j+6)[0] == 'B')
                        BombNumber++;

                    //we check down
                    if(j<42 && game.get(j+7)[0] == 'B')
                        BombNumber++;

                    //we check down right
                    if(j<41 && (j%7+1) != 7 && game.get(j+8)[0] == 'B')
                        BombNumber++;

                    //we put the right value in the emplacement 
                    if(BombNumber != 0)
                    {
                        game.get(j)[0] = (char)(BombNumber + '0');
                    }
                }
            }

            session.setGameState(game);


        }
        catch(Exception e){
            System.err.println("Error Try WS Init: " + e);
        }
    }

    public void revealZeroCell(List<char[]> game, int line, int row){
        

        int cell = line*7 + row;
        
        
        if(game.get(cell)[1] == 'S')
            return;


        if(game.get(cell)[0] != '0')
        {
            game.get(cell)[1] = 'S';
            session.setUnrevealedCell(session.getUnrevealedCell() -1);
            
        }
        else
        {
            game.get(cell)[1] = 'S';
            session.setUnrevealedCell(session.getUnrevealedCell() -1);
            
            //up
            if(line>0)
            revealZeroCell(game, line-1, row);
            //up left
            if(line>0 && row>0)
                revealZeroCell(game,line-1, row-1);
            //up right
            if(line>0 && row < 6)
                revealZeroCell(game,line-1, row+1);

            //left
            if(row>0)
                revealZeroCell(game,line, row-1);
            //right
            if(row <6)
                revealZeroCell(game, line, row+1);
            //down
            if(line < 6)
                revealZeroCell(game, line+1, row);
            //down left
            if(line <6 && row >0)
                revealZeroCell(game, line+1, row-1);
            //down right
            if(line <6 && row < 6)
                revealZeroCell(game, line+1, row+1);
            
        }

    }
}