// sd55617

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        int[] bits = {
                0,1,0,1, 1,0,1,0, 1,1,0,1, 0,1,1,0, 1,0,1,1
        };

        int[] bits_test = {1,1,0,1};

//        int[] codedTest = HammingCodeInternal(bits_test);
//        System.out.println("Bits after coding" +Arrays.toString(codedTest));
//
//        int[] decodedTest = HammingDecodeInternal(codedTest);
//        System.out.println("Bits after decoding" + Arrays.toString(decodedTest));

        List<Integer> codedFull = HammingCode(bits);
        System.out.println("Bits after coding : " + codedFull);
        System.out.println("Size of coded bits : " + codedFull.size());

        List<Integer> decodedFull = HammingDecode(codedFull);
        System.out.println("Bits after decoding : " + decodedFull);


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

    static int[] HammingDecodeInternal(int[] bits){
        int[] corrected = Arrays.copyOf(bits, bits.length);

        int x3 = corrected[2];
        int x5 = corrected[4];
        int x6 = corrected[5];
        int x7 = corrected[6];

        int x1 = x3 ^ x5 ^ x7;
        int x2 = x3 ^ x6 ^ x7;
        int x4 = x5 ^ x6 ^ x7;

        int x1_roof = corrected[0] ^ x1;
        int x2_roof = corrected[1] ^ x2;
        int x4_roof = corrected[3] ^ x4;

        int target = x1_roof * 1 + x2_roof * 2 + x4_roof * 4;
        System.out.println("Error position: " + target);
        if(target > 0 ) {
            corrected[target - 1] ^= 1;
        }
        return corrected;
    }


    static List<Integer> HammingDecode(List<Integer> encodedBits) {
        List<Integer> decoded = new ArrayList<>();

        for (int i = 0; i < encodedBits.size(); i += 7) {
            int[] block = new int[7];

            for (int j = 0; j < 7; j++) {
                if (i + j < encodedBits.size()) {
                    block[j] = encodedBits.get(i + j);
                } else {
                    block[j] = 0;
                }
            }

            int[] correctedBlock = HammingDecodeInternal(block);

            decoded.add(correctedBlock[2]);
            decoded.add(correctedBlock[4]);
            decoded.add(correctedBlock[5]);
            decoded.add(correctedBlock[6]);
        }

        return decoded;
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
