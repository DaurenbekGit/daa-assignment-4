Assignment 4 — Algorithm Analysis and Verification
Course: Design and Analysis of Algorithms (DAA)
Assignment: 4 — Dynamic Programming and String Processing
Repository: DaurenbekGit/daa-assignment-4
Language: Java 17
Submission format: one UTF-8 JSON request per line and one JSON response per line. Indices are zero-based.
1. Environment and execution
   The implementation is in Java/source/Algorithms.java and uses standard Java collections and arrays. The teacher-provided JSON runner uses submission.json to select Java. All eight algorithms are implemented without third-party algorithm libraries.

Local verification: Windows PowerShell, Microsoft OpenJDK 17.0.18, and the provided Python launcher. On 8 November 2026 the student observed 44/44 public checks PASS and state reset PASS. The student's 28 Java assertions also passed, and the Java search benchmark completed successfully. The student's machine CPU/RAM specifications were not recorded.

Run these commands from the repository root (with java and javac pointing to JDK 17 or newer):
java -version
javac -version
python tools/run.py --check

javac -d Java/build Java/source/Algorithms.java tests/student/StudentTests.java
java -cp Java/build StudentTests

javac -d Java/build Java/source/Algorithms.java tests/student/SearchBenchmark.java
java -cp Java/build SearchBenchmark
The first command checks the official public examples; the other two run the student's additional Java tests and search benchmark. These checks do not replace hidden tests or guarantee the full practical score.
2. Algorithm explanations and correctness
   2.1 lcs — Longest Common Subsequence
   State. Let a have length n and b have length m. dp[i][j] is the maximum LCS length of prefixes a[0..i-1] and b[0..j-1].

Base cases. dp[0][j] = dp[i][0] = 0 because an empty string has no nonempty common subsequence. The implementation handles n = 0 or m = 0 without special indexing.

Transition and order. Fill rows with i = 1..n, then columns with j = 1..m:
- If a[i-1] == b[j-1], set dp[i][j] = dp[i-1][j-1] + 1.
- Otherwise set dp[i][j] = max(dp[i-1][j], dp[i][j-1]).
  All referenced smaller prefix states have been filled before the current cell. On matching characters, an optimal common subsequence can include the shared last character. On a mismatch, any common subsequence must exclude at least one of the two last characters; taking the better smaller prefix is therefore optimal. Induction over prefix sizes establishes correctness.

Reconstruction and ties. Backtrack from (n,m). For equal last characters, append the character and move diagonally. Otherwise move to the neighbor with the larger DP value. On equality the code prefers moving upward (>=). Reverse the collected characters. This creates one valid optimum; the exact optimum witness can differ if there are ties.

Time: O((n+1)(m+1)). Auxiliary memory: O((n+1)(m+1)) for the DP array, plus up to O(min(n,m)) characters for the reconstructed output. An output of length L requires O(L) space.

Counterexample/boundary: a="AXBYCZ", b="ABC" returns length 3 and "ABC", proving this is a subsequence problem, not substring search. Two empty strings return (0, "").
2.2 merge — Optimal ordered adjacent-block merging
State. dp[l][r] is the minimum cost to merge the original consecutive blocks l..r into one block. The prefix array prefix permits sum(l,r) = prefix[r+1] - prefix[l] in constant time using Java long.

Base case. dp[i][i] = 0: a single block needs no merge. For n <= 1, return cost zero and an empty plan.

Transition and order. For interval lengths 2 through n, try every possible last split k with l <= k < r:

dp[l][r] = min_k(dp[l][k] + dp[k+1][r] + sum(l,r)).

The two halves must already be merged before the final merge, and that final merge costs the total size of the interval. Considering every legal last split covers all valid adjacent-merge plans. The subintervals are shorter, so length-increasing order satisfies DP dependencies.

Reconstruction and ties. Store a minimizing split[l][r]. Recursively emit the left and right child merges before emitting [l,k,r]. This is chronological and never tries to merge blocks that do not yet exist. If several k values yield equal cost, the first minimum found is retained. n > 1 produces exactly n-1 operations.

Time: O(n^3) for all intervals and possible splits. Auxiliary memory: O(n^2) for dp and split, O(n) for prefix sums and recursion depth; output plan O(n). All accumulated costs use signed 64-bit long values.

Counterexample/boundary: For [2,3,4], the best plan has cost 14; merging the right two blocks first costs 16. [1000000000,1000000000,1000000000] requires long: minimum total cost is 5000000000, which exceeds 32-bit signed range.
2.3 tree — Maximum-weight independent set on a tree
State. Root the tree at vertex 0. take[u] is the best weight in the subtree of u when u is selected; skip[u] is the best when u is not selected.

Base case. For a leaf, take[u] = weight[u] and skip[u] = 0. For an empty tree, return weight zero and no selected vertices. Negative weights are permitted, and the empty independent set is valid.

Transition. For every child v of u:
- take[u] = weight[u] + sum(skip[v]), because neighbors of a selected vertex cannot be selected.
- skip[u] = sum(max(take[v], skip[v])), because an unselected parent does not constrain which state is chosen in the child subtree.
  Processing order. An iterative traversal records parents before children. Processing the reversed order computes every child state before its parent. The algorithm does not recurse down the tree, so a chain of 100,000 vertices does not overflow the call stack.

Correctness and reconstruction. Conditional on the state of u, the child subtrees are independent, so summing their optimal values is optimal. Start with the better root state, process vertices in parent-before-child order, and select a vertex only if its parent is not selected and take[u] > skip[u]. Ties go to the skip state; selected vertices are pairwise nonadjacent. The optimum is max(take[0],skip[0]).

Time: O(n+1). Auxiliary memory: O(n+1) for adjacency arrays, parent/order arrays, DP states and chosen flags; output up to O(n) IDs.

Counterexample/boundary: On the chain weights [4,9,6], selecting vertices 0 and 2 yields 10 (better than selecting 1 for 9). On weights [-5,-2,-7], the empty set yields 0.
2.4 naive — Direct exact string matching
Invariant and update. For every start index from 0 through n-m, compare pattern[j] to text[start+j] from left to right. A start is reported only after all m characters match. Increase the start by one, not by m, so overlapping occurrences are retained.

Base cases. If m=0, every boundary 0..n is a match (including the boundary after the last character). If m>n, there is no valid start and the result is empty.

Correctness. Every possible length-`m` window is visited exactly once and is output if and only if all its characters equal the pattern. The returned starts are strictly increasing.

Time: O(n*m+n+1) worst case (including empty-pattern output). Auxiliary memory: O(1) excluding the O(z+1) output positions, where z is the number of matches.

Counterexample: text="aaaaa", pattern="aaa" must return [0,1,2], not only [0], because overlaps count.
2.5 kmp — Knuth–Morris–Pratt
Prefix-function invariant. pi[i] is the length of the longest proper prefix of pattern[0..i] that is also a suffix of it. Build pi left to right; on mismatch repeatedly replace j with pi[j-1] until the next comparison can succeed or j=0.

Search update. During the text scan, j is the length of the currently matched pattern prefix ending at the preceding text character. On mismatch, use the same prefix fallback rather than rescanning consumed text. On equality increment j. At j=m, emit start i-m+1, then set j=pi[j-1] to preserve overlapping matches.

Base cases and correctness. For an empty pattern, emit all n+1 boundaries before indexing pattern[0]. The prefix function records exactly the alternative prefixes that may still match after a mismatch. Therefore a fallback cannot discard a possible occurrence and the left-to-right scan enumerates every exact match.

Time: O(n+m+z+1) including output. Auxiliary memory: O(m+1) for pi; output O(z+1).

Counterexample: text="abababa", pattern="aba" yields [0,2,4]. Resetting j to zero after each full match would miss overlaps.
2.6 rk — Exact Rabin–Karp with collision verification
Hash invariant. Each length-`m` window is represented by a polynomial hash of ASCII codes, with base 31, modulus 101, and the leftmost character at power 31^(m-1). Pattern and first-window hashes are computed in O(m) time. When shifting the window, subtract the outgoing character multiplied by highPower, normalize the remainder, multiply by 31, then add the incoming character, all modulo 101.

Update and correctness. Unequal hashes rule out an exact match. Equal hashes produce a candidate, not a verified occurrence: compare all characters before reporting the start. This ensures hash collisions cannot produce false matches. Check m=0 and m>n before reading a window. Shift by one position so overlapping matches are included.

Time: O(n+m+c*m+z) for 1<=m<=n, where c is the number of equal-hash candidate windows, including collisions. The worst case is O(n*m+n+m+1) because each candidate may require m character checks. Auxiliary memory: O(1) for the rolling hash, excluding O(z+1) output. This implementation is not guaranteed linear time.

Collision counterexample: The hash values of "ab" and "dj" are both 75 modulo 101. text="abdj", pattern="ab" must return [0], not [0,2]; direct character verification rejects the collision.
2.7 suffix — Prefix-doubling suffix array and Kasai LCP
Suffix-array invariant. sa[i] is the start of the suffix at sorted position i. rank[pos] identifies the equivalence class of the initial step characters of the suffix starting at pos. At each doubling iteration, compare the rank pair (rank[pos],rank[pos+step]), using -1 for an out-of-range second half. This is equivalent to comparing prefixes of twice the previous length.

Order and correctness. Sort pairs stably with two counting-sort passes: first by the second rank, then by the first rank. Reassign dense class IDs in sorted order. Starting with single-character classes and doubling step builds correct order for prefixes of lengths 2, 4, 8, etc. When all suffixes receive distinct classes, the suffix order is complete. Shorter suffixes sort first when one is a prefix of another. No empty or sentinel suffix is returned.

Kasai LCP. Build inverse[start], the sorted position of each original suffix. Traverse original starting positions from left to right and compare each suffix with its preceding suffix in sa. If two neighboring original suffixes are shifted one character, their common prefix can decrease by at most one, so the current common counter is reduced by only one between iterations. This makes the total comparisons linear. Set lcp[0]=0, with lcp[i]=LCP(sa[i-1],sa[i]) for i>0.

Longest repeated substring. Every repeated substring is a prefix of two distinct suffixes, and the maximum common prefix of two suffixes is attained by some adjacent pair in sorted order. Thus a substring of length max(lcp) is longest; overlapping occurrences are allowed. When there are ties, the first strictly larger maximum encountered is retained.

Time: O(n log(n+1)) for prefix doubling with counting/radix sorting of rank pairs, plus O(n) for Kasai. Auxiliary memory: O(n+1) for arrays and counting buckets; output O(n+|repeated|). For the empty string all results are empty.

Counterexample: "banana" has suffix array [5,3,1,0,4,2], LCP [0,1,3,0,0,2] and longest repeat "ana". For "aaaa", the repeated substring is "aaa" even though the two occurrences overlap.
2.8 aho — Aho–Corasick multi-pattern counting
Automaton. Build a trie for all nonempty patterns using 95 printable ASCII characters (' ' through '~'). The array endState[i] stores the terminal state for dictionary entry i; identical patterns may share a terminal state but remain separate output columns. A BFS builds the failure link of each state, pointing to the longest proper suffix that is also a trie prefix, and fills missing transitions using the failure transitions.

Scanning and propagation. For each input text, reset the current state to the root and allocate a fresh zero-filled visit array. Follow one completed automaton transition per character and increment the visited state's counter. Then process trie states in reverse BFS order and add visits[node] to visits[failure[node]]. This sends counts to all suffix patterns without enumerating every overlapping match individually. Read visits[endState[i]] for each original dictionary entry.

Correctness. After processing a character, the automaton state represents the longest dictionary-prefix suffix of the text scanned so far. Every matched pattern is either that state or an ancestor along its failure chain. Reverse-failure propagation adds precisely the occurrences of each suffix pattern. Duplicates share counts; resetting for each text ensures separate query results.

Base cases: Empty pattern dictionary -> one empty output row per text; empty texts -> no rows; empty text -> zeros for all patterns. Dictionary patterns themselves are nonempty by contract.

Time: With L total pattern length, S trie states, p dictionary entries, q texts and T total text length, preprocessing is O(L+95*S+p) and all queries take O(T+q*(S+p)). Auxiliary memory: O(95*S+p) for the automaton and one O(S) visit array; output: O(q*p+q) values. Occurrence counts use Java long.

Counterexample: Patterns ["he","she","he"], texts ["sheshe","","he"] give [[2,2,2],[0,0,0],[1,0,1]], demonstrating suffix matches, duplicate dictionary entries and independent text queries.
3. Own tests: inputs, expected output and observed result
   The repository includes tests/student/StudentTests.java, with 28 deterministic assertions. Each test invokes the Java algorithm directly, compares the result with its expected value, and throws an AssertionError on mismatch. The student ran this class and observed TOTAL: 28/28 PASS. The table records the inputs, expectations, and the kind of defect each case is intended to find. Observed: PASS for each case during the student run.
   Test	Input (relevant fields)	Expected result checked	Defect targeted	Observed
   LCS empty	a="", b=""	length 0, sequence ""	Empty-prefix handling	PASS
   LCS subsequence	a="AXBYCZ", b="ABC"	length 3, sequence "ABC"	Confusing subsequence with substring	PASS
   LCS repeated	a="AAAA", b="AA"	length 2, sequence "AA"	Repeated-character matching	PASS
   LCS tie	a="ABC", b="BAC"	valid common subsequence of length 2	Incorrect tie reconstruction	PASS
   Merge empty	sizes=[]	cost 0	Empty interval handling	PASS
   Merge single	sizes=[5]	cost 0	Unnecessary single-block merge	PASS
   Merge optimal	sizes=[2,3,4]	cost 14	Wrong choice of split	PASS
   Merge 64-bit	sizes=[10^9,10^9,10^9]	cost 5000000000	32-bit overflow	PASS
   Tree empty	n=0	weight 0	Empty-tree handling	PASS
   Tree negative	weights [-5,-2,-7], chain	weight 0	Incorrectly forcing a negative vertex	PASS
   Tree chain	weights [4,9,6], chain	weight 10, selected [0,2]	Selecting adjacent vertices	PASS
   Tree tie	two adjacent vertices, both weight 5	weight 5	Incorrect handling of equal alternatives	PASS
   Naive overlap	text "aaaaa", pattern "aaa"	positions [0,1,2]	Missing overlapping occurrences	PASS
   Naive empty pattern	text "abc", pattern ""	positions [0,1,2,3]	Missing final boundary	PASS
   Naive case-sensitive	text "aA aA", pattern "aA"	positions [0,3]	Case/space handling	PASS
   KMP overlap	text "abababa", pattern "aba"	positions [0,2,4]	Wrong prefix fallback	PASS
   KMP both empty	text "", pattern ""	positions [0]	Empty-pattern handling	PASS
   KMP long pattern	text "ab", pattern "abc"	positions []	Out-of-range comparison	PASS
   RK overlap	text "aaaaa", pattern "aaa"	positions [0,1,2]	Missing overlapping matches	PASS
   RK hash collision	text "abdj", pattern "ab"	positions [0]	Reporting collision as exact match	PASS
   RK long pattern	text "ab", pattern "abc"	positions []	Invalid initial window	PASS
   Suffix banana	text "banana"	sa=[5,3,1,0,4,2], lcp=[0,1,3,0,0,2], repeat "ana"	Ordering and LCP errors	PASS
   Suffix empty	text ""	empty arrays and ""	Empty suffix-array handling	PASS
   Suffix repeated	text "aaaa"	sa=[3,2,1,0], lcp=[0,1,2,3], repeat "aaa"	Overlap and repeated suffixes	PASS
   Aho duplicate	patterns ["he","she","he"], texts ["sheshe","","he"]	[[2,2,2],[0,0,0],[1,0,1]]	Failure links, duplicates, reset	PASS
   Aho empty dictionary	patterns [], texts ["abc",""]	[[],[]]	Incorrect output shape	PASS
   Aho no texts	patterns ["a"], texts []	[]	Empty query collection	PASS
   Aho overlap	patterns ["a","aa","aaa"], text "aaaa"	[[4,3,2]]	Missing overlapping suffix matches	PASS


The LCS tie test checks that the returned sequence is a valid optimum, rather than requiring a particular tie-breaking string. For Merge and Tree, these Java assertions verify the listed optimum values; not every case independently validates the full witness. The official public validator also checks contract-specific witness requirements.

Public check: python tools/run.py --check reported 44/44 PASS and state reset PASS, including two batches in one process. The state-reset check verifies that independent requests do not retain stale state.

Scope limitation: There is no committed scale_check.py or separate seven-case stress-test suite in this repository, so no such suite is claimed as part of this submission. The teacher's hidden scale checks are separate from the tests described here.
4. Search algorithm comparison: Naive vs. KMP vs. Rabin–Karp
   4.1 Methodology
   The submitted benchmark is tests/student/SearchBenchmark.java. It compares the same text and pattern across Naive, KMP, and Rabin–Karp. Before timing, each implementation is checked against the occurrence positions returned by Java String.indexOf; this is used as a correctness oracle only in the benchmark, not in the submitted algorithms.
- Sizes: (n,m)=(300,12), (1200,48), (3000,120).
- Random strings: characters sampled from "abcd" with Java Random(seed), where seed = 20261108 + 31*n + 17*m; the pattern is taken from the generated text, so at least one match exists.
- Repetitive strings: text is "a".repeat(n) and pattern is "a".repeat(m), generating n-m+1 overlapping matches.
- Measurement: Each algorithm is warmed up for max(200,calls) calls and measured three times using System.nanoTime(). The reported median is the middle of the three readings. The timed loop includes the selected search method, construction of its output list and result-count accumulation; it does not include compilation, JSON I/O, or expected-output generation.
- Environment: The following readings were obtained by the student on Windows with Microsoft OpenJDK 17.0.18. CPU and RAM details were not recorded. Timings depend on JIT compilation, garbage collection and other machine activity.
  4.2 Observed timing results
  Times are in milliseconds for all calls in that row, not milliseconds per search. Three runs are shown for reproducibility. PASS means the positions matched the independent expected list.
  Input	n	m	Calls/run	Algorithm	Run 1	Run 2	Run 3	Median	Matches	Correct
  Random	300	12	1000	Naive	5.080	5.128	5.051	5.080	1	PASS
  Random	300	12	1000	KMP	4.162	4.704	5.048	4.704	1	PASS
  Random	300	12	1000	Rabin–Karp	3.660	3.432	3.496	3.496	1	PASS
  Random	1200	48	200	Naive	5.434	4.860	4.215	4.860	1	PASS
  Random	1200	48	200	KMP	4.732	4.246	4.637	4.637	1	PASS
  Random	1200	48	200	Rabin–Karp	4.202	4.141	3.763	4.141	1	PASS
  Random	3000	120	40	Naive	0.218	0.215	0.227	0.218	1	PASS
  Random	3000	120	40	KMP	0.214	0.236	0.248	0.236	1	PASS
  Random	3000	120	40	Rabin–Karp	0.800	0.800	0.795	0.800	1	PASS
  Repetitive	300	12	1000	Naive	5.741	7.829	4.569	5.741	289	PASS
  Repetitive	300	12	1000	KMP	1.995	2.985	2.703	2.703	289	PASS
  Repetitive	300	12	1000	Rabin–Karp	6.413	5.722	6.146	6.146	289	PASS
  Repetitive	1200	48	200	Naive	13.150	13.565	14.383	13.565	1153	PASS
  Repetitive	1200	48	200	KMP	2.188	2.650	2.262	2.262	1153	PASS
  Repetitive	1200	48	200	Rabin–Karp	16.476	16.095	16.367	16.367	1153	PASS
  Repetitive	3000	120	40	Naive	16.017	16.156	15.967	16.017	2881	PASS
  Repetitive	3000	120	40	KMP	2.634	0.639	0.644	0.644	2881	PASS
  Repetitive	3000	120	40	Rabin–Karp	16.263	16.302	16.191	16.263	2881	PASS


The benchmark completed with all 18 algorithm/case correctness checks passing.
4.3 Interpretation
- Naive: Worst-case O(n*m) comparisons. On random text, many windows fail after checking only a few characters, so Naive can be competitive.
- KMP: O(n+m) matching work (plus match output). Its prefix function prevents repeated checks of long matching prefixes. It was the fastest of the three methods for all three repetitive input sizes.
- Rabin–Karp: Rolling hash filtering is cheap, but every equal-hash candidate is verified character by character. Repetitive inputs generate a match at nearly every starting position, producing worst-case O(n*m) work in this exact-matching implementation. The small modulus 101 also permits accidental hash collisions.
- Measurement limitations: The benchmark reduces calls/run as input size grows, so elapsed times for different size rows cannot be compared directly. The 3000-character repetitive KMP measurements show notable variation (2.634 vs. 0.639 ms). JIT, garbage collection, CPU scheduling and memory allocation contribute noise. Theoretical complexity should be derived from algorithm steps, not inferred solely from these timings.
5. Complexity overview
   Algorithm	Worst-case time	Auxiliary space (excluding output)
   LCS	O(n*m)	O(n*m)
   Ordered merge DP	O(n^3)	O(n^2)
   Tree independent set DP	O(n)	O(n)
   Naive search	O(n*m+n+1)	O(1)
   KMP	O(n+m+z+1) including output	O(m)
   Rabin–Karp	O(n*m+n+m+1)	O(1)
   Suffix array + Kasai LCP	O(n log(n+1))	O(n)
   Aho–Corasick	O(L+95*S+T+q*(S+p))	O(95*S+p+S)


z is the number of reported matches. For Aho–Corasick, L is the combined pattern length, S the trie state count, T the combined text length, q the number of texts and p the number of patterns. Output storage is additional.
6. Sources, AI assistance, and limitations
   Course materials: The teacher-provided README.md, docs/Assignment_4_EN.md, docs/guide/CONTRACTS.md, docs/guide/RUBRIC.md, and docs/guide/TESTING.md, plus lectures on dynamic programming and string processing. Standard algorithm ideas include prefix and interval DP, tree take/skip DP, KMP, polynomial rolling hashes, suffix doubling and Kasai LCP, and Aho–Corasick.

AI disclosure: OpenAI ChatGPT assisted with the Java implementation, drafting this analysis, and preparing the additional Java tests and the search benchmark. The student ran the public checks, Java tests and benchmark on Windows with JDK 17, then supplied the console results. A separate independent Linux testing environment was also used during development; its measurements are not represented as the student's local benchmark results above. The student is responsible for reviewing the submitted work and explaining the algorithms in the individual defence; assistance during the assessed individual quiz must follow the instructor's rules.

Conclusion: The implementation passed 44 published checks, the provided state-reset check and 28 student-written Java assertions. Its search benchmark returned correct positions on all 18 measured algorithm/case combinations. These checks support correctness for the observed cases but cannot establish that all hidden grading cases will pass.