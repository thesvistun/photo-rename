package name.svistun.picture;

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

import name.svistun.picture.type.Exif;
import name.svistun.picture.type.Mov;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.drew.imaging.ImageProcessingException;

public final class PictureFactory {
  public static final List<String> EXIF_EXTS = Arrays.asList("nef", "jpg");
  public static final List<String> MOV_EXTS = Arrays.asList("mov");
  
  private PictureFactory() {}
  
  public static Picture getPicture(File pictureFile) throws IOException,
      ImageProcessingException, NotImageFileException
  {
    String pictureFileName = pictureFile.getName();
    String pictureFileExt = pictureFileName.substring(pictureFileName.lastIndexOf('.') + 1).toLowerCase();
    if (EXIF_EXTS.contains(pictureFileExt)) {
      return new Exif(pictureFile);
    } else if (MOV_EXTS.contains(pictureFileExt)) {
      return new Mov(pictureFile);
    }
    return null;
  }
}
