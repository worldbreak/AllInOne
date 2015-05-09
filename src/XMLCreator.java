import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.Random;

/**
 * Created by sumo on 17.2.2015.
 */
public class XMLCreator {

    MatlabImport matlabImport;
    boolean poisson = false;
    final int secPerHour = 60;
    double speedHelp;

    public Element createVehicle(Element element, int actualCar, double time, int iTime, int lane, String type){
        Random rnd = new Random();
        double randomSpeed;
        do {
            randomSpeed = rnd.nextGaussian();

        }
        while (randomSpeed>0 || randomSpeed<-5);

        double speed = randomSpeed + matlabImport.getSpeed(iTime, lane) / 3.6;
        if (speed > speedHelp) speed = speedHelp;
        if (speed<0) speed=0;
       // if (speed>36) speed=36;
        element.addAttribute("id", Integer.toString(actualCar))
                .addAttribute("type", type)
                .addAttribute("route", "wholeHighway")
                .addAttribute("depart", Double.toString(time))
                .addAttribute("departPos", Double.toString(95))
                .addAttribute("departSpeed", Double.toString(speed))
                .addAttribute("departLane",Integer.toString(lane));
        return element;

    }


    public Element createVType(Element element, int actualCar, String model, int iTime, int lane){

         /* Gipps */
        NormalDistribution rndAccel = new NormalDistribution(1.7,0.3);

        double randomAccel = 1.5;//rndAccel.sample();
        double randomDecel = 5.0; //2.0*randomAccel;
        double headwayTime = 1.5;
        double length = 4.25;
        double minGap = 2;
        NormalDistribution rndDesiredSpeed = new NormalDistribution(36.11,3.6);
        double maxSpeed = rndDesiredSpeed.sample();

        speedHelp = maxSpeed;

        element.addAttribute("id","type"+actualCar)
                .addAttribute("accel", Double.toString(randomAccel))
                .addAttribute("decel", Double.toString(randomDecel))
                .addAttribute("sigma", Double.toString(headwayTime))
                .addAttribute("minGap", Double.toString(minGap))
                .addAttribute("length", Double.toString(length));
        element.addAttribute("maxSpeed", Double.toString(maxSpeed));
     //          .addAttribute("id","type"+actualCar);
        // IDM, Krauss, Gipps, Wiedemann
        if (model.equals("IDM")){
            Element cfIDM = element.addElement("carFollowing-IDM");
        }
        else if (model.equals("Gipps")){
            Element cfGipps = element.addElement("carFollowing-GIPPS");
            cfGipps.addAttribute("desiredSpeed", Double.toString(maxSpeed));
        }
        else if (model.equals("Krauss")){
            element.addElement("carFollowing-Krauss");
        }
        else if (model.equals("Wiedemann")){
            element.addElement("carFollowing-Wiedemann");
        }
        return element;
    }





    public Document createRouteExpDocument(String model) {
        Document document = DocumentHelper.createDocument();
        Element routes = document.addElement( "routes" );
        Element route = routes.addElement( "route" )
                .addAttribute("id", "wholeHighway")
                .addAttribute( "color", "1,1,0" )
                .addAttribute("edges", "7b 8b 9b 10b 11b 12b 13b 14b 15b 16b 17b 18b 19b 20b");


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
            Element vType = routes.addElement( "vType" );
            Element vehicle = routes.addElement("vehicle");
            vType = createVType(vType,i,model,car[i].iTime,car[i].lane);
            vehicle = createVehicle(vehicle,i,car[i].time,car[i].iTime,car[i].lane,"type"+i);
        }
        System.out.println("Počet vozidel: "+ matlabImport.getAllCarsInDay());
        return document;
    }




    public Document createRouteEqualDocument(String model) {
        Document document = DocumentHelper.createDocument();
        Element routes = document.addElement( "routes" );


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
                Element vType = routes.addElement( "vType" );
                Element vehicle = routes.addElement("vehicle");
                if (time0 == time1) {
                    Element vType2 = routes.addElement( "vType" );
                    Element vehicle2 = routes.addElement("vehicle");
                    vType = createVType(vType, actualCar, model,i,0);
                    vehicle = createVehicle(vehicle, actualCar, time0, i, 0, "type" + actualCar);
                    actualCar++;
                    vType2 = createVType(vType2, actualCar, model,i,1);
                    vehicle2 = createVehicle(vehicle2, actualCar, time1, i, 1, "type" + actualCar);
                    actualCar++;
                    time0 += delta0;
                    time1 += delta1;
                    j++;
                } else if (time0 < time1) {
                    vType = createVType(vType, actualCar, model,i,0);
                    vehicle = createVehicle(vehicle, actualCar, time0, i, 0, "type"+actualCar);
                    time0 += delta0;
                    actualCar++;
                } else {
                    vType = createVType(vType, actualCar, model,i,1);
                    vehicle = createVehicle(vehicle, actualCar, time1, i, 1, "type"+actualCar);
                    time1 += delta1;
                    actualCar++;
                }
            }
        }
        System.out.println("Počet vozidel: "+ matlabImport.getAllCarsInDay());




        return document;
    }






    public XMLCreator(String fileNameMatlab, String fileNameCars,String Model, String type){

        try {
            matlabImport = new MatlabImport(fileNameMatlab);
            FileWriter outRou = new FileWriter(fileNameCars);
            Document RouteEqualDocument;
            if (type.equals("exp"))
                RouteEqualDocument = createRouteExpDocument(Model);
            else
                RouteEqualDocument = createRouteEqualDocument(Model);

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
