public class Boat{
  // variables
  private float x, y;  
  private PImage img, img2;
  
  // constructor to load image and position
  public Boat(){
    img = loadImage("img/boat.png");
    img2 = loadImage("img/boatHard.png");
    x = width - 150;
    y = 70;
  }
  
  public void drawScore(){
    // translate and rotate boat
    translate(x, height-y);
    rotate(-sin((time+200)/25)/(4*PI));
    // draw boat and score
    if(difficulty == 0){
      image(img, 0, 0);
      fill(20);
    } else {
      image(img2, 0, -55);
      fill(180, 60, 60);
    }
    
    
    textSize(35);
    text(score, 0, 20);
    // change y position using a sine wave
    y = 50 + 10*sin(time/25);
    resetMatrix();
  }
}
