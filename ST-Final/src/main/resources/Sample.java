public class Sample {

    public int calculate(int a, int b) {
        int result = 0;


        if (a > 10) {
            result = a + b;
        } else {
            result = a - b;
        }


        for (int i = 0; i < 5; i++) {
            result++;
        }


        if (b == 0)
            return -1;

        return result;
    }
}
