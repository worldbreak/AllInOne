import org.apache.commons.math3.distribution.PoissonDistribution;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.FileWriter;
import java.util.Random;

/**
 * Created by sumo on 17.2.2015.
 */
public class XMLCreator {

    MatlabImport matlabImport;
    boolean poisson = false;
    final int secPerHour = 60;

    public Element createVehicle(Element element, int actualCar, double time, int iTime, int lane){
        Random rnd = new Random();
        double randomSpeed;
        double speed;
        do {
            randomSpeed = rnd.nextGaussian();

        }
        while (randomSpeed>0 || randomSpeed<-5);

        speed = randomSpeed + matlabImport.getSpeed(iTime, lane) / 3.6;
        if (speed>36) speed=36;
        element.addAttribute("id", Integer.toString(actualCar))
                .addAttribute("type", "type1")
                .addAttribute("route", "wholeHighway")
                .addAttribute("depart", Double.toString(time))
                .addAttribute("departPos", Double.toString(95))
                .addAttribute("departSpeed", Double.toString(speed))
                .addAttribute("departLane",Integer.toString(lane));
        ;

        return element;

    }

    public Document createRouteDocument() {
        Document document = DocumentHelper.createDocument();
        Element routes = document.addElement( "routes" );


        Element vType = routes.addElement( "vType" )
                .addAttribute( "id" , "type1")
                .addAttribute("accel", Double.toString(0.8))
                .addAttribute( "decel", Double.toString(4.5))
                .addAttribute( "sigma", Double.toString(0.5))
                .addAttribute( "length", Double.toString(5))
                .addAttribute( "maxSpeed", Double.toString(70))
                ;

        Element route = routes.addElement( "route" )
                .addAttribute("id", "wholeHighway")
                .addAttribute( "color", "1,1,0" )
                .addAttribute("edges", "7b 8b 9b 10b 11b");


        int actualCar = 0;

        for (int i=0;i<1440;i++) {
            double time0 = secPerHour * i;
            double time1 = secPerHour * i;
            double countlane0 = matlabImport.getNumCars(i, 0);
            double countlane1 = matlabImport.getNumCars(i, 1);
            double delta0,delta1;
            PoissonDistribution lane0=null,lane1=null;
            if (poisson) {
                lane0 = new PoissonDistribution(countlane0 / secPerHour);
                lane1 = new PoissonDistribution(countlane1 / secPerHour);
                delta0 = lane0.sample();
                delta1 = lane1.sample();
            } else {
                if (countlane0 == 0) {
                    delta0 = secPerHour+1;
                } else {
                    delta0 = (secPerHour / countlane0);
                }
                if (countlane1 == 0) {
                    delta1 = secPerHour + 1;
                } else {
                    delta1 = (secPerHour / countlane1);
                }
            }
            time0 = time0 + delta0;
            time1 = time1 + delta1;

            for (int j = 0; j < (matlabImport.getNumCars(i, 0) + matlabImport.getNumCars(i, 1)); j++) {
                Element vehicle = routes.addElement("vehicle");
                if (time0 == time1) {
                    Element vehicle2 = routes.addElement("vehicle");
                    vehicle = createVehicle(vehicle, actualCar, time0, i, 0);
                    actualCar++;
                    vehicle2 = createVehicle(vehicle2, actualCar, time1, i, 1);
                    actualCar++;
                    if (poisson) {
                        time0 = time0 + lane0.sample();
                        time1 = time1 + lane1.sample();
                    } else {
                        time0 = time0 + delta0;
                        time1 = time1 + delta1;
                    }
                    j++;
                } else if (time0 < time1) {
                    vehicle = createVehicle(vehicle, actualCar, time0, i, 0);
                    if (poisson) {
                        time0 = time0 + lane0.sample();
                    } else {
                        time0 = time0 + delta0;
                    }
                    actualCar++;
                } else {
                    vehicle = createVehicle(vehicle, actualCar, time1, i, 1);
                    if (poisson) {
                        time1 = time1 + lane1.sample();
                    } else {
                        time1 = time1 + delta1;
                    }
                    actualCar++;

                }

            }

            //  pocet = pocet + matlabImport.getNumCars(i,1);

        }
        System.out.println("Počet vozidel: "+ matlabImport.getAllCarsInDay());




        return document;
    }






    public XMLCreator(String fileNameMatlab, String fileNameCars){

        try {
            System.out.println("Tento modul funguje správně pouze pro data ze dvou pruhů");
            matlabImport = new MatlabImport(fileNameMatlab);
            FileWriter outRou = new FileWriter(fileNameCars);
            Document RouteDocument = createRouteDocument();

           OutputFormat format = OutputFormat.createPrettyPrint();

            XMLWriter writerRou = new XMLWriter( outRou, format );
            writerRou.write(RouteDocument);
            writerRou.close();
            System.out.println("Soubor s vozidly úspěšně vytvořen");
        }
        catch (Exception e){
            System.out.println(e);
        }

    }
}
