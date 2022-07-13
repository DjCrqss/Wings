public class Trail{
  // variables
  private float xInitial, x, y, angle;
  
  // constructor
  public Trail(float startX){
    xInitial = startX - 20;
    x = -10;
    y = -10;
  }

  // method to reset trail to the start inside the plane to look continuous
  public void reset(Plane curPlane){
    angle = curPlane.getAngle();
    x = xInitial + 15*cos(angle);
    y = curPlane.getY() + 15*sin(angle);
  }
  
  // draw trail with the correct angle
  public void drawTrail(){
    // white line
    stroke(255, 255, 255, x * 2);
    strokeWeight(10);
    line(x, height-y, x - 15*cos(angle), height-y - 15*sin(angle));
  }
  
  // shift trail off screen
  public void move(){
    x -= 6;
  }
}
