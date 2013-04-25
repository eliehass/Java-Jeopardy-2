import java.util.Random;
import java.util.concurrent.Semaphore;

public class Announcer implements Runnable
{
	Random randomNum = new Random(System.currentTimeMillis());
	Thread myThread;
	public static long time = System.currentTimeMillis();
	static long startTime =  System.currentTimeMillis();
	protected static boolean doneSelecting = false;
	private static 	Contestant selectedContestants[] = new Contestant[3];
	int numRounds;
	int numQuestions;
	int questionValues;
	double rightPercent; 
	int room_capacity;
	int initial_num_contestants;
	Semaphore mutex;
	Semaphore exam;
	Semaphore roomReady;
	Semaphore results;
	Semaphore room;
	Semaphore introduce;
	Semaphore readyToPlay;
	Semaphore gameHasBegun;
	Semaphore waitingForAnswer;
	Semaphore[] contestantsWait;
	
	//Constructor
	public  Announcer(String threadName, int numRounds, int numQuestions, int questionValues, double rightPercent, int room_capacity, int initial_num_contestants, Semaphore mutex, 
			Semaphore exam, Semaphore roomReady, Semaphore results, Semaphore room, Semaphore introduce, Semaphore readyToPlay, Semaphore gameHasBegun, Semaphore waitingForAnswer,
			Semaphore[] contestantsWait)
	{
		myThread = new Thread(this, threadName);
		myThread.start();
		this.numRounds = numRounds;
		this.numQuestions = numQuestions;
		this.questionValues = questionValues;
		this.rightPercent = rightPercent;
		this.room_capacity = room_capacity;
		this.initial_num_contestants = initial_num_contestants;
		this.mutex = mutex;
		this.exam = exam;
		this.roomReady = roomReady;
		this.results = results;
		this.room = room;
		this.introduce = introduce;
		this.readyToPlay = readyToPlay;
		this.gameHasBegun = gameHasBegun;
		this.waitingForAnswer = waitingForAnswer;
		this.contestantsWait = contestantsWait;
	}
	
	public void run() 
	{
		Contestant contestantArray[] = new Contestant[10];
		//create contestants
		for(int i = 0; i < 10; i++)
		{
			contestantArray[i] = new Contestant("contestant" + i, room_capacity, initial_num_contestants, mutex, exam, roomReady, results, room, introduce, readyToPlay, waitingForAnswer,
					contestantsWait);
		}
		
		//wait for the exam room to fill to capacity
		//P(roomReady)
		try {
			roomReady.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//start the exam
		//V(exam)
		exam.release(initial_num_contestants);
		try {
			myThread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//when the contestants are done selecting numbers
		int max = 0;
		int index = 0;
			
		//generate initial_num_contestants random numbers
		int randNums[] = new int[initial_num_contestants];
		for (int i = 0; i < initial_num_contestants; i++)
		{
				randNums[i] = randomNum.nextInt(1001);
		}
				
		//pick the 3 winners
		for(int i = 0; i < 3; i++)
		{
			max = 0;
			for(int j = 0; j < initial_num_contestants; j++)
			{
				//find the max number guessed. makes sure you don't look in a null element of the array
				if(contestantArray[j] != null && randNums[j] >= max)
				{
					index = j;
					max = randNums[j];
				}
			}
			//put the contestant with the highest number into the selected contestants array
			selectedContestants[i] = contestantArray[index];
			selectedContestants[i].setSelected(true);
			//remove the selected contestant from the contestantArray.
			contestantArray[index] = null;
			this.msg(selectedContestants[i].getName() + " has been selected.");
		}
		for(int i = 0; i < initial_num_contestants; i++)
			if(contestantArray[i] != null)
				this.msg(contestantArray[i].getName() + " has been eliminated.");

		try {
			myThread.sleep(randomNum.nextInt(5001));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//start the show
		this.msg("Announcer: Ok contestants, it's time to intoduce yourselves!");
		
		results.release(initial_num_contestants);
		
		
		//give each contestant a unique number
		for(int i = 0; i < 3; i++)
		{
			selectedContestants[i].contestantNum = i;
		}
		
		//V(introduce)
		introduce.release(3);
			
		//create the Host
		Host theHost = new Host("theHost", numRounds, numQuestions, rightPercent, questionValues, gameHasBegun, waitingForAnswer, contestantsWait);
		
		//P(readyToPlay)
		try {
			readyToPlay.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		this.msg("Announcer: Ok, now it's time to play JEOPARDY!");
		
		//V(gameHasBegun)
		gameHasBegun.release();
	}
	
	public void msg(String m) {
		System.out.println(myThread.getName()+" ["+(System.currentTimeMillis()-time)+"] "+": "); System.out.println(m);
		}
	
	public static Contestant[] getSelectedContestants()
	{
		return selectedContestants;
	}
	
	protected static final long age() 
	{
		return System.currentTimeMillis() - startTime; 
	}
}
