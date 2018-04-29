import java.util.ArrayList;
import java.io.*;

class SequentialSieve{

  private final int n;
  private int counter;
  private ArrayList<Integer> primes;
  private long[] primesA;
  private byte[] numbers;

  public SequentialSieve(int n){
    this.n = n;

  }

  /*
  First half copied from SieveDemo program. Adds 2 and 3 to the list of primes,
  then flips the values for products of 3 (see flip method description), and
  finds the next prime to work with by adding 2 to the current one to skip even
  numbers.

  When all primes up to the square root of n are found, adds 2 to the current
  prime again, and adds the remaining primes up to n.
  */
  public void seqSieve(){
    primes = new ArrayList<Integer>();
    numbers = new byte[n];
    int currentP;
    counter = 1;
    currentP = 3;

    do{
      flip(currentP);
      currentP = findNextPrime(currentP+2);

      if(currentP == 0){
        System.out.println("Something went wrong with finding the next prime!");
        break;
      }

    }while(currentP < Math.sqrt(n));

    for(int i = 3; i < n; i+= 2){
      if(isPrime(i)){
        counter++;
      }
    }
    primesA = new long[counter];
    counter = 1;
    primesA[0] = 2;

    for(int i = 3; i < n; i+= 2){
      if(isPrime(i)){
        primesA[counter] = i;
        counter++;
      }
    }


//    printPrimes();

//    addRemainingPrimes(primes.get(primes.size()-1) + 2);
//    printPrimes();

  }


  /*From SieveDemo.
  Takes in a param p, which is a prime number. Multiplies p with itself, and
  crosses off the resulting number as not being a prime number, and repeats with
  all products of p*2.
  (Combined the traverse and flip method from SieveDemo)
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
  Iterates from starting index, which is the current prime + 2, and checks all
  odd numbers after it if it's a prime. Returns 0 on error.
  */
  public int findNextPrime(int start){
    for(int i = start; i < n; i += 2){
      if(isPrime(i)){
        return i;
      }
    }
    System.out.println("Start: " +start);
    return 0;
  }

  /*
  From SieveDemo.
  Takes an index i, divides by 16 (because 8 bits in each byte, and ignoring the
  even numbers) to get the index in the byte array, and finds the desired bit
  position within the byte. Returns whether the bit is set to 0 (prime) or 1 (not
  prime)
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
  public void addRemainingPrimes(int start){
    for(int i = start; i < n; i+= 2){
      if(isPrime(i)){
        primes.add(i);
        counter++;
      }
    }
    //primes[i]*primes[i] < num
    primesA = new long[counter];
    for(int i = 0; i < primes.size(); i++){
      primesA[i] = primes.get(i);
    }

  }

  public ArrayList<Integer> getPrimes(){
    return primes;
  }

  public long[] getPrimesA(){
    return primesA;
  }


  /*
  Factorises the numbers between n*n-100 to n*n and adds the prime factors to a
  list, which is then sent to writeToFile method, where they are written
  to a .txt file. If it iterates through the entire list of primes without
  finding a factor, it adds the number itself to the list and breaks the loop.
  */
  public void factorise(){
    long product, casting;
    int j;
    ArrayList<Long> factors;

    try{
      File f = new File("./seq_factors.txt");
      FileWriter fw = new FileWriter(f);
      long ln = n;

      for(long i = (ln*ln)-100; i < ln*ln; i++){
        product = i;
        factors = new ArrayList<Long>();
        j = 0;

        do{
          if(product % primesA[j] == 0){
            factors.add(primesA[j]);
            product /= primesA[j];
          }else{
            j++;
          }

          if(j >= primesA.length-1){
            factors.add(product);
            break;
          }
        }while((product > 1) && (primesA[j]*primesA[j] < product));

        if(product > 1){
          factors.add(product);
        }

        writeToFile(factors, i, f, fw);

      }
      fw.close();

    }catch(IOException e){
      e.printStackTrace();
    }

  }

  /*
  Takes the number that was factorised and the list of factors for it, a file
  pointer, and a file writer, and writes the factors to the specified file,
  along with the number.
  */
  public void writeToFile(ArrayList<Long> factors, long product,
                File f, FileWriter fw){

    try{
      fw.write("\n"+product + " : ");
      for(int i = 0; i < factors.size()-1; i++){
        fw.write(factors.get(i) + " * ");
      }
      fw.write(factors.get(factors.size()-1) + " ");

    }catch(IOException e){
      e.printStackTrace();
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
