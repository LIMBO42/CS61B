package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

public class RandomTest {
    @Test
    public void randomTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                System.out.println("size: " + size);
            }
            if(L.size()>0){
                if(operationNumber == 2){
                    L.getLast();
                }else if(operationNumber == 3){
                    L.removeLast();
                }
            }
        }
    }
}
