package name.svistun.photo;

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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.drew.imaging.ImageProcessingException;

class Photo {
  private static Pattern patternPhotoFile;
  private static Map<String, String> paramFileExtToFolderNameMap;
  private Date dateTaken;
  private File photoFile, paramFile;
  private Pattern patternParamsFile;

  static {
    paramFileExtToFolderNameMap = new HashMap<>();
    paramFileExtToFolderNameMap.put("nksc", "NKSC_PARAM");
    patternPhotoFile = Pattern.compile("(.+)(\\.(.+))");
  }

  Photo(File photoFile)
      throws IOException,
      ImageProcessingException,
      NotImageFileException
  {
    this.photoFile = photoFile;
    init();
  }

  boolean rename(boolean dryRun, SimpleDateFormat sdf) {
    return  check(photoFile, patternPhotoFile, sdf)
      && (null == paramFile || check(paramFile, patternParamsFile, sdf))
      && process(photoFile, patternPhotoFile, dryRun, sdf)
      && (null == paramFile || process(paramFile, patternParamsFile, dryRun, sdf));
  }

  private void init() throws IOException, ImageProcessingException, NotImageFileException {
    Matcher matcherPhoto = patternPhotoFile.matcher(photoFile.getName());
    if (matcherPhoto.find()) {
      String photoFileExt = matcherPhoto.group(3);
      if (paramFileExtToFolderNameMap.containsKey(photoFileExt.toLowerCase())) {
        throw new NotImageFileException("recognized as a settings file.");
      }
      for (String paramFileExt : paramFileExtToFolderNameMap.keySet()) {
        File paramFileFolder = new File(photoFile.getParent()
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
                  paramFile = paramFileFolderFile;
                  patternParamsFile = paramsFilePattern;
                  break;
                }
              }
            }
            if (paramFile != null) {
              break;
            }
          }
        }
      }
    }
    dateTaken = PhotoUtilities.getDateTaken(photoFile);
  }

  private boolean check(File file, Pattern pattern, SimpleDateFormat sdf) {
    Matcher matcher = pattern.matcher(file.getName());
    if (matcher.matches()) {
      File newFile = new File(file.getParent()
          + File.separator
          + sdf.format(dateTaken)
          + matcher.group(2));
      int count = 0;
      while (newFile.exists()) {
        if (file.getName().equals(newFile.getName())) {
          System.out.println(String.format("File [%s] already has properly name.",
              file.getAbsoluteFile()));
          return false;
        }
        count++;
        newFile = new File(file.getParent()
            + File.separator
            + sdf.format(dateTaken)
            + String.format("_%s", count)
            + matcher.group(2));
      }
      if (! file.canWrite() || ! newFile.getParentFile().canWrite()) {
        System.err.println(String.format(
            "Access problems. File [%s] can not be renamed.",
            file.getAbsoluteFile()));
        return false;
      }
      return true;
    }
    return false;
  }

  private boolean process(File file,
      Pattern pattern,
      boolean dryRun,
      SimpleDateFormat sdf)
  {
    Matcher matcher = pattern.matcher(file.getName());
    if (matcher.matches()) {
      File newFile = new File(file.getParent()
          + File.separator
          + sdf.format(dateTaken)
          + matcher.group(2));
      int count = 0;
      while (newFile.exists()) {
        count++;
        newFile = new File(file.getParent()
            + File.separator
            + sdf.format(dateTaken)
            + String.format("_%s", count)
            + matcher.group(2));
      }
      if (! dryRun) {
        if (! file.renameTo(newFile)) {
          System.err.println(String.format("%s -X-> %s",
              file.getAbsoluteFile(),
              newFile.getName()));
          return false;
        }
      }
      System.out.println(String.format("%s --> %s",
          file.getAbsoluteFile(), newFile.getName()));
    }
    return true;
  }

  @Override
  public String toString() {
    return "Photo " + photoFile.getAbsolutePath()
        + (null == paramFile ? "" : String.format(" (%s)",
        paramFile.getAbsolutePath()));
  }
}
