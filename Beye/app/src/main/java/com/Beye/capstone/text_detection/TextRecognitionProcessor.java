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

import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.util.Log;

import com.Beye.capstone.BusNumActivity;
import com.Beye.capstone.others.FrameMetadata;
import com.Beye.capstone.others.GraphicOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

//import com.ajeetkumar.textdetectionusingmlkit.others.VisionProcessorBase;

/**
 * Processor for the text recognition demo.
 */
public class TextRecognitionProcessor {

	private static final String TAG = "TextRecProc";

	private final FirebaseVisionTextRecognizer detector;
	TextToSpeech tts;

	// Whether we should ignore process(). This is usually caused by feeding input data faster than
	// the model can handle.
	private final AtomicBoolean shouldThrottle = new AtomicBoolean(false);

	public TextRecognitionProcessor() {
		detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
	}



	//region ----- Exposed Methods -----


	public void stop() {
		try {
			detector.close();
		} catch (IOException e) {
			Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
		}
	}


	public void process(ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay) throws FirebaseMLException {

		if (shouldThrottle.get()) {
			return;
		}
		FirebaseVisionImageMetadata metadata =
				new FirebaseVisionImageMetadata.Builder()
						.setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
						.setWidth(frameMetadata.getWidth())
						.setHeight(frameMetadata.getHeight())
						.setRotation(frameMetadata.getRotation())
						.build();

		detectInVisionImage(FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata, graphicOverlay);
	}

	//endregion

	//region ----- Helper Methods -----

	protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
		return detector.processImage(image);
	}


	protected void onSuccess(@NonNull FirebaseVisionText results, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {

		BusNumActivity bus = new BusNumActivity();
		boolean hancheck=false;
		char[] temp = bus.getBusNum().toCharArray();
		int check = Character.getType(temp[0]);
		if(check==5){
			hancheck=true;
		}
		graphicOverlay.clear();
		String resultText = results.getText();
		for (FirebaseVisionText.TextBlock block: results.getTextBlocks()) {
			for (FirebaseVisionText.Line line: block.getLines()) {
				for (FirebaseVisionText.Element element: line.getElements()) {
					String elementText = element.getText();
					TextGraphic textGraphic = new TextGraphic(graphicOverlay, element);
					double w = textGraphic.getWidth();
					double h = textGraphic.getHeight();

					if(w>150 || h>80) {

						if (hancheck == true) {
							if (bus.getBusNum().contains(elementText)) {
								BusNumActivity.tts.speak("전방에" + bus.getBusNum() + "번 버스입니다.", TextToSpeech.QUEUE_FLUSH, null);

								try {
									Thread.sleep(3000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						} else if (hancheck == false) {
							if (elementText.contains(bus.getBusNum())) {
								BusNumActivity.tts.speak("전방에" + bus.getBusNum() + "번 버스입니다.", TextToSpeech.QUEUE_FLUSH, null);

								try {
									Thread.sleep(3000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
		List<FirebaseVisionText.TextBlock> blocks = results.getTextBlocks();
		for (int i = 0; i < blocks.size(); i++) {
			List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
			for (int j = 0; j < lines.size(); j++) {
				List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
				for (int k = 0; k < elements.size(); k++) {
					GraphicOverlay.Graphic textGraphic = new TextGraphic(graphicOverlay, elements.get(k));
					graphicOverlay.add(textGraphic);


				}
			}
		}
	}

	protected void onFailure(@NonNull Exception e) {
		Log.w(TAG, "Text detection failed." + e);
	}

	private void detectInVisionImage(FirebaseVisionImage image, final FrameMetadata metadata, final GraphicOverlay graphicOverlay) {

		detectInImage(image)
				.addOnSuccessListener(
						new OnSuccessListener<FirebaseVisionText>() {
							@Override
							public void onSuccess(FirebaseVisionText results) {
								shouldThrottle.set(false);
								TextRecognitionProcessor.this.onSuccess(results, metadata, graphicOverlay);
							}
						})
				.addOnFailureListener(
						new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								shouldThrottle.set(false);
								TextRecognitionProcessor.this.onFailure(e);
							}
						});
		// Begin throttling until this frame of input has been processed, either in onSuccess or
		// onFailure.
		shouldThrottle.set(true);
	}

	//endregion


}
