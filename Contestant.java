import java.util.Random;
import java.util.concurrent.Semaphore;

public class Contestant implements Runnable
{
	Random randomNum1 = new Random(System.currentTimeMillis());
	Random randomNum = new Random(randomNum1.nextInt());
	Thread myThread;
	public static long time = System.currentTimeMillis();
	static long startTime =  System.currentTimeMillis();
	protected int numberGuessed = 0;
	protected boolean selected = false;
	int room_capacity;
	int initial_num_contestants;
	Semaphore mutex;
	Semaphore room;
	Semaphore exam;
	Semaphore roomReady;
	Semaphore results;
	Semaphore introduce;
	Semaphore readyToPlay;
	Semaphore waitingForAnswer;
	Semaphore[] contestantsWait;
	//keeps track of the amount of contestants that have selected a number
	protected static int counter = 0;
	//keeps track of how many of the contestants have introduced themselves
	protected static int introCounter = 0;
	private int score = 0;
	private int wager = 0;
	//keeps track of which contestant (0, 1 or 2) this contestant is
	int contestantNum;
	//keeps track of the number of contestants that didn't make it to final jeopardy
	private static int leftNumber = 0;
	//keeps track of how many contestants are ready for the final jeopardy question
	private static int contestantsReady = 0;
	
	//Constructor
	public  Contestant(String threadName, int room_capacity, int initial_num_contestants, Semaphore mutex, Semaphore exam, Semaphore roomReady, Semaphore results, Semaphore room, 
			Semaphore introduce, Semaphore readyToPlay, Semaphore waitingForAnswer, Semaphore[] contestantsWait)
	{
		myThread = new Thread(this, threadName);
		myThread.start();
		this.room_capacity = room_capacity;
		this.initial_num_contestants = initial_num_contestants;
		this.mutex = mutex;
		this.exam = exam;
		this.roomReady = roomReady;
		this.results = results;
		this.room = room;
		this.introduce = introduce;
		this.readyToPlay = readyToPlay;
		this.waitingForAnswer = waitingForAnswer;
		this.contestantsWait = contestantsWait;
	}
	
	public void run() 
	{
		//P(mutex)
		try {
			mutex.acquire();
		} catch (InterruptedException e1) {
			
			e1.printStackTrace();
		}
		//increment the counter
		counter++;
		//create a group of room_capacity
		if(counter%room_capacity != 0 && counter != initial_num_contestants)
		{
			//V(mutex)
			mutex.release();
			//P(room)
			try {
				room.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}		
			//wait for exam to start
			//P(exam)
			try {
				exam.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else
		{	
			//V(room)
			for(int i = 0; i < room_capacity - 1; i++)
			{
				room.release();
			}
			//V(mutex)
			mutex.release();
			if(counter == initial_num_contestants)
			{
				//let the announcer know that all the contestants are seated in the room
				//V(roomReady)
				roomReady.release();
			}
			//wait for exam to start
			//P(exam)
			try {
				exam.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		try {
			myThread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//P(results)
		try {
			results.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		if(selected == false)
		{
			this.msg("Good-bye");
			return;
		}
		else if(selected == true)
		{
			//P(introduce)
			try {
				introduce.acquire();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			
			//introduce yourself
			//P(mutex)
			try {
				mutex.acquire();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			this.msg(": Hi, my name is " + myThread.getId());
			introCounter++;
			//V(mutex)
			mutex.release();
			if(introCounter == 3)
			{
				//V(readyToPlay)
				readyToPlay.release();
			}
			
			//regular jeopardy
			while(!Host.getFinalJeopardy())
			{
				//P(contestantsWait)
				try {
					contestantsWait[contestantNum].acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(Host.getFinalJeopardy())
				{
					//P(contestantsWait)
					try {
						contestantsWait[contestantNum].acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					break;
				}
				this.msg("Here is my answer.");
				//V(waitingForAnswer)
				waitingForAnswer.release();
			}
			
			
			//final jeopardy
			if(this.score > 0)
			{
				//increment the amount of ready contestants
				//P(mutex)
				try {
					mutex.acquire();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				contestantsReady++;
				wager = randomNum.nextInt(score+1);
				if(contestantsReady + leftNumber == 3)
				{
					//V(waitingForAnswer)
					waitingForAnswer.release();
				}
				//V(mutex)
				mutex.release();
				//P(contestantsWait)
				try {
					contestantsWait[contestantNum].acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else
			{
				this.msg("Good-bye");
				//keeps track of how many contestants did not participate in final jeopardy
				//P(Mutex)
				try {
					mutex.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				leftNumber++;
				if(leftNumber + contestantsReady == 3)
				{
					waitingForAnswer.release();
				}
				mutex.release();
			}
				
		}
	}
	
	public String getName()
	{
		return myThread.getName();
	}
	
	public void incrementScore(int value)
	{
		score+=value;
	}
	
	public void setSelected(boolean value)
	{
		selected = value;
	}
	
	public void decrementScore(int value)
	{
		score-=value;
	}
	
	public int getScore()
	{
		return score;
	}
	
	public int getWager()
	{
		return this.wager;
	}
	
	public void incScore(int inc)
	{
		this.score = this.score += inc;
	}
	
	//allow access to the thread
	public Thread getThread()
	{
		return myThread;
	}
	
	public void msg(String m) {
		System.out.println(myThread.getName()+" ["+(System.currentTimeMillis()-time)+"] "+": " + m);
		}
	
	protected  final long age() 
	{
		return System.currentTimeMillis() - startTime; 
	}

}
