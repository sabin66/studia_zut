// sd55617
// pomoc przy funkcji tworzenia macierzy, HammingCodeInternal i HammingDecodeInternal - ChatGPT
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    private static final int n = 15;
    private static final int k = 11;
    private static final int r = n - k;

    private static final int[] PARITY_POS_1 = {1, 2, 4, 8};

    private static final int[] DATA_POS_1 = new int[k];
    static {
        int idx = 0;
        outer:
        for (int i = 1; i <= n; i++) {
            for (int p : PARITY_POS_1) {
                if (p == i) continue outer;
            }
            DATA_POS_1[idx++] = i;
        }
    }

    private static final int[][] G = new int[k][n];
    static {
        for (int row = 0; row < k; row++) {
            int dataPos0 = DATA_POS_1[row] - 1;
            G[row][dataPos0] = 1;
            for (int i = 0; i < r; i++) {
                if (((DATA_POS_1[row] >> i) & 1) == 1) {
                    int parityCol0 = PARITY_POS_1[i] - 1;
                    G[row][parityCol0] = 1;
                }
            }
        }
    }

    private static final int[][] H = new int[r][n];
    static {
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < n; j++) {
                H[i][j] = ((j+1) >> i) & 1;
            }
        }
    }

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

    private static final int[][] P = new int[k][r];
    static {
        for(int j=0;j<k;j++){
            for(int i=0;i<r;i++){
                P[j][i] = ((j+1) >> i) & 1;
            }
        }
    }

    public static int[] HammingCodeInternal(int[] m) {
        int[] c = new int[n];
        for(int i=0;i<r;i++){
            int sum = 0;
            for(int j=0;j<k;j++){
                sum += m[j]*P[j][i];
            }
            c[i] = sum & 1;
        }
        System.arraycopy(m, 0, c, r, k);
        return c;
    }

    public static List<Integer> HammingDecode(List<Integer> coded) {
        List<Integer> msg = new ArrayList<>();
        for(int i=0; i<coded.size(); i+=n) {
            int[] block = new int[n];
            for(int j=0;j<n;j++) block[j] = coded.get(i+j);
            int[] corr = HammingDecodeInternal(block);
            for(int j=0;j<k;j++){
                msg.add(corr[r + j]);
            }
        }
        return msg;
    }


    public static int[] HammingDecodeInternal(int[] c) {
        int syndrome = 0;
        for(int i=0;i<r;i++){
            int sum = c[i];
            for(int j=0;j<k;j++){
                sum += c[r+j]*P[j][i];
            }
            if((sum & 1) == 1) syndrome |= 1<<i;
        }
        if(syndrome>0 && syndrome<=n) {
            c[syndrome-1] ^= 1;
        }
        return c;
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
