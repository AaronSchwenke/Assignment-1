import java.util.Arrays;
import java.lang.System;
import java.lang.Math;

public class NonParallelComparison
{
    public static void main(String[] args)
    {
        long startTime = System.nanoTime();
        int max = 100000000;
        double sqrtmax = Math.sqrt(max) + 0.0000001;
        boolean[] sieve = new boolean[max + 1];
        Arrays.fill(sieve, true);

        for (int i=2; i<=sqrtmax; i++)
        {
            if (sieve[i])
            {
                int j = 2;
                int product = i*j;
                while (product <= max)
                {
                    sieve[product] = false;
                    j++;
                    product = i*j;
                }
            }
        }

        int count = 0;
        long sum = 0;
        int[] largest = new int[10];
        int j = 0;

        for (int i=max; i>=2; i--)
        {
            if (sieve[i])
            {
                count++;
                sum += i;
                if (j < 10)
                {
                    largest[j++] = i;
                }
            }
        }

        long executionTime = System.nanoTime() - startTime;

        System.out.println(count);
        System.out.println(sum);
        System.out.println(Arrays.toString(largest));
        System.out.println(executionTime / 1E+9);
    }
}