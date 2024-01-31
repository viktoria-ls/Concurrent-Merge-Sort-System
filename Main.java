import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class Main {
    static final int SEED = 123;
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // Seed your randomizer
        Random random = new Random(SEED);

        // Get array size and thread count from user
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter array size: ");
        int N = sc.nextInt();

        System.out.print("Enter thread count: ");
        int THREAD_COUNT = sc.nextInt();
        sc.close();

        // Generate a random array of given size
        ArrayList<Integer> shuffledList = new ArrayList<>();
        for (int i = 1; i <= N; i++) {
            shuffledList.add(i);
        }

        Collections.shuffle(shuffledList, random);
        int[] shuffledArr = new int[N];
        for (int i = 0; i < N; i++) {
            shuffledArr[i] = shuffledList.get(i);
        }

        // Call the generate_intervals method to generate the merge sequence
        List<Interval> intervals = generate_intervals(0, N - 1);

        // Call merge on each interval in sequence
        // for(Interval i : intervals) {
        //     merge(shuffledArr, i.getStart(), i.getEnd());
        // }

        // Once you get the single-threaded version to work, it's time to 
        // implement the concurrent version. Good luck :)

        // map of interval and whether its done or not
        HashMap<Interval, Boolean> intervalMap = new HashMap<>();
        for (Interval i : intervals) {
            intervalMap.put(i, false);
        }

        ExecutorService es = Executors.newFixedThreadPool(THREAD_COUNT);
        ReadWriteLock lock = new ReentrantReadWriteLock();

        long startTime = System.currentTimeMillis();

        while(!intervals.isEmpty()) {
            lock.writeLock().lock();
            Interval i = intervals.remove(0);
            // if interval with size 1 or no dependency, assign to first available thread
            if(i.getStart() == i.getEnd()) {
                MergeRunnable mr = new MergeRunnable(i.getStart(), i.getEnd(), shuffledArr, intervalMap);
                es.execute(mr);
                // while(res.get() != true) {continue;}
                // intervalMap.put(i, true);
            }
            // if not ready, check if direct dependencies are done
            else {
                int leftStart = i.getStart();
                int leftEnd = i.getStart() + (i.getEnd() - i.getStart()) / 2;

                int rightStart = leftEnd + 1;
                int rightEnd = i.getEnd();

                // if direct dependencies are done, assign this interval to a thread
                if(intervalMap.get(new Interval(rightStart, rightEnd)) == true && intervalMap.get(new Interval(leftStart, leftEnd)) == true) {
                    MergeRunnable mr = new MergeRunnable(i.getStart(), i.getEnd(), shuffledArr, intervalMap);
                    es.execute(mr);
                    // Future<Boolean> res = es.submit(mr, true);
                    // while(res.get() != true) {continue;}
                    // intervalMap.put(i, true);
                }
                // otherwise add this interval back to the queue
                else {
                    intervals.add(i);
                }
            }
            lock.writeLock().unlock();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.print("TOTAL TIME: " + totalTime + " milliseconds");

        es.shutdown();

        System.out.println("DONE");

        // for(int i = 0; i < N; i++) {
        //     System.out.println(shuffledArr[i]);
        // }
    }

    /*
    This function generates all the intervals for merge sort iteratively, given 
    the range of indices to sort. Algorithm runs in O(n).

    Parameters:
    start : int - start of range
    end : int - end of range (inclusive)

    Returns a list of Interval objects indicating the ranges for merge sort.
    */
    public static List<Interval> generate_intervals(int start, int end) {
        List<Interval> frontier = new ArrayList<>();
        frontier.add(new Interval(start,end));

        int i = 0;
        while(i < frontier.size()){
            int s = frontier.get(i).getStart();
            int e = frontier.get(i).getEnd();

            i++;

            // if base case
            if(s == e){
                continue;
            }

            // compute midpoint
            int m = s + (e - s) / 2;

            // add prerequisite intervals
            frontier.add(new Interval(m + 1,e));
            frontier.add(new Interval(s,m));
        }

        List<Interval> retval = new ArrayList<>();
        for(i = frontier.size() - 1; i >= 0; i--) {
            retval.add(frontier.get(i));
        }

        return retval;
    }

    /*
    This function performs the merge operation of merge sort.

    Parameters:
    array : vector<int> - array to sort
    s     : int         - start index of merge
    e     : int         - end index (inclusive) of merge
    */
    public static void merge(int[] array, int s, int e) {
        int m = s + (e - s) / 2;
        int[] left = new int[m - s + 1];
        int[] right = new int[e - m];
        int l_ptr = 0, r_ptr = 0;
        for(int i = s; i <= e; i++) {
            if(i <= m) {
                left[l_ptr++] = array[i];
            } else {
                right[r_ptr++] = array[i];
            }
        }
        l_ptr = r_ptr = 0;

        for(int i = s; i <= e; i++) {
            // no more elements on left half
            if(l_ptr == m - s + 1) {
                array[i] = right[r_ptr];
                r_ptr++;

            // no more elements on right half or left element comes first
            } else if(r_ptr == e - m || left[l_ptr] <= right[r_ptr]) {
                array[i] = left[l_ptr];
                l_ptr++;
            } else {
                array[i] = right[r_ptr];
                r_ptr++;
            }
        }
    }
}