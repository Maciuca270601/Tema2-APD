import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.*;

public class Tema2 {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

        //Extracting the number of threads and the filepath
        Integer numberOfThreads = Integer.parseInt(args[1]);
        String filePath = args[0];
        ArrayList<Thread> orders = new ArrayList<Thread>();

        //Creating the buffer reader for the order threads
        Scanner scanner = new Scanner(new File(filePath + "/orders.txt"));

        //Creating the output files
        FileWriter orderWriter = null;
        FileWriter orderProductWriter = null;
        try {
            orderWriter = new FileWriter("orders_out.txt");
            orderProductWriter = new FileWriter("order_products_out.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Creating an executor that only has numberOfThreads workers
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);

        //Creating numberOfThreads order threads that are level 1 threads
        for (int i = 0; i < numberOfThreads; i++) {
            Thread order = new Thread(new OrderThread(service, scanner, filePath, orderWriter, orderProductWriter));
            order.start();
            orders.add(order);
        }

        //Joining the order Threads
        for(Thread t: orders) {
            t.join();
        }

        //Shut down the executor after all order threads are done since it indirectly means
        //that products threads are also done
        service.shutdown();

        //Closing the output files
        orderProductWriter.close();
        orderWriter.close();

    }
}
