import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.*; 
import processing.sound.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Wings extends PApplet {




// audio variables
boolean music = true; // set false if marking without speakers
boolean loadedMusic = false;
SoundFile normal, hard, wind, collect, crash, splash;

// objects
Plane p1;
Pipe[] pipes = new Pipe[3]; // reuse pipes rather than create new for performance
Graphic[] graphics = new Graphic[10]; // 0+1 are water, 2 is sun, 3, is cliff 4+5+6+7+8+9 are clouds
Trail[] trails = new Trail[4]; // reusable trail objects
Coin c1;
Boat b1;

// variables for running game
String mode = "home";
int difficulty = 0;
String[] diffTypes = {"Normal", "Hard"};
boolean alive = false;
boolean running = false;
int score;
float time;
int pipeMode = 0;
int gap = 250;
int curTrail = 0;

// scores
ArrayList<Integer> normScores = new ArrayList<Integer>();
ArrayList<Integer> hardScores = new ArrayList<Integer>();
ArrayList<Integer> sortedScores;

// images
private PImage homeNorm, homeHard, plane, deathScreen, cursor;

// set up
public void setup(){
  // set up framerate and screen
  
  frameRate(60);
  
  // load local images
  homeNorm = loadImage("img/homeNorm.png");
  homeHard = loadImage("img/homeHard.png");
  plane = loadImage("img/plane.png");
  cursor = loadImage("img/cursor.png");
  cursor(cursor);
  
  // audio
  if(music){
    // load multiple threads in the background so game does not lag
    thread("loadMusicHard");
    thread("loadMusic");
  }
  
  // create and load objects into memory
  p1 = new Plane();
  for(int i = 0; i<pipes.length; i++){
    pipes[i] = new Pipe(0, 0);
  }
  for(int i = 0; i<graphics.length; i++){
      graphics[i] = new Graphic(i);
  }
  for(int i = 0; i<trails.length; i++){
     trails[i] = new Trail(150);
  }
  c1 = new Coin();
  b1 = new Boat();
  
}

public void draw(){
  
  // ------------ home screen ------------
  if(mode.equals("home")){
    cursor(cursor);
    // draw background
    if(difficulty == 0){
      image(homeNorm, width/2, height/2);
    } else {
      image(homeHard, width/2, height/2);
    }
    
    // draw plane
    image(plane, width/2 +(mouseX - width/2)/20, 2.0f/5.0f * height +(mouseY - height/2)/20);
    
    // draw buttons
    fill(50, 50, 50);
    noStroke();
    rect(width*0.1f +(mouseX - width/2)/200, height*0.7f, width*0.45f, 100, 20);
    rect(width * 0.65f +(mouseX - width/2)/200, height*0.7f, width*0.25f, 100, 20);
    
    // draw text
    fill(255);
    textSize(46);
    textAlign(CENTER);
    text("Play " + diffTypes[difficulty], width*0.325f +(mouseX - width/2)/200, height*0.7f+65);
    text(diffTypes[(difficulty+1)%2], width*0.775f +(mouseX - width/2)/200, height*0.7f+65);
    
  }
  
  // ------------ single player ------------
  else if(mode.equals("singlePlay")){
    noCursor();
    // draw sky depending on difficulty
    if(difficulty == 0){
      background(126, 206, 204);
    } else {
      background(10, 10, 20);
    }
    
    if(running){ // code for when game is running
      // move objects
      moveObjects();
      
      // check collisions
      checkCollisions();
      
      // check adding coin
      if(time%750 == 500 && !c1.active){
        c1.createCoin();
      }
      
      // increase time
      time++;
    }
 
    // draw graphics
    drawGraphics();
   
    // draw score
    b1.drawScore();
    /*fill(255);
    textSize(54);
    text(score, 45, height-20);*/
    
    // add overlay for night mode
    if(difficulty == 1){
      noStroke();
      fill(10,10,15,100);
      rect(0, 0, width, height);
    }
    
    // death calculation
    if(!alive && running){ 
        mode = "singleScores";
        //filter(BLUR, 6);
        filter(BLUR, 6);
        deathScreen = get(0, 0, width, height);
        if(difficulty == 0){
          normScores.add(score);
          sortedScores = new ArrayList<Integer>(normScores);
        } else {
          hardScores.add(score);
          sortedScores = new ArrayList<Integer>(hardScores);
        }
        Collections.sort(sortedScores);
      }
  }
  
  // ------------ scores screen ------------
  else if(mode.equals("singleScores")){
    cursor(cursor);
    noStroke();
    //image(home, width/2, height/2);
    image(deathScreen, width/2, height/2);
    
    /*textSize(128);
    text(score, width/2, height/2);
    textSize(80);*/
    
    //draw score and medal
    fill(50, 50, 50);
    noStroke();
    rect(width*0.1f, height*0.175f, width*0.45f, height*0.4f, 20);
    rect(width*0.65f, height*0.175f, width*0.25f, height*0.4f, 20);
    textSize(46);
    fill(255);
    text("Good flight", width*0.325f, height*0.3f - 15);
    text("Scores", width*0.775f, height*0.3f - 15);
    textSize(108);
    text(score, width*0.325f, height*0.475f);
    
    // draw high scores
    for(int i=0; i<3; i++){
      textSize(28 + (3-i)*10);
      if(i < sortedScores.size()){
        text(sortedScores.get(sortedScores.size()-i-1), width*0.775f, height*0.375f + 60*i);
      } else {
        text("--", width*0.775f, height*0.39f + 60*i);
      }
    }
    
    // draw buttons
    fill(50, 50, 50);
    rect(width*0.1f, height*0.7f, width*0.25f, 100, 20);
    rect(width * 0.45f, height*0.7f, width*0.45f, 100, 20);
    // draw button text
    fill(255);
    textSize(46);
    textAlign(CENTER);
    text("Home", width*0.225f, height*0.7f+65);
    text("Fly again", width*0.675f, height*0.7f+65);
    
  }
  
}


// setting up game method
public void setupGame(){
    // reset variables
    score = 0;
    alive = true;
    running = false;
    time = 0;
    // reset plane and pipes
    pipeMode = 1;
    p1.resetPlane(150, height/2);
    for (int i=0; i<pipes.length; i++){
      pipes[i].resetPipe(gap, width + (width/pipes.length)*(i));
    }
    for(int i=0; i<trails.length; i++){
      trails[i].reset(p1);
    }
    graphics[3].resetCliff();
    c1.resetCoin();
    // finally, set mode to singlePlay
    if(music){
      wind.stop();
    }
    mode = "singlePlay";
}

// moving all objects on screen when running method
public void moveObjects(){
  p1.flyPlane(); // move plane
  for(Pipe curPipe : pipes){ // move every pipe
    curPipe.move();
  }
  for(Graphic curGraphic : graphics){ // move all background objects
    curGraphic.move();
  }
  if(time%(trails.length*2) == 0){ // move and reset trails
      trails[curTrail%trails.length].reset(p1);
      curTrail++;
    }
  for(int i=0; i<trails.length; i++){
    trails[i].move();
  }
  if(c1.active){ // move coin if active
    c1.moveCoin();
  }
}

// check plane collisions with objects 
public void checkCollisions(){
  if(c1.active){
    c1.checkCollision(p1);
  }
  alive = !p1.checkCollisions();
  if(!alive){ 
    mode = "singleScores";
  }
}

// draw all the graphics when running
public void drawGraphics(){
  for(int i=2; i<graphics.length; i++){
    graphics[i].drawGraphic();
  }
  for(int i=0; i<trails.length; i++){
    trails[i].drawTrail();
  }
  if(c1.active){
    c1.drawCoin();
  }
  p1.drawPlane();
  for(Pipe curPipe : pipes){
    curPipe.drawPipe();
  }
  for(int i=0; i<2; i++){
    graphics[i].drawGraphic();
  }
}

// mouse press handling for buttons
public void mouseClicked(){
  // on the home screen
  if(mode.equals("home")){
      // if clicking play 
      if(mouseX > width*0.1f && mouseX < width*0.55f
       && mouseY > height * 0.7f && mouseY < height * 0.7f + 100 ){
          setupGame();
      }
      // else clicking mode switch
      if(mouseX > width*0.65f && mouseX < width*0.9f
       && mouseY > height * 0.7f && mouseY < height * 0.7f + 100 ){
          // toggle difficulty
          difficulty = (difficulty+1)%2;
          // chanage music if it is enabled
          if(music && loadedMusic){
            if(difficulty == 0){
                hard.stop();
                normal.loop();
            } else {
              normal.stop();
              hard.loop();
            }
          }
      }
  }
  // on the score screen
  if(mode.equals("singleScores")){
      // if clicking to back home 
      if(mouseX > width*0.1f && mouseX < width*0.35f
       && mouseY > height * 0.7f && mouseY < height * 0.7f + 100 ){
         // set mode to home and enable wind sound if toggled
         mode = "home";
         if(music){
           wind.loop();
         }
      }
      // else restarting
      if(mouseX > width*0.45f && mouseX < width*0.9f
       && mouseY > height * 0.7f && mouseY < height * 0.7f + 100 ){
         // reset game
          setupGame();
      }
  
  }

}

// key handling
public void keyPressed(){
  // space key for plane
  if(key == ' '){
    if(mode.equals("singlePlay")){
      p1.flyUp = true;
      // start game if paused
      if(!running){
        running = true;
      }
    }
  }
}

public void keyReleased(){
  if(mode.equals("singlePlay")){
    if(p1.flyUp){
      p1.flyUp = false;
    }
  }
}


// custom threads to load music
// load default music
public void loadMusic(){
    // load files
    normal = new SoundFile(this, "audio/normal.wav");
    wind = new SoundFile(this, "audio/wind.wav");
    normal.loop();
    wind.loop();
    // slowly fade in the sound
    normal.amp(0.01f);
    wind.amp(0.01f);
    for(float i=0.01f; i<1; i+= 0.05f){
      normal.amp(i);
      wind.amp(i);
      delay(100);
    }
}
// load hard mode music
public void loadMusicHard(){
  hard = new SoundFile(this, "audio/hard.wav");
  // load sound effects
  collect = new SoundFile(this, "audio/coin.wav");
  splash = new SoundFile(this, "audio/splash.wav");
  crash = new SoundFile(this, "audio/crash.wav");
  loadedMusic = true;
}
   
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
      x = (3.0f/4.0f) * width;
    }
    else if(id == 3){ // is cliff
      img = loadImage("img/cliff.png");
      y = (3.0f/4.0f) * height;
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
      y = random(50, (3.0f/4.0f) * height);
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
        x = 1.5f*width;
      }
    }
    // else if cloud, move by ID 
    else if(id > 3){
        x -= id/2;
        if(x < -210){
          x = width + 210;
          y = random(100, (2.0f/3.0f) * height);
        }
    }
    // else if sun then rotate
    else if(id == 2){
      if(difficulty == 0){
        angle+=0.001f;
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
      yVelo += 0.3f;
      // set max velocity
      if(yVelo > 15){
        yVelo = 15;
      }
    } 
    // otherwise, slowly decrease velo
    else {
      yVelo -= 0.3f;
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
  public void settings() {  size(1088, 760); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Wings" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
