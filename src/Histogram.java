import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;


public class Histogram {



    public static Document parse(String fileName) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(fileName);
        return document;
    }


    public static double max(double[] values) {
        double max = Double.MIN_VALUE;
        for(double value : values) {
            if(value > max)
                max = value;
        }
        return max;
    }

    public static void hist(String fileNameInput,String fileNameOutput,int numberOfFiles) throws DocumentException {
        Document[] documents = new Document[numberOfFiles];
        double[][] numCars = new double[numberOfFiles][1440];
        double[] sumCars = new double[1440];
        try {
            for (int h=0;h<numberOfFiles;h++) {
                File f = new File(fileNameInput.replaceFirst("[.][^.]+$", "") + "_" + h + ".xml");
                documents[h] = parse(f.getName());
                Element root = documents[h].getRootElement();
                int loop = 0;
                for ( Iterator i = root.elementIterator( "interval" ); i.hasNext(); ) {
                    Element foo = (Element) i.next();
                    for ( Iterator j = foo.attributeIterator(); j.hasNext(); ) {
                        Attribute attribute = (Attribute) j.next();
                        String name = attribute.getName();
                        if (name.equals("nVehContrib")) {
                            double pomoc = Double.valueOf(attribute.getValue());
                            numCars[h][loop] = pomoc;
                            loop++;
                        }
                    }
                }
            }


            for (int minute=0;minute<1440;minute++)
                for (int h=0;h<numberOfFiles;h++) {
                {
                    sumCars[minute] += numCars[h][minute];
                }

            }

            double max = max(sumCars);
            double[] freq = new double[(int)max+1];
            for (int i=0;i<Array.getLength(sumCars);i++){
                freq[(int)sumCars[i]]++;
            }

            FileWriter outFileFreq = new FileWriter("cetnosti.txt");
            PrintWriter outFreq = new PrintWriter(outFileFreq);

            for (int i=0;i<Array.getLength(freq);i++)
                outFreq.println(i+"    "+freq[i]);

            outFreq.close();

            FileWriter outFileGNU = new FileWriter("gnuPlotCetnosti.txt");
            PrintWriter outGNU = new PrintWriter(outFileGNU);



            outGNU.println("set terminal pdf");
            outGNU.println("set output '"+fileNameOutput+"'");
            outGNU.println("set ylabel 'Četnost'");
            outGNU.println("set xlabel 'Počet vozidel'");
            outGNU.println("set xrange [0:"+Array.getLength(freq)+"]");
            outGNU.println("set yrange [0:"+max(freq)+"]");
            outGNU.println("set style fill transparent solid 0.5 noborder");
            outGNU.print("plot 'cetnosti.txt' u 1:2 w boxes lc rgb\"green\" notitle");
            outGNU.close();

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public Histogram(String fileNameInput, String fileNameOutput) throws DocumentException {
        int numberOfFiles=0;
        for (int i=0; i<4;i++)
        {
            File f = new File(fileNameInput.replaceFirst("[.][^.]+$", "")+"_"+i+".xml");
            if (f.exists())
                numberOfFiles++;
        }
        hist(fileNameInput,fileNameOutput,numberOfFiles);

/*           Document document = parse(fileNameInput);
           bar(document, fileNameOutput);*/
    }
}
