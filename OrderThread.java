import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.*;

public class OrderThread implements Runnable{

    private final ExecutorService service;
    private final Scanner buff;
    private final String filePath;

    private FileWriter orderWriter;
    private FileWriter orderProductWriter;

    public OrderThread(ExecutorService service, Scanner buff, String filePath, FileWriter orderWriter, FileWriter orderProductWriter) {
        this.service = service;
        this.buff = buff;
        this.filePath = filePath;
        this.orderWriter = orderWriter;
        this.orderProductWriter = orderProductWriter;
    }

    @Override
    public void run() {

        String line = null, commandId = null, numberOfProducts = null;
        Integer counterShipped = 0;

        //Every order thread will handle one commandId and when is shipping that order,
        //will handle the next one until there is no command to ship.
        while(true) {

            ArrayList<ProductThread> products = new ArrayList<ProductThread>();
            ArrayList<Future<?>> futures = new ArrayList<Future<?>>();

            synchronized (buff) {
                try {
                    line = buff.nextLine();
                    commandId = line.split(",")[0];
                    numberOfProducts = line.split(",")[1];

                } catch (NoSuchElementException e) {
                    break;
                }
            }

            //If there is a command with 0 products then move on
            if (Integer.parseInt(numberOfProducts) == 0) {
                return;
            }

            //Creating numberOfProducts Product Threads for every product
            for (int i = 0; i < Integer.parseInt(numberOfProducts); i++) {
                ProductThread var = new ProductThread(filePath, commandId, orderProductWriter, i + 1, 0);
                products.add(var);
                Future<?> f = service.submit(var);
                futures.add(f);
            }

            //Waiting for all the product threads to have finished before
            //declaring if a command should be shipped or not
            for (Future<?> f: futures) {
                try {
                    f.get();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }

            //At this point all products have been shipped and if the number of shipped products
            //corresponds with the numberOfProducts then mark the order as shipped
            int sumOfShipped = 0;
            for (ProductThread p: products) {
                sumOfShipped += p.getCounterShipped();
            }

            if (sumOfShipped == Integer.parseInt(numberOfProducts)) {
                synchronized (orderWriter) {
                    try {
                        orderWriter.write(line + ",shipped\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
