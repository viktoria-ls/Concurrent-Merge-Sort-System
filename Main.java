import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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


        long startTime = System.currentTimeMillis();
        List<Interval> intervals = generate_intervals(0, N - 1);
        HashMap<Interval, Future> runnableList = new HashMap<>();
        ExecutorService test = Executors.newFixedThreadPool(THREAD_COUNT);

        for(Interval i: intervals) {
            MergeRunnable temp = new MergeRunnable(i, shuffledArr);

            if(i.getStart() == i.getEnd()) {
                runnableList.put(i, test.submit(temp));
            }
            else {
                int m = i.getStart() + (i.getEnd() - i.getStart()) / 2;
                int leftStart = i.getStart();
                int leftEnd = m;

                int rightStart = m + 1;
                int rightEnd = i.getEnd();

                List<Future> tempChildren = new ArrayList<>();

                tempChildren.add(runnableList.get(new Interval(leftStart, leftEnd)));
                tempChildren.add(runnableList.get(new Interval(rightStart, rightEnd)));
                temp = new MergeRunnable(i, shuffledArr, tempChildren);

                runnableList.put(i, test.submit(temp));
            }
        }

        test.shutdown();

        try {
            test.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // for(int i : shuffledArr) {
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
    Interval givenInterval;
    int[] arrayToBeSorted;
    List<Future> dependencies = null;

    MergeRunnable(Interval i, int[] a) {
        this.givenInterval = i;
        this.arrayToBeSorted= a;
    }

    MergeRunnable(Interval i, int[] a, List<Future> dependencies) {
        this.givenInterval = i;
        this.arrayToBeSorted= a;
        this.dependencies = dependencies;
    }

    @Override
    public void run() {
        if(dependencies != null) {
            for(Future d : dependencies) {
                try {
                    d.get();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        merge(arrayToBeSorted, givenInterval.getStart(), givenInterval.getEnd());
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

// class MergeThread extends Thread {
//     List<Interval> intervals;
//     List<Interval> finishedIntervals;
//     Interval threadInterval;
//     Integer threadCount;
//     ReentrantLock writeLock;
//     int[] arrayToBeSorted;

//     MergeThread(Interval i, int[] arrayToBeSorted, Integer THREAD_COUNT, ReentrantLock writeLock) {
//         this.threadInterval = i;
//         this.arrayToBeSorted = arrayToBeSorted;
//         this.threadCount = THREAD_COUNT;
//         this.writeLock = writeLock;
//     }

//     public void run() {
//         merge(arrayToBeSorted, threadInterval.getStart(), threadInterval.getEnd());
//         threadCount--;
//     }

//     public static void merge(int[] array, int s, int e) {
//         //s = 0 e = 2
//         int m = s + (e - s) / 2;
//         //m = 1 | left[2] right[1]
//         int[] left = new int[m - s + 1];
//         int[] right = new int[e - m];
//         int l_ptr = 0, r_ptr = 0;
//         for(int i = s; i <= e; i++) {
//             if(i <= m) {
//                 left[l_ptr++] = array[i];
//             } else {
//                 right[r_ptr++] = array[i];
//             }
//         }
//         l_ptr = r_ptr = 0;

//         for(int i = s; i <= e; i++) {
//             // no more elements on left half
//             if(l_ptr == m - s + 1) {
//                 array[i] = right[r_ptr];
//                 r_ptr++;

//             // no more elements on right half or left element comes first
//             } else if(r_ptr == e - m || left[l_ptr] <= right[r_ptr]) {
//                 array[i] = left[l_ptr];
//                 l_ptr++;
//             } else {
//                 array[i] = right[r_ptr];
//                 r_ptr++;
//             }
//         }
//     }
// }


// for(int i = 0;i < THREAD_COUNT;i++) {
//     List<Interval> tempIntervals;
//     if(i == THREAD_COUNT - 1) {
//         tempEnd = N - 1;
//     }
//     else {
//         tempEnd = tempStart + spaceBetween;
//     }

//     tempIntervals = generate_intervals(tempStart, tempEnd);

//     tempStart = tempEnd + 1;
        
//     listOfThreads.add(new MergeThread(tempIntervals, finishedIntervals, shuffledArr));
//     listOfThreads.get(listOfThreads.size()-1).start();
// }


// for(MergeThread t : listOfThreads) {
//     try {
//         t.join();
//     } catch (InterruptedException e) {
//         // TODO Auto-generated catch block
//         e.printStackTrace();
//     }
// }

// for(int i = intervalList.size() - (intervalList.size() - finishedIntervals.size());i < intervalList.size();i++) {
//     merge(shuffledArr, intervalList.get(i).getStart(), intervalList.get(i).getEnd());
// }
