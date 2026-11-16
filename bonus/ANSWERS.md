# Bonus: answers

Name(s): Ruiyang Xu

### 1. Composite

Q: Which part of the invariant breaks, after which calls? What is wrong in `update`?

Consider a chain A -> B -> C, built by attaching B under A and then C under B, with C holding the largest value. The numeric maximum propagates, but the original update gives A a pointer to C rather than its own child B. Passing this to the parent preserves the direct-child witness.


Q: Why does JBMC need K = 4 to find a bug that needs two calls?

The same K also bounds initialization and the scan of the three-node array. K=4 allows these loops to complete all three iterations and reach the exit check; K=2 and 3 cut paths without producing a SAT formula.


Q: How large is the formula? What would you need to verify `Composite` with OpenJML?

On the three-node leaf-only experiment before fixing update, K=4 used 163428 variables and 377509 clauses; K=5 used 233246 and 563796. Compare Range (3150 variables, 5634 clauses after the fix). References encode multiple possible identities, links and updates. An OpenJML proof would need a model of reachability and ownership, acyclicity, parent-child consistency, cached maxima, writable locations, and a termination argument for propagation.


Q (★): What goes wrong when subtrees are allowed?

Allowing an existing subtree introduces a new obligation: it must not contain the prospective parent. Local maxima and matching child-parent pointers alone cannot rule out a cycle. The extended implementation checks ancestry before linking, rejects a cyclic attachment, and the harness checks that walking parent links terminates within the node count.


### 2. SafeLock for any number of events

Q: Why does OpenJML think `failed++` may overflow?

Without the class invariant the verifier cannot derive a small upper bound for failed at method entry. With the invariant, failed is between zero and MAX_ATTEMPTS and reaches the upper bound exactly in BLOCKED. Thus a LOCKED increment starts strictly below that bound.


Q: Does your invariant catch the bug of homework plus, exercise 5? What does this proof give you that JBMC did not?

Yes. Rechecking the original close code with the new invariant produced InvariantExit. The repaired class verifies. Induction over constructor and method boundaries preserves these state facts for every finite sequence, with no K. That does not automatically establish every possible history-dependent property.
