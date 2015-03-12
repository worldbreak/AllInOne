import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.Arrays;
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

    public Document createRouteExpDocument() {
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
        Car[] car = new Car[matlabImport.getAllCarsInDay()];
        for (int min=0;min<1440;min++)
        {
            double timeMin = min*secPerHour;
            for (int lane=0;lane<matlabImport.getNumLanes();lane++)
            {
                double lambda = 0.01;
                double cislo = matlabImport.getNumCars(min,lane);
                if (cislo>0.5)
                    lambda = cislo;
                ExponentialDistribution ed = new ExponentialDistribution(lambda);
                for (int carInMin=0;carInMin<cislo;carInMin++)
                {
                    timeMin = timeMin + ed.sample();
                    car[actualCar] = new Car(lane, timeMin, min, actualCar);
                    actualCar++;
                }
            }
        }

        boolean change = true;
        while (change) {
            change = false;
            for (int j = 0; j < Array.getLength(car)-1; j++) {
                if (car[j].time > car[j + 1].time) {
                    Car carpomoc = new Car();
                    carpomoc.CopyCar(car[j]);
                    car[j].CopyCar(car[j + 1]);
                    car[j + 1].CopyCar(carpomoc);
                    change = true;
                }
            }
        }

        for (int i=0;i< Array.getLength(car);i++)
        {
            Element vehicle = routes.addElement("vehicle");
            System.out.println(car[i].time);
            vehicle = createVehicle(vehicle,i,car[i].time,car[i].iTime,car[i].lane);
        }

        return document;
    }


    public Document createRouteEqualDocument() {
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

            time0 += delta0;
            time1 += delta1;

            for (int j = 0; j < (matlabImport.getNumCars(i, 0) + matlabImport.getNumCars(i, 1)); j++) {
                Element vehicle = routes.addElement("vehicle");
                if (time0 == time1) {
                    Element vehicle2 = routes.addElement("vehicle");
                    vehicle = createVehicle(vehicle, actualCar, time0, i, 0);
                    actualCar++;
                    vehicle2 = createVehicle(vehicle2, actualCar, time1, i, 1);
                    actualCar++;
                    time0 += delta0;
                    time1 += delta1;
                    j++;
                } else if (time0 < time1) {
                    vehicle = createVehicle(vehicle, actualCar, time0, i, 0);
                    time0 += delta0;
                    actualCar++;
                } else {
                    vehicle = createVehicle(vehicle, actualCar, time1, i, 1);
                    time1 += delta1;
                    actualCar++;
                }
            }
        }
        System.out.println("Počet vozidel: "+ matlabImport.getAllCarsInDay());




        return document;
    }






    public XMLCreator(String fileNameMatlab, String fileNameCars,String type){

        try {
            matlabImport = new MatlabImport(fileNameMatlab);
            FileWriter outRou = new FileWriter(fileNameCars);
            Document RouteEqualDocument;
            if (type=="exp")
                RouteEqualDocument = createRouteExpDocument();
            else
                RouteEqualDocument = createRouteEqualDocument();

            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writerRou = new XMLWriter( outRou, format );
            writerRou.write(RouteEqualDocument);
            writerRou.close();
            System.out.println("Soubor s vozidly úspěšně vytvořen");
        }
        catch (Exception e){
            System.out.println(e);
        }

    }
}
