package name.svistun.picture.type;

/*
 * MIT License
 *
 * Copyright (c) 2022 Aleksey Svistunov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import name.svistun.picture.Picture;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.mp4.Mp4Directory;

public final class Mp4 extends Picture {
  
  public Mp4(File videoFile) throws ImageProcessingException, IOException {
    super(videoFile);
    initDateTaken();
  }

  @Override
  protected void initDateTaken() throws ImageProcessingException, IOException {
    Date dateTaken = null;
    Metadata metadata = ImageMetadataReader.readMetadata(getPictureFile());
    // obtain the QuickTime directory
    for (Mp4Directory directory : metadata.getDirectoriesOfType(Mp4Directory.class)) {
      dateTaken = directory.getDate(Mp4Directory.TAG_CREATION_TIME);
      if (dateTaken != null) {
        break;
      }
    }
    if (null == dateTaken) {
      throw new ImageProcessingException("could not find date taken in metadate");
    }
    setDateTaken(dateTaken);
  }
  
  
}
