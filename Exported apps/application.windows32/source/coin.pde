public class Coin{
  // variables
  private float x = -10;
  private float y = -10;
  boolean active = false;
  float value;
  private PImage bronze, silver, gold;
  
  // load images on initialisation
  public Coin(){
    bronze = loadImage("img/bronze.png");
    silver = loadImage("img/silver.png");
    gold = loadImage("img/gold.png");
  }
  
  // generate a coin with a random value
  public void createCoin(){
    active = true;
    x = width + 20;
    y = height/2;
    value = (int)random(9);
  }
  
  // reset coin offscreen
  public void resetCoin(){
    x = -10;
    active = false;
  }
  
  // draw coin image with outline
  public void drawCoin(){
    noFill();
    circle(x, height-y, 30);
    if(value <= 4){
      image(bronze, x, height-y);
    } else if(value <= 7){
      image(silver, x, height-y);
    } else {
      image(gold, x, height-y);
    }
    
  }
  
  // shift coin along screen
  public void moveCoin(){
    x -= 4;
    y = height/2 + 200*sin(x/200);
  }
  
  // check if touching plane
  public void checkCollision(Plane p1){
    // if within the range of the plane and the difference in y < 50
    if(x < 200 && x > 110 && abs(y - p1.getY()) < 50 && active){
      // add value of coin to score and disable coin
      collect.play();
      if(value <= 4){
        score += 3;
      } else if(value <= 7){
        score += 5;
      } else {
        score += 8;
      }
      active = false;
    }
    // disable coin offscreen
    else if(x < 0){
        active = false;
    }
  
  }
  
}
