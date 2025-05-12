package OTF_benchmark.PT;

public class ValmariLehtinen {

    final RefinablePartition brp;
    final RefinablePartition trp;

    final int n, m;

    final int[] tail;
    final int[] label;
    final int[] head;

    final int[] inTransitionsTrans;
    final int[] inTransitionsStates;

    final int[] unreadySpls;
    int unreadySplsPtr;

    final int[] touchedBlocks;
    int touchedBlocksPtr;
    final int[] touchedSpls;
    int touchedSplsPtr;

    int[] initialBlocks;
    int maxBlock;

    public ValmariLehtinen(int[] blocks, int maxBlock, int[] tail, int[] label, int[] head) {

        this.n = blocks.length;
        this.m = tail.length;

        this.brp = new RefinablePartition(n);
        this.trp = new RefinablePartition(m);

        this.initialBlocks = blocks;
        this.maxBlock = maxBlock;
        this.tail = tail;
        this.label = label;
        this.head = head;

        this.inTransitionsTrans = new int[m];
        this.inTransitionsStates = new int[n + 1];

        this.unreadySpls = new int[tail.length];
        this.touchedBlocks = new int[blocks.length];
        this.touchedSpls = new int[tail.length];

    }

    public void computeCoarsestStablePartition() {
        if (n > 0 && m > 0) {
            initializeClusters();
            initializeInTransitions();
            initializeBlocks();

            mainLoop();
        }
    }

    void initializeBlocks() {
        // sidx is initialized in the constructor

        for (int i = 0; i < n; i++) {
            brp.elems[i] = i;
            brp.loc[brp.elems[i]] = i;
        }

        brp.end[0] = initialBlocks.length;
        int block = 0;

        for (int i = 1; i <= maxBlock; i++) {
            for (int j = 0; j < initialBlocks.length; j++) {
                if (initialBlocks[j] == i) {
                    brp.mark(j);
                }
            }
            splitBlocks(block++);
        }
    }

    void initializeClusters() {

        int prevLabel2 = label[0];

        for (int t = 0; t < m; t++) {
            trp.elems[t] = t;
            trp.loc[trp.elems[t]] = t;

            // setup boundaries
            if (label[t] != prevLabel2) {
                trp.end[trp.sets] = t;
                trp.sets++;
                trp.first[trp.sets] = t;
                trp.mid[trp.sets] = t;
                prevLabel2 = label[t];
            }
            trp.sidx[t] = trp.sets;
        }

        trp.end[trp.sets] = m;

        for (int i = 0; i <= trp.sets; i++) {
            unreadySpls[unreadySplsPtr++] = i;
        }
    }

    void initializeInTransitions() {
        // essentially a counting sort on head

        for (int i = 0; i < m; i++) {
            inTransitionsStates[head[i]]++;
        }

        // prefix sum
        for (int i = 1; i <= n; i++) {
            inTransitionsStates[i] += inTransitionsStates[i - 1];
        }

        for (int i = 0; i < m; i++) {
            inTransitionsTrans[--inTransitionsStates[head[i]]] = i;
        }
    }

    void mainLoop() {
        while (unreadySplsPtr > 0) {
            int p = unreadySpls[--unreadySplsPtr];
            int t = trp.first(p);
            while (t >= 0) {
                int q = tail[t];
                int bPrime = brp.sidx[q];
                if (brp.noMarks(bPrime)) {
                    touchedBlocks[touchedBlocksPtr++] = bPrime;
                }
                brp.mark(q);
                t = trp.next(t);
            }
            while (touchedBlocksPtr > 0) {
                splitBlocks(touchedBlocks[--touchedBlocksPtr]);
            }
        }
    }

    void splitBlocks(int b) {

        int bPrime = brp.split(b);

        if (bPrime >= 0) {
            if (brp.size(b) < brp.size(bPrime)) {
                bPrime = b;
            }
            int q = brp.first(bPrime);
            while (q >= 0) {
                for (int j = inTransitionsStates[q]; j < inTransitionsStates[q + 1]; j++) {
                    int t = inTransitionsTrans[j];
                    int p = trp.sidx[t];
                    if (trp.noMarks(p)) {
                        touchedSpls[touchedSplsPtr++] = p;
                    }
                    trp.mark(t);
                }
                q = brp.next(q);
            }
            while (touchedSplsPtr > 0) {
                int p = touchedSpls[--touchedSplsPtr];
                int pPrime = trp.split(p);
                if (pPrime >= 0) {
                    unreadySpls[unreadySplsPtr++] = pPrime;
                }
            }
        }
    }

    private static void heapsort(int[] arr, int[] keys) {

        int start = arr.length / 2;
        int end = arr.length;

        while (end > 1) {
            if (start > 0) {
                start--;
            } else {
                end--;
                swap(arr, end, 0);
            }

            int root = start;
            while (2 * root + 1 < end) {
                int child = 2 * root + 1;
                if (child + 1 < end && keys[arr[child]] < keys[arr[child + 1]]) {
                    child++;
                }

                if (keys[arr[root]] < keys[arr[child]]) {
                    swap(arr, root, child);
                    root = child;
                } else {
                    break;
                }
            }
        }
    }

    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    static class RefinablePartition {

        int sets;
        final int[] elems;
        final int[] first;
        final int[] mid;
        final int[] end;
        final int[] loc;
        final int[] sidx;

        RefinablePartition(int size) {
            this(size, new int[size]);
        }

        RefinablePartition(int size, int[] sidx) {
            this.sets = 0;
            this.elems = new int[size];
            this.first = new int[size];
            this.mid = new int[size];
            this.end = new int[size];
            this.loc = new int[size];
            this.sidx = sidx;
        }

        void mark(int e) {
            final int s = sidx[e];
            final int l = loc[e];
            final int m = mid[s];

            if (l >= m) {
                elems[l] = elems[m];
                loc[elems[l]] = l;
                elems[m] = e;
                loc[e] = m;
                mid[s] = m + 1;
            }
        }

        int split(int s) {
            if (mid[s] == end[s]) {
                mid[s] = first[s];
            }

            if (mid[s] == first[s]) {
                return -1;
            }

            sets++;
            first[sets] = first[s];
            mid[sets] = first[s];
            end[sets] = mid[s];
            first[s] = mid[s];
            for (int i = first[sets]; i < end[sets]; i++) {
                sidx[elems[i]] = sets;
            }

            return sets;
        }

        int size(int e) {
            return end[e] - first[e];
        }

        boolean noMarks(int s) {
            return mid[s] == first[s];
        }

        int first(int s) {
            return elems[first[s]];
        }

        int next(int e) {
            if (loc[e] + 1 >= end[sidx[e]]) {
                return -1;
            } else {
                return elems[loc[e] + 1];
            }
        }
    }

}
