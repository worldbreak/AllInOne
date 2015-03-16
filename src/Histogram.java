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

    private static int toMins(String s) {
        if (s.equals("-1")) return -1;
        String[] hourMin = s.split(":");
        int hour = Integer.parseInt(hourMin[0]);
        int mins = Integer.parseInt(hourMin[1]);
        int hoursInMins = hour * 60;
        return hoursInMins + mins;
    }

    public static void hist(String fileNameInput,String fileNameOutput,int numberOfFiles,String from, String to) throws DocumentException {
        Document[] documents = new Document[numberOfFiles];
        int fromInt = toMins(from);
        int toInt = toMins(to);
        int sizeOfArray = 1440;
        if (to!="-1")
        {
            if (fromInt<toInt)
                sizeOfArray = toInt - fromInt;
            else
                sizeOfArray = 1440-fromInt + toInt;
        }

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

            if (to=="-1")
            {
                for (int minute=0;minute<sizeOfArray;minute++)
                {
                    for (int h=0;h<numberOfFiles;h++)
                    {
                        sumCars[minute] += numCars[h][minute];
                    }
                }
            }
            else
            {
                if (fromInt < toInt)
                for (int minute=fromInt;minute<toInt;minute++)
                {
                    for (int h=0;h<numberOfFiles;h++)
                    {
                        sumCars[minute] += numCars[h][minute];
                    }
                }
                else
                {
                    for (int minute=fromInt;minute<1440;minute++)
                    {
                        for (int h=0;h<numberOfFiles;h++)
                        {
                            sumCars[minute] += numCars[h][minute];
                        }
                    }
                    for (int minute=0;minute<toInt;minute++)
                    {
                        for (int h=0;h<numberOfFiles;h++)
                        {
                            sumCars[minute] += numCars[h][minute];
                        }
                    }
                }


            }

            double max = max(sumCars);
            double[] freq = new double[(int)max+1];

            if (to=="-1")
            {
               for (int i=0;i<Array.getLength(sumCars);i++)
               {
                   freq[(int)sumCars[i]]++;
               }
            }
            else
            {
                if (fromInt < toInt)
                {
                    for (int i=fromInt;i<toInt;i++)
                        freq[(int)sumCars[i]]++;
                }
                else
                {
                    for (int i=fromInt;i<1440;i++)
                        freq[(int)sumCars[i]]++;
                    for (int i=0;i<toInt;i++)
                        freq[(int)sumCars[i]]++;
                }
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

    public Histogram(String fileNameInput, String fileNameOutput,String from, String to) throws DocumentException {
        int numberOfFiles=0;
        for (int i=0; i<4;i++)
        {
            File f = new File(fileNameInput.replaceFirst("[.][^.]+$", "")+"_"+i+".xml");
            if (f.exists())
                numberOfFiles++;
        }
        hist(fileNameInput,fileNameOutput,numberOfFiles,from,to);

/*           Document document = parse(fileNameInput);
           bar(document, fileNameOutput);*/
    }
}
