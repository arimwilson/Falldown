package com.arilwilson.seismo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;

public class SeismoViewThread extends Thread {
  public SeismoViewThread(SurfaceHolder holder, boolean filter, int axis,
                          int period) {
    holder_ = holder;
    setFilter(filter);
    setAxis(axis);
    period_ = period;
  }

  @Override
  public void run() {
    while (running_) {
      synchronized (holder_) {
        Canvas canvas = holder_.lockCanvas();
        canvas.drawARGB(255, 255, 255, 255);

        Paint scale_paint = new Paint();
        scale_paint.setARGB(255, 137, 137, 137);
        scale_paint.setStrokeWidth(canvas_width_ / 300f);
        scale_paint.setAntiAlias(true);
        float text_size = canvas_width_ / 35f;
        scale_paint.setTextSize(text_size);
        
        // Draw g scale.
        scale_paint.setTextAlign(Paint.Align.CENTER);
        for (int i = -MAX_G + 1; i <= MAX_G - 1; ++i) {
          float x = canvas_width_ / 2 * (1 + (float)i / MAX_G);
          canvas.drawLine(x, 0, x, canvas_height_ / 20, scale_paint);
          canvas.drawText(Integer.toString(i) + "g", x,
                          canvas_height_ / 20 + 1.2f * text_size,
                          scale_paint);
        }

        // Draw time scale in seconds.
        scale_paint.setTextAlign(Paint.Align.LEFT);
        int max_second = time_ * period_ / 1000;
        int min_second = (int) Math.ceil((time_ - canvas_height_) * period_ /
                                         1000);
        for (int s = max_second; s >= 0 && s > min_second; --s) {
          float y = s * 1000 / period_ - time_ + canvas_height_;
          canvas.drawLine(0, y, canvas_width_ / 20, y, scale_paint);
          canvas.drawText(Integer.toString(s) + "s",
                          canvas_width_ / 20 + 0.2f * text_size,
                          y + 0.5f * text_size, scale_paint);
        }


        // Draw line.
        float[] pts = new float[(history_.size() - start_.nextIndex()) * 4];
        int i = 0;
        for (ListIterator<ArrayList<Float>> index = start_; index.hasNext();) {
          ArrayList<Float> history = index.next();
          pts[i] = canvas_width_ / 2 *
                       (1 + history.get(axis_) / MAX_ACCELERATION);
          pts[i + 1] = i - 1;
          pts[i + 2] = canvas_width_ / 2 *
                           (1 + history.get(axis_) / MAX_ACCELERATION);
          pts[i + 3] = i;
          i += 4;
        }
        Paint line_paint = new Paint();
        line_paint.setARGB(255, 0, 0, 0);
        line_paint.setStrokeWidth(canvas_width_ / 300f);
        line_paint.setAntiAlias(false);
        canvas.drawLines(pts, line_paint);
        holder_.unlockCanvasAndPost(canvas);
      }
      try {
        Thread.sleep(period_);
      } catch (Exception e) {
        // Do nothing.
      }
    }
  }

  public void update(float x, float y, float z) {
    synchronized (holder_) {
      ArrayList<Float> acceleration = new ArrayList<Float>(3);
      if (filter_) {
        filter_acceleration_[0] = x * FILTERING_FACTOR +
                           filter_acceleration_[0] * (1.0f - FILTERING_FACTOR);
        acceleration.add(x - filter_acceleration_[0]);
        filter_acceleration_[1] = y * FILTERING_FACTOR +
                           filter_acceleration_[1] * (1.0f - FILTERING_FACTOR);
        acceleration.add(y - filter_acceleration_[1]);
        filter_acceleration_[2] = z * FILTERING_FACTOR +
                           filter_acceleration_[2] * (1.0f - FILTERING_FACTOR);
        acceleration.add(z - filter_acceleration_[2]);
      } else {
        acceleration.add(x);
        acceleration.add(y);
        acceleration.add(z);
      }
      history_.add(acceleration);
      if (history_.size() > canvas_height_) {
        start_.next();
      }
      if (history_.size() > SECONDS_TO_SAVE * 1000 / period_) {
        history_.removeFirst();
      }
      ++time_;
    }
  }

  public void setSurfaceSize(int canvas_width, int canvas_height) {
    synchronized (holder_) {
      canvas_width_ = canvas_width;
      canvas_height_ = canvas_height;
      history_ = new LinkedList<ArrayList<Float>>();
      start_ = history_.listIterator();
      time_ = 0;
    }
  }

  public void setRunning(boolean running) {
    running_ = running;
  }

  public void save() {
    // TODO(ariw): Save!
  }

  public void setFilter(boolean filter) {
    filter_ = filter;
  }
  
  public void setAxis(int axis) {
    axis_ = axis;
  }

  private static final int MAX_G = 3;
  private static final float MAX_ACCELERATION = MAX_G * 9.807f;
  private static final float FILTERING_FACTOR = 0.1f;
  private static final int SECONDS_TO_SAVE = 60;

  private LinkedList<ArrayList<Float>> history_ =
      new LinkedList<ArrayList<Float>>();
  private ListIterator<ArrayList<Float>> start_ = history_.listIterator();
  private float[] filter_acceleration_ = new float[3];
  private int time_ = 0;
  private int canvas_height_ = 1;
  private int canvas_width_ = 1;
  private boolean running_;
  private boolean filter_;
  private int axis_ = 2;
  private SurfaceHolder holder_;
  private int period_;
}
