package com.zoxal.labs.iapd.power.math;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;

public class DischargeApproximator {
    public static void main(String[] args) {
        Deque<Pair> l = new ArrayDeque<>();
//        for (long i = 0; i < 2; i++) {
//            l.add(new Pair(i, i*8 - 3));
//        }
//        l.add(new Pair(86, 1511910133));
//        l.add(new Pair(85, 1511910206));
//        l.add(new Pair(84, 1511910272));
//        l.add(new Pair(83, 1511910345));
//        l.add(new Pair(82, 1511910412));

        l.add(new Pair(1511910133,86 ));
        l.add(new Pair(1511910206, 85));
        l.add(new Pair(1511910272, 84 ));
        l.add(new Pair(1511910345, 83 ));
        l.add(new Pair(1511910412, 82 ));


        // 90 - 1511909849
        // 89 - 1511909922

        // 86 - 1511910133
        // 85 - 1511910206
        // 84 - 1511910272
        // 83 - 1511910345
        // 82 - 1511910412
//        Instant.ofEpochSecond(approximate(l));
        System.out.println(DischargeApproximator.approximate(l) - 1511910133L);

        Duration dischargeTime = Duration.between(
                Instant.ofEpochSecond(DischargeApproximator.approximate(l)),
                Instant.ofEpochSecond(1511910133L)
        );
        System.out.println(
                String.format("%dD; %02d:%02d - %d", -(dischargeTime.toDays()), dischargeTime.toHours(), dischargeTime.toMinutes(), dischargeTime.getSeconds())
        );
    }

    public static long approximate(Deque<Pair> rawData) {
        double sumXY = 0;
        double sumX = 0;
        double sumY = 0;
        double sumX2 = 0;

        for (Pair e : rawData) {
            sumX += e.time;
            sumY += e.capacity;
            sumXY += e.time* e.capacity;
            sumX2 += e.time * e.time;
        }
        long n = rawData.size();

        double a = (n*sumXY - (sumX * sumY))/(n * sumX2 - sumX * sumX);
        double b = (sumY - a * sumX) / n;

        System.out.println(a);
        System.out.println(b);
        System.out.println(-b/a); // should be 0.375

        return (long)(-b/a);
    }

    public static class Pair {
        public long time;
        public long capacity;

        public Pair(long time, long capacity) {
            this.time = time;
            this.capacity = capacity;
        }

        public static Pair ofCapacity(long capacity) {
            return new Pair(Instant.now().getLong(INSTANT_SECONDS), capacity);
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "time=" + time +
                    ", capacity=" + capacity +
                    '}';
        }
    }
}
