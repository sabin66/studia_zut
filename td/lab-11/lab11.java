// sd55617
// pomoc przy funkcji HammingCodeInternal - https://www.youtube.com/watch?v=mtILckTBtI8&ab_channel=Dr.Dhiman%28Learntheartofproblemsolving%29
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        System.out.println("Sygnal po zakodoowaniu : ");
        for (int i = 0; i < coded.size(); i += 15) {
            for (int j = 0; j < 15; j++) {
                System.out.print(coded.get(i + j));
            }
            System.out.println();
        }

        List<Integer> decoded = HammingDecode(coded);
        System.out.println("Sygnal po dekodowaniu : ");
        for (int i = 0; i < decoded.size(); i += 11) {
            for (int j = 0; j < 11; j++) {
                System.out.print(decoded.get(i + j));
            }
            System.out.println();
        }

        List<Integer> codedError = makeError(coded);
        System.out.println("Sygnal po zakodowaniu z bledem w kazdym slowie : ");
        for (int i = 0; i < codedError.size(); i += 15) {
            for (int j = 0; j < 15; j++) {
                System.out.print(codedError.get(i + j));
            }
            System.out.println();
        }

        List<Integer> decodedError = HammingDecode(codedError);
        System.out.println("Sygnal po dekodowaniu i korekcji bledu : ");
        for (int i = 0; i < decodedError.size(); i += 11) {
            for (int j = 0; j < 11; j++) {
                System.out.print(decodedError.get(i + j));
            }
            System.out.println();
        }

        System.out.println("Czy macierze po dekodowaniu sa takie same ? 0 - nie, 1 - tak");
        System.out.println(compareMatrices(decoded,decodedError));


    }

    public static List<Integer> HammingCode(int[] bits) {
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
    public static List<Integer> HammingDecode(List<Integer> coded){
        List<Integer> decoded = new ArrayList<>();
        for (int i = 0; i < coded.size(); i+=15) {
            int[] newBlock = new int[15];
            for (int j = 0; j < 15; j++) {
                newBlock[j] = coded.get(i + j);
            }
            int[] corrected = HammingDecodeInternal(newBlock);
            int[] dataPosition = {2,4,5,6,8,9,10,11,12,13,14};
            for(int pos :dataPosition){
                decoded.add(corrected[pos]);
            }
        }
        return decoded;
    }
    public static int[] HammingDecodeInternal(int[] coded){
        int s1 = coded[0]^coded[2]^coded[4]^coded[6]^coded[8]^coded[10]^coded[12]^coded[14];
        int s2 = coded[1]^coded[2]^coded[5]^coded[6]^coded[9]^coded[10]^coded[13]^coded[14];
        int s4 = coded[3]^coded[4]^coded[5]^coded[6]^coded[11]^coded[12]^coded[13]^coded[14];
        int s8 = coded[7]^coded[8]^coded[9]^coded[10]^coded[11]^coded[12]^coded[13]^coded[14];
        int syndrome = s1 + s2*2 + s4*4 + s8*8;
        if (syndrome > 0) {
            System.out.println(syndrome);
            coded[syndrome-1] ^= 1;
        }
        return coded;
    }
    public static List<Integer> makeError(List<Integer> coded){
        List<Integer> tmp = new ArrayList<>(coded);
        Random rnd = new Random();
        for (int i = 0; i < tmp.size(); i+=15) {
            int bit = i + rnd.nextInt(15);
            tmp.set(bit,1-tmp.get(bit));
        }
        return tmp;
    }
    public static int compareMatrices(List<Integer> coded1, List<Integer> coded2){
        int result = 0;
        for (int i = 0; i < coded1.size(); i++) {
            for(int j = 0; j < coded2.size(); j++){
                if(coded1.get(i).equals(coded2.get(j))){
                    result++;
                }
            }
        }
        if(result != 0){
            return 1;
        }else{
            return 0;
        }
    }
}
