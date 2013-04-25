import java.util.Random;
import java.util.concurrent.Semaphore;

public class Host implements Runnable
{
	Random randomNum = new Random(System.currentTimeMillis());
	Thread myThread;
	public static long time = System.currentTimeMillis();
	private int numRounds;
	private int numQuestions;
	private int questionValues;
	private double rightPercent;
	private int currentRound = 1;
	private int currentQuestion = 1;
	private static Contestant[] contestants;
	private static boolean finalJeopardy = false;
	private static boolean finalQuestion = false;
	Semaphore gameHasBegun;
	Semaphore waitingForAnswer;
	Semaphore[] contestantsWait;
	
	public  Host(String threadName,int numRounds, int numQuestions, double rightPercent, int questionValues, Semaphore gameHasBegun, Semaphore waitingForAnswer, Semaphore[] contestantsWait)
	{
		//takes the array of selected contestants from the announcer
		contestants = Announcer.getSelectedContestants();
		myThread = new Thread(this, threadName);
		myThread.start();
		this.gameHasBegun = gameHasBegun;
		this.numRounds = numRounds;
		this.numQuestions = numQuestions;
		this.rightPercent = rightPercent;
		this.questionValues = questionValues;
		this.waitingForAnswer = waitingForAnswer;
		this.contestantsWait = contestantsWait;
	}
	
	public void run()
	{
		int answeringContestant = 0;
			
		//P(gameHasBegun)
		try {
			gameHasBegun.acquire();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		while(currentRound <= numRounds)
		{
			this.msg("Lets begin round " + currentRound);
			currentQuestion = 1;
			while(currentQuestion <= numQuestions)
			{
				this.msg("And now here is your question!");
				//pick a contestant to answer
				answeringContestant = randomNum.nextInt(3);
				contestantsWait[answeringContestant].release();
				//P(waitingForAnswer)
				try {
					waitingForAnswer.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				//decide if the answer was correct
				if(randomNum.nextInt(100) > (rightPercent * 100))
				{
					this.msg(contestants[answeringContestant].getName() + ", that's correct!");
					contestants[answeringContestant].incrementScore(questionValues);
				}
				else
				{
					this.msg("I'm sorry, " + contestants[answeringContestant].getName() + ", that's incorrect...");
					contestants[answeringContestant].decrementScore(questionValues);
				}
				currentQuestion++;
			}
			currentRound++;
		}
			
		//final Jeopardy
		finalJeopardy = true;
		
		for(int i = 0; i < 3; i++)
		{
			contestantsWait[i].release();
		}
		for(int i = 0; i < 3; i++)
		{
			contestantsWait[i].release();
		}
		
		//wait for the contestants to wager
		//P(waitingForAnswer)
		try {
			waitingForAnswer.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		

		this.msg("Here is the final jeopardy question!");
		
		//update the scores
		for(int i = 0; i < 3; i++)
		{
			contestants[i].incScore(contestants[i].getWager());
		}

		
		//print the scores
		this.msg("The scores are in");
		for(int i = 0; i < 3; i++)
		{
			this.msg(contestants[i].getName() + " has a score of " + contestants[i].getScore());
		}
		
		//determine the winner
		int maxScore = contestants[0].getScore();
		int winnerIndex = 0;
		for(int i = 1; i < 3; i++)
		{
			if(contestants[i].getScore() >= maxScore)
			{
				maxScore = contestants[i].getScore();
				winnerIndex = i;
			}
		}
		
		this.msg("And the winner is: " + contestants[winnerIndex].getName());

	}
	
	public void msg(String m) {
		System.out.println(myThread.getName()+" ["+(System.currentTimeMillis()-time)+"] "+": "); System.out.println(m);
		}
	
	//checks if it's final Jeopardy
	public static boolean getFinalJeopardy()
	{
		return finalJeopardy;
	}
	
	public void interrupt()
	{
		myThread.interrupt();
	}
}
