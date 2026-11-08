import java.util.*;

public class StudentTests {

    static int passed = 0;

    static void check(String name, boolean condition) {
        if (!condition) {
            throw new AssertionError("FAIL: " + name);
        }
        System.out.println("PASS: " + name);
        passed++;
    }

    static boolean isSubsequence(String sub, String text) {
        int j = 0;

        for (int i = 0; i < text.length() && j < sub.length(); i++) {
            if (text.charAt(i) == sub.charAt(j)) {
                j++;
            }
        }
        return j == sub.length();
    }

    public static void main(String[] args) {

        // =========================
        // PART 1: LCS (4 tests)
        // =========================

        Map<String, Object> lcs1 = Algorithms.lcs(
                Map.of("a", "", "b", ""));
        check("LCS empty", lcs1.get("length").equals(0)
                && lcs1.get("sequence").equals(""));

        Map<String, Object> lcs2 = Algorithms.lcs(
                Map.of("a", "AXBYCZ", "b", "ABC"));
        check("LCS subsequence", lcs2.get("length").equals(3)
                && lcs2.get("sequence").equals("ABC"));

        Map<String, Object> lcs3 = Algorithms.lcs(
                Map.of("a", "AAAA", "b", "AA"));
        check("LCS repeated", lcs3.get("length").equals(2)
                && lcs3.get("sequence").equals("AA"));

        Map<String, Object> lcs4 = Algorithms.lcs(
                Map.of("a", "ABC", "b", "BAC"));
        String seq = (String) lcs4.get("sequence");
        check("LCS tie", lcs4.get("length").equals(2)
                && seq.length() == 2
                && isSubsequence(seq, "ABC")
                && isSubsequence(seq, "BAC"));

        // =========================
        // PART 2: MERGE DP (4 tests)
        // =========================

        Map<String, Object> merge1 = Algorithms.merge(
                Map.of("sizes", List.of()));
        check("Merge empty", merge1.get("cost").equals(0L));

        Map<String, Object> merge2 = Algorithms.merge(
                Map.of("sizes", List.of(5)));
        check("Merge single", merge2.get("cost").equals(0L));

        Map<String, Object> merge3 = Algorithms.merge(
                Map.of("sizes", List.of(2, 3, 4)));
        check("Merge optimal", merge3.get("cost").equals(14L));

        Map<String, Object> merge4 = Algorithms.merge(
                Map.of("sizes", List.of(
                        1000000000, 1000000000, 1000000000)));
        check("Merge 64-bit",
                merge4.get("cost").equals(5000000000L));

        // =========================
        // PART 3: TREE DP (4 tests)
        // =========================

        Map<String, Object> tree1 = Algorithms.tree(
                Map.of("n", 0,
                        "weights", List.of(),
                        "edges", List.of()));
        check("Tree empty", tree1.get("weight").equals(0L));

        Map<String, Object> tree2 = Algorithms.tree(
                Map.of("n", 3,
                        "weights", List.of(-5, -2, -7),
                        "edges", List.of(
                                List.of(0, 1),
                                List.of(1, 2))));
        check("Tree negative", tree2.get("weight").equals(0L));

        Map<String, Object> tree3 = Algorithms.tree(
                Map.of("n", 3,
                        "weights", List.of(4, 9, 6),
                        "edges", List.of(
                                List.of(0, 1),
                                List.of(1, 2))));
        check("Tree chain", tree3.get("weight").equals(10L)
                && tree3.get("selected").equals(List.of(0, 2)));

        Map<String, Object> tree4 = Algorithms.tree(
                Map.of("n", 2,
                        "weights", List.of(5, 5),
                        "edges", List.of(List.of(0, 1))));
        check("Tree tie", tree4.get("weight").equals(5L));

        // =========================
        // PART 4A: NAIVE (3 tests)
        // =========================

        Map<String, Object> naive1 = Algorithms.naive(
                Map.of("text", "aaaaa", "pattern", "aaa"));
        check("Naive overlap", naive1.get("positions")
                .equals(List.of(0, 1, 2)));

        Map<String, Object> naive2 = Algorithms.naive(
                Map.of("text", "abc", "pattern", ""));
        check("Naive empty pattern", naive2.get("positions")
                .equals(List.of(0, 1, 2, 3)));

        Map<String, Object> naive3 = Algorithms.naive(
                Map.of("text", "aA aA", "pattern", "aA"));
        check("Naive case sensitive", naive3.get("positions")
                .equals(List.of(0, 3)));

        // =========================
        // PART 4B: KMP (3 tests)
        // =========================

        Map<String, Object> kmp1 = Algorithms.kmp(
                Map.of("text", "abababa", "pattern", "aba"));
        check("KMP overlap", kmp1.get("positions")
                .equals(List.of(0, 2, 4)));

        Map<String, Object> kmp2 = Algorithms.kmp(
                Map.of("text", "", "pattern", ""));
        check("KMP both empty", kmp2.get("positions")
                .equals(List.of(0)));

        Map<String, Object> kmp3 = Algorithms.kmp(
                Map.of("text", "ab", "pattern", "abc"));
        check("KMP long pattern", kmp3.get("positions")
                .equals(List.of()));

        // =========================
        // PART 4C: RABIN-KARP (3 tests)
        // =========================

        Map<String, Object> rk1 = Algorithms.rk(
                Map.of("text", "aaaaa", "pattern", "aaa"));
        check("RK overlap", rk1.get("positions")
                .equals(List.of(0, 1, 2)));

        Map<String, Object> rk2 = Algorithms.rk(
                Map.of("text", "abdj", "pattern", "ab"));
        check("RK hash collision", rk2.get("positions")
                .equals(List.of(0)));

        Map<String, Object> rk3 = Algorithms.rk(
                Map.of("text", "ab", "pattern", "abc"));
        check("RK long pattern", rk3.get("positions")
                .equals(List.of()));

        // =========================
        // PART 5: SUFFIX ARRAY (3 tests)
        // =========================

        Map<String, Object> suffix1 = Algorithms.suffix(
                Map.of("text", "banana"));
        check("Suffix banana",
                suffix1.get("sa").equals(
                        List.of(5, 3, 1, 0, 4, 2))
                        && suffix1.get("lcp").equals(
                        List.of(0, 1, 3, 0, 0, 2))
                        && suffix1.get("repeated").equals("ana"));

        Map<String, Object> suffix2 = Algorithms.suffix(
                Map.of("text", ""));
        check("Suffix empty",
                suffix2.get("sa").equals(List.of())
                        && suffix2.get("lcp").equals(List.of())
                        && suffix2.get("repeated").equals(""));

        Map<String, Object> suffix3 = Algorithms.suffix(
                Map.of("text", "aaaa"));
        check("Suffix repeated",
                suffix3.get("sa").equals(List.of(3, 2, 1, 0))
                        && suffix3.get("lcp").equals(List.of(0, 1, 2, 3))
                        && suffix3.get("repeated").equals("aaa"));

        // =========================
        // PART 6: AHO-CORASICK (4 tests)
        // =========================

        Map<String, Object> aho1 = Algorithms.aho(
                Map.of("patterns", List.of("he", "she", "he"),
                        "texts", List.of("sheshe", "", "he")));
        check("Aho duplicate patterns",
                aho1.get("counts").equals(List.of(
                        List.of(2L, 2L, 2L),
                        List.of(0L, 0L, 0L),
                        List.of(1L, 0L, 1L))));

        Map<String, Object> aho2 = Algorithms.aho(
                Map.of("patterns", List.of(),
                        "texts", List.of("abc", "")));
        check("Aho empty dictionary",
                aho2.get("counts").equals(List.of(
                        List.of(), List.of())));

        Map<String, Object> aho3 = Algorithms.aho(
                Map.of("patterns", List.of("a"),
                        "texts", List.of()));
        check("Aho no texts",
                aho3.get("counts").equals(List.of()));

        Map<String, Object> aho4 = Algorithms.aho(
                Map.of("patterns", List.of("a", "aa", "aaa"),
                        "texts", List.of("aaaa")));
        check("Aho overlapping",
                aho4.get("counts").equals(
                        List.of(List.of(4L, 3L, 2L))));

        // =========================
        // FINAL RESULT
        // =========================

        System.out.println();
        System.out.println("TOTAL: " + passed + "/28 PASS");
    }
}