# Homework: answers

Name(s): Ruiyang Xu

## OpenJML

### 1. Absolute

Q: What does OpenJML complain about before you write a contract, and for which input?

It reports ArithmeticOperationRange at `return -n;`. When n is -2147483648, its mathematical negation is 2147483648, one greater than Integer.MAX_VALUE.

Q: Which `requires` did you need, and why? Could you avoid it by changing the `ensures`?

`requires n != Integer.MIN_VALUE;` excludes the only int whose absolute value cannot fit in an int. Changing only the postcondition would not fix the arithmetic check: OpenJML must prove that the negation in the method body is in range even without a postcondition.

### 2. SumX

Q: Which proof obligations fail? Is the program wrong?

OpenJML could not prove the sum formula at the return or rule out overflow at `s = s + i`. The associated declaration points back to the same postcondition. The loop is correct for the given inputs: the original invariant bounds i but says nothing about s. Adding `s == i * (i + 1) / 2` records the sum so far. At exit i equals x, so this gives the required result.

Q: Largest N for which `x <= N` verifies? What happens with N + 1, and why?

OpenJML verified N = 65535. Its sum is 2147450880, within the int limit of 2147483647. For N = 65536 the sum reaches 2147516416: the last addition overflows and the stored result no longer matches the mathematical sum. OpenJML reported ArithmeticOperationRange on the addition and LoopInvariant on the sum invariant. The submitted bound is 1000 as requested.

### 3. Max

Q: Is the remaining failure caused by your invariants or by the code? (Your hypothesis.)

I suspect the loop stops one element too soon. The invariants establish that m is the maximum of the elements visited so far, but the guard excludes the last index. For example, on [2, 7] the loop never starts and m stays 2. That is an array element, but it is not at least as large as every element. This is the postcondition OpenJML still reports.

### 4. BinarySearch

Q: What does OpenJML report, in which method, and for which concrete call? How large must the array be?

In midpoint, OpenJML reports integer addition overflow and cannot prove that the returned index lies between lo and hi. In this exercise, midpoint(1 << 30, 1 << 30) should just return that same index, but adding the two endpoints exceeds Integer.MAX_VALUE. The minimum array length for such indices is (1 << 30) + 1: for a shorter array, even twice the last index fits in an int. Computing half the distance first avoids the overflowing sum.

Q: What happens without `/*@ pure @*/` on `midpoint`, and why?

Removing pure added an Assignable failure at the call in search and failures for both loop invariants about excluded array elements. The caller uses the helper's contract: without a restriction on writes, it cannot assume that the array contents survive the call unchanged. Restoring pure and fixing the midpoint calculation verified both methods.

## JBMC

### 5. Max, again

Q: What input did JBMC find, and what goes wrong? Was your hypothesis of exercise 3 right?

My run produced [3, 32770]. Max returned 3; at j = 1 the harness checked 3 >= 32770 and failed. This confirms that the loop skipped the last element.

Q: Smallest `--unwind` and `--max-nondet-array-length` that find it? Relation to the small scope hypothesis?

With unwinding assertions disabled, unwind 2 and array-length bound 2 suffice. Unwind 1 misses the comparison with the second element, and length bound 1 cannot expose a skipped maximum: the first element is also the maximum. This illustrates how a small input can exhibit a real error.

Q: Why does `--unwind 5` with unwinding assertions fail on the fixed version? Smallest K with `--max-nondet-array-length 4`, and why not one less? What has been proved?

With no explicit length limit of 4, JBMC still allows arrays too long for the harness loop to finish within 5. The failing property was an unwinding assertion, not the maximum-value check. With length at most 4, K = 4 still fails there and K = 5 passes: the harness scans four elements and needs the final loop-exit check. This proves the contract for all non-null arrays of lengths 1 through 4 and arbitrary int elements in that scope. OpenJML separately verified the corrected method without that length bound.

### 6. SumX

Q: Which values of `x` were checked with `--unwind 10 --no-unwinding-assertions`? Why does it fail with unwinding assertions? Which K is a proof? Sizes and times for K = 10, 100 and that K.

At K = 10 only x = 0 through 9 reach the postcondition. The loop needs x iterations and an exit check. Disabling unwinding assertions drops longer paths; enabling them reports that the loop may still be running. K = 1001 covers x = 1000 and thus the whole precondition.

| K (unwinding assertions enabled) | SAT variables | Clauses | Solver time | Result |
| ---: | ---: | ---: | ---: | --- |
| 10 | 4638 | 13644 | 0.00 s | Unwinding failure |
| 100 | 9587 | 30260 | 0.01 s | Unwinding failure |
| 1001 | 66870 | 666136 | 1.14 s | Verified |

Q: What did OpenJML need from you, and what did JBMC need?

OpenJML used the invariant linking s to the sum through i to prove the contract without expanding the loop for every iteration. JBMC used an executable harness (assume the input range, call sum, assert the formula) and enough loop unwinding to cover that entire input range.

### 7. BinarySearch

Q: Why does JBMC miss the bug with the `search` harness? Could a larger bound help?

The harness itself assumes a.length <= 5, so every index used by search is small. Increasing only the command-line bound cannot override this assumption. In principle both length limits could be raised, but checking enormous arrays and the loops that inspect them is impractical. Checking the helper directly avoids the need for an array.

Q: What did the `midpoint` harness find?

On the original implementation the bounded search check passed (length at most 5, unwind 6), but the direct midpoint assertion failed: JBMC found nonnegative endpoints whose sum overflowed, giving a negative midpoint outside the interval.

On the corrected implementation, the direct check verified that the midpoint lies between its endpoints for all int inputs satisfying 0 <= lo <= hi. It needs no array or loop; unwind 1 sufficed. This run had 38 properties hold and 0 fail.

## Comparison

### 8. Comparison

| program | OpenJML: result, what you wrote | JBMC: result, bound used |
| --- | --- | --- |
| SumX | Added an invariant for the running sum; proved the formula for every allowed x. | Wrote a harness for the same formula. K = 1001 covers the whole input range, 0 to 1000. |
| Max | Specified the maximum of the visited prefix and a decreasing measure. The original loop could not establish the postcondition; the corrected loop verified for all nonempty arrays. | Found a two-element counterexample. After the fix, K = 5 verified all nonempty arrays of length at most 4. |
| BinarySearch | The given contracts exposed overflow in midpoint. Kept pure and changed the calculation; search and midpoint verified. | At K = 6, arrays of length at most 5 passed even before the fix. The direct midpoint harness at K = 1 failed before the fix and passed afterward, with no array-length restriction. |

Q: When would you use each tool?

I would use JBMC first when a short execution could explain a suspected error, as with Max. I would use OpenJML when the claim should hold for any array length and I can describe what each loop preserves. For a loop-free helper like midpoint, a direct JBMC harness can cover every permitted integer input without building a large array.

