
//import java.security.InvalidParameterException;

/**
 * Created by Chris on 4/15/2015.
 */
public class DrawnPoint {

    public int mX;
    public int mY;
    public int mColor;

    public DrawnPoint(int x, int y, int color){
        mX = x;
        mY = y;
        mColor = color;
    }

    public DrawnPoint(String point){
        String [] data = point.split(",");
        if(data.length != 3){
            mX = 0;
            mY = 0;
            mColor = 0;
            return;
        }
        mX = Integer.parseInt(data[0].substring(1));
        mY = Integer.parseInt(data[1]);
        mColor = Integer.parseInt(data[2].substring(0, data[2].length()-1));
    }

    @Override
    public String toString(){
        return "{" + mX + "," + mY + "," + mColor + "}";
    }

    @Override
    public boolean equals(Object o){
        if(o.getClass() != DrawnPoint.class)
            return false;
        DrawnPoint p = (DrawnPoint) o;
        return mX == p.mX && mY == p.mY;
    }
}
