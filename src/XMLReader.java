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


public class XMLReader {



    public static Document parse(String fileName) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(fileName);
        return document;
    }

    private static Calendar setStart(Calendar cal)
    {
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return cal;
    }


    public static void timeSeries(String fileNameInput,String fileNameOutput,int numberOfFiles, String language) throws DocumentException {
        int carsCount=0;
        Document[] documents = new Document[numberOfFiles];
        double[][] numCars = new double[numberOfFiles][1440];
        String directory = fileNameOutput.replaceFirst("[.][^.]+$", "");
        new File(directory).mkdir();
        try {
            for (int h=0;h<numberOfFiles;h++) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                Calendar now = Calendar.getInstance();
                now = setStart(now);
                File f = new File(fileNameInput.replaceFirst("[.][^.]+$", "") + "_" + h + ".xml");
                documents[h] = parse(f.getName());
                String FileNameTime = directory+"/pocetvozidelpruh_"+h+".txt";
                FileWriter outFileTime = new FileWriter(FileNameTime);
                PrintWriter outTime = new PrintWriter(outFileTime);
                FileWriter outFileGNU = new FileWriter(directory+"/gnuplot_"+h+".txt");
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
                            double pomoc = Double.valueOf(attribute.getValue());
                            outTime.println(pomoc);
                            numCars[h][loop] = pomoc;
                            loop++;
                        }
                    }
                    now.add(Calendar.MINUTE, 1);
                }
                outGNU.println("set terminal pdf");
                outGNU.println("set output '"+fileNameOutput.replaceFirst("[.][^.]+$", "") + "_" + h + ".pdf");
                outGNU.println("set xrange [0:1440]");
                outGNU.println("set yrange [0:60]");
                outGNU.println("set xlabel 'čas [min]'");
          /*  outGNU.println("set style data histogram");
            outGNU.println("set style histogram cluster gap 1");
            outGNU.println("set style fill solid border -1");
            outGNU.println("set boxwidth 0.9");
            outGNU.println("set xtic rotate by -45 scale 0");*/
                outGNU.println("plot '"+FileNameTime+"' using 1:2 with lines");
                outGNU.close();
                outFileTime.close();

            }

            String FileNameTime = directory+"/pocetvozidelpruh.txt";
            String FileNameTotalCount = directory+"/pocetcelkem.txt";
            FileWriter outFileTime = new FileWriter(FileNameTime);
            FileWriter outFileTotalCount = new FileWriter(FileNameTotalCount);
            PrintWriter outFile = new PrintWriter(outFileTime);
            PrintWriter outFileTC = new PrintWriter(outFileTotalCount);
            String FileNameGEH = directory+"/GEH.txt";
            FileWriter outFileGEH = new FileWriter(FileNameGEH);
            PrintWriter outGEH = new PrintWriter(outFileGEH);
            double[] sum = new double[1440];
            Calendar now = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            now = setStart(now);
            double count=0;
            for(int time=0;time<numCars[0].length;time++)
            {

                outFile.print(sdf.format(now.getTime())+"    ");
                for (int f=0;f<numberOfFiles;f++)
                {
                    sum[time] = sum[time]+numCars[f][time];
                    count += numCars[f][time];
                    outFile.print(numCars[f][time]+"    ");
                }
                outFile.print(sum[time]+"   ");
                outFile.println();
                outFileTC.println(sum[time]);
                if ((time+1) % 60 == 0){
                    outGEH.println(count);
                    count = 0;
                }
                now.add(Calendar.MINUTE, 1);
            }

            outFile.close();
            outFileTC.close();
            outGEH.close();
            FileWriter outFileGNU = new FileWriter(directory+"/gnuplot.txt");
            PrintWriter outGNU = new PrintWriter(outFileGNU);
            outGNU.println("set terminal pdf");
            outGNU.println("set output '"+directory+"/"+fileNameOutput);

            if (language=="cs") {
                outGNU.println("set xlabel 'čas [hod:min]'");
                outGNU.println("set ylabel 'Počet vozidel'");
            }
            else {
                outGNU.println("set xlabel 'time [h:min]'");
                outGNU.println("set ylabel 'Number of cars'");
            }
            outGNU.println("set xdata time");
            outGNU.println("set timefmt '%H:%M'");
            outGNU.println("set format x '%H:%M'");
            outGNU.println("set yrange [0:60]");
            outGNU.print("plot ");
            for (int c=0;c<=numberOfFiles;c++) {
                int column = c + 2;
                int lane = c + 1;
                if (c!=numberOfFiles)
                    if (language=="cs")
                        outGNU.print("'" + FileNameTime + "' using 1:" + column +" with lines title 'pruh "+c+"', ");
                    else
                        outGNU.print("'" + FileNameTime + "' using 1:" + column +" with lines title 'lane "+c+"', ");
                else
                    if (language=="cs")
                        outGNU.println("'" + FileNameTime + "' using 1:" + column +" with lines title 'součet'");
                    else
                        outGNU.println("'" + FileNameTime + "' using 1:" + column +" with lines title 'sum'");
            }
      //      outGNU.println("plot '"+FileNameTime+"' using 1:2 with lines, '"+FileNameTime+"' using 1:3 with lines, '"+FileNameTime+"'using 1:4 with lines, '"+FileNameTime+"' using 1:5 with lines");
            outGNU.close();
            Runtime.getRuntime().exec("gnuplot "+directory+"/gnuplot.txt");
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public XMLReader(String fileNameInput, String fileNameOutput,String language) throws DocumentException {
        int numberOfFiles=0;
        for (int i=0; i<4;i++)
        {
            File f = new File(fileNameInput.replaceFirst("[.][^.]+$", "")+"_"+i+".xml");
            if (f.exists())
                numberOfFiles++;
        }
        timeSeries(fileNameInput,fileNameOutput,numberOfFiles, language);

/*           Document document = parse(fileNameInput);
           bar(document, fileNameOutput);*/
    }
}
