/**
 * Created by sumo on 11.3.2015.
 */
public class Car {
    public int lane;
    public double time;
    public int iTime;
    public int id;

    public Car(){}

    public Car(int lane, double time, int iTime, int id)
    {
        this.lane = lane;
        this.time = time;
        this.iTime = iTime;
        this.id = id;
   }


    public void CopyCar(Car from)
    {
       this.time = from.time;
       this.lane = from.lane;
       this.iTime = from.iTime;
       this.id = from.id;
    }
}
