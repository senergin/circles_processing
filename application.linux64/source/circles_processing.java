import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class circles_processing extends PApplet {

public class Tuple3F { 
  public float first; 
  public float second;
  public float third;

  public String toString() {
    return first + ", " + second + ", " + third;
  }
}

public class Tuple3<T1, T2, T3> { 
  public T1 first; 
  public T2 second;
  public T3 third;

  public String toString() {
    return first + ", " + second + ", " + third;
  }
}

public class RandomValue { 
  float value;

  public RandomValue(float min, float max) { 
    set(min, max);
  }

  public void set(float min, float max) {
    value = random(min, max);
  }
}

class Circle {
  class Size {
    
    public float radius;
    
    final float medianMin = 80.0f;
    final float medianMax = 100.0f;
    final float deltaMin = 20.0f;
    final float deltaMax = 50.0f;
    final float sinSpeedMin = PI/2.0f;
    final float sinSpeedMax = PI;

    RandomValue radiusMedian = null;
    RandomValue radiusDelta = null;
    RandomValue sinSpeed = null;
    float angle = 0.0f;

    Size() {
      reset();
    }

    public void reset() {
      if (radiusMedian == null) {
        radiusMedian = new RandomValue(medianMin, medianMax);
      } else {
        radiusMedian.set(medianMin, medianMax);
      }

      if (radiusDelta == null) {
        radiusDelta = new RandomValue(deltaMin, deltaMax);
      } else {
        radiusDelta.set(deltaMin, deltaMax);
      }

      if (sinSpeed == null) {
        sinSpeed = new RandomValue(sinSpeedMin, sinSpeedMax);
      } else {
        sinSpeed.set(sinSpeedMin, sinSpeedMax);
      }

      update(0.0f);
    }

    public void update(float deltaSeconds) {
      angle += sinSpeed.value * deltaSeconds;
      radius = radiusMedian.value + (sin(angle) * radiusDelta.value);
    }
  }

  public class Color { 

    public Tuple3F value = new Tuple3F();
    
    final float medianeMin = 0.0f;
    final float medianMax = 255.0f;
    final float deltaMin = -512.0f;
    final float deltaMax = 512.0f;
    final float sinSpeedMin = PI;
    final float sinSpeedMax = 2 * PI;

    Tuple3<RandomValue, RandomValue, RandomValue> colorMedian = null;
    Tuple3<RandomValue, RandomValue, RandomValue> colorDelta = null;
    RandomValue sinSpeed = null;
    float angle = 0.0f;

    public Color() { 
      reset();
    }

    public void reset() {
      if (colorMedian == null) {
        colorMedian = new Tuple3<RandomValue, RandomValue, RandomValue>();
        colorMedian.first = new RandomValue(medianeMin, medianMax);
        colorMedian.second = new RandomValue(medianeMin, medianMax);
        colorMedian.third = new RandomValue(medianeMin, medianMax);
      } else {
        colorMedian.first.set(medianeMin, medianMax);
        colorMedian.second.set(medianeMin, medianMax);
        colorMedian.third.set(medianeMin, medianMax);
      }

      if (colorDelta == null) {
        colorDelta = new Tuple3<RandomValue, RandomValue, RandomValue>();
        colorDelta.first = new RandomValue(deltaMin, deltaMax);
        colorDelta.second = new RandomValue(deltaMin, deltaMax);
        colorDelta.third = new RandomValue(deltaMin, deltaMax);
      } else {
        colorDelta.first.set(deltaMin, deltaMax);
        colorDelta.second.set(deltaMin, deltaMax);
        colorDelta.third.set(deltaMin, deltaMax);
      }
      
      if (sinSpeed == null) {
        sinSpeed = new RandomValue(sinSpeedMin, sinSpeedMax);
      } else {
        sinSpeed.set(sinSpeedMin, sinSpeedMax);
      }

      update(random(0));
    }

    public void update(float deltaSeconds) {
      angle += sinSpeed.value * deltaSeconds;
      float ratio = sin(angle);
      value.first = colorMedian.first.value + (ratio * colorDelta.first.value);
      value.second = colorMedian.second.value + (ratio * colorDelta.second.value);
      value.third = colorMedian.third.value + (ratio * colorDelta.third.value);
    }
  }

  Size size = new Size();
  Color ccolor = new Color();
  RandomValue moveSpeedPerSecond = new RandomValue(100.0f, 400.0f);
  RandomValue drawDistance = new RandomValue(5.0f, 10.0f);
  PVector center;
  PVector direction;
  float remainderDelta;
  boolean isColor = true;

  public Circle() {
    reset();
  }
  
  public void toggleReset() {
    isColor = !isColor;
    reset();
  }
  
  public void reset() {
    resetSize();
    resetColor();
    resetCenter();
    resetDirection();
  }

  public void draw(float deltaTimeSeconds) {
    float frameDeltaMag = (moveSpeedPerSecond.value * deltaTimeSeconds) + remainderDelta;
    int drawCount = (int)(frameDeltaMag / drawDistance.value);
    while (drawDistance.value < frameDeltaMag) {
      float drawDeltaSeconds = deltaTimeSeconds / drawCount;

      size.update(drawDeltaSeconds);
      ccolor.update(drawDeltaSeconds);

      updateDrawState(center, direction, drawDistance.value);

      smooth();
      hint(DISABLE_DEPTH_TEST);

      if (isColor) {
        fill(ccolor.value.first, ccolor.value.second, ccolor.value.third);
      } else {
        fill(255);
      }
      ellipse(center.x, center.y, size.radius * 1.0f, size.radius * 1.0f);

      fill(255, 0);
      ellipse(center.x, center.y, size.radius * 0.8f, size.radius * 0.8f);

      frameDeltaMag -= drawDistance.value;
    }

    remainderDelta = frameDeltaMag;
  }

  public void resetSize() {
    size.reset();
  }

  public void resetColor() {
    ccolor.reset();
  }

  public void resetCenter() {
    center = new PVector((random(-1, 1) * width / 2) + (width / 2), (random(-1, 1) * height / 2) + (height / 2));
  }

  public void resetDirection() {
    direction = PVector.random2D();
  }

  public void updateDrawState(PVector pos, PVector dir, float deltaX) {
    PVector frameDelta = PVector.mult(dir, deltaX);
    PVector nextPosition = PVector.add(pos, frameDelta);
    while (nextPosition.x < 0 || nextPosition.x > width || nextPosition.y < 0 || nextPosition.y > height) {
      if (nextPosition.x < 0) {
        nextPosition.x = -nextPosition.x;
        dir.x = -dir.x;
      } else if (nextPosition.x > width) {
        nextPosition.x = (2 * width) - nextPosition.x;
        dir.x = -dir.x;
      } else if (nextPosition.y < 0) {
        nextPosition.y = -nextPosition.y;
        dir.y = -dir.y;
      } else if (nextPosition.y > height) {
        nextPosition.y = (2 * height) - nextPosition.y;
        dir.y = -dir.y;
      }
    }

    center = nextPosition;
    direction = dir;
  }
}

int framesPerSecond = 60;
int lastTimeMillis;
float deltaTimeSecond;
int circleCount = 20;
Circle[] circles = new Circle[circleCount];

public void setup() {
  lastTimeMillis = millis();
  deltaTimeSecond = 0.0f;

  frameRate(framesPerSecond);

  //fullScreen();
  
  surface.setResizable(true);

  for (int i = 0; i < circles.length; ++i) {
    circles[i] = new Circle();
  }
}

public void mouseReleased() {
  resetView();
}

public void draw() {
  updateTime();

  for (int i = 0; i < circles.length; ++i) {
    circles[i].draw(deltaTimeSecond);
  }
}

public void updateTime() {
  int time = millis();
  deltaTimeSecond = (time - lastTimeMillis) / 1000.0f;
  lastTimeMillis = time;
}

public void resetView() {
  background(200, 200, 200);
  for (int i = 0; i < circles.length; ++i) {
    circles[i].toggleReset();
  }
}
  public void settings() {  size(1280, 960); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "circles_processing" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
