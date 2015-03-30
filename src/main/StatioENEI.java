/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author Tiago
 */
public class StatioENEI {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        float[][] custos;
        List<String> ordem;
        List<String> cidades;
        float custoTotal;
        int nCidades = Integer.parseInt(args[0]);
        String cidadeInicial = args[1];
        String cidadeFinal = args[2];

        for (Mode m : Mode.values()) {
            custos = new float[nCidades][nCidades];
            cidades = new ArrayList<>(nCidades);
            ordem = new ArrayList<>(nCidades);
            readFromCSV(m.file, custos, cidades);

            custoTotal = calculate(custos,
                    cidades.indexOf(cidadeInicial),
                    cidades.indexOf(cidadeFinal),
                    ordem);

            printRota(custoTotal, cidadeInicial, cidadeFinal, cidades, ordem, m);
        }
    }

    private static void readFromCSV(String filename, float[][] outCustos, List<String> outCidades) throws IOException {
        CSVParser c = new CSVParser(new FileReader(filename), CSVFormat.EXCEL.withDelimiter(';').withNullString(""));
        int lineNumber;
        for (CSVRecord record : c) {
            // System.out.println(record);

            if ((lineNumber = (int) record.getRecordNumber()) == 1) {
                continue;
            }
            outCidades.add(record.get(0));
            //     System.out.printf("\n%10s", record.get(0));
            for (int i = lineNumber - 1; i < outCustos.length + 1; i++) {
                //        System.out.printf("\t%-6s|", (record.get(i) == null) ? "null" : record.get(i));
                outCustos[lineNumber - 2][i - 1] = outCustos[i - 1][lineNumber - 2] = Float.parseFloat((record.get(i) == null) ? "0.0" : record.get(i));
            }
        }
    }

    private static float calculate(float[][] custos, int cidadeInicial, int cidadeFinal, List<String> ordem) {

       // System.out.printf("%d -> %d\n", cidadeInicial, cidadeFinal);
        ordem.clear();
        ordem.add(0, String.valueOf(cidadeInicial));
        ordem.add(1, String.valueOf(cidadeFinal));
        int i;
        for (i = 0; i < custos.length; i++) {
            if (ordem.contains(String.valueOf(i))) {
                continue;
            }
            float minimo = Float.MAX_VALUE;
            int idxMin = 1;
         //   System.out.println(i);
            for (int j = 1; j < ordem.size(); j++) {
           //     System.out.printf(">%d|%d\n>>B4: %s\n",i, j, ordem);
                ordem.add(j, String.valueOf(i));
          //      System.out.printf(">>After: %s\n", ordem);
                float value = calcCost(ordem, custos);
              //  System.out.println("Value: "+value);
                ordem.remove(j);
                
                if (value < minimo) { 
                    minimo = value;
                    idxMin = j;
                }
            }
            ordem.add(idxMin, String.valueOf(i));
         //    System.out.println(ordem);
        }
        return calcCost(ordem, custos);
    }

    private static float calcCost(List<String> list, final float[][] custos) {
        float sum = 0;
        for (int i = 1; i < list.size(); i++) {
            sum += custos[Integer.parseInt(list.get(i - 1))][Integer.parseInt(list.get(i))];
        }
        return sum;
    }

    private static void printRota(final float custoTotal, final String cidadeInicial, final String cidadeFinal, final List<String> cidades, final List<String> ordem, final Mode M) {
        String result = String.format("A rota que percorre menos %s começando em %s e terminando em %s segue o percurso: ", M.name, cidadeInicial, cidadeFinal);
        for (int i = 0; i < ordem.size(); i++) {
            if (i > 0) {
                result += ", ";
            }
            result += (cidades.get(Integer.parseInt(ordem.get(i))));

        }
        result += String.format(" percorrendo um total de %3.2f %s.", custoTotal, M.units);
        System.out.println(result);
    }

    public enum Mode {

        COST("€", "cost.csv", "custo"),
        DISTANCE("km", "distance.csv", "distância"),
        TIME("h", "time.csv", "tempo");
        String units, file, name;

        private Mode(String units, String file, String name) {
            this.units = units;
            this.file = file;
            this.name = name;
        }

    }

}
