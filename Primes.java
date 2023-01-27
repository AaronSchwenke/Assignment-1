import java.lang.Math;
import java.lang.System;
import java.util.Arrays;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Primes
{
    public static final int maxValue = 100000000;
    public static final int numThreads = 8;
    public static final boolean[] sieve = new boolean[maxValue+1];
    public static final PrimeFinder[] allFinders = new PrimeFinder[numThreads];
    public static final Thread[] allThreads = new Thread[numThreads];
    public static double sqrtMaxValue = Math.sqrt(maxValue) + 0.0000001;
    public static void main(String[] args)
    {
        long startTime = System.nanoTime();
        Arrays.fill(sieve, true);
        int sqrt = (int)Math.sqrt(maxValue + 0.00001);
        if ((sqrt * sqrt) > maxValue)
            sqrt--;
        sqrtMaxValue = sqrt;

        for (int i=0; i<numThreads; i++)
        {
            Primes.allFinders[i] = new PrimeFinder(2+i);
            Primes.allThreads[i] = new Thread(Primes.allFinders[i]);
            Primes.allThreads[i].start();
        }

        for (int i=0; i<numThreads; i++)
        {
            try
            {
                Primes.allThreads[i].join();
            }
            catch (Exception e)
            {

            }
        }

        File outputFile = new File("primes.txt");

        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) * 1E-9;

        try
        {
            FileWriter output = new FileWriter(outputFile);
            output.write("Execution Time: " + executionTime+ "\n");
            output.write("Count: " + PrimeFinder.sharedCalculator.getPrimeCount() + "\n");
            output.write("Sum: " + PrimeFinder.sharedCalculator.getPrimeSum()+ "\n");
            output.write("10 Largest: ");
            for (int i=9; i>=1; i--)
            {
                output.write(PrimeFinder.sharedCalculator.getLargestPrimes()[i] + ", ");
            }
            output.write("" + PrimeFinder.sharedCalculator.getLargestPrimes()[0]);
            output.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

class PrimeFinder implements Runnable
{
    public Counter counter;
    public static SharedCalculator sharedCalculator = new SharedCalculator();
    private boolean isFinished = false, isAsleep = false;
    private long partialSum = 0;
    private int partialCount = 0;
    private int[] partialLargest = new int[10];

    public PrimeFinder(int start)
    {
        counter = new Counter(start);
    }

    public void run()
    {
        int curNum = this.counter.getAndAdd(Primes.numThreads);
        while (curNum < Primes.sqrtMaxValue)
        {
            siftPrimes(curNum);
            curNum = this.counter.getAndAdd(Primes.numThreads);
        }
        this.isFinished = true;
        helpOthersSift();
        sleepUnlessAlone();

        curNum = this.counter.getAndAdd(-1);
        int largestCount = 0;
        while (curNum >= 2)
        {
            if (Primes.sieve[curNum] == true)
            {
                this.partialSum += curNum;
                this.partialCount++;
                if (largestCount < 10)
                    this.partialLargest[largestCount++] = curNum;
            }

            curNum -= Primes.numThreads;
        }

        sharedCalculator.addToCount(this.partialCount);
        sharedCalculator.addToSum(this.partialSum);
        sharedCalculator.keepTenLargest(this.partialLargest);
    }

    public void siftPrimes(int factor)
    {
        if (Primes.sieve[factor] == true)
            for (int product=2*factor; product<=Primes.maxValue; product+=factor)
            {
                Primes.sieve[product] = false;
            }
        
        return;
    }

    public void helpOthersSift()
    {
        for (int i=0; i<Primes.numThreads; i++)
        {
            PrimeFinder curFinder = Primes.allFinders[i];
            if (curFinder.isFinished == false)
            {
                int curNum = curFinder.counter.getAndAdd(Primes.numThreads);
                while (curNum < Primes.sqrtMaxValue)
                {
                    siftPrimes(curNum);
                    curNum = curFinder.counter.getAndAdd(Primes.numThreads);
                }
            }
        }

        return;
    }

    public synchronized void sleepUnlessAlone()
    {
        this.isAsleep = true;
        boolean allAsleep = true;
        for (int i=0; i<Primes.allFinders.length; i++)
        {
            if (!Primes.allFinders[i].getIsAsleep())
            {
                allAsleep = false;
                try
                {
                    this.wait();
                }
                catch (Exception e)
                {
                }
            }
        }

        if (allAsleep)
            for (int i=0; i<Primes.allFinders.length; i++)
            {
                Primes.allFinders[i].counter.setCount(Primes.maxValue - i);
                Primes.allThreads[i].interrupt();
            }

        return;
    }

    public boolean getIsAsleep()
    {
        return this.isAsleep;
    }
}

class SharedCalculator
{
    private long primeSum;
    private int primeCount;
    private int[] largestPrimes;

    public SharedCalculator()
    {
        this.primeSum = 0;
        this.primeCount = 0;
        this.largestPrimes = new int[10];
    }

    public synchronized void addToSum(long partialSum)
    {
        this.primeSum += partialSum;
        return;
    }

    public synchronized void addToCount(int partialCount)
    {
        this.primeCount += partialCount;
        return;
    }

    public synchronized void keepTenLargest(int[] samples)
    {
        for (int i=0; i<samples.length; i++)
            keepLargest(samples[i]);
    }

    public void keepLargest(int sample)
    {
        for (int i=0; i<this.largestPrimes.length; i++)
            if (sample > this.largestPrimes[i])
            {
                for (int j=this.largestPrimes.length-1; j>i; j--)
                {
                    largestPrimes[j] = largestPrimes[j-1];
                }
                largestPrimes[i] = sample;
                break;
            }
        
            return;
    }

    public long getPrimeSum()
    {
        return this.primeSum;
    }

    public int getPrimeCount()
    {
        return this.primeCount;
    }

    public int[] getLargestPrimes()
    {
        return largestPrimes;
    }
}

class Counter
{
    private int count;

    public Counter(int start)
    {
        this.count = start;
    }

    public synchronized int getAndAdd(int num)
    {
        int temp = count;
        count += num;
        return temp;
    }

    public synchronized void setCount(int num)
    {
        this.count = num;
        return;
    }
}
