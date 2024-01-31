import java.util.Objects;

public class Interval {
    private int start;
    private int end;

    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
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

    @Override
    public boolean equals(Object o) {
        Interval i = (Interval) o;
        return (i.getStart() == this.start && i.getEnd() == this.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}