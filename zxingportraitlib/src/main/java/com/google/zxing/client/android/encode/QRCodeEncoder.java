/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.encode;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.common.BitMatrix;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

/**
 * This class does the work of decoding the user's request and extracting all
 * the data to be encoded in a barcode.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class QRCodeEncoder {

    private static final String TAG = QRCodeEncoder.class.getSimpleName();

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private final Context activity;
    private String contents;
    private String displayContents;
    private BarcodeFormat format;
    private final int dimension;
    private final boolean useVCard;

    public QRCodeEncoder(Context activity, Intent intent, int dimension, boolean useVCard) throws WriterException {
        this.activity = activity;
        this.dimension = dimension;
        this.useVCard = useVCard;
        String action = intent.getAction();
        if (Intents.Encode.ACTION.equals(action)) {
            encodeContentsFromZXingIntent(intent);
        } else if (Intent.ACTION_SEND.equals(action)) {
            encodeContentsFromShareIntent(intent);
        }
    }

    String getContents() {
        return contents;
    }

    String getDisplayContents() {
        return displayContents;
    }

    boolean isUseVCard() {
        return useVCard;
    }

    // It would be nice if the string encoding lived in the core ZXing library,
    // but we use platform specific code like PhoneNumberUtils, so it can't.
    private void encodeContentsFromZXingIntent(Intent intent) {
        // Default to QR_CODE if no format given.
        String formatString = intent.getStringExtra(Intents.Encode.FORMAT);
        format = null;
        if (formatString != null) {
            try {
                format = BarcodeFormat.valueOf(formatString);
            } catch (IllegalArgumentException iae) {
                // Ignore it then
            }
        }
        if (format == null || format == BarcodeFormat.QR_CODE) {
            String type = intent.getStringExtra(Intents.Encode.TYPE);
            if (type != null && !type.isEmpty()) {
                this.format = BarcodeFormat.QR_CODE;
                encodeQRCodeContents(intent, type);
            }
        } else {
            String data = intent.getStringExtra(Intents.Encode.DATA);
            if (data != null && !data.isEmpty()) {
                contents = data;
                displayContents = data;
            }
        }
    }

    // Handles send intents from multitude of Android applications
    private void encodeContentsFromShareIntent(Intent intent) throws WriterException {
        // Check if this is a plain text encoding, or contact
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
        } else {
            encodeFromTextExtras(intent);
        }
    }

    private void encodeFromTextExtras(Intent intent) throws WriterException {
        // We only do QR code.
        format = BarcodeFormat.QR_CODE;
        if (intent.hasExtra(Intent.EXTRA_SUBJECT)) {
            displayContents = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        } else if (intent.hasExtra(Intent.EXTRA_TITLE)) {
            displayContents = intent.getStringExtra(Intent.EXTRA_TITLE);
        } else {
            displayContents = contents;
        }
    }

    private void encodeQRCodeContents(Intent intent, String type) {
        if (type.equals(Contents.Type.TEXT)) {
            String textData = intent.getStringExtra(Intents.Encode.DATA);
            if (textData != null && !textData.isEmpty()) {
                contents = textData;
                displayContents = textData;
            }

        }
    }

    private static List<String> toList(String[] values) {
        return values == null ? null : Arrays.asList(values);
    }

    public Bitmap encodeAsBitmap() throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(contentsToEncode, format, dimension, dimension, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

}
