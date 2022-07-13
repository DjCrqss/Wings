import java.util.*;
import processing.sound.*;

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
void setup(){
  // set up framerate and screen
  size(1088, 760);
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

void draw(){
  
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
    image(plane, width/2 +(mouseX - width/2)/20, 2.0/5.0 * height +(mouseY - height/2)/20);
    
    // draw buttons
    fill(50, 50, 50);
    noStroke();
    rect(width*0.1 +(mouseX - width/2)/200, height*0.7, width*0.45, 100, 20);
    rect(width * 0.65 +(mouseX - width/2)/200, height*0.7, width*0.25, 100, 20);
    
    // draw text
    fill(255);
    textSize(46);
    textAlign(CENTER);
    text("Play " + diffTypes[difficulty], width*0.325 +(mouseX - width/2)/200, height*0.7+65);
    text(diffTypes[(difficulty+1)%2], width*0.775 +(mouseX - width/2)/200, height*0.7+65);
    
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
    rect(width*0.1, height*0.175, width*0.45, height*0.4, 20);
    rect(width*0.65, height*0.175, width*0.25, height*0.4, 20);
    textSize(46);
    fill(255);
    text("Good flight", width*0.325, height*0.3 - 15);
    text("Scores", width*0.775, height*0.3 - 15);
    textSize(108);
    text(score, width*0.325, height*0.475);
    
    // draw high scores
    for(int i=0; i<3; i++){
      textSize(28 + (3-i)*10);
      if(i < sortedScores.size()){
        text(sortedScores.get(sortedScores.size()-i-1), width*0.775, height*0.375 + 60*i);
      } else {
        text("--", width*0.775, height*0.39 + 60*i);
      }
    }
    
    // draw buttons
    fill(50, 50, 50);
    rect(width*0.1, height*0.7, width*0.25, 100, 20);
    rect(width * 0.45, height*0.7, width*0.45, 100, 20);
    // draw button text
    fill(255);
    textSize(46);
    textAlign(CENTER);
    text("Home", width*0.225, height*0.7+65);
    text("Fly again", width*0.675, height*0.7+65);
    
  }
  
}


// setting up game method
void setupGame(){
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
void moveObjects(){
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
void checkCollisions(){
  if(c1.active){
    c1.checkCollision(p1);
  }
  alive = !p1.checkCollisions();
  if(!alive){ 
    mode = "singleScores";
  }
}

// draw all the graphics when running
void drawGraphics(){
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
void mouseClicked(){
  // on the home screen
  if(mode.equals("home")){
      // if clicking play 
      if(mouseX > width*0.1 && mouseX < width*0.55
       && mouseY > height * 0.7 && mouseY < height * 0.7 + 100 ){
          setupGame();
      }
      // else clicking mode switch
      if(mouseX > width*0.65 && mouseX < width*0.9
       && mouseY > height * 0.7 && mouseY < height * 0.7 + 100 ){
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
      if(mouseX > width*0.1 && mouseX < width*0.35
       && mouseY > height * 0.7 && mouseY < height * 0.7 + 100 ){
         // set mode to home and enable wind sound if toggled
         mode = "home";
         if(music){
           wind.loop();
         }
      }
      // else restarting
      if(mouseX > width*0.45 && mouseX < width*0.9
       && mouseY > height * 0.7 && mouseY < height * 0.7 + 100 ){
         // reset game
          setupGame();
      }
  
  }

}

// key handling
void keyPressed(){
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

void keyReleased(){
  if(mode.equals("singlePlay")){
    if(p1.flyUp){
      p1.flyUp = false;
    }
  }
}


// custom threads to load music
// load default music
void loadMusic(){
    // load files
    normal = new SoundFile(this, "audio/normal.wav");
    wind = new SoundFile(this, "audio/wind.wav");
    normal.loop();
    wind.loop();
    // slowly fade in the sound
    normal.amp(0.01);
    wind.amp(0.01);
    for(float i=0.01; i<1; i+= 0.05){
      normal.amp(i);
      wind.amp(i);
      delay(100);
    }
}
// load hard mode music
void loadMusicHard(){
  hard = new SoundFile(this, "audio/hard.wav");
  // load sound effects
  collect = new SoundFile(this, "audio/coin.wav");
  splash = new SoundFile(this, "audio/splash.wav");
  crash = new SoundFile(this, "audio/crash.wav");
  loadedMusic = true;
}
   
