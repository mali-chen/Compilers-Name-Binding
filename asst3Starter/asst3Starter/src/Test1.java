// The starter file should NOT be able to parse this
class Main extends Lib {
    int count;
    public void main() {
        int n = count+2;
        n++;
        while (true) {
            int count = n+4;
            foo(count);
        }
    }
    public void foo(int a) {
        int[][][] xxx = new int[34][][];
    }
}
class Foo extends Baz {
    public void print() {
        for (int i = 0; i < 100; i++) {
            printInt(i);
            printStr("\n");
        }
    }
    
}
class Baz extends Main {
    public int xyz(int a, String b) {
        boolean bb = a > count || b != null;
        return a+1;
    }
}
