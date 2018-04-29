import java.util.Arrays;
import java.util.ArrayList;

class Oblig3{

  private final int n, k;
  private double sTimeP, sTimeF, pTimeP, pTimeF;
  private long t;
  private long[] ssp, psp;

  public Oblig3(int n, int k){
    this.n = n;
    this.k = k;
  }


  public static void main(String[] args) {

    int n, k;

    if(args.length == 0){
      System.out.println("Please run with arguments (see rapport.txt)");
      return;
    }else if(args.length == 1){
      n = Integer.parseInt(args[0]);
      System.out.println("Setting nr of threads to "+
      Runtime.getRuntime().availableProcessors());
      k = Runtime.getRuntime().availableProcessors();
    }else{
      n = Integer.parseInt(args[0]);
      k = Integer.parseInt(args[1]);
      if(k == 0){
        k = Runtime.getRuntime().availableProcessors();
      }
    }


    Oblig3 o3 = new Oblig3(n, k);
    o3.doThings();
  }//end main


  //Creates sieves and starts timing
  public void doThings(){


    timeSieves();
  }


  //Runs each sieve 7 times, prints median time taken, and speedup
  public void timeSieves(){
    sequential();
    parallell();

    System.out.println("\n***      Succcessful: " + testSieves() + "         ***");
    System.out.printf("***  Median speedup primes: %.3f  ***%n", (sTimeP/pTimeP));
    System.out.printf("***  Median speedup factors: %.3f ***%n", (sTimeF/pTimeF));

  }


  public void sequential(){

    SequentialSieve ss = new SequentialSieve(n);


    double[] sTimesP = new double[7];
    double[] sTimesF = new double[7];

    for(int i = 0; i < 7; i++){
      t = System.nanoTime();
      ss.seqSieve();
      sTimesP[i] = (System.nanoTime()-t)/1000000.0;
      System.out.printf("%nS time[%d] primes: %.2f ms%n", i, sTimesP[i]);
      t = System.nanoTime();
      ss.factorise();
      sTimesF[i] = (System.nanoTime()-t)/1000000.0;
      System.out.printf("S time[%d] factors: %.2f ms%n", i, sTimesF[i]);
    }
    Arrays.sort(sTimesP);

    sTimeP = sTimesP[3];
    sTimeF = sTimesF[3];

    ssp = ss.getPrimesA();

    System.out.printf("\n--  Sequential median primes:  %.2f ms  --%n", sTimeP);
    System.out.printf("--  Sequential median factors: %.2f ms  --%n", sTimeF);

  }


  public void parallell(){
    double[] pTimesP = new double[7];
    double[] pTimesF = new double[7];

    ParallellSieve ps = new ParallellSieve(n, k);

    //Parallell
    for(int i = 0; i < 7; i++){
      t = System.nanoTime();
      ps.parSieve();
      pTimesP[i] = (System.nanoTime()-t)/1000000.0;
      t = System.nanoTime();
      System.out.printf("%nP time[%d] primes: %.2f ms%n", i, pTimesP[i]);
      ps.factorise();
      pTimesF[i] = (System.nanoTime()-t)/1000000.0;
      System.out.printf("P time[%d] factors: %.2f ms%n", i, pTimesF[i]);
    }
    Arrays.sort(pTimesP);

    pTimeP = pTimesP[3];
    pTimeF = pTimesF[3];

    psp = ps.getPrimes();

    System.out.printf("\n--  Parallell median primes:  %.2f ms  --%n", pTimeP);
    System.out.printf("--  Parallell median factors: %.2f ms  --%n", pTimeF);
  }


  public boolean testSieves(){

    if(ssp.length != psp.length){
      System.out.println("Different sizes - S: " +ssp.length + " P: " +psp.length);
      return false;
    }

    for(int i = 0; i < ssp.length; i++){
      if(ssp[i] != psp[i]){
        System.out.println("Error in primes: " + ssp[i] + " " +psp[i]);
         return false;
      }
    }
    return true;
  }

}
