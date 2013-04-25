import java.util.concurrent.Semaphore;


public class CS340Project2 
{
	
	public static void main(String args[])
	{
		Integer numRounds;
		Integer numQuestions;
		Integer questionValues;
		Double rightPercent;
		Integer room_capacity;
		Integer initial_num_contestants;
		Semaphore mutex = new Semaphore(1);
		Semaphore exam = new Semaphore(0);
		Semaphore roomReady = new Semaphore(0);
		Semaphore results = new Semaphore(0);
		Semaphore room = new Semaphore(0);
		Semaphore introduce = new Semaphore(0);
		Semaphore readyToPlay = new Semaphore(0);
		Semaphore gameHasBegun = new Semaphore(0);
		Semaphore waitingForAnswer = new Semaphore(0);
		Semaphore[] contestantsWait = new Semaphore[3];
		for(int i = 0; i < 3; i++)
		{
			contestantsWait[i] = new Semaphore(0);
		}
		
		//try to take numRounds from command line args[0], otherwise use 3
		try
		{
			numRounds = new Integer(args[0]);
		}catch(Exception e){
			numRounds = new Integer(3);
		}
		//try to take numQuestions from command line args[1], otherwise use 6
		try
		{
			numQuestions = new Integer(args[1]);
		}catch(Exception e){
			numQuestions = new Integer(6);
		}
		//try to take questionValues from command line args[2], otherwise use 200
		try
		{
			questionValues = new Integer(args[2]);
		}catch(Exception e){
			questionValues = new Integer(200);
		}
		//try to take rightPercent from command line args[3], otherwise use 0.70
		try
		{
			rightPercent = new Double(args[3]);
		}catch(Exception e){
			rightPercent = new Double(0.70);
		}
		//try to take room_capacity from command line args[4], otherwise use 4
		try
		{
			room_capacity = new Integer(args[4]);
		}catch(Exception e){
			room_capacity = new Integer(4);
		}
		//try to take initial_num_contestants from command line args[5], otherwise use 10
		try
		{
			initial_num_contestants = new Integer(args[5]);
		}catch(Exception e){
			initial_num_contestants = new Integer(10);
		}
		//create the announcer
		Announcer announcer = new Announcer("Announcer", numRounds.intValue(), numQuestions.intValue(), questionValues.intValue(), rightPercent.doubleValue(), 
				room_capacity.intValue(), initial_num_contestants.intValue(), mutex, exam, roomReady, results, room, introduce, readyToPlay, gameHasBegun, waitingForAnswer,
				contestantsWait);
	}
}
