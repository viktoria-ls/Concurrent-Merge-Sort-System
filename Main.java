import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    static final int SEED = 123;
    public static void main(String[] args) {
        // Seed your randomizer
        Random random = new Random(SEED);

        // Get array size and thread count from user
        Scanner sc = new Scanner(System.in);
        
        System.out.print("Enter array size: ");
        int N = sc.nextInt();

        System.out.print("Enter thread count: ");
        Integer THREAD_COUNT = sc.nextInt();
        sc.close();

        if(THREAD_COUNT > N)
            THREAD_COUNT = N;

        int numPerThread = (int) Math.ceil((double) N/THREAD_COUNT);

        // Generate a random array of given size
        ArrayList<Integer> shuffledList = new ArrayList<>();
        for (int i = 1; i <= N; i++) {
            shuffledList.add(i);
        }

        Collections.shuffle(shuffledList, random);

        ArrayList<ArrayList<Integer>> partitionedList = new ArrayList<>();
        int start = 0;
        int end = start + numPerThread;

        
        for(int i = 0;i < THREAD_COUNT;i++) {
            partitionedList.add(new ArrayList<Integer>());
            if(i == THREAD_COUNT - 1) {
                end = N;
            }
            for(int x = start; x < end;x++) {
                partitionedList.get(partitionedList.size()-1).add(shuffledList.get(x));
            }
            start = end;
            end = start + numPerThread;
        }

        ArrayList<int[]> pSA = new ArrayList<>(); //pSA = Partitioned Shuffled Array

        

        for(ArrayList<Integer> a : partitionedList) {
            pSA.add(new int[a.size()]);
            int counter = 0;
            for(Integer i : a) {
                pSA.get(pSA.size()-1)[counter] = i;
                counter++;
            }
        }

        ArrayList<List<Interval>> intervalLists = new ArrayList<>();

        for(int[] a : pSA) {
            intervalLists.add(generate_intervals(0, a.length - 1));
        }

        long startTime = System.currentTimeMillis();
        ExecutorService test = Executors.newFixedThreadPool(THREAD_COUNT);
        for(int i = 0;i < intervalLists.size();i++) {
            MergeRunnable temp = new MergeRunnable(intervalLists.get(i), pSA.get(i));
            test.execute(temp);
        }

        test.shutdown();

        try {
            test.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int[] finalArray = pSA.get(0); //Get first array in pSA

        for(int i = 1; i < pSA.size();i++) {
            finalArray = mergeArrays(finalArray, pSA.get(i));
        }    

        // for(int i : finalArray) {
        //     System.out.println(i);
        // }
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.print("TOTAL TIME: " + totalTime + " milliseconds");

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
            // s = 4 and e = 5 -> 4 + (5 - 4) / 2
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

    public static int[] mergeArrays(int[] left, int[] right) {
        int[] array = new int[left.length + right.length];

        int s = 0;
        int e = ((right.length + left.length) - 1);
        /*
         * s = 0
         * e = 9
         * 
         * left[5]
         * right[5]
         * 
         * 
         */
        int m = left.length - 1;
        int l_ptr = 0, r_ptr = 0;


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

        return array;
    }
}

class Interval {
    private int start;
    private int end;

    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean equals(Object o) { 
        Interval i = (Interval) o;
        return (i.getStart() == this.getStart() && i.getEnd() == this.getEnd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getStart(), this.getEnd());
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}

class MergeRunnable implements Runnable {
    List<Interval> givenIntervals;
    int[] arrayToBeSorted;

    MergeRunnable(List<Interval> i, int[] a) {
        this.givenIntervals = i;
        this.arrayToBeSorted= a;
    }

    @Override
    public void run() {
        for(Interval i : givenIntervals) {
            merge(arrayToBeSorted, i.getStart(), i.getEnd());
        }
    }

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