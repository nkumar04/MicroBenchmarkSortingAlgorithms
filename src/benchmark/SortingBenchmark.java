package benchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
@State(Scope.Thread)
public class SortingBenchmark {
    final static int SIZE = 5*10*1024*1024; //array size = 50MB
    final static int MAX_GAP_BETWEEN_TIMESTAMPS_MILLIS = 100;
    static ArrayList<long[]> listofTimestampArrays = new ArrayList<>();

    //Percentage of unsorted data in nearly sorted array
    @Param({"0.01f", "0.02f", "0.03f"})
    static float shuffle_factor;




    // Merge Sort implementation
    public static void mergeSort(long[] arr) {
        if (arr.length <= 1) {
            return;
        }

        int mid = arr.length / 2;
        long[] left = Arrays.copyOfRange(arr, 0, mid);
        long[] right = Arrays.copyOfRange(arr, mid, arr.length);

        mergeSort(left);
        mergeSort(right);

        merge(arr, left, right);
    }
    private static void merge(long[] arr, long[] left, long[] right) {
        int leftIdx = 0;
        int rightIdx = 0;
        int arrIdx = 0;

        while (leftIdx < left.length && rightIdx < right.length) {
            if (left[leftIdx] < right[rightIdx]) {
                arr[arrIdx++] = left[leftIdx++];
            } else {
                arr[arrIdx++] = right[rightIdx++];
            }
        }

        while (leftIdx < left.length) {
            arr[arrIdx++] = left[leftIdx++];
        }

        while (rightIdx < right.length) {
            arr[arrIdx++] = right[rightIdx++];
        }
    }

    // Heap Sort implementation
    public static void heapSort(long[] arr) {
        int n = arr.length;
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }
        for (int i = n - 1; i > 0; i--) {
            long temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;
            heapify(arr, i, 0);
        }
    }
    private static void heapify(long[] arr, int n, int i) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < n && arr[left] > arr[largest]) {
            largest = left;
        }

        if (right < n && arr[right] > arr[largest]) {
            largest = right;
        }

        if (largest != i) {
            long temp = arr[i];
            arr[i] = arr[largest];
            arr[largest] = temp;
            heapify(arr, n, largest);
        }
    }

    // Insertion Sort implementation
    public static long[] insertionSort(long[] arr) {
        int n = arr.length;
        for (int i = 1; i < n; i++) {
            long key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
        return arr;
    }

    // Timsort using Arrays.sort() (Java's default sorting algorithm)
    public static long[] timsort(long[] arr) {
        Arrays.sort(arr);
        return arr;
    }

    public static void generateListOfNearlySortedArray(){
        int[] s_range = {50, 100, 200, 300, 400, 500, 600};
        long curr_time = System.currentTimeMillis();
        Random random = new Random();
        int size = SIZE;
        for (long count = 0; count < 10; count++){
            long[] arr = new long[size];
            long j = curr_time;
            for (int i = 0; i < size; i++) {
                arr[i] = j;
                j += random.nextInt(MAX_GAP_BETWEEN_TIMESTAMPS_MILLIS);
            }
            // Shuffle the array slightly
            int numSwaps = (int) (size * shuffle_factor);
            for (int i = 0; i < numSwaps; i++) {
                int s = s_range[random.nextInt(s_range.length)];
                int idx1 = ThreadLocalRandom.current().nextInt(s, size);
                int idx2 = idx1 - random.nextInt(s);
                long temp = arr[idx1];
                arr[idx1] = arr[idx2];
                arr[idx2] = temp;
            }
            listofTimestampArrays.add(arr);
            curr_time += (365*24*3600*1000); //msec in 1 year
        }
    }


    @Setup
    public void setup() {
        generateListOfNearlySortedArray();
        assert listofTimestampArrays.size() > 0;
        System.out.println("Setup complete. benchmarking list of size: " + listofTimestampArrays.size() + " each of size: " + listofTimestampArrays.get(0).length);
    }

    @TearDown
    public void tearDown() {
        listofTimestampArrays.clear();
        System.out.println("Teardown complete.  benchmarking list of size: " + listofTimestampArrays.size());
    }
    // Benchmark methods for each sorting algorithm

    @Benchmark
    @Fork(2)
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    public void benchmarkTimsort(Blackhole blackhole) {
        for (int i = 0; i < listofTimestampArrays.size(); i++) {
            long[] arr = listofTimestampArrays.get(i);
            long[] sorted_arr = timsort(arr);
            blackhole.consume(sorted_arr);
        }
    }

    @Benchmark
    @Fork(2)
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    public void benchmarkInsertionSort(Blackhole blackhole) {
        for (int i = 0; i < listofTimestampArrays.size(); i++) {
            long[] arr = listofTimestampArrays.get(i);
            long[] sorted_arr = insertionSort(arr);
            blackhole.consume(sorted_arr);
        }
    }

/*
    @Benchmark
    @Fork(2)
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    public void benchmarkHeapSort() {
        for (int i = 0; i < listofTimestampArrays.size(); i++) {
            long[] arr = listofTimestampArrays.get(i);
            heapSort(arr);
        }
    }

    @Benchmark
    @Fork(2)
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    public void benchmarkMergeSort() {
        for (int i = 0; i < listofTimestampArrays.size(); i++) {
            long[] arr = listofTimestampArrays.get(i);
            mergeSort(arr);
        }
    }
*/
    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
