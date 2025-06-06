// sd55617

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        int[] bits = {0, 1, 0, 1, 1, 0, 1, 0, 1, 1,
                      0, 1, 0, 1, 1, 0, 1, 0, 1, 1,
                      0, 1, 0, 1, 1, 0, 1, 0, 1, 1,
                      0, 1, 0, 1, 1, 0, 1, 0, 1, 1,};
        int[] bits_test = {1,1,0,1};
        int[] coded = HammingCodeInternal(bits_test);
        System.out.println(Arrays.toString(coded));

        List<Integer> codedFull = HammingCode(bits);
        System.out.println(codedFull);

    }
    static int[] HammingCodeInternal(int[] bits){
        int x3 = bits[0];
        int x5 = bits[1];
        int x6 = bits[2];
        int x7 = bits[3];

        int x1 = x3 ^ x5 ^ x7;
        int x2 = x3 ^ x6 ^ x7;
        int x4 = x5 ^ x6 ^ x7;

        return new int[]{x1, x2, x3, x4, x5, x6, x7};
    }

    static List<Integer> HammingCode(int[] bits) {
        List<Integer> encoded = new ArrayList<>();

        for (int i = 0; i < bits.length; i += 4) {
            int[] newBlock = new int[4];
            for (int j = 0; j < 4; j++) {
                if (i + j < bits.length) {
                    newBlock[j] = bits[i + j];
                } else {
                    newBlock[j] = 0;
                }
            }
            int[] encodedBlock = HammingCodeInternal(newBlock);
            for (int bit : encodedBlock) {
                encoded.add(bit);
            }
        }

        return encoded;
    }

}
