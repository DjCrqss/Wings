public class Pipe{
  // variables
  private float x, y, gap, topModifier, bottomModifier; // y is the middle of the opening
  private PImage img, img2, img3;
  private int mode = 1;
  private float transparency;
  //private float oldTime, newTime;
  
  // initialise pipes
  public Pipe(float gapSize, float startX){
    x = startX;
    y = random(height/3, height-(height/4));
    gap = gapSize;
    // load image
    img2 = loadImage("img/pipe.png");
    img3 = loadImage("img/pipeHard.png");
    imageMode(CENTER);
    // set type of pipe to current mode in hardMode
    mode = pipeMode;
  }
  
  // reset pipe with new variables
  public void resetPipe(float gapSize, float startX){
    x = startX + 20;
    mode = pipeMode;
    gap = gapSize;
    if(difficulty == 0){
        img = img2;
      } else {
        img = img3;
      }
  }
  
  public void drawPipe(){
    // draw top line
    stroke(142, 102, 85);
    strokeWeight(25);
    tint(255, transparency);
    // draw top pipe
    image(img, x, height-y-290-gap/2 - topModifier);
    // draw bottom pipe
    image(img, x, height-y+290+gap/2 + bottomModifier);
    
    //line(x, height, x, height-y+gap/2);
    //line(x, 0, x, height-y-gap/2);
    tint(255, 255);
  }
  
  // return X position
  public float getX(){
    return x;
  }
  
  // return Y position
  public float getY(){
    return y;
  }
  
  
  // move pipe along
  public void move(){
    x -= 6;
    /*newTime = millis();
    x -= (newTime - oldTime)/3;
    oldTime = newTime;*/
    
    if(x<-10){ // reset pipe with new position if offscreen
      x = width+10;
      y = random(height/3, height-(height/4));
      mode = pipeMode;
    }
    // change y values if in hard mode
    transparency = 255;
    if(difficulty == 1){
      if(mode == 1){ // mode 1 is pipes closing in
        if(x > width/2){
          topModifier = x - width/2;
          bottomModifier = x - width/2;
        }
      } 
      else if(mode==2){ // mode 2 is sine wave
        topModifier = 100*sin((x-150)/200);
        bottomModifier = -100*sin((x-150)/200);
      } 
      else { // mode 3 is transparent pipes fading in
        transparency = map(x, 150, width, 255, 0);
        topModifier = 0;
        bottomModifier = 0;
      }
    } 
    // otherwise, dont touch the modifiers
    else {
      topModifier = 0;
      bottomModifier = 0;
    }
  }

}
