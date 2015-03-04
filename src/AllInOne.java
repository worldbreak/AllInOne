import org.dom4j.DocumentException;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by sumo on 17.2.2015.
 */
public class AllInOne {


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

        -m matlabToXml - Xml bude mít stejný tvar jako výstupní soubor z detektoru
        prvni argument - nazev souboru s branou
        druhy argument - nazev souboru s vozidly

        -x rozdělení a práce s XML
        prvni argument XML soubor (je potřeba rozšířit)

    */

        while (i < args.length) {
            arg = args[i];

            if (arg.equals("-c")) {
                if(Array.getLength(args)>(i+2)){
                    if (args[i+1].endsWith(".mat")){
                        if (args[i+2].endsWith(".rou.xml")){
                            new XMLCreator(args[i+1],args[i+2]);
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
                            try {
                                new XMLReader(args[i+1],args[i+2]);
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