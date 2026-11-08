
import java.util.*;

public class SearchBenchmark {

    private static volatile long sink;

    private static final String[] ALGORITHMS = {
            "Naive", "KMP", "Rabin-Karp"
    };

    // Run selected search algorithm
    private static List<?> search(
            String algorithm,
            Map<String, Object> input
    ) {
        Map<String, Object> result;

        switch (algorithm) {
            case "Naive":
                result = Algorithms.naive(input);
                break;
            case "KMP":
                result = Algorithms.kmp(input);
                break;
            case "Rabin-Karp":
                result = Algorithms.rk(input);
                break;
            default:
                throw new IllegalArgumentException(algorithm);
        }

        return (List<?>) result.get("positions");
    }

    // Independent correctness check
    private static List<Integer> expectedPositions(
            String text, String pattern
    ) {
        List<Integer> result = new ArrayList<>();

        int from = 0;
        while (true) {
            int pos = text.indexOf(pattern, from);

            if (pos == -1) {
                break;
            }

            result.add(pos);
            from = pos + 1;
        }

        return result;
    }

    // Measure elapsed execution time
    private static double measure(
            String algorithm,
            Map<String, Object> input,
            int calls
    ) {
        long totalMatches = 0;
        long start = System.nanoTime();

        for (int i = 0; i < calls; i++) {
            totalMatches += search(algorithm, input).size();
        }

        long elapsed = System.nanoTime() - start;
        sink = totalMatches;

        return elapsed / 1_000_000.0;
    }

    // Calculate median of three measurements
    private static double median(double[] times) {
        double[] sorted = times.clone();
        Arrays.sort(sorted);
        return sorted[1];
    }

    // Benchmark one test case
    private static void runCase(
            String type,
            String text,
            String pattern,
            int calls
    ) {
        Map<String, Object> input = Map.of(
                "text", text,
                "pattern", pattern
        );

        List<Integer> expected =
                expectedPositions(text, pattern);

        System.out.println();
        System.out.println("==============================");
        System.out.println("Input type: " + type);
        System.out.println("Text length: " + text.length());
        System.out.println("Pattern length: " + pattern.length());
        System.out.println("Calls per run: " + calls);
        System.out.println("Expected matches: " + expected.size());
        System.out.println("==============================");

        for (String algorithm : ALGORITHMS) {

            // Check correctness before measuring
            List<?> actual = search(algorithm, input);

            if (!actual.equals(expected)) {
                throw new AssertionError(
                        algorithm + " returned wrong positions"
                );
            }

            // JVM warm-up
            int warmup = Math.max(200, calls);

            for (int i = 0; i < warmup; i++) {
                search(algorithm, input);
            }

            // Three independent timed runs
            double[] times = new double[3];

            for (int i = 0; i < 3; i++) {
                times[i] = measure(
                        algorithm, input, calls
                );
            }

            System.out.printf(
                    Locale.US,
                    "%-12s | %.3f | %.3f | %.3f | Median: %.3f ms | PASS%n",
                    algorithm,
                    times[0],
                    times[1],
                    times[2],
                    median(times)
            );
        }
    }

    public static void main(String[] args) {

        int[][] sizes = {
                {300, 12, 1000},
                {1200, 48, 200},
                {3000, 120, 40}
        };

        // Random input benchmarks
        for (int[] size : sizes) {
            int n = size[0];
            int m = size[1];
            int calls = size[2];

            long seed = 20261108L + 31L * n + 17L * m;
            Random random = new Random(seed);

            StringBuilder text = new StringBuilder();

            for (int i = 0; i < n; i++) {
                text.append(
                        "abcd".charAt(random.nextInt(4))
                );
            }

            String value = text.toString();
            String pattern =
                    value.substring(n / 3, n / 3 + m);

            runCase("Random", value, pattern, calls);
        }

        // Repetitive input benchmarks
        for (int[] size : sizes) {
            int n = size[0];
            int m = size[1];
            int calls = size[2];

            String text = "a".repeat(n);
            String pattern = "a".repeat(m);

            runCase("Repetitive", text, pattern, calls);
        }

        System.out.println();
        System.out.println("BENCHMARK COMPLETED");
        System.out.println("All correctness checks passed.");
    }
}
