package OTF_benchmark.PT;

import java.util.BitSet;

public class ValmariDet {

    RefinablePartition B;
    RefinablePartition C;

    int nn;
    int mm;
    int ff;
    int rr;
    int q0;

    int[] T;
    int[] L;
    int[] H;

    int[] A;
    int[] F;

    int[] M, W;
    int w = 0;

    BitSet accepting;

    public ValmariDet(int q0, int nn, BitSet accepting, int[] t, int[] l, int[] h) {
        T = t;
        L = l;
        H = h;

        this.nn = nn;
        this.mm = t.length;
        this.ff = accepting.cardinality();
        this.q0 = q0;

        A = new int[mm];
        F = new int[nn + 1];

        B = new RefinablePartition();
        C = new RefinablePartition();
        B.init(nn);

        this.accepting = accepting;
    }

    public void computeCoarsestStablePartition() {
        /* Remove states that cannot be reached from the initial state, and from which final states cannot be reached */
        reach(q0);
        rem_unreachable(T, H);

        for (int i = accepting.nextSetBit(0); i >= 0; i = accepting.nextSetBit(i + 1)) {
            if (B.L[i] < B.P[0]) {
                reach(i);
            }
        }

        ff = rr;
//        rem_unreachable(H, T);

        /* Make initial partition */
        W = new int[mm + 1];
        M = new int[mm + 1];
        M[0] = ff;
        if (ff > 0) {
            W[w++] = 0;
            B.split();
        }
        /* Make transition partition */
        C.init(mm);
        if (mm > 0) {
//            std::sort (C.E, C.E + mm, cmp );
//            heapsort(C.E, L);
            C.z = M[0] = 0;
            int a = L[C.E[0]];
            for (int i = 0; i < mm; ++i) {
                int t = C.E[i];
                if (L[t] != a) {
                    a = L[t];
                    C.P[C.z++] = i;
                    C.F[C.z] = i;
                    M[C.z] = 0;
                }
                C.S[t] = C.z;
                C.L[t] = i;
            }
            C.P[C.z++] = mm;
        }

        /* Split blocks and cords */
        make_adjacent(H);
        int b = 1, c = 0, i, j;
        while (c < C.z) {
            for (i = C.F[c]; i < C.P[c]; ++i) {
                B.mark(T[C.E[i]]);
            }
            B.split();
            ++c;
            while (b < B.z) {
                for (i = B.F[b]; i < B.P[b]; ++i) {
                    for (j = F[B.E[i]]; j < F[B.E[i] + 1]; ++j) {
                        C.mark(A[j]);
                    }
                }
                C.split();
                ++b;
            }
        }
    }

    void reach(int q) {
        int i = B.L[q];
        if (i >= rr) {
            B.E[i] = B.E[rr];
            B.L[B.E[i]] = i;
            B.E[rr] = q;
            B.L[q] = rr++;
        }
    }

    void make_adjacent(int[] K) {
        int q, t;
        for (q = 0; q <= nn; ++q) {
            F[q] = 0;
        }
        for (t = 0; t < mm; ++t) {
            ++F[K[t]];
        }
        for (q = 0; q < nn; ++q) {
            F[q + 1] += F[q];
        }
        for (t = mm - 1; t >= 0; t--) {
            A[--F[K[t]]] = t;
        }
    }

    void rem_unreachable(int[] T, int[] H) {
        make_adjacent(T);
        int i, j;
        for (i = 0; i < rr; ++i) {
            for (j = F[B.E[i]]; j < F[B.E[i] + 1]; ++j) {
                reach(H[A[j]]);
            }
        }
        j = 0;
        for (int t = 0; t < mm; ++t) {
            if (B.L[T[t]] < rr) {
                H[j] = H[t];
                L[j] = L[t];
                T[j] = T[t];
                ++j;
            }
        }
        mm = j;
        B.P[0] = rr;
        rr = 0;
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

    public class RefinablePartition {

        int z;
        int[] E, L, S, F, P;

        void init(int n) {
            z = Math.min(1, n);
            E = new int[n];
            L = new int[n];
            S = new int[n];
            F = new int[n];
            P = new int[n];

            for (int i = 0; i < n; i++) {
                E[i] = i;
                L[i] = i;
            }

            if (z > 0) {
                P[0] = n;
            }
        }

        void mark(int e) {
            int s = S[e];
            int i = L[e];
            int j = F[s] + M[s];
            E[i] = E[j];
            L[E[i]] = i;
            E[j] = e;
            L[e] = j;

            if (M[s]++ == 0) {
                W[w++] = s;
            }
        }

        void split() {
            while (w > 0) {
                int s = W[--w];
                int j = F[s] + M[s];

                if (j == P[s]) {
                    M[s] = 0;
                    continue;
                }
                if (M[s] <= P[s] - j) {
                    F[z] = F[s];
                    P[z] = j;
                    F[s] = j;
                } else {
                    P[z] = P[s];
                    F[z] = j;
                    P[s] = j;
                }

                for (int i = F[z]; i < P[z]; i++) {
                    S[E[i]] = z;
                }

                M[s] = 0;
                M[z++] = 0;
            }
        }

    }

}
