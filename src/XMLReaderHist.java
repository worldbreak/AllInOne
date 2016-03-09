import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;


public class XMLReaderHist {



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

    private static Calendar setStart(Calendar cal)
    {
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return cal;
    }


    public static void hist(String fileNameInput,String fileNameOutput,int numberOfFiles, String w, String language) throws DocumentException {
        Document[] documents = new Document[numberOfFiles];
        int window = Integer.parseInt(w);
        int sizeOfArray = 0;
        int increaser;
        if ((window == -1)||(1440%window!=0)) {
            sizeOfArray = 1440;
            increaser = 1;
        }
        else {
            sizeOfArray = (int) (1440 / window);
            increaser = window;
        }
        String directory = fileNameOutput.replaceFirst("[.][^.]+$", "");
        new File(directory).mkdir();

        double[][] numCars = new double[numberOfFiles][sizeOfArray];

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
                            int index = (int) loop / increaser;
                            numCars[h][index] = numCars[h][index] + pomoc;
                            loop++;
                        }
                    }
                }
            }

            String FileNameTime = directory+"/pocetvozidelhist.txt";
            FileWriter outFileTime = new FileWriter(FileNameTime);
            PrintWriter outFile = new PrintWriter(outFileTime);
            double[] sum = new double[sizeOfArray];
            Calendar now = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            now = setStart(now);
            for(int lane=0;lane<sizeOfArray;lane++)
            {

                outFile.print(sdf.format(now.getTime())+"    ");
                for (int f=0;f<numberOfFiles;f++)
                {
                    sum[lane] = sum[lane]+numCars[f][lane];
                    outFile.print(numCars[f][lane]+"    ");
                }
                outFile.print(sum[lane]+"   ");
                outFile.println();
                now.add(Calendar.MINUTE, increaser);
            }

            outFile.close();

            FileWriter outFileGNU = new FileWriter(directory+"/gnuplot.txt");
            PrintWriter outGNU = new PrintWriter(outFileGNU);

            outGNU.println("set terminal pdf");
            outGNU.println("set output '"+directory+"/"+fileNameOutput+"'");

            outGNU.println("set style fill solid 1.00 border -1");
            outGNU.println("set style histogram rowstacked");
            outGNU.println("set style data histograms");
            outGNU.println("set xrange [0:"+sizeOfArray+"]");
            outGNU.println("set ytics out nomirror");
            outGNU.println("set style fill solid border -1");
            outGNU.println("set boxwidth 0.5 relative");
            if (language == "cs") {
                outGNU.println("set ylabel 'Počet vozidel'");
                outGNU.println("set xlabel 'Čas'");
            } else {
                outGNU.println("set ylabel 'Number of cars'");
                outGNU.println("set xlabel 'Time'");
            }

            outGNU.println("set style fill transparent solid 0.5 noborder");
            if (language == "cs")
                outGNU.print("plot '"+directory+"/pocetvozidelhist.txt' using 2:xtic(strcol(1)) title 'Počet vozidel - 0. pruh', ");
            else
                outGNU.print("plot '"+directory+"/pocetvozidelhist.txt' using 2:xtic(strcol(1)) title 'Number of cars - 0. lane', ");

            for (int c=1;c<numberOfFiles;c++) {
                int column=c+3;
                if (c!=numberOfFiles)
                    if (language == "cs")
                        outGNU.print("'" + FileNameTime + "' using " + column +" title 'Počet vozidel - "+c+". pruh', ");
                    else
                        outGNU.print("'" + FileNameTime + "' using " + column +" title 'Number of cars - "+c+". lane', ");
                else
                    if (language == "cs")
                        outGNU.println("'" + FileNameTime + "' using " + column +" title 'Počet vozidel - "+c+". pruh'");
                    else
                        outGNU.println("'" + FileNameTime + "' using " + column +" title 'Number of cars - "+c+". lane'");
            }
         //   outGNU.print("plot 'pocetvozidelhist.txt' using 2:xtic(strcol(1)) lc rgb 'green',  'pocetvozidelhist.txt' using 3 lc rgb 'red', 'pocetvozidelhist.txt' using 4 lc rgb 'yellow'");
            //using 2:xticlabels(1) lc rgb 'green'
       /*     for (int c=0;c<=numberOfFiles;c++) {
                int column = c + 2;
                int lane = c + 1;
                if (c!=numberOfFiles)
                    outGNU.print("'" + FileNameTime + "' using 1:" + column +" with lines title 'pruh"+c+"', ");
                else
                    outGNU.println("'" + FileNameTime + "' using 1:" + column +" with lines title 'součet'");
            } */
      //      outGNU.println("plot '"+FileNameTime+"' using 1:2 with lines, '"+FileNameTime+"' using 1:3 with lines, '"+FileNameTime+"'using 1:4 with lines, '"+FileNameTime+"' using 1:5 with lines");
            outGNU.close();
            Runtime.getRuntime().exec("gnuplot "+directory+"/gnuplot.txt");
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public XMLReaderHist(String fileNameInput, String fileNameOutput,String w, String language) throws DocumentException {
        int numberOfFiles=0;
        for (int i=0; i<4;i++)
        {
            File f = new File(fileNameInput.replaceFirst("[.][^.]+$", "")+"_"+i+".xml");
            if (f.exists())
                numberOfFiles++;
        }
        hist(fileNameInput,fileNameOutput,numberOfFiles,w,language);

/*           Document document = parse(fileNameInput);
           bar(document, fileNameOutput);*/
    }
}
