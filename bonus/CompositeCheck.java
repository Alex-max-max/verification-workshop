import org.cprover.CProver;

// 1. JBMC builds trees of NODES nodes by calling addChild on any pair of nodes
// allowed by its precondition, and checks the class invariant of every node after
// each call. --unwind K bounds the number of calls (and every other loop).
//   ./bmc CompositeCheck.addChildren --unwind 4 --no-unwinding-assertions
public class CompositeCheck {

    static final int NODES = 3;

    public static void addChildren() {
        Composite[] nodes = new Composite[NODES];
        for (int i = 0; i < NODES; i++) {
            nodes[i] = new Composite(CProver.nondetInt());
        }
        while (CProver.nondetBoolean()) {
            Composite p = nodes[anyIndex()];
            Composite c = nodes[anyIndex()];
            CProver.assume(c != p && c.parent == null);
            // A root in our own ancestry is not an admissible subtree.
            boolean cycle = false;
            for (Composite ancestor = p; ancestor != null; ancestor = ancestor.parent) {
                cycle |= ancestor == c;
            }
            try {
                p.addChild(c);
                assert !cycle;
            } catch (IllegalArgumentException ex) {
                assert cycle;
            }
            for (Composite n : nodes) {
                checkInvariant(n);
            }
        }
    }

    static int anyIndex() {
        int i = CProver.nondetInt();
        CProver.assume(0 <= i && i < NODES);
        return i;
    }

    // The class invariant of Composite, from the comments on its fields.
    static void checkInvariant(Composite n) {
        int maximum = n.initValue;
        boolean found = false;
        for (Composite c = n.firstChild; c != null; c = c.nextSibling) {
            maximum = Math.max(maximum, c.value);
            found |= c == n.maxChild;
            assert c.parent == n;                  // each child points back to its parent
        }
        assert n.value == maximum;
        assert n.value == n.initValue ? n.maxChild == null : found && n.maxChild.value == n.value;
        // A finite tree has at most NODES ancestors including this node.
        Composite ancestor = n;
        for (int k = 0; k < NODES && ancestor != null; k++) ancestor = ancestor.parent;
        assert ancestor == null;
    }
}
