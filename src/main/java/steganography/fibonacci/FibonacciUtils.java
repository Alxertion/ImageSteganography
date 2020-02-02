package steganography.fibonacci;

/**
 * Not currently used. An attempted encoding using the fibonacci function was present,
 * but the quick increasing of the fibonacci function values made it unusable without
 * any other modifications.
 */
public class FibonacciUtils {
    /**
     * The golden ratio, stored in a constant so that we calculate it only once.
     */
    private static final double GOLDEN_RATIO = (1 + Math.sqrt(5)) / 2.0;

    /**
     * This method returns the next fibonacci number after a given fibonacci number.
     * It uses one of the properties of fibonacci numbers: when multiplied with the
     * golden ratio = 1.618..., and rounded, the next fibonacci number is obtained.
     * Complexity: O(1).
     */
    public static int getNextFibonacciNumber(int currentNumber) {
        return (int) Math.round(currentNumber * GOLDEN_RATIO);
    }

    /**
     * This method returns the nth fibonacci number, n given as a parameter.
     * Complexity: O(n).
     */
    public static int getNthFibonacciNumber(int n) {
        // initialization
        int f1 = 1;
        int f2 = 2;
        int f3;

        // if our n is 0 (the first number), we return 1
        if (n == 0) {
            return f1;
        }

        // if our n is >= 2, we compute the next fibonacci numbers
        for (int i = 2; i <= n; i++) {
            f3 = f1 + f2;
            f1 = f2;
            f2 = f3;
        }

        // we return f2 (as that will be the nth number).
        return f2;
    }
}
