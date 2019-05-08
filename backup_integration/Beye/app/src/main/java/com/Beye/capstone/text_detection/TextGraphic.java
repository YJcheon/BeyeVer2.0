// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.Beye.capstone.text_detection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.Beye.capstone.others.GraphicOverlay;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
public class TextGraphic extends GraphicOverlay.Graphic {

  private static final int TEXT_COLOR = Color.WHITE;
  private static final float TEXT_SIZE = 54.0f;
  private static final float STROKE_WIDTH = 4.0f;

  private final Paint rectPaint;
  private final Paint textPaint;
  private final FirebaseVisionText.Element text;
  private final RectF rect;

  TextGraphic(GraphicOverlay overlay, FirebaseVisionText.Element text) {
    super(overlay);

    this.text = text;
    rect = new RectF(text.getBoundingBox());
    rectPaint = new Paint();
    rectPaint.setColor(TEXT_COLOR);
    rectPaint.setStyle(Paint.Style.STROKE);
    rectPaint.setStrokeWidth(STROKE_WIDTH);

    textPaint = new Paint();
    textPaint.setColor(TEXT_COLOR);
    textPaint.setTextSize(TEXT_SIZE);
    // Redraw the overlay, as this graphic has been added.
    postInvalidate();
  }

  /** Draws the text block annotations for position, size, and raw value on the supplied canvas. */
  @Override
  public void draw(Canvas canvas) {
    if (text == null) {
      throw new IllegalStateException("Attempting to draw a null text.");
    }

    // Draws the bounding box around the TextBlock.


    canvas.drawRect(rect, rectPaint);
    // Renders the text at the bottom of the box.
    canvas.drawText(text.getText(), rect.left, rect.bottom, textPaint);
  }

  public double getWidth() {
    return translateX(rect.right) - translateX(rect.left);
  }

  public double getHeight() {
    return translateY(rect.bottom) - translateY(rect.top);
  }
}
