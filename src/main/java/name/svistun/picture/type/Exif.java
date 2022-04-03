package name.svistun.picture.type;

/*
 * MIT License
 *
 * Copyright (c) 2017 Aleksey Svistunov
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

import name.svistun.picture.NotImageFileException;
import name.svistun.picture.Picture;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public final class Exif extends Picture {
  private static Map<String, String> paramFileExtToFolderNameMap;

  static {
    paramFileExtToFolderNameMap = new HashMap<>();
    paramFileExtToFolderNameMap.put("nksc", "NKSC_PARAM");
  }

  public Exif(File photoFile) throws IOException,
      ImageProcessingException,
      NotImageFileException
  {
    super(photoFile);
    init();
  }
  
  @Override
  protected void initDateTaken() throws IOException, ImageProcessingException {
    Date dateTaken = null;
    Metadata metadata = ImageMetadataReader.readMetadata(getPictureFile());
    // obtain the Exif SubIFD directory
    for (ExifSubIFDDirectory directory : metadata.getDirectoriesOfType(ExifSubIFDDirectory.class)) {
      dateTaken = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
      if (dateTaken != null) {
        break;
      }
    }
    if (null == dateTaken) {
      throw new ImageProcessingException("could not find date taken in EXIF");
    }
    setDateTaken(dateTaken);
  }

  private void init() throws IOException, ImageProcessingException, NotImageFileException {
    Matcher matcherPhoto = getPatternPictureFile().matcher(getPictureFile().getName());
    if (matcherPhoto.find()) {
      String photoFileExt = matcherPhoto.group(3);
      if (paramFileExtToFolderNameMap.containsKey(photoFileExt.toLowerCase())) {
        throw new NotImageFileException("recognized as a settings file.");
      }
      for (String paramFileExt : paramFileExtToFolderNameMap.keySet()) {
        File paramFileFolder = new File(getPictureFile().getParent()
            + File.separator + paramFileExtToFolderNameMap.get(paramFileExt));
        if (paramFileFolder.exists()) {
          File[] paramFileFolderFiles = paramFileFolder.listFiles();
          if (null == paramFileFolderFiles) {
            throw new IOException(String.format("Directory %s returned null at getting list of its files.",
                    paramFileFolder.getName()));
          } else {
            Pattern paramsFilePattern = Pattern.compile(String.format("(.+?)((\\.(%s))?\\.(%s|%s))",
                photoFileExt,
                paramFileExt,
                paramFileExt.toUpperCase()));
            for (File paramFileFolderFile : paramFileFolderFiles) {
              Matcher matcherParams = paramsFilePattern.matcher(paramFileFolderFile.getName());
              if (matcherParams.matches()) {
                if (matcherParams.group(1).equals(matcherPhoto.group(1))) {
                  setParamFile(paramFileFolderFile);
                  setPatternParamsFile(paramsFilePattern);
                  break;
                }
              }
            }
            if (getParamFile() != null) {
              break;
            }
          }
        }
      }
    }
    initDateTaken();
  }
}
