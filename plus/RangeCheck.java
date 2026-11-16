import org.cprover.CProver;

// 3. A harness for the record Range.
//   ./bmc RangeCheck.size --unwind 1
public class RangeCheck {

    public static void size(int lo, int hi) {
        CProver.assume(lo <= hi);
        Range r = new Range(lo, hi);
        assert r.size() == (long)hi - lo + 1;
    }
}
