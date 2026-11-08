import java.util.*;

public final class Algorithms {
    private Algorithms() {}

    // Part 1: Longest Common Subsequence
    static Map<String, Object> lcs(Map<String, Object> input) {
        String a = (String) input.get("a");
        String b = (String) input.get("b");
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        StringBuilder reversed = new StringBuilder();
        int i = n, j = m;

        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                reversed.append(a.charAt(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }

        return Map.of(
                "length", dp[n][m],
                "sequence", reversed.reverse().toString()
        );
    }

    // Part 2: Ordered Adjacent-Block Merging
    static Map<String, Object> merge(Map<String, Object> input) {
        List<?> sizes = (List<?>) input.get("sizes");
        int n = sizes.size();

        if (n <= 1) {
            return Map.of("cost", 0L, "plan", List.of());
        }

        long[] prefix = new long[n + 1];

        for (int i = 0; i < n; i++) {
            prefix[i + 1] =
                    prefix[i] + ((Number) sizes.get(i)).longValue();
        }

        long[][] dp = new long[n][n];
        int[][] split = new int[n][n];

        for (int len = 2; len <= n; len++) {
            for (int left = 0; left + len <= n; left++) {
                int right = left + len - 1;
                long total = prefix[right + 1] - prefix[left];
                long best = Long.MAX_VALUE;

                for (int k = left; k < right; k++) {
                    long candidate =
                            dp[left][k] + dp[k + 1][right] + total;

                    if (candidate < best) {
                        best = candidate;
                        split[left][right] = k;
                    }
                }

                dp[left][right] = best;
            }
        }

        List<List<Integer>> plan = new ArrayList<>();
        buildMergePlan(0, n - 1, split, plan);

        return Map.of(
                "cost", dp[0][n - 1],
                "plan", plan
        );
    }

    private static void buildMergePlan(
            int left,
            int right,
            int[][] split,
            List<List<Integer>> plan
    ) {
        if (left >= right) {
            return;
        }

        int k = split[left][right];

        buildMergePlan(left, k, split, plan);
        buildMergePlan(k + 1, right, split, plan);

        plan.add(List.of(left, k, right));
    }

    // Part 3: Maximum-Weight Independent Set on a Tree
    static Map<String, Object> tree(Map<String, Object> input) {
        int n = ((Number) input.get("n")).intValue();

        if (n == 0) {
            return Map.of("weight", 0L, "selected", List.of());
        }

        List<?> weights = (List<?>) input.get("weights");
        List<?> edges = (List<?>) input.get("edges");

        int[] head = new int[n];
        Arrays.fill(head, -1);

        int[] to = new int[2 * (n - 1)];
        int[] next = new int[2 * (n - 1)];

        int edgeCount = 0;

        for (Object edgeObject : edges) {
            List<?> edge = (List<?>) edgeObject;

            int u = ((Number) edge.get(0)).intValue();
            int v = ((Number) edge.get(1)).intValue();

            to[edgeCount] = v;
            next[edgeCount] = head[u];
            head[u] = edgeCount++;

            to[edgeCount] = u;
            next[edgeCount] = head[v];
            head[v] = edgeCount++;
        }

        int[] parent = new int[n];
        int[] order = new int[n];

        parent[0] = -1;
        order[0] = 0;

        int orderSize = 1;

        for (int i = 0; i < orderSize; i++) {
            int u = order[i];

            for (int e = head[u]; e != -1; e = next[e]) {
                int v = to[e];

                if (v == parent[u]) {
                    continue;
                }

                parent[v] = u;
                order[orderSize++] = v;
            }
        }

        long[] take = new long[n];
        long[] skip = new long[n];

        for (int i = n - 1; i >= 0; i--) {
            int u = order[i];

            take[u] =
                    ((Number) weights.get(u)).longValue();

            for (int e = head[u]; e != -1; e = next[e]) {
                int v = to[e];

                if (v == parent[u]) {
                    continue;
                }

                take[u] += skip[v];
                skip[u] += Math.max(take[v], skip[v]);
            }
        }

        boolean[] chosen = new boolean[n];
        List<Integer> selected = new ArrayList<>();

        for (int u : order) {
            boolean parentChosen =
                    parent[u] >= 0 && chosen[parent[u]];

            if (!parentChosen && take[u] > skip[u]) {
                chosen[u] = true;
                selected.add(u);
            }
        }

        return Map.of(
                "weight", Math.max(take[0], skip[0]),
                "selected", selected
        );
    }

    // Part 4a: Naive String Search
    static Map<String, Object> naive(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");

        int n = text.length();
        int m = pattern.length();

        List<Integer> positions = new ArrayList<>();

        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                positions.add(i);
            }

            return Map.of("positions", positions);
        }

        for (int start = 0; start + m <= n; start++) {
            int j = 0;

            while (j < m &&
                    text.charAt(start + j) == pattern.charAt(j)) {
                j++;
            }

            if (j == m) {
                positions.add(start);
            }
        }

        return Map.of("positions", positions);
    }

    // Part 4b: Knuth-Morris-Pratt
    static Map<String, Object> kmp(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");

        int n = text.length();
        int m = pattern.length();

        List<Integer> positions = new ArrayList<>();

        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                positions.add(i);
            }

            return Map.of("positions", positions);
        }

        int[] pi = new int[m];

        for (int i = 1, j = 0; i < m; i++) {
            while (j > 0 &&
                    pattern.charAt(i) != pattern.charAt(j)) {
                j = pi[j - 1];
            }

            if (pattern.charAt(i) == pattern.charAt(j)) {
                j++;
            }

            pi[i] = j;
        }

        for (int i = 0, j = 0; i < n; i++) {
            while (j > 0 &&
                    text.charAt(i) != pattern.charAt(j)) {
                j = pi[j - 1];
            }

            if (text.charAt(i) == pattern.charAt(j)) {
                j++;
            }

            if (j == m) {
                positions.add(i - m + 1);
                j = pi[j - 1];
            }
        }

        return Map.of("positions", positions);
    }

    // Part 4c: Rabin-Karp
    static Map<String, Object> rk(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");

        int n = text.length();
        int m = pattern.length();

        List<Integer> positions = new ArrayList<>();

        if (m == 0) {
            for (int i = 0; i <= n; i++) {
                positions.add(i);
            }

            return Map.of("positions", positions);
        }

        if (m > n) {
            return Map.of("positions", positions);
        }

        final int base = 31;
        final int mod = 101;

        int highPower = 1;

        for (int i = 1; i < m; i++) {
            highPower = (highPower * base) % mod;
        }

        int patternHash = 0;
        int windowHash = 0;

        for (int i = 0; i < m; i++) {
            patternHash =
                    (patternHash * base + pattern.charAt(i)) % mod;

            windowHash =
                    (windowHash * base + text.charAt(i)) % mod;
        }

        for (int start = 0; start + m <= n; start++) {
            if (patternHash == windowHash) {
                int j = 0;

                while (j < m &&
                        pattern.charAt(j) == text.charAt(start + j)) {
                    j++;
                }

                if (j == m) {
                    positions.add(start);
                }
            }

            if (start + m < n) {
                int removed =
                        (text.charAt(start) * highPower) % mod;

                windowHash =
                        (windowHash - removed + mod) % mod;

                windowHash =
                        (windowHash * base +
                                text.charAt(start + m)) % mod;
            }
        }

        return Map.of("positions", positions);
    }

    // Part 5: Suffix Array + Kasai LCP
    static Map<String, Object> suffix(Map<String, Object> input) {
        String text = (String) input.get("text");
        int n = text.length();

        if (n == 0) {
            return Map.of(
                    "sa", List.of(),
                    "lcp", List.of(),
                    "repeated", ""
            );
        }

        int[] sa = new int[n];
        int[] rank = new int[n];
        int[] temp = new int[n];
        int[] newRank = new int[n];

        int[] count = new int[Math.max(130, n + 2)];

        for (int i = 0; i < n; i++) {
            sa[i] = i;
            rank[i] = text.charAt(i);
        }

        int classes = 127;

        for (int step = 1; step < n; step *= 2) {
            // Counting sort by second rank
            Arrays.fill(count, 0);

            for (int v : sa) {
                int key = v + step < n
                        ? rank[v + step] + 1
                        : 0;

                count[key]++;
            }

            for (int i = 1; i < count.length; i++) {
                count[i] += count[i - 1];
            }

            for (int i = n - 1; i >= 0; i--) {
                int v = sa[i];

                int key = v + step < n
                        ? rank[v + step] + 1
                        : 0;

                temp[--count[key]] = v;
            }

            // Counting sort by first rank
            Arrays.fill(count, 0);

            for (int v : temp) {
                count[rank[v] + 1]++;
            }

            for (int i = 1; i < count.length; i++) {
                count[i] += count[i - 1];
            }

            for (int i = n - 1; i >= 0; i--) {
                int v = temp[i];
                sa[--count[rank[v] + 1]] = v;
            }

            int newClasses = 1;
            newRank[sa[0]] = 0;

            for (int i = 1; i < n; i++) {
                int a = sa[i - 1];
                int b = sa[i];

                int secondA = a + step < n
                        ? rank[a + step]
                        : -1;

                int secondB = b + step < n
                        ? rank[b + step]
                        : -1;

                if (rank[a] != rank[b] ||
                        secondA != secondB) {
                    newClasses++;
                }

                newRank[b] = newClasses - 1;
            }

            int[] swap = rank;
            rank = newRank;
            newRank = swap;

            classes = newClasses;

            if (classes == n) {
                break;
            }
        }

        int[] inverse = new int[n];

        for (int i = 0; i < n; i++) {
            inverse[sa[i]] = i;
        }

        int[] lcp = new int[n];

        int common = 0;
        int bestLength = 0;
        int bestStart = 0;

        for (int i = 0; i < n; i++) {
            int sortedIndex = inverse[i];

            if (sortedIndex == 0) {
                common = 0;
                continue;
            }

            int prev = sa[sortedIndex - 1];

            while (i + common < n &&
                    prev + common < n &&
                    text.charAt(i + common) ==
                            text.charAt(prev + common)) {
                common++;
            }

            lcp[sortedIndex] = common;

            if (common > bestLength) {
                bestLength = common;
                bestStart = i;
            }

            if (common > 0) {
                common--;
            }
        }

        List<Integer> saResult = new ArrayList<>(n);
        List<Integer> lcpResult = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            saResult.add(sa[i]);
            lcpResult.add(lcp[i]);
        }

        return Map.of(
                "sa", saResult,
                "lcp", lcpResult,
                "repeated",
                text.substring(bestStart, bestStart + bestLength)
        );
    }

    // Part 6: Aho-Corasick
    static Map<String, Object> aho(Map<String, Object> input) {
        List<?> patterns = (List<?>) input.get("patterns");
        List<?> texts = (List<?>) input.get("texts");

        final int alphabet = 95;

        int maxStates = 1;

        for (Object p : patterns) {
            maxStates += ((String) p).length();
        }

        int[] go = new int[maxStates * alphabet];
        int[] failure = new int[maxStates];
        int[] endState = new int[patterns.size()];

        int states = 1;

        for (int i = 0; i < patterns.size(); i++) {
            String pattern = (String) patterns.get(i);
            int state = 0;

            for (int j = 0; j < pattern.length(); j++) {
                int c = pattern.charAt(j) - 32;
                int index = state * alphabet + c;

                if (go[index] == 0) {
                    go[index] = states++;
                }

                state = go[index];
            }

            endState[i] = state;
        }

        int[] bfsOrder = new int[states];
        bfsOrder[0] = 0;

        int queueHead = 0;
        int queueTail = 1;

        while (queueHead < queueTail) {
            int state = bfsOrder[queueHead++];

            for (int c = 0; c < alphabet; c++) {
                int edge = state * alphabet + c;
                int child = go[edge];

                if (child != 0) {
                    failure[child] = (state == 0)
                            ? 0
                            : go[failure[state] * alphabet + c];

                    bfsOrder[queueTail++] = child;

                } else if (state != 0) {
                    go[edge] =
                            go[failure[state] * alphabet + c];
                }
            }
        }

        List<List<Long>> allCounts =
                new ArrayList<>(texts.size());

        for (Object textObject : texts) {
            String text = (String) textObject;

            long[] visits = new long[states];
            int state = 0;

            for (int i = 0; i < text.length(); i++) {
                state = go[
                        state * alphabet +
                                (text.charAt(i) - 32)
                        ];

                visits[state]++;
            }

            for (int i = states - 1; i > 0; i--) {
                int node = bfsOrder[i];
                visits[failure[node]] += visits[node];
            }

            List<Long> counts =
                    new ArrayList<>(patterns.size());

            for (int node : endState) {
                counts.add(visits[node]);
            }

            allCounts.add(counts);
        }

        return Map.of("counts", allCounts);
    }
}