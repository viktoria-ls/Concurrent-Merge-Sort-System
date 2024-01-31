import java.util.HashMap;

public class MergeRunnable implements Runnable {
    private int start;
    private int end;
    private int[] arr;
    private HashMap<Interval, Boolean> map;

    public MergeRunnable(int start, int end, int[] arr, HashMap<Interval, Boolean> map) {
        this.start = start;
        this.end = end;
        this.arr = arr;
        this.map = map;
    }

    public MergeRunnable(int[] arr) {
        start = -1;
        end = -1;
        this.arr = arr;
    }

    public void run() {
        merge(arr, start, end);
        map.put(new Interval(start, end), true);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    private void merge(int[] array, int s, int e) {
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
