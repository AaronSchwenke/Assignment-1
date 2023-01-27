# Assignment-1

## Compile/Run Instructions
Type the following lines into the command line while in the same directory as the project:
javac Primes.java
java Primes

## Proof of Correctness
To confirm the correctness of the concurrent prime calculations, I compared the outputted prime count and largest prime values to those that are available online such as on http://compoasso.free.fr/primelistweb/page/prime/liste_online_en.php. I also made a nonconcurrent version of the assignment to compare the outputted values to, including the sum of the primes. Primes.java was tested multiple times with the same results (barring execution time).

Also, the code is written such that the prime numbers are discovered through counters which can only be accessed by one thread at a time. These counters collectively reach each number from 2 to the max value, ensuring all primes are discovered. Then, the threads wait until completion is verified so that the next computations will not misidentify a prime. Each thread creats its own sum of primes, count of primes, and largest primes list which are combined using a shared calculator which only one thread can access at a time. These values are easily combined to produce accurate results.

## Efficiency/Evaluation
Primes.java on my machine runs in about 0.4-0.5 seconds whereas the non-concurrent version tends to run in about 0.85-0.95 seconds. While using the eratosthenes sieve, each thread is able to evaluate a different factor and edit the sieve at the same time. Afterwards, each thread helps to finish the factors ahead of the other threads to ensure they work roughly evenly.

When working on the measurements, the threads have static loads because dynamic loads require locking for each iteration which is more expensive than allowing the threads to work uninterrupted and completing simple calculations (confirmed via running both solutions). However, the threads all start at roughly the same value and move in increments equal to the number of threads since more primes are found near smaller values and this method ensures they work through very similarly sized values. Most of the work is therefore parallel, with the exception of the time it takes to lock and unlock the counter for populating the sieve, time spent waiting to confirm completion of other threads, and initialization plus printing.
