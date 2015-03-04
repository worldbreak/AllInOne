import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Iterator;


public class XMLReader {



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




    public static void casovaRada(Document document, String fileNameOutput,int actualFileNumber, int numberOfFiles) throws DocumentException {
        int carsCount=0;
        int time=0;
        try {
            String FileNameTime = "cas_"+actualFileNumber+".txt";;
            FileWriter outFileTime = new FileWriter(FileNameTime);
            PrintWriter outTime = new PrintWriter(outFileTime);
            FileWriter outFileGNU = new FileWriter("gnuplot_"+actualFileNumber+".txt");
            PrintWriter outGNU = new PrintWriter(outFileGNU);

            Element root = document.getRootElement();

            // iterate through child elements of root with element name "foo"
            for ( Iterator i = root.elementIterator( "interval" ); i.hasNext(); ) {
                outTime.print(time+"    ");
                Element foo = (Element) i.next();
                for ( Iterator j = foo.attributeIterator(); j.hasNext(); ) {
                    Attribute attribute = (Attribute) j.next();
                    String name = attribute.getName();
                    if (name.equals("nVehContrib")) {
                        double pomoc = Double.valueOf(attribute.getValue());
                        outTime.println(pomoc);
                        System.out.println(time+"   "+pomoc);
                    }
                }
            time++;
            }
            outGNU.println("set terminal pdf");
            outGNU.println("set output '"+fileNameOutput+"'");
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


        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public XMLReader(String fileNameInput, String fileNameOutput) throws DocumentException {
        int numberOfFiles=0;
        for (int i=0; i<3;i++)
        {
            File f = new File(fileNameInput.replaceFirst("[.][^.]+$", "")+"_"+i+".xml");
            if (f.exists())
                numberOfFiles++;
                Document document = parse(f.getName());
                casovaRada(document, fileNameOutput.replaceFirst("[.][^.]+$", "")+"_"+i+".pdf",i,0);
            

        }


/*           Document document = parse(fileNameInput);
           bar(document, fileNameOutput);*/
    }
}
