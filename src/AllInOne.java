import org.dom4j.DocumentException;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by sumo on 17.2.2015.
 */
public class AllInOne {

    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    public AllInOne(String args[]) {
        int i = 0;
        String arg;
        String inputFileGate="";
        String outputFileVehicles="";
        String outputFileAdditional="";
    /*
        -c - createVehicles
        prvni argument - nazev souboru s branou
        druhy argument - nazev souboru s vozidly
        treti (nepovinny argument) - typ generovani vozidel - exp - exponenciální; default - deterministický

        -m matlabToXml - Xml bude mít stejný tvar jako výstupní soubor z detektoru
        prvni argument - nazev souboru s branou
        druhy argument - nazev souboru s vozidly

        -x rozdělení a práce s XML - časová řada
        prvni argument XML soubor (bez _0)
        druhý argument výstupní soubor
        po vygenerování je potřeba spustit gnuplot.txt

        -h
        histogram
        prvni argument XML soubor (bez _0)
        druhý argument výstupní soubor
        po vygenerování je potřeba spustit gnuplotcetnosti.txt
    */

        while (i < args.length) {
            arg = args[i];

            if (arg.equals("-c")) {
                if(Array.getLength(args)>(i+2)){
                    if (args[i+1].endsWith(".mat")){
                        if (args[i+2].endsWith(".rou.xml")){
                            if(Array.getLength(args)>(i+3))
                            {
                                if ((args[i + 3]).equals("exp"))
                                    new XMLCreator(args[i+1],args[i+2],args[i+3]);
                                else
                                    new XMLCreator(args[i+1],args[i+2],"det");
                            }
                            else
                            {
                                new XMLCreator(args[i+1],args[i+2],"det");
                            }

                        }else{
                            System.out.println("Musíte zadat název výstupního souboru (přípona .rou.xml)");
                        }
                    }else{
                        System.out.println("Nejprve zadejte název souboru s bránou");
                    }

                }else{
                    System.out.println("Zadejte argumenty -cv");
                }
            }

            if (arg.equals("-m")) {
                if(Array.getLength(args)>(i+2)) {
                    if (args[i+1].endsWith(".mat")) {
                        if (args[i+2].endsWith(".xml")){
                            new MatlabToXML(args[i+1],args[i+2]);
                        }else{
                            System.out.println("Musíte zadat název výstupního souboru (přípona .xml)");
                        }
                    }else{
                        System.out.println("Nejprve zadejte název souboru s bránou");
                    }

                }else{
                    System.out.println("Zadejte argumenty -m");
                }

            }

            if (arg.equals("-x")) {
                if(Array.getLength(args)>(i+2)) {
                    if (args[i+1].endsWith(".xml")) {
                        if (args[i+2].endsWith(".pdf")){
                            try
                            {
                                new XMLReader(args[i+1],args[i+2]);
                            } catch (DocumentException e)
                            {
                                System.out.println(e);
                            }
                        }else{
                            System.out.println("Musíte zadat název výstupního souboru (přípona .pdf)");
                        }
                    }else{
                        System.out.println("Nejprve zadejte název souboru s detektorem (.xml)");
                    }

                }else{
                    System.out.println("Zadejte argumenty -x");
                }

            }

            if (arg.equals("-xh")) {
                if(Array.getLength(args)>(i+2)) {
                    if (args[i+1].endsWith(".xml")) {
                        if (args[i+2].endsWith(".pdf")){
                            try {
                                if(Array.getLength(args)>(i+3))
                                {
                                    if (isInteger(args[i + 3]))
                                        new XMLReaderHist(args[i + 1], args[i + 2], args[i + 3]);
                                    else
                                        new XMLReaderHist(args[i+1],args[i+2],"-1");
                                }
                                else
                                    new XMLReaderHist(args[i+1],args[i+2],"-1");
                            } catch (DocumentException e) {
                                System.out.println(e);
                            }
                        }else{
                            System.out.println("Musíte zadat název výstupního souboru (přípona .pdf)");
                        }
                    }else{
                        System.out.println("Nejprve zadejte název souboru s detektorem (.xml)");
                    }

                }else{
                    System.out.println("Zadejte argumenty -x");
                }

            }

            if (arg.equals("-h")) {
                if(Array.getLength(args)>(i+2)) {
                    if (args[i+1].endsWith(".xml")) {
                        if (args[i+2].endsWith(".pdf")){
                            try {
                                new Histogram(args[i+1],args[i+2]);
                            } catch (DocumentException e) {
                                System.out.println(e);
                            }
                        }else{
                            System.out.println("Musíte zadat název výstupního souboru (přípona .pdf)");
                        }
                    }else{
                        System.out.println("Nejprve zadejte název souboru s detektorem (.xml)");
                    }

                }else{
                    System.out.println("Zadejte argumenty -x");
                }

            }

            i++;
        }

    }


    public static void main (String args[]){
        if (Array.getLength(args)==0) {
           System.out.println("Nezadali jste žádné parametry");
           System.exit(0);
        }
        else new AllInOne(args);

    }
}
