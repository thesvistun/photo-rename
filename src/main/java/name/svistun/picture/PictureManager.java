package name.svistun.picture;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.drew.imaging.ImageProcessingException;
import org.apache.commons.lang3.StringUtils;

public final class PictureManager {
  private SimpleDateFormat sdf;
          
  public PictureManager(String dateFormat) {
    sdf = new SimpleDateFormat(dateFormat);
    sdf.setTimeZone(TimeZone.getTimeZone("GTM"));
  }
  
  public void execute(CliOptions cliOptions) {
    List<Picture> failedPictures = new ArrayList<>();
    try {
      List<Picture> pictures = scanDir(cliOptions.getPictureDirPaths(),
          cliOptions.getMaxDepth());
      for (Picture picture : pictures) {
        if (! rename(picture,
            cliOptions.isDryRun()))
        {
          System.err.println(picture + " was not renamed.");
          failedPictures.add(picture);
        }
      }
    } catch (IOException ex) {
      System.err.println(ex.toString() + System.lineSeparator()
          + StringUtils.join(ex.getStackTrace(), System.lineSeparator()));
      System.exit(1);
    }
    if (failedPictures.size() > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append("Failed to rename pictures:").append(System.lineSeparator());
      for (Picture failedPicture : failedPictures) {
        sb.append(String.format(
            "\t%s" + System.lineSeparator(),
            failedPicture));
      }
      System.err.println(sb);
    }
  }
  
  public List<Picture> scanDir(String[] pictureDirPaths,
      int maxDepth)
      throws IOException
  {
    List<Picture> pictures = new ArrayList<>();
    List<File> dirs = new ArrayList<>();
    for (String pictureDirPath : pictureDirPaths) {
      dirs.add(new File(pictureDirPath));
    }
    int maxDepthCounter = maxDepth;
    while (! dirs.isEmpty() && maxDepthCounter != 0) {
      List<File> newDirs = new ArrayList<>();
      for (File dir : dirs) {
        File[] dirFiles = dir.listFiles();
        if (null == dirFiles) {
          System.err.println(String.format(
              "Dir %s returned null at getting list of its files.",
              dir.getAbsolutePath()));
          continue;
        }
        for (File file : dirFiles) {
          if (file.isDirectory()) {
            newDirs.add(file);
          } else {
            try {
              Picture picture = PictureFactory.getPicture(file);
              if (picture == null) {
                System.out.println(String.format("File [%s] unrecognized.", file));
                continue;
              }
              pictures.add(picture);
            } catch (ImageProcessingException e) {
              System.err.println(String.format(
                  "File %s, %s",
                  file.getAbsolutePath(),
                  e.getMessage()));
            } catch (NotImageFileException e) {
              System.out.println(String.format(
                  "Skip processing file %s as an image, %s",
                  file.getAbsolutePath(),
                  e.getMessage()));
            }
          }
        }
      }
      dirs = newDirs;
      maxDepthCounter--;
    }
    return pictures;
  }
  
  private boolean rename(Picture picture, boolean dryRun) {
    return  check(picture.getPictureFile(), picture.getPatternPictureFile(), picture.getDateTaken())
      && (null == picture.getParamFile()
            || check(picture.getParamFile(), picture.getPatternParamsFile(), picture.getDateTaken()))
      && process(picture.getPictureFile(), picture.getPatternPictureFile(), picture.getDateTaken(), dryRun)
      && (null == picture.getParamFile()
            || process(picture.getParamFile(), picture.getPatternParamsFile(), picture.getDateTaken(), dryRun));
  }
  
  private boolean check(File file,
      Pattern pattern,
      Date dateTaken)
  {
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
      Date dateTaken,
      boolean dryRun)
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
}
