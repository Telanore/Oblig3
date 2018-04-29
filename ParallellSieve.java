import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

class ParallellSieve{

  private static long global;
  private static long[] factors;
  private final int n, k;
  private int currentP, counter;
  private ArrayList<Integer> primes;
  private byte[] numbers;
  private long[] primesA;

  private Thread t;
  private Thread[] threads;
  private PrimeWorker pw;

  public ParallellSieve(int n, int k){
    this.n = n;
    this.k = k;

  }

  /*
  First half copied from SieveDemo program. Adds 2 and 3 to the list of primes,
  then flips the values for products of 3 (see flip method description), and
  finds the next prime to work with by adding 2 to the current one to skip even
  numbers.

  When all primes up to the square root of n are found, adds 2 to the current
  prime again, and adds the remaining primes up to n.
  */
  public void parSieve(){
    primes = new ArrayList<Integer>();
    numbers = new byte[n];
    threads = new Thread[k];

    primes.add(2);
    primes.add(3);

    double seqPart = Math.sqrt(Math.sqrt(n));

    currentP = 3;

    for(int i = 0; i < seqPart; i++){
      flip(currentP);

      currentP = findNextPrime(currentP+2);

      if(currentP == 0){
        System.out.println("Something went wrong with finding the next prime!");
        break;
      }
      primes.add(currentP);
    }
    seqPart++;

    addRemainingPrimes(primes.get(primes.size()-1) + 2, (int)Math.sqrt(n));
    makeThreads((int)seqPart);

    try{
      for(Thread s : threads){
        s.start();
        s.join();
      }
    }catch(Exception e){
      e.printStackTrace();
    }

    counter = 0;
    for(int i = 0; i < n; i+= 2){
      if(isPrime(i)){
        counter++;
      }
    }

    primesA = new long[counter];
    int j = 1;
    primesA[0] = 2;
    for(int i = 3; i < n; i+= 2){
      if(isPrime(i)){
        primesA[j] = i;
        j++;
      }
    }
  }

  /*
  Creates and initialises threads. First thread starts at the square root of the
  square root of n (where the sequential part left off)
  */
  public void makeThreads(int j){

    int start = j, end, range;
    range = (primes.size() - j) / k;

    end = start + range;

    for(int i = 0; i < k-1; i++){

      pw = new PrimeWorker(start, end);
      t = new Thread(pw);

      threads[i] = t;

      start = end + 1;
      end = start + range;

    }
    pw = new PrimeWorker(start, primes.size()-1);
    t = new Thread(pw);
    threads[k-1] = t;

  }


  /*From SieveDemo.
  (Combined the traverse and flip method from SieveDemo into one)
  */
  public void flip(int p){
    int byteCell;
    int bit;

    for(int i = p*p; i < n; i += p*2){
      byteCell = i/16;
      bit = (i/2) % 8;
      numbers[byteCell] |= (1 << bit);
    }
  }

  /*
  From SieveDemo.
  */
  public int findNextPrime(int start){
    for(int i = start; i < n; i += 2){
      if(isPrime(i)){
        return i;
      }
    }
    return 0;
  }

  /*
  From SieveDemo.
  */
  public boolean isPrime(int i) {
      int byteCell    = i / 16;
      int bit         = (i/2) % 8;
      return (numbers[byteCell] & (1 << bit)) == 0;

  }


  /*
  Starts iterating from the index of the last prime found + 2, searches the
  byte array for all remaining indexes with bits set to 0, and adds the index
  to the list of primes.
  */
  public void addRemainingPrimes(int start, int end){

    for(int i = start; i < end; i+= 2){
      if(isPrime(i)){
        primes.add(i);
      }
    }
  }

  public long[] getPrimes(){
    return primesA;
  }



  private class PrimeWorker implements Runnable{
    private int start, end;

    public PrimeWorker(int start, int end){
      this.start = start;
      this.end = end;
    }

    public void run(){
      for(int i = start; i <= end; i++){
        flip(primes.get(i));
      }
    }
  }


  /*
  Factorises the numbers between n*n-100 to n*n and adds the prime factors to a
  list, which is then sent to writeToFile method, where they are written
  to a .txt file.
  */
  public void factorise(){
    FactorWorker fw;
    ReentrantLock l = new ReentrantLock();

    try{
      File f = new File("./par_factors.txt");
      FileWriter writer = new FileWriter(f);
      long ln = n;

      for(long i = (ln*ln)-100; i < ln*ln; i++){
        factors = new long[31];
        global = i;
        counter = 0;

        for(int j = 0; j < k; j++){
          fw = new FactorWorker(j, l, i);
          t = new Thread(fw);
          t.start();
          threads[j] = t;
        }

        try{
          for(Thread s: threads){
            s.join();
          }
          if(factors[0] == 0){
            factors[counter] = i;
          }else if(global >= 1){
            factors[counter] = global;
          }
          counter++;
        }catch(Exception e){
          e.printStackTrace();
        }
        writeToFile(factors, i, f, writer);
      }
      writer.close();
    }catch(IOException e){
      e.printStackTrace();
    }

  }


  /*
  Takes the number that was factorised and the list of factors for it, a file
  pointer, and a file writer, and writes the factors to the specified file,
  along with the number.
  */
  public void writeToFile(long[] factors, long product,
                File f, FileWriter writer){

    try{
      writer.write("\n" +product+ " : ");
      for(int i = 0; i < counter-1; i++){
        writer.write(factors[i] + " * ");
      }
      writer.write(factors[counter-1] + " ");

    }catch(IOException e){
      e.printStackTrace();
    }

  }


  private class FactorWorker implements Runnable{

    private long local, number, p;
    private int start;
    private ReentrantLock l;

    public FactorWorker(int start, ReentrantLock l, long number){
      this.start = start;
      this.l = l;
      local = global;
      this.number = number;
    }


    public void run(){

      for(int i = start; i < primesA.length; i += k){
        p = primesA[i];

        //stop conditions:
        if(global <= 1){
          break;
        }else if(p*p > local){
          break;
        }

        if(local % p == 0){ //Prime is factor
          updateGlobal();
          i -= k;
        }else{
          if(local != global){ //Prime not factor - check if need to update local
            updateLocal();
          }
        }
      }
    }

    public void updateGlobal(){
      try{
        l.lock();
        global = global / p;
        local = global;
        factors[counter] = p;
        counter++;
      }catch(Exception e){
        e.printStackTrace();

      }finally{
        l.unlock();
      }
    }


    public void updateLocal(){
      try{
        l.lock();
        local = global;

      }catch(Exception e){
        e.printStackTrace();

      }finally{
        l.unlock();
      }
    }


  }


  //Prints the primes. Shocking, huh?
  public void printPrimes(){
    System.out.println("Primes up to " +n+ ":");
    for(long i: primesA){
      System.out.print(i + "  ");
    }
    System.out.println();
  }



}
