package RateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 
 * RATE LIMITER LLD (Token Bucket) - Complete Java Implementation
 
 */

/**
 * RateLimiter interface
 *
 * We define a contract:
 * "Given a key (like userId/apiKey), should we allow request?"
 */
interface RateLimiter {
    boolean allowRequest(String key);
}

/**
 * TokenBucket class (core logic per user)
 */
class TokenBucket {

    // Maximum number of tokens bucket can hold
    private final long capacity;

    // How many tokens are added per second
    private final long refillTokensPerSecond;

    // Current tokens in the bucket
    private long currentTokens;

    // Last time we refilled the bucket
    private long lastRefillTimeNanos;

    public TokenBucket(long capacity, long refillTokensPerSecond) {
        this.capacity = capacity;
        this.refillTokensPerSecond = refillTokensPerSecond;
        this.currentTokens = capacity;
        this.lastRefillTimeNanos = System.nanoTime();
    }

    /**
     * Try to consume one token
     */
    public synchronized boolean tryConsume() {

        refillTokensIfNeeded();

        if (currentTokens > 0) {
            currentTokens--;
            return true;
        }

        return false;
    }

    /**
     * Refill tokens based on elapsed time
     */
    private void refillTokensIfNeeded() {

        long nowNanos = System.nanoTime();

        long elapsedNanos = nowNanos - lastRefillTimeNanos;

        double elapsedSeconds = elapsedNanos / 1_000_000_000.0;

        long tokensToAdd = (long) (elapsedSeconds * refillTokensPerSecond);

        if (tokensToAdd <= 0) {
            return;
        }

        currentTokens = Math.min(capacity, currentTokens + tokensToAdd);

        long nanosPerToken = 1_000_000_000L / refillTokensPerSecond;

        lastRefillTimeNanos += tokensToAdd * nanosPerToken;
    }
}

/**
 * TokenBucket based RateLimiter
 */
class TokenBucketRateLimiter implements RateLimiter {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    private final long capacity;
    private final long refillTokensPerSecond;

    public TokenBucketRateLimiter(long capacity, long refillTokensPerSecond) {
        this.capacity = capacity;
        this.refillTokensPerSecond = refillTokensPerSecond;
    }

    @Override
    public boolean allowRequest(String key) {

        TokenBucket bucket = buckets.computeIfAbsent(
                key,
                k -> new TokenBucket(capacity, refillTokensPerSecond)
        );

        return bucket.tryConsume();
    }
}

/**
 * Demo / Driver class
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        RateLimiter rateLimiter = new TokenBucketRateLimiter(5, 2);

        String user = "user-123";

        System.out.println("Burst test:");

        for (int i = 1; i <= 7; i++) {
            System.out.println("Request " + i + " allowed ? "
                    + rateLimiter.allowRequest(user));
        }

        Thread.sleep(1000);

        System.out.println("\nAfter 1 second:");

        for (int i = 8; i <= 11; i++) {
            System.out.println("Request " + i + " allowed ? "
                    + rateLimiter.allowRequest(user));
        }
    }
}
