import java.io.PrintStream;

/**
 * Created by SMPS on 2017/9/14.
 */
public class RingBuffer {

    final byte[] buf;
    volatile int head;
    volatile int tail;
    final int size;

    public RingBuffer(int size) {
        this.buf = new byte[size + 1];
        this.head = 0;
        this.tail = 0;
        this.size = size;
    }

    public boolean write(byte[] src) {
        int remain = head - tail - 1;
        if(remain < 0) {
            remain = remain + size + 1;
        }
        if(remain < src.length) {
            return false;
        }
        else {
            if (size - tail >= src.length) {
                System.arraycopy(src, 0, buf, tail + 1, src.length);
                tail = tail + src.length;
            } else {
                System.arraycopy(src, 0, buf, tail + 1, size - tail);
                System.arraycopy(src, size - tail, buf, 0, src.length - size + tail);
                tail = tail + src.length - size - 1;
            }
            return true;
        }

    }

    public boolean read(byte[] dst) {
        int occupy = tail - head;
        if(occupy < 0) {
            occupy = occupy + size + 1;
        }
        if(occupy < dst.length) {
            return false;
        }
        else {
            if (size - head >= dst.length) {
                System.arraycopy(buf, head + 1, dst, 0, dst.length);
                head = head + dst.length;
            } else {
                System.arraycopy(buf, head + 1, dst, 0, size - head);
                System.arraycopy(buf, 0, dst, size - head, dst.length - size + head);
                head = head + dst.length - buf.length;
            }
            return true;
        }
    }

    public void print(PrintStream ps) {
        if(tail < head) {
            for(int i=head+1;i<buf.length;++i) {
                ps.print(buf[i] + " ");
            }
            for(int i=0;i<=tail;++i) {
                ps.print(buf[i] + " ");
            }
        }
        else {
            for(int i=head+1;i<=tail;++i) {
                ps.print(buf[i] + " ");
            }
        }
        ps.println("");
    }

    public static void main(String[] args) throws InterruptedException {

        int m = 1000;
        int n = 13;
        byte[] buf = new byte[n];

        RingBuffer r = new RingBuffer(100);
        RingBuffer r2 = new RingBuffer(100);
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int j=0;j<m;++j)
                    for(int i=0;i<m;++i) {
                        // r.write(buf);
                        r.write(buf);
                        while(!r2.read(buf));
                    }
                System.out.println("t1 end");
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int j=0;j<m;++j)
                    for(int i=0;i<m;++i) {
                        // assert r2.write(buf);
                        r2.write(buf);
                        while(!r.read(buf));
                    }
                System.out.println("t2 end");
            }
        });
        long tot = - System.currentTimeMillis();
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        tot = tot + System.currentTimeMillis();
        System.out.println(tot);

    }

}
