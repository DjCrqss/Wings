public class Plane{
  // variables
  public boolean flyUp;
  private float x, y, yVelo;
  private int nextPipe = 0;
  private PImage img;
  private float planeSize = 10;
  
  // plane constructor
  public Plane(){
    // load plane image and set drawing image to center
    img = loadImage("img/p1.png");
    imageMode(CENTER);
  }
  
  // reset plane before game with new coordinates
  public void resetPlane(float startX, float startY){
    x = startX;
    y = startY; 
    yVelo = 0;
    flyUp = false;
  }
  
  // return y position
  public float getY(){
    return y;
  }
  
  // return angle of plane for trail
  public float getAngle(){
    return - (yVelo / (4 *PI));
  }
  
  // draw plane graphics
  public void drawPlane(){
    fill(100,100,100);
    // rotate plane based on speed then draw
    translate(x+10, height-y);
    rotate(- (yVelo / (4 *PI)));
    image(img, 0, 0);
    resetMatrix();
  }
  
  // change plane's velocity
  public void flyPlane(){
    // if space bar held, slowly increase the velocity
    if(flyUp){
      yVelo += 0.3;
      // set max velocity
      if(yVelo > 15){
        yVelo = 15;
      }
    } 
    // otherwise, slowly decrease velo
    else {
      yVelo -= 0.3;
      if(yVelo < -15){
        yVelo = -15;
      }
    }
    // smoothly minimise flying at the top of the screen
    if(height - y -(2*planeSize)< 120 && yVelo > (height-(2*planeSize)-y)/8){
      yVelo = (height-y-(2*planeSize))/8;
    }
    // change y value vy velocity
    y += yVelo;
  }
  
  // check collisions
  public boolean checkCollisions(){
    // if in the water
    if(y<0){
      // play sound effect and return true
      splash.play();
      return true;
    }
    
    // check if colliding with closest pipe on screen
    if(pipes[nextPipe%pipes.length].getX() < x){
      // check if collides (120 is gapWidth, 25 is strokeWidth
      if( y + planeSize  > pipes[nextPipe%pipes.length].getY()+(gap/2) -25  
        || y - planeSize  < pipes[nextPipe%pipes.length].getY()-(gap/2) +20 ){
           crash.play();
           return true;
       }
      // increment to next pipe and increase score
      nextPipe++;
      score++;
      // change type of pipes in hardMode every 10 points
      if(score%10==0){
        pipeMode = (pipeMode + 1)%3;
      }
    }   
    // else the plane is safe
    return false;
  }  

}
