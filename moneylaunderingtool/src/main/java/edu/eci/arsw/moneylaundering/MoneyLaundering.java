package edu.eci.arsw.moneylaundering;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MoneyLaundering {

    private TransactionAnalyzer transactionAnalyzer;
    private TransactionReader transactionReader;
    private int amountOfFilesTotal;
    private AtomicInteger amountOfFilesProcessed;
    private static int cantidadHilos = 5;
    private static boolean pausado, ejecutando = false;
    private final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public MoneyLaundering() {
        transactionAnalyzer = new TransactionAnalyzer();
        transactionReader = new TransactionReader();
        amountOfFilesProcessed = new AtomicInteger();
    }

    public void processTransactionData(int start, int finish) {

        boolean ejecutando = true;
        do {
            ejecutando = false;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();

            }

            amountOfFilesProcessed.set(0);
            List<File> transactionFiles = getTransactionFileList();
            amountOfFilesTotal = transactionFiles.size();
            for (int i = start; i < finish; i++) {
                File transactionFile = transactionFiles.get(i);
                List<Transaction> transactions = transactionReader.readTransactionsFromFile(transactionFile);
                for (Transaction transaction : transactions) {
                    transactionAnalyzer.addTransaction(transaction);
                }
                amountOfFilesProcessed.incrementAndGet();
            }
            try {
                bufferedReader.readLine();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            control();
        } while (ejecutando);
    }

    public boolean corriendo() {
        return ejecutando;
    }
    
    public void pausa(){
        pausado = true;
    }
    
    public void control(){
        pausado = false;
        synchronized(this){
            this.notify();
        }
    }


    public List<String> getOffendingAccounts() {
        return transactionAnalyzer.listOffendingAccounts();
    }

    private List<File> getTransactionFileList() {
        List<File> csvFiles = new ArrayList<>();
        try (Stream<Path> csvFilePaths = Files.walk(Paths.get("src/main/resources/")).filter(path -> path.getFileName().toString().endsWith(".csv"))) {
            csvFiles = csvFilePaths.map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvFiles;
    }

    public static void main(String[] args) {
        System.out.println(getBanner());
        System.out.println(getHelp());
        MoneyLaundering moneyLaundering = new MoneyLaundering();
        int cantidadArchivos = moneyLaundering.getTransactionFileList().size();
        int cantidadArchivosPorHilo = cantidadArchivos / cantidadHilos;
        int i = 0;
        int j = cantidadArchivosPorHilo;
        for (int x = 0; x < cantidadHilos; x++) {
          
            int in = i;
            int cont = j;
            if (x == cantidadHilos - 1) {
                Thread processingThread = new Thread(() -> moneyLaundering.processTransactionData(in, cantidadArchivos));
                processingThread.start();
            }
            Thread processingThread = new Thread(() -> moneyLaundering.processTransactionData(in, cont));
            processingThread.start();
            i = j;
            j += cantidadArchivosPorHilo;
            
            if (pausado){
                try{
                    synchronized(processingThread){
                        processingThread.wait();
                    }
                }catch(InterruptedException ex){
                    ex.printStackTrace();
                }
            }
        }

        while (true) {
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            if (line.contains("exit")) {
                System.exit(0);
            }

            String message = "Processed %d out of %d files.\nFound %d suspect accounts:\n%s";
            List<String> offendingAccounts = moneyLaundering.getOffendingAccounts();
            String suspectAccounts = offendingAccounts.stream().reduce("", (s1, s2) -> s1 + "\n" + s2);
            message = String.format(message, moneyLaundering.amountOfFilesProcessed.get(), moneyLaundering.amountOfFilesTotal, offendingAccounts.size(), suspectAccounts);
            System.out.println(message);
        }
    }
    
    
        
   

    private static String getBanner() {
        String banner = "\n";
        try {
            banner = String.join("\n", Files.readAllLines(Paths.get("src/main/resources/banner.ascii")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return banner;
    }

    private static String getHelp() {
        String help = "Type 'exit' to exit the program. Press 'Enter' to get a status update\n";
        return help;
    }
}
