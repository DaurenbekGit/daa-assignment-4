# Assignment 4 — Algorithm Analysis and Verification

**Course:** Design and Analysis of Algorithms (DAA)  
**Assignment:** 4 — Dynamic Programming and String Processing  
**Repository:** DaurenbekGit/daa-assignment-4  
**Implementation:** Java (JDK 17)  
**Input/Output:** UTF-8 JSON requests and responses. All indices are zero-based.

---

## 1. Environment, Execution, and Verification

The assignment is implemented in Java using standard libraries, arrays, and collections. No external algorithm libraries are used.

The project contains eight algorithm implementations in `Java/source/Algorithms.java`.

**Development environment:**

- Operating System: Windows
- IDE: IntelliJ IDEA
- Java Version: Microsoft OpenJDK 17.0.18
- Programming Language: Java
- Testing Tool: Python launcher provided in the assignment template

### Running the Tests

From the project root directory, execute:

```powershell
java -version
javac -version
python tools/run.py --check
```

### Public Test Results

The implementation successfully passed all 44 public tests.

| Test Category | Result |
|---|---|
| LCS | PASS |
| Optimal Merge | PASS |
| Tree DP | PASS |
| Naive Search | PASS |
| KMP | PASS |
| Rabin–Karp | PASS |
| Suffix Array | PASS |
| Aho–Corasick | PASS |
| State Reset | PASS |

**Final public test result: 44/44 PASS.**

The state-reset verification also passed, confirming that consecutive requests can be handled without retaining incorrect state.

Additional independent verification of the implementation included 28 deterministic cases and seven larger stress cases.

The additional tests and benchmarks were performed in a separate Linux environment using OpenJDK 21 with Java 17 compilation compatibility.

Passing public tests confirms correctness for those test cases but does not guarantee success on all hidden tests.

---

## 2. Algorithm Explanations and Correctness

### 2.1 Longest Common Subsequence (LCS)

**Algorithm:** Dynamic Programming

**Objective:** Find the longest sequence of characters appearing in both strings in the same relative order. The characters do not need to be consecutive.

**DP State:**

`dp[i][j]` represents the length of the Longest Common Subsequence between the first `i` characters of string `a` and the first `j` characters of string `b`.

**Base Case:**

If either string is empty, the LCS length is zero.

`dp[0][j] = 0`

`dp[i][0] = 0`

**Transition:**

If the characters match:

`dp[i][j] = dp[i-1][j-1] + 1`

Otherwise:

`dp[i][j] = max(dp[i-1][j], dp[i][j-1])`

**Processing Order:**

The DP table is filled from smaller prefixes to larger prefixes. Each state depends only on previously calculated states.

**Reconstruction:**

Starting from `dp[n][m]`, the algorithm moves backward through the table.

If characters match, the character is added to the result.

Otherwise, the algorithm moves toward the neighboring cell with the larger DP value.

If both values are equal, the implementation chooses the upper cell.

The reconstructed sequence is reversed before returning.

**Correctness:**

The algorithm considers all possible prefixes. If the final characters match, they can extend an optimal common subsequence. Otherwise, at least one final character must be excluded. The recurrence chooses the better alternative, ensuring optimality.

**Complexity:**

- Time Complexity: `O(n * m)`
- Space Complexity: `O(n * m)`

**Example:**

Input:

`a = "AXBYCZ"`

`b = "ABC"`

Output:

`length = 3`

`sequence = "ABC"`

This example demonstrates that LCS is different from the longest common substring.

---

### 2.2 Optimal Ordered Adjacent-Block Merging

**Algorithm:** Interval Dynamic Programming

**Objective:** Find the minimum total cost required to merge consecutive blocks into one block.

Only adjacent blocks can be merged.

**DP State:**

`dp[l][r]` represents the minimum cost of merging all blocks from index `l` to index `r`.

**Base Case:**

A single block requires no merging.

`dp[i][i] = 0`

For zero or one block, the result is zero.

**Transition:**

For each interval `[l, r]`, every possible split position `k` is considered.

`dp[l][r] = min(dp[l][k] + dp[k+1][r] + sum(l,r))`

Here, `sum(l,r)` represents the total size of blocks in the interval.

Prefix sums are used to calculate interval sums efficiently.

**Processing Order:**

Intervals are processed by increasing length, starting from intervals of length two.

This ensures that smaller subproblems are solved before larger ones.

**Reconstruction:**

The algorithm stores the optimal splitting position for each interval.

A recursive procedure reconstructs the merge plan, first processing the left and right subintervals and then recording their final merge.

**Correctness:**

Every valid merge plan has a final operation combining two adjacent intervals. The algorithm considers every possible final split and selects the minimum-cost solution. Therefore, the resulting plan has optimal total cost.

**Complexity:**

- Time Complexity: `O(n^3)`
- Space Complexity: `O(n^2)`

**Example:**

Input:

`sizes = [2, 3, 4]`

First merge:

`2 + 3 = 5`

Second merge:

`5 + 4 = 9`

Total cost:

`5 + 9 = 14`

**Output:**

`cost = 14`

The implementation uses the Java `long` type to prevent integer overflow when calculating large costs.

---

### 2.3 Maximum-Weight Independent Set on a Tree

**Algorithm:** Tree Dynamic Programming

**Objective:** Select vertices with the maximum possible total weight so that no two selected vertices are connected by an edge.

**DP States:**

`take[u]` represents the maximum weight in the subtree rooted at vertex `u` when vertex `u` is selected.

`skip[u]` represents the maximum weight when vertex `u` is not selected.

**Base Cases:**

For a leaf vertex:

`take[u] = weight[u]`

`skip[u] = 0`

An empty tree has an optimal weight of zero.

**Transitions:**

If vertex `u` is selected, its children cannot be selected:

`take[u] = weight[u] + sum(skip[v])`

If vertex `u` is not selected, each child can either be selected or skipped:

`skip[u] = sum(max(take[v], skip[v]))`

**Processing Order:**

An iterative traversal determines the parent-child relationships.

Vertices are processed in reverse traversal order so that child states are computed before parent states.

This avoids recursion-depth problems in large trees.

**Reconstruction:**

A vertex is selected if its parent is not selected and choosing the vertex gives a greater result than skipping it.

The final optimal weight is:

`max(take[0], skip[0])`

**Correctness:**

When a vertex is selected, all adjacent child vertices must be excluded. When a vertex is skipped, every child subtree can independently choose its optimal state. These transitions cover every valid independent set.

**Complexity:**

- Time Complexity: `O(n)`
- Space Complexity: `O(n)`

**Example:**

Vertex weights:

`[4, 9, 6]`

Edges:

`[[0,1], [1,2]]`

Selecting vertices `0` and `2` gives:

`4 + 6 = 10`

**Output:**

`weight = 10`

`selected = [0, 2]`

For a tree containing only negative weights, choosing no vertices gives an optimal weight of zero.

---

### 2.4 Naive String Matching

**Algorithm:** Brute Force String Search

**Objective:** Find every starting position where a pattern appears inside a text.

**Approach:**

The algorithm checks every possible starting position in the text.

For each position, it compares the pattern characters with the corresponding characters in the text.

If every character matches, the starting position is added to the result.

**Correctness:**

Every possible window of length `m` is examined exactly once. A starting position is returned only when all pattern characters match.

Therefore, no valid occurrence is missed.

**Special Cases:**

- Empty pattern: every position from `0` to `n` is a match.
- Pattern longer than text: no matches.
- Overlapping matches are included.

**Complexity:**

- Worst-case Time Complexity: `O(n * m)`
- Auxiliary Space Complexity: `O(1)`, excluding the output list.

**Example:**

Text: `"aaaaa"`

Pattern: `"aaa"`

Output:

`[0, 1, 2]`

The algorithm returns overlapping occurrences.

---

### 2.5 Knuth–Morris–Pratt (KMP)

**Algorithm:** Prefix Function-Based String Matching

**Objective:** Find every occurrence of a pattern without repeatedly comparing characters that have already been processed.

**Main Idea:**

KMP uses a prefix function to determine how far the pattern can shift after a mismatch.

**Prefix Function:**

`pi[i]` stores the length of the longest proper prefix of the pattern substring ending at index `i` that is also its suffix.

**Preprocessing:**

The prefix function is constructed in linear time.

When a mismatch occurs, the algorithm uses:

`j = pi[j-1]`

This avoids restarting the comparison from the beginning.

**Searching:**

The text is scanned from left to right.

If the current character matches, the matched prefix length increases.

When the entire pattern matches, its starting position is recorded.

After finding a match, the prefix function is used again to detect overlapping occurrences.

**Correctness:**

The prefix function identifies the longest previously matched prefix that can still form part of a valid occurrence. Therefore, the algorithm avoids unnecessary comparisons without skipping possible matches.

**Complexity:**

- Time Complexity: `O(n + m)`
- Auxiliary Space Complexity: `O(m)`

**Example:**

Text: `"abababa"`

Pattern: `"aba"`

Output:

`[0, 2, 4]`

KMP efficiently detects overlapping occurrences.

---

### 2.6 Rabin–Karp String Matching

**Algorithm:** Rolling Hash

**Objective:** Find pattern occurrences by comparing hash values before performing exact character comparisons.

**Hash Function:**

The implementation uses:

- Base: `31`
- Modulus: `101`

Each substring is represented by a polynomial hash.

**Preprocessing:**

The algorithm calculates the hash of the pattern and the first text window of the same length.

**Rolling Hash:**

When the window moves one character forward, the old leftmost character is removed and the new rightmost character is added.

This allows the next hash to be calculated in constant time.

**Collision Handling:**

Two different strings can have the same hash.

Therefore, when the pattern hash equals the current window hash, the implementation compares the actual characters.

A match is recorded only when the strings are equal.

**Correctness:**

Different hash values guarantee that the strings are different. Equal hash values are verified through character comparisons, eliminating false matches caused by collisions.

**Complexity:**

- Average-case Time Complexity: approximately `O(n + m)` under favorable hashing and input distributions.
- Worst-case Time Complexity: `O(n * m)`
- Auxiliary Space Complexity: `O(1)`, excluding output.

**Example:**

Text: `"abdj"`

Pattern: `"ab"`

Output:

`[0]`

The substrings `"ab"` and `"dj"` produce the same hash under the selected hash function, but direct verification prevents an incorrect second match.

---

### 2.7 Suffix Array and Kasai LCP

**Algorithms:** Prefix Doubling and Kasai's Algorithm

**Objective:** Build the suffix array, compute the Longest Common Prefix array, and find the longest repeated substring.

**Suffix Array:**

A suffix array contains the starting positions of all suffixes sorted in lexicographical order.

**Construction:**

The implementation uses a prefix-doubling algorithm.

Initially, suffixes are ranked by their first character.

During each iteration, the algorithm compares pairs of ranks representing progressively longer prefixes.

Stable counting sort is used to efficiently sort these rank pairs.

The process continues until the suffixes receive distinct ranks.

**LCP Array:**

The LCP array stores the longest common prefix lengths of adjacent suffixes in sorted order.

Kasai's algorithm computes the LCP array using the suffix array and its inverse.

Previously calculated prefix information is reused to avoid unnecessary comparisons.

**Longest Repeated Substring:**

The longest repeated substring corresponds to the maximum value in the LCP array.

Overlapping occurrences are allowed.

**Correctness:**

Prefix doubling eventually distinguishes all suffixes and places them in lexicographical order.

Kasai's algorithm correctly computes the common prefixes of adjacent sorted suffixes.

The maximum adjacent LCP identifies the longest substring appearing at least twice.

**Complexity:**

- Suffix Array Construction: `O(n log n)`
- Kasai LCP Construction: `O(n)`
- Auxiliary Space Complexity: `O(n)`

**Example:**

Input:

`"banana"`

Suffix Array:

`[5, 3, 1, 0, 4, 2]`

LCP Array:

`[0, 1, 3, 0, 0, 2]`

Longest Repeated Substring:

`"ana"`

---

### 2.8 Aho–Corasick Multiple Pattern Matching

**Algorithm:** Trie, Failure Links, and Breadth-First Search

**Objective:** Count occurrences of multiple patterns in multiple texts efficiently.

**Trie Construction:**

All dictionary patterns are inserted into a trie.

Each trie node represents a prefix of at least one pattern.

**Failure Links:**

Failure links are constructed using Breadth-First Search.

A failure link connects a state to the longest suffix that is also a prefix represented in the trie.

**Automaton Transitions:**

Missing transitions are completed using failure links.

This allows text processing without repeatedly restarting the search.

**Searching:**

For each text, the algorithm starts from the root state.

Characters are processed one at a time, updating the automaton state.

Every visited state increases its occurrence counter.

**Occurrence Propagation:**

After scanning the text, occurrence counts are propagated through failure links in reverse BFS order.

This ensures that suffix patterns are counted correctly.

**Correctness:**

The automaton maintains the longest dictionary prefix matching a suffix of the processed text.

Failure links represent all shorter suffix patterns. Propagating occurrence counts through these links counts all matching patterns, including overlapping and duplicate dictionary entries.

**Special Cases:**

- Empty dictionary: empty result rows.
- Empty text: zero occurrences.
- Duplicate patterns: identical occurrence