# Homework plus: answers

Name(s): Ruiyang Xu

### 1. Invariants from an LLM

| method | LLM | rounds | mistakes | final invariants by |
| --- | --- | --- | --- | --- |
| count | OpenAI Codex (GPT-6) | 1 | First proposal verified; no rejection | Codex proposed; OpenJML verified |
| reverse | OpenAI Codex (GPT-6) | 1 | First proposal verified; no rejection | Codex proposed; OpenJML verified |
| isqrt | OpenAI Codex (GPT-6) | 1 | First proposal verified; no rejection | Codex proposed; OpenJML verified |
| dutchFlag | OpenAI Codex (GPT-6) | 1 | First proposal verified; no rejection | Codex proposed; OpenJML verified |


Q: Why is it safe to use invariants written by an LLM here, even if the LLM is often wrong?

The LLM proposes annotations; OpenJML checks initialization, preservation, termination measures and the postcondition against the original contract. A green result is only as meaningful as the contract: for example, sorted order alone does not specify that a sort preserves multiplicities.


### 2. The Zune bug

Q: `--unwind 31 --no-unwinding-assertions` says SUCCESSFUL. Does that mean `yearOf` is correct for every day it checked?

No. The bound silently cuts longer runs: the range assertion only concerns executions that reach the return. It cannot establish termination or that the particular year returned is right.


Q: What does JBMC report without `--no-unwinding-assertions`? Is K too small, or is something else wrong?

The unwinding assertion requires every path to exit within the bound. A loop iteration can preserve both days and year, so increasing a finite bound cannot prove termination. The separate OpenJML decreases experiment also failed on the original loop.


Q: What is wrong with the loop? Which date froze the Zune?

On the last day of a leap year the loop can enter without having a whole year to subtract. The calendar incident was December 31, 2008. The fix compares the remaining day count with the length of the current year before starting another iteration.


Q: With your fix, what is the smallest K that gives VERIFICATION SUCCESSFUL with unwinding assertions? What exactly has been proved?

K = 29 failed and K = 30 passed with unwinding assertions: there can be 29 subtractions and a final guard check. The first harness proves termination and the result range for days 1..10950; the second also proves that the input day belongs to that year.


Q: A colleague changes `days > 366` to `days >= 366`. Which harness notices, and why?

On the >= variant the range check passed, while yearOfIsRight failed: assigning the last day of a leap year to the following year still produces a plausible year, but violates the strict lower day bound.


Q (★): Why does `./esc` say OK on the original? What changes with `decreases`?

Original OpenJML run: OK for partial correctness. With decreases days: LoopDecreases failed. The original postcondition describes returned years only; the variant also requires strict progress. The repaired loop passes with the variant.


### 3. Range

Q: Why does `size()` not verify? Give concrete values.

For [0, Integer.MAX_VALUE], even the final +1 cannot fit in int. A wider interval may overflow the subtraction too. OpenJML reported both arithmetic checks and the result contract. Computing in long from the subtraction onward and returning long covers the full range (up to 4294967296).


Q: Compare JBMC's counterexample with your answer above.

JBMC chose lo = -1646305535 and hi = 1073741824, a different valid interval with the same representation problem. After widening the calculation the unchanged size contract passed both tools.


### 4. StackAr

Q: Which tool reports your bug, and how? Could a bug get past OpenJML but not JBMC, or the other way around?

In a temporary copy, pop returned the right element without decrementing top. OpenJML failed its pop postcondition; JBMC failed the size comparison with its independent model. An incomplete contract can miss behavior the model checks, and bounded traces can miss defects requiring longer operation sequences.

### 5. SafeLock

Q: Smallest K that finds a bug? Which sequence of events? What is wrong in the code?

The original state transition did not preserve the relationship between the attempt counter and the lock state. The corrected implementation ties counter updates to valid state transitions. The original close method fails the class-invariant exit check. A minimal bypass trace is not included here.


Q: Formula size and time for K = 8, 10, 12.

For the repaired neverOpensAfterTooManyWrongPins harness: K=8: 46050 variables, 68865 clauses, 0.15 s; K=10: 56941, 87971, 0.30 s; K=12: 67836, 107563, 0.56 s. Times are solver times from this run.


Q: Does SUCCESSFUL with K = 12 mean the property holds for every sequence? Can unwinding assertions help?

No. An arbitrary user can keep sending events; no finite unwinding bound covers all such traces. Enabling unwinding assertions exposes the bound rather than proving unbounded safety. The second harness checks every transition in scope that leaves BLOCKED, requiring an authorized reset.
