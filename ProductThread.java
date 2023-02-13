import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class ProductThread implements Runnable{

    private final String filePath;
    private final String stringToFind;

    private Integer idProduct;
    private FileWriter orderProductWriter;

    private Integer counterShipped;

    public Integer getCounterShipped() {
        return counterShipped;
    }

    public ProductThread(String filePath, String stringToFind, FileWriter orderProductWriter, Integer idProduct, Integer counterShipped) {
        this.filePath = filePath;
        this.stringToFind = stringToFind;
        this.orderProductWriter = orderProductWriter;
        this.idProduct = idProduct;
        this.counterShipped = counterShipped;
    }


    @Override
    public void run() {
        Scanner scanner = null;
        String line = null, commandId = null, productId = null;

        //Creating order_products.txt file
        try {
            scanner = new Scanner(new File(filePath + "/order_products.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        //Searching for the specific order products in order to ship them if found
        while(scanner.hasNextLine()) {
            line = scanner.nextLine();
            commandId = line.split(",")[0];
            productId = line.split(",")[1];

            synchronized (ProductThread.class) {

                //Searching for the specific product and if found the idProduct
                //should be equal with 0
                if (commandId.equals(stringToFind)) {
                    idProduct--;

                    if (idProduct == 0) {
                        try {
                            orderProductWriter.write(line + ",shipped\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        counterShipped++;
                    }
                }
            }
        }
    }
}
