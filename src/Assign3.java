import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collections;

public class Assign3 {
	
	/**
	 * the first function to be called
	 * @param args the command line args
	 */
	public static void main(String[] args) {
		// the amount of digits of PI to compute
        int size = 1000;

		// the FIFO queue of digit positions
        TaskQueue taskQueue = new TaskQueue(size);
		// the result table of each digit that was computed
        ResultTable resultTable = new ResultTable();

		// gets the number of threads to create, one for each processor
        int numThreads = Runtime.getRuntime().availableProcessors();
		// creates an array of threads
        Thread[] threads = new Thread[numThreads];
		// for each thread create a new worker
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Worker(taskQueue, resultTable);
        }

		// get the start time
        long startTime = System.currentTimeMillis();
		// for each thread start the task
        for (int i = 0; i < numThreads; i++) {
            threads[i].start();
        }

        while(true) {
			// if the queue is empty
            if (taskQueue.isEmpty()) {
                boolean isDone = true;

				// for each thread check if it is done
                for (int i = 0; i < numThreads; i++) {
					// if the thread is done and isDone is now isDone
					// if thread is not done isDone is false
                    isDone = isDone && !threads[i].isAlive();

					// if one thread is not done end for loop
					if (!isDone) {
						break;
					}
                }

				// if all done break while loop
                if (isDone) {
                    break;
                }
            }
        }

		// get the time it took to run
        long runTime = System.currentTimeMillis() - startTime;

		// print the function to resturns the resultTable
        System.out.println(resultTable.displayResult());

		// display how long it took to compute
        System.out.printf("Pi Computation took %d ms\n", runTime);

    }
}

class Worker extends Thread{

    private TaskQueue queue;
    private ResultTable result;

    Worker(TaskQueue queue, ResultTable result) {
        System.out.flush();
        this.queue = queue;
        this.result = result;
    }

    public void run() {
        while (!this.queue.isEmpty()) {
            int topDigit = this.queue.getFirst();
            this.result.addItem(topDigit, calculate((long)topDigit));
        }
    }

    int calculate(long digit) {
        long av, a, vmax, N, num, den, k, kq, kq2, t, v, s, i;
		double sum;

		N = (long) ((digit + 20) * Math.log(10) / Math.log(2));

		sum = 0;

		for (a = 3; a <= (2 * N); a = nextPrime(a)) {

			vmax = (long) (Math.log(2 * N) / Math.log(a));
			av = 1;
			for (i = 0; i < vmax; i++)
				av = av * a;

			s = 0;
			num = 1;
			den = 1;
			v = 0;
			kq = 1;
			kq2 = 1;

			for (k = 1; k <= N; k++) {

				t = k;
				if (kq >= a) {
					do {
						t = t / a;
						v--;
					} while ((t % a) == 0);
					kq = 0;
				}
				kq++;
				num = mulMod(num, t, av);

				t = (2 * k - 1);
				if (kq2 >= a) {
					if (kq2 == a) {
						do {
							t = t / a;
							v++;
						} while ((t % a) == 0);
					}
					kq2 -= a;
				}
				den = mulMod(den, t, av);
				kq2 += 2;

				if (v > 0) {
					t = modInverse(den, av);
					t = mulMod(t, num, av);
					t = mulMod(t, k, av);
					for (i = v; i < vmax; i++)
						t = mulMod(t, a, av);
					s += t;
					if (s >= av)
						s -= av;
				}

			}

			t = powMod(10, digit - 1, av);
			s = mulMod(s, t, av);
			sum = (sum + (double) s / (double) av) % 1;
		}
		return (int) (sum * 1e1); // 1e9 is 9 decimal places
    }

    private long mulMod(long a, long b, long m) {
		return (long) (a * b) % m;
	}

	private long modInverse(long a, long n) {
		long i = n, v = 0, d = 1;
		while (a > 0) {
			long t = i / a, x = a;
			a = i % x;
			i = x;
			x = d;
			d = v - t * x;
			v = x;
		}
		v %= n;
		if (v < 0)
			v = (v + n) % n;
		return v;
	}

	private long powMod(long a, long b, long m) {
		long tempo;
		if (b == 0)
			tempo = 1;
		else if (b == 1)
			tempo = a;

		else {
			long temp = powMod(a, b / 2, m);
			if (b % 2 == 0)
				tempo = (temp * temp) % m;
			else
				tempo = ((temp * temp) % m) * a % m;
		}
		return tempo;
	}

	private boolean isPrime(long n) {
		if (n == 2 || n == 3)
			return true;
		if (n % 2 == 0 || n % 3 == 0 || n < 2)
			return false;

		long sqrt = (long) Math.sqrt(n) + 1;

		for (long i = 6; i <= sqrt; i += 6) {
			if (n % (i - 1) == 0)
				return false;
			else if (n % (i + 1) == 0)
				return false;
		}
		return true;
	}

	private long nextPrime(long n) {
		if (n < 2)
			return 2;
		if (n == 9223372036854775783L) {
			System.err.println("Next prime number exceeds Long.MAX_VALUE: " + Long.MAX_VALUE);
			return -1;
		}
		for (long i = n + 1;; i++)
			if (isPrime(i))
				return i;
	}
}

class ResultTable {
    private HashMap<Integer, Integer> results;

    ResultTable() {
        this.results = new HashMap<>();
    }

    public void addItem(int digit, int num) {
        synchronized (this.results) {
            this.results.put(digit, num);
        }
    }

    public String displayResult() {
		String resultString = "3.";
        for (int i = 1; i <= this.results.size(); i++) {
            resultString = resultString.concat(Integer.toString(this.results.get(i)));
        }
        return resultString;
    }

}

class TaskQueue {
    private LinkedList<Integer> queue = new LinkedList<>();
    private int startSize;

    public TaskQueue(int size) {
        this.startSize = size;
        ArrayList<Integer> digits = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            digits.add(i);
        }
        Collections.shuffle(digits);
        for (int digit : digits) {
            this.queue.add(digit);
        }
    }

    public boolean isEmpty() {
        synchronized (this.queue) {
            return this.queue.size() == 0;
        }
    }

    public int getSize() {
        synchronized (this.queue) {
            return this.queue.size();
        }
    }

    public int getFirst() {
        synchronized (this.queue) {
            int digit = this.queue.remove();
            displayStatus();
            return digit;
        }
    }

    private void displayStatus() {
        if ((this.startSize - this.queue.size()) % 10 == 0) {
            System.out.print(". ");
            if ((this.startSize - this.queue.size()) % 100 == 0) {
                System.out.println();
            }
            System.out.flush();
        }
    }
}

