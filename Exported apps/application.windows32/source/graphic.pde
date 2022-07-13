public class Graphic{
  // graphic class is for water and clouds and sun
  private int id;
  private PImage img, img2; // img2 is for hard mode secondary graphics
  private float x, y;
  private float angle;
  
  // constructor
  public Graphic(int number){
    imageMode(CENTER);
    id = number;
    // load img
    if(id < 2){ // is water
      img = loadImage("img/water.png");
      img2 = loadImage("img/waterHard.png");
      y = height-50;
      x = width/2 + width*id;
    }
    else if(id == 2){ // is sun
      img = loadImage("img/sun.png");
      img2 = loadImage("img/moon.png");
      y = 80;
      x = (3.0/4.0) * width;
    }
    else if(id == 3){ // is cliff
      img = loadImage("img/cliff.png");
      y = (3.0/4.0) * height;
    }
    else{ // choose a cloud based on ID
      if(id < 6){
        img = loadImage("img/cloud3.png");
      } else if(id < 8){
        img = loadImage("img/cloud2.png");
      } else {
        img = loadImage("img/cloud1.png");
      }
      x = width + 210 + (id-6)*(width/2);
      y = random(50, (3.0/4.0) * height);
    }
  }
  
  // draw the graphic
  public void drawGraphic(){
    if(id == 2){ // sun
      translate(x, y);
      if(difficulty == 0){
        rotate(angle);
        image(img, 0, 0);
      } else {
        image(img2, 0, 0);
      }
      
      resetMatrix();
      
    } 
    else if(id < 2){ // water
      if(difficulty == 0){
        image(img, x, y);
      } else {
        image(img2, x, y);
      }
    }
    else if(id != 3 || time < 50){ // else cloud or other element that sidescrolls
      image(img, x, y);
    }
  }
  
  // move graphic along
  public void move(){
    // if water then move fastest
    if(id < 2){
      x -= 7;
      if(x < -(width/2)){
        x = 1.5*width;
      }
    }
    // else if cloud, move by ID 
    else if(id > 3){
        x -= id/2;
        if(x < -210){
          x = width + 210;
          y = random(100, (2.0/3.0) * height);
        }
    }
    // else if sun then rotate
    else if(id == 2){
      if(difficulty == 0){
        angle+=0.001;
      }
    }
    // otherwise cliff
    else if(time < 50){ 
      x -= 3;
    }
  }
  
  // reset cliff when restarting game
  public void resetCliff(){
   x = 80;
  }
  
}
