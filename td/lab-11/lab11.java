// sd55617
// pomoc przy funkcji HammingCodeInternal - https://www.youtube.com/watch?v=mtILckTBtI8&ab_channel=Dr.Dhiman%28Learntheartofproblemsolving%29
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        int[] bits = {
                1,0,1,0,0,1,1,0,1,1,1,
                0,1,1,1,1,0,0,1,0,1,0,
                1,1,0,0,0,1,1,1,0,1,0,
                0,0,1,1,0,1,1,0,1,0,0,
                1,1,0,1,1,1,0,0,1,0,1,
                0,1,0,0,1,1,1,1,0,1,0
        };
        List<Integer> coded = HammingCode(bits);
        for (int i = 0; i < coded.size(); i += 15) {
            for (int j = 0; j < 15; j++) {
                System.out.print(coded.get(i + j));
            }
            System.out.println();
        }


    }

    static List<Integer> HammingCode(int[] bits) {
        List<Integer> encoded = new ArrayList<>();

        for (int i = 0; i < bits.length; i += 11) {
            int[] newBlock = new int[11];
            for (int j = 0; j < 11; j++) {
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
    public static int[] HammingCodeInternal(int[] bits) {
        int[] codeword = new int[15];

        codeword[2] = bits[0];
        codeword[4] = bits[1];
        codeword[5] = bits[2];
        codeword[6] = bits[3];
        codeword[8] = bits[4];
        codeword[9] = bits[5];
        codeword[10] = bits[6];
        codeword[11] = bits[7];
        codeword[12] = bits[8];
        codeword[13] = bits[9];
        codeword[14] = bits[10];

        codeword[0] = codeword[2] ^ codeword[4] ^ codeword[6] ^ codeword[8] ^ codeword[10] ^ codeword[12] ^ codeword[14];
        codeword[1] = codeword[2] ^ codeword[5] ^ codeword[6] ^ codeword[9] ^ codeword[10] ^ codeword[13] ^ codeword[14];
        codeword[3] = codeword[4] ^ codeword[5] ^ codeword[6] ^ codeword[11] ^ codeword[12] ^ codeword[13] ^ codeword[14];
        codeword[7] = codeword[8] ^ codeword[9] ^ codeword[10] ^ codeword[11] ^ codeword[12] ^ codeword[13] ^ codeword[14];

        return codeword;
    }

}