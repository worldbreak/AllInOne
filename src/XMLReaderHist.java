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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;


public class XMLReaderHist {



    public static Document parse(String fileName) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(fileName);
        return document;
    }

    public static void cetnosti(Document document, String fileNameOutput,int actualFileNumber) throws DocumentException {
        int carsCount=0;
        int maximum=0;
        try {
            FileWriter outFileFreq = new FileWriter("cetnosti_"+actualFileNumber+".txt");
            PrintWriter outFreq = new PrintWriter(outFileFreq);
            FileWriter outFileGNU = new FileWriter("gnuplot_"+actualFileNumber+".txt");
            PrintWriter outGNU = new PrintWriter(outFileGNU);

            Element root = document.getRootElement();

            // iterate through child elements of root with element name "foo"
            for ( Iterator i = root.elementIterator( "interval" ); i.hasNext(); ) {
                Element foo = (Element) i.next();
                for ( Iterator j = foo.attributeIterator(); j.hasNext(); ) {
                    Attribute attribute = (Attribute) j.next();
                    String name = attribute.getName();
                    if (name.equals("nVehContrib")) {
                        double pomoc = Double.valueOf(attribute.getValue());
                        if (maximum < ((int) pomoc))
                            maximum = (int) pomoc;
                        carsCount += (int) pomoc;
                    }
                }
            }
            int[] cetnosti = new int[maximum+1];
            for ( Iterator it = root.elementIterator( "interval" ); it.hasNext(); ) {
                Element foo = (Element) it.next();
                for (Iterator kk = foo.attributeIterator(); kk.hasNext(); ) {
                    Attribute attribute = (Attribute)kk.next();
                    String name = attribute.getName();
                    if (name.equals("nVehContrib")) {
                        double pomoc = Double.valueOf(attribute.getValue());
                        int value = (int) pomoc;
                        cetnosti[value]++;
                    }
                }
            }
            for (int i=0;i<maximum+1;i++)
                outFreq.println(cetnosti[i]);

            outGNU.println("set terminal pdf");
            outGNU.println("set output '"+fileNameOutput+"'");
            outGNU.println("set xrange [0:"+ Array.getLength(cetnosti)+"]");
            outGNU.println("set yrange [0:600]");
            outGNU.println("set style data histogram");
            outGNU.println("set style histogram cluster gap 1");
            outGNU.println("set style fill solid border -1");
            outGNU.println("set boxwidth 0.9");
            outGNU.println("set xtic rotate by -45 scale 0");
            outGNU.println("plot 'cetnosti.txt' using 1");
            outGNU.close();
            outFreq.close();


        } catch (Exception e) {
                System.out.println("Error creating file.");
        }

    }

    private static Calendar setStart(Calendar cal)
    {
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return cal;
    }


    public static void casovaRada(String fileNameInput,String fileNameOutput,int numberOfFiles, String w) throws DocumentException {
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

        double[][] numCars = new double[numberOfFiles][sizeOfArray];

        try {
            for (int h=0;h<numberOfFiles;h++) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                Calendar now = Calendar.getInstance();
                now = setStart(now);
                File f = new File(fileNameInput.replaceFirst("[.][^.]+$", "") + "_" + h + ".xml");
                documents[h] = parse(f.getName());
                String FileNameTime = "pocetvozidelpruh_"+h+".txt";
                FileWriter outFileTime = new FileWriter(FileNameTime);
                PrintWriter outTime = new PrintWriter(outFileTime);
                FileWriter outFileGNU = new FileWriter("gnuplot_"+h+".txt");
                PrintWriter outGNU = new PrintWriter(outFileGNU);
                Element root = documents[h].getRootElement();
                int loop = 0;
                for ( Iterator i = root.elementIterator( "interval" ); i.hasNext(); ) {
                    outTime.print(sdf.format(now.getTime())+"    ");
                    Element foo = (Element) i.next();
                    for ( Iterator j = foo.attributeIterator(); j.hasNext(); ) {
                        Attribute attribute = (Attribute) j.next();
                        String name = attribute.getName();
                        if (name.equals("nVehContrib")) {
                            if (loop==1439)
                                System.out.println();
                            double pomoc = Double.valueOf(attribute.getValue());
                            outTime.println(pomoc);
                            int index = (int) loop / increaser;
                            numCars[h][index] = numCars[h][index] + pomoc;
                            System.out.println(loop);
                            System.out.println(index);
                            loop++;
                        }
                    }
                    now.add(Calendar.MINUTE, increaser);
                }
                outGNU.println("set terminal pdf");
                outGNU.println("set output '"+fileNameOutput.replaceFirst("[.][^.]+$", "") + "_" + h + ".pdf");
                outGNU.println("set xrange [0:1440]");
                outGNU.println("set yrange [0:60]");
          /*  outGNU.println("set style data histogram");
            outGNU.println("set style histogram cluster gap 1");
            outGNU.println("set style fill solid border -1");
            outGNU.println("set boxwidth 0.9");
            outGNU.println("set xtic rotate by -45 scale 0");*/
                outGNU.println("plot '"+FileNameTime+"' using 1:2 with lines");
                outGNU.close();
                outFileTime.close();
            }

            String FileNameTime = "pocetvozidelpruh.txt";
            FileWriter outFileTime = new FileWriter(FileNameTime);
            PrintWriter outFile = new PrintWriter(outFileTime);
            double[] sum = new double[1440];
            Calendar now = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            now = setStart(now);
            for(int lane=0;lane<numCars[0].length;lane++)
            {

                outFile.print(sdf.format(now.getTime())+"    ");
                for (int f=0;f<numberOfFiles;f++)
                {
                    sum[lane] = sum[lane]+numCars[f][lane];
                    outFile.print(numCars[f][lane]+"    ");
                }
                outFile.print(sum[lane]+"   ");
                outFile.println();
                now.add(Calendar.MINUTE, 1);
            }

            outFile.close();

            FileWriter outFileGNU = new FileWriter("gnuplot.txt");
            PrintWriter outGNU = new PrintWriter(outFileGNU);

            outGNU.println("set terminal pdf");
            outGNU.println("set output '"+fileNameOutput);
            outGNU.println("set xdata time");
            outGNU.println("set timefmt '%H:%M'");
            outGNU.println("set format x '%H:%M'");
            outGNU.println("set yrange [0:60]");
            outGNU.print("plot ");
            for (int c=0;c<=numberOfFiles;c++) {
                int column = c + 2;
                int lane = c + 1;
                if (c!=numberOfFiles)
                    outGNU.print("'" + FileNameTime + "' using 1:" + column +" with lines title 'pruh"+c+"', ");
                else
                    outGNU.println("'" + FileNameTime + "' using 1:" + column +" with lines title 'součet'");
            }
      //      outGNU.println("plot '"+FileNameTime+"' using 1:2 with lines, '"+FileNameTime+"' using 1:3 with lines, '"+FileNameTime+"'using 1:4 with lines, '"+FileNameTime+"' using 1:5 with lines");
            outGNU.close();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public XMLReaderHist(String fileNameInput, String fileNameOutput,String w) throws DocumentException {
        int numberOfFiles=0;
        for (int i=0; i<4;i++)
        {
            File f = new File(fileNameInput.replaceFirst("[.][^.]+$", "")+"_"+i+".xml");
            if (f.exists())
                numberOfFiles++;
        }
        casovaRada(fileNameInput,fileNameOutput,numberOfFiles,w);

/*           Document document = parse(fileNameInput);
           bar(document, fileNameOutput);*/
    }
}
