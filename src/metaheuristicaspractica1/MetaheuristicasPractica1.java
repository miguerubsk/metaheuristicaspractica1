/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaheuristicaspractica1;

import java.util.Random;
import java.io.*;
import static java.lang.Math.exp;
import static java.lang.Math.log10;
import java.util.regex.*;

/**
 *
 * @author ROBERTO
 */
public class MetaheuristicasPractica1 {
    
    //Parámetros a modificar
    static final int SEMILLA = 77585604; //Semilla del generador de valores aleatorios.
    static final String FICHERO_DAT = "cnf10.dat"; //Ruta del fichero de datos.
    static final boolean BOLTZMANN = false; //Mecanismo de enfriamiento (true = Boltzmann, false = geometrico).
    static final String FICHERO_LOG_PEM = "PeMlog.log"; //Ruta del fichero de resultados del algoritmo Primero el Mejor.
    static final String FICHERO_LOG_ES = "ESlog.log"; //Ruta del fichero de resultados del algoritmo Enfriamiento Simulado.
    
    //Constantes
    static final int MAX_ITERACIONES = 50000; //Iteraciones maximas que realizan los algoritmos antes de finalizar (si no encuentran un optimo local/global).
    static final double ALFA = 0.9; //Ratio de disminucion de la temperatura en el Enfriamiento Simulado.
    
    /**
     * Funcion que calcula el coste de una solucion. 
     * @param tam Numero de unidades y situaciones a emparejar (tamaño de las matrices).
     * @param matrizFlujos Matriz que almacena los flujos entre las unidades i y j en las coordenadas [i][j].
     * @param matrizDistancias Matriz que almacena las distancias entre las situaciones i y j en las coordenadas [i][j].
     * @param sol Vector en el que se almacena la permutacion solucion.
     * @return coste de la solucion.
    */
    
    static int Coste(int tam, int[][] matrizFlujos, int[][] matrizDistancias, int sol[]){
        
        int coste = 0;
        
        for (int i = 0; i<tam; i++){
            for (int j = 0; j<tam; j++){
                coste+=matrizFlujos[i][j]*matrizDistancias[sol[i]][sol[j]];
            }
        }
        
        return coste;
    }
    
    /**
     * Funcion que genera un nuevo vecino intercambiando dos posiciones en la permutacion. 
     * @param sol Vector en el que se almacena la permutacion solucion.
     * @param posi Primera posicion a intercambiar.
     * @param posj Segunda posicion a intercambiar.
    */
    static void Intercambio(int sol[], int posi, int posj){
        int tmp = sol[posi];
        sol[posi] = sol[posj];
        sol[posj] = tmp;
    }
    
    /**
     * Funcion que calcula el coste de la solucion tras un intercambio. 
     * @param tam Numero de unidades y situaciones a emparejar (tamaño de las matrices).
     * @param matrizFlujos Matriz que almacena los flujos entre las unidades i y j en las coordenadas [i][j].
     * @param matrizDistancias Matriz que almacena las distancias entre las situaciones i y j en las coordenadas [i][j].
     * @param sol Vector en el que se almacena la permutacion solucion.
     * @param posi Primera posicion intercambiada.
     * @param posj Segunda posicion intercambiada.
     * @param costeAct Coste de la solucion previa al intercambio.
     * @return coste de la nueva solucion.
    */
    
    static int CosteIntercambio(int tam, int[][] matrizFlujos, int[][] matrizDistancias, int sol[], int posi, int posj, int costeAct){
        int costeTmp, costeAdd=0, costeSub=0;
        for(int i=0; i<tam; i++){
            if(i!=posi && i!=posj){
             costeSub += matrizFlujos[posi][i]*matrizDistancias[sol[posj]][sol[i]] + matrizFlujos[posj][i]*matrizDistancias[sol[posi]][sol[i]];
             costeAdd += matrizFlujos[posi][i]*matrizDistancias[sol[posi]][sol[i]] + matrizFlujos[posj][i]*matrizDistancias[sol[posj]][sol[i]];
            }
        }
        costeTmp = costeAct + costeAdd - costeSub;
        return costeTmp;
    }
    
    /**
     * Funcion que genera una solucion aleatoria factible (permutacion). 
     * @param tam Numero de valores a generar (tamaño de la solucion).
     * @param sol Vector en el que se almacena la permutacion solucion.
    */
    static void GenerarAleatoria(int tam, int[] sol){
        Random rand = new Random();
        rand.setSeed(SEMILLA);
        boolean adjudicado[] = new boolean[tam];
        for(int i=0; i<tam; i++){
            adjudicado[i] = false;
        }
        for(int i=0; i<tam; i++){
            sol[i] = rand.nextInt(tam);
            while(adjudicado[sol[i]]){
                sol[i] = rand.nextInt(tam);
            }
            adjudicado[sol[i]] = true;
        }
        System.out.printf("\n\n\nSolucion Aleatoria = ");
        for(int i=0; i<tam; i++){
            System.out.printf("%d ",sol[i]);
        }
    }
    
    /**
     * Funcion que devuelve los resultados del algoritmo Primero el Mejor a un fichero .log. 
     * @param tam Numero de unidades y situaciones a emparejar (tamaño de las matrices).
     * @param sol Vector en el que se almacena la permutacion solucion.
     * @param coste Coste de la solucion.
     * @param dlb Vector con el valor del Don't Look Bits.
     * @param iter Numero de iteracion actual.
     * @param existe Indica si el fichero se va crear/sobreescribir o se va a actualizar.
    */
    public static void PeMlog(int tam, int sol[], int coste, int[] dlb, int iter, boolean existe) {
        try {
            File archivo = new File(FICHERO_LOG_PEM);

            FileWriter escribir = new FileWriter(FICHERO_DAT+"_"+SEMILLA+"_"+archivo, existe);

            escribir.write("ITERACION "+iter+"\nSolucion:    ");
                
                for(int i=0; i<tam; i++){
                    escribir.write(""+sol[i]+" ");
                }
                escribir.write("\nCoste: "+coste);
                escribir.write("\ndlb:    ");
                for(int i=0; i<tam; i++){
                    escribir.write(""+dlb[i]);
                }
                escribir.write("\n\n\n\n");
                
            escribir.close();
            
        } 
        catch (Exception e) {
            System.err.println("Error al escribir");
        }
    }
      
    /**
     * Funcion que devuelve los resultados del algoritmo Enfriamiento Simulado a un fichero .log. 
     * @param tam Numero de unidades y situaciones a emparejar (tamaño de las matrices).
     * @param sol Vector en el que se almacena la permutacion solucion.
     * @param coste Coste de la solucion.
     * @param dlb Vector con el valor del Don't Look Bits.
     * @param iter Numero de iteracion actual.
     * @param temperatura Temperatura del algoritmo en la iteracion actual.
     * @param probAcep Probabilidad de la aceptacion de la nueva solucion (en caso de ser peor).
     * @param movimiento Indica si se realiza el movimiento.
     * @param existe Indica si el fichero se va crear/sobreescribir o se va a actualizar.
    */
    public static void ESlog(int tam, int sol[], int coste, int[] dlb, int iter, double temperatura, double probAcep, String movimiento, boolean existe) {
        try {
            File archivo = new File(FICHERO_LOG_ES);

            FileWriter escribir = new FileWriter(FICHERO_DAT+"_"+SEMILLA+"_"+archivo, existe);

            escribir.write("ITERACION "+iter+"\nSolucion:    ");
                
                for(int i=0; i<tam; i++){
                    escribir.write(""+sol[i]+" ");
                }
                escribir.write("\nCoste: "+coste);
                escribir.write("\nTemperatura: "+temperatura);
                escribir.write("\nProbabilidad de aceptación: "+probAcep);
                escribir.write("\nMovimiento: "+movimiento);
                escribir.write("\n\n\n\n");
                
            escribir.close();
            
        } 
        catch (Exception e) {
            System.err.println("Error al escribir");
        }
    }
    
    /**
     * Función que implementa el algoritmo Greedy. 
     * Relaciona la unidad de montaje con mayor flujo con la situación con menor distancia.
     * @param tam Numero de unidades y situaciones a emparejar (tamaño de las matrices).
     * @param matrizFlujos Matriz que almacena los flujos entre las unidades i y j en las coordenadas [i][j].
     * @param matrizDistancias Matriz que almacena las distancias entre las situaciones i y j en las coordenadas [i][j].
     * @param sol vector en el que se almacena la permutacion solucion.
    */
    
    static void Greedy(int tam, int[][] matrizFlujos, int[][] matrizDistancias, int sol[]){
        int[][] valores = new int[tam][2];
        int minDist, maxFlux, distIndex = 0, costIndex = 0;
        for(int i=0; i<tam; i++){
            valores[i][0] = 0;
            valores[i][1] = 0;
        }
        for(int i=0; i<tam; i++){
            for(int j=0; j<tam; j++){
                valores[i][0]+=matrizFlujos[i][j];
                valores[i][1]+=matrizDistancias[i][j];
            }
        }
        int cont=0;
        while(cont<tam){
            minDist = 100000;
            maxFlux = -1;
            for(int i=0; i<tam; i++){
                if(valores[i][0] < minDist){
                    distIndex = i;
                    minDist = valores[i][0];
                }
                if(valores[i][1] > maxFlux){
                    costIndex = i;
                    maxFlux = valores[i][1];
                }
            }
            sol[costIndex] = distIndex;
            valores[distIndex][0] = 100000;
            valores[costIndex][1] = -1;
            cont++;
        }
    }
    
    /**
     * Función que implementa el algoritmo Primero el Mejor. 
     * Comprueba los posibles vecinos hasta que encuentra uno que mejora la solucion actual y lo selecciona.
     * @param tam Numero de unidades y situaciones a emparejar (tamaño de las matrices).
     * @param matrizFlujos Matriz que almacena los flujos entre las unidades i y j en las coordenadas [i][j].
     * @param matrizDistancias Matriz que almacena las distancias entre las situaciones i y j en las coordenadas [i][j].
     * @param sol vector en el que se almacena la permutacion solucion.
    */
    static void PrimerMejor(int tam, int[][] matrizFlujos, int[][] matrizDistancias, int sol[]){
        boolean inicioFichero = false;
        int[] dlb = new int[tam];
        boolean end = false, mejora;
        for(int i=0; i<tam; i++){
            dlb[i]=0;
        }
        int contIter = 0;
        GenerarAleatoria(tam, sol); //generar solucion aleatoria
        int costeAct = Coste(tam, matrizFlujos, matrizDistancias, sol), costeTmp;
        while(!end && contIter < MAX_ITERACIONES){
            for(int i=0; i<tam; i++){
                if(dlb[i]==0){
                    mejora = false;
                    for(int j=0; j<tam; j++){
                        Intercambio(sol, i, j);
                        costeTmp = CosteIntercambio(tam, matrizFlujos, matrizDistancias, sol, i, j, costeAct);
                        if(costeTmp < costeAct){
                            costeAct = costeTmp;
                            dlb[i] = 0;
                            dlb[j] = 0;
                            mejora = true;
                        }else{
                            Intercambio(sol, j, i);
                        }
                    }
                    if(!mejora){
                        dlb[i]=1;
                    }
                }
                if(contIter > 0){
                    inicioFichero = true;
                }
                PeMlog(tam, sol, costeAct, dlb, contIter, inicioFichero);
                contIter++;
            }
            end=true;
            for(int i=0; i<tam; i++){
                if(dlb[i]==0){
                    end=false;
                }
            }
        }
    }
    
    /**
     * Función que implementa el algoritmo Enfriamiento Simulado. 
     * Comprueba los posibles vecinos escogiendo aquellos que mejoren la solución actual o alguno con peor solución (con cierta probabilidad) para evitar caer en optimos locales.
     * @param tam Numero de unidades y situaciones a emparejar (tamaño de las matrices).
     * @param matrizFlujos Matriz que almacena los flujos entre las unidades i y j en las coordenadas [i][j].
     * @param matrizDistancias Matriz que almacena las distancias entre las situaciones i y j en las coordenadas [i][j].
     * @param sol vector en el que se almacena la permutacion solucion.
    */
    static void EnfriamientoSimulado(int tam, int[][] matrizFlujos, int[][] matrizDistancias, int sol[]){
        boolean inicioFichero = false;
        int[] dlb = new int[tam];
        boolean end = false, mejora;
        for(int i=0; i<tam; i++){
            dlb[i]=0;
        }
        int contIter = 0;
        Random rand = new Random();
        rand.setSeed(SEMILLA);
        GenerarAleatoria(tam, sol); //generar solucion aleatoria
        int[] solOpt = new int[tam];
        for(int i=0; i<tam; i++){
            solOpt[i] = sol[i];
        }
        int costeAct = Coste(tam, matrizFlujos, matrizDistancias, sol), costeTmp = 0, costeOpt = costeAct;
        double temperatura = 1.5*costeAct, temp_ini = temperatura;
        String mov = "NO";
        while(!end && contIter < MAX_ITERACIONES && temperatura >= temp_ini*0.05){
            for(int i=0; i<tam; i++){
                if(dlb[i]==0){
                    mejora = false;
                    for(int j=0; j<tam; j++){
                        Intercambio(sol, i, j);
                        costeTmp = CosteIntercambio(tam, matrizFlujos, matrizDistancias, sol, i, j, costeAct);
                        if(((costeTmp - costeAct) < 0) || rand.nextDouble() <= exp(-(costeTmp - costeAct)/temperatura)){
                            costeAct = costeTmp;
                            dlb[i] = 0;
                            dlb[j] = 0;
                            if(costeOpt > costeAct){
                                for(int k=0; k<tam; k++){
                                    solOpt[k] = sol[k];
                                }
                                costeOpt = costeAct;
                            }
                            mejora = true;
                            mov = "SI";
                        }else{
                            Intercambio(sol, j, i);
                            mov = "NO";
                        }
                    }
                    if(!mejora){
                        dlb[i]=1;
                    }
                }
                if(contIter > 0){
                    inicioFichero = true;
                }
                ESlog(tam, sol, costeAct, dlb, contIter, temperatura, exp(-(costeTmp - costeAct)/temperatura), mov, inicioFichero);
                contIter++;
                if(BOLTZMANN){
                    temperatura = temp_ini/(1+log10(contIter));
                }else{
                    temperatura *= ALFA;
                }
            }
            end=true;
            for(int i=0; i<tam; i++){
                if(dlb[i]==0){
                    end=false;
                }
            }
        }
        for(int i=0; i<tam; i++){
            sol[i] = solOpt[i];
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        int tam = 0;
        String s; 
        try{
            //Lectura de los datos.
            DataInputStream datos = new DataInputStream(new BufferedInputStream(new FileInputStream(FICHERO_DAT)));
            
            //Obtencion del tamaño de las matrices.
            s = datos.readLine();
            String[] campos = s.split(" ");
            System.out.printf("%s \n", campos[0]);
            tam = Integer.parseInt(campos[0]);
//            System.out.printf("\n");
            
            //Linea en blanco en el fichero.
            s=datos.readLine();
//            System.out.printf("\n");
            
            //Inicialización y relleno de la matriz de flujos.
            int[][] matrizFlujos = new int[tam][tam];
            for(int i=0; i<tam; i++){
                s=datos.readLine();
                Pattern patron = Pattern.compile(" * ");
                Matcher encaja = patron.matcher(s);
                String resultado = encaja.replaceAll(" ");
                campos = resultado.split(" ");
                for(int j=0; j<tam; j++){
                    matrizFlujos[i][j] = Integer.parseInt(campos[j+1]);
                    System.out.printf("%d ", matrizFlujos[i][j]);
                }
                System.out.printf("\n");
            }
            
            //Linea en blanco entre matrices en el fichero.
            s=datos.readLine();
            System.out.printf("\n");
            
            //Inicialización y relleno de la matriz de distancias.
            int[][] matrizDistancias = new int[tam][tam];
            for(int i=0; i<tam; i++){
                s=datos.readLine();
                Pattern patron = Pattern.compile(" * ");
                Matcher encaja = patron.matcher(s);
                String resultado = encaja.replaceAll(" ");
                campos = resultado.split(" ");
                for(int j=0; j<tam; j++){
                    matrizDistancias[i][j] = Integer.parseInt(campos[j+1]);
                    System.out.printf("%d ", matrizDistancias[i][j]);
                }
                System.out.printf("\n");
            }
            
            //Cierre del fichero.
            datos.close();
            int[] sol = new int[tam];
            
            //Greedy
            long startTime = System.currentTimeMillis();
            Greedy(tam,matrizFlujos ,matrizDistancias, sol);
            long endTime = System.currentTimeMillis() - startTime;
            System.out.printf("\nCosto del Greedy = %d",Coste(tam,matrizFlujos ,matrizDistancias, sol));
            System.out.println("\nTiempo de ejecución del Greedy: " + endTime + " ms.");
            System.out.printf("Solucion del Greedy = ");
            for(int i=0; i<tam; i++){
                System.out.printf("%d ",sol[i]);
            }
            
            //Primero el Mejor
            long startTime1 = System.currentTimeMillis();
            PrimerMejor(tam,matrizFlujos ,matrizDistancias, sol);
            long endTime1 = System.currentTimeMillis() - startTime1;
            System.out.printf("\n\nCosto Primero el Mejor = %d",Coste(tam, matrizFlujos, matrizDistancias, sol));
            System.out.println("\nTiempo de ejecución primero el Mejor: " + endTime1 + " ms.");
            System.out.printf("Solucion del Primero el Mejor = ");
            for(int i=0; i<tam; i++){
                System.out.printf("%d ",sol[i]);
            }
            
            //Enfriamiento Simulado
            long startTime2 = System.currentTimeMillis();
            EnfriamientoSimulado(tam,matrizFlujos ,matrizDistancias, sol);
            long endTime2 = System.currentTimeMillis() - startTime2;
            System.out.printf("\n\nCosto Enfriamiento Simulado = %d",Coste(tam, matrizFlujos, matrizDistancias, sol));
            System.out.println("\nTiempo de ejecución Enfriamiento Simulado: " + endTime2 + " ms.");
            System.out.printf("Solucion del Enfriamiento Simulado = ");
            for(int i=0; i<tam; i++){
                System.out.printf("%d ",sol[i]);
            }
            
            //Comprobación de posibles excepciones.
        }catch(FileNotFoundException e){
            System.out.println("No se ha encontrado el fichero");
        }catch(IOException ioe){
            System.out.println("Error en la E/S");
        }    
        
        
    }
    
}
