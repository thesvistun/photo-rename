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

import java.io.File;
import java.util.Date;
import java.util.regex.Pattern;

public abstract class Picture {
  private Date dateTaken;
  private File pictureFile, paramFile;
  private Pattern patternPictureFile, patternParamsFile;
  
  public Picture(File pictureFile) {
    this.pictureFile = pictureFile;
    patternPictureFile = Pattern.compile("(.+)(\\.(.+))");
  }
  
  /**
  * @return a date when the picture was taken.
  */
  public Date getDateTaken() {
    return dateTaken;
  }
  
  /**
  * @param dateTaken a date when the picture was taken.
  */
  public void setDateTaken(Date dateTaken) {
    this.dateTaken = dateTaken;
  }

  /**
  * @return a file with parameters that tune the picture.
  */
  public File getParamFile() {
    return paramFile;
  }
  
  /**
  * @param paramFile a file with parameters that tune the picture.
  */
  public void setParamFile(File paramFile) {
    this.paramFile = paramFile;
  }

  /**
  * @return a pattern that are matched with the parameter file.
  */
  public Pattern getPatternParamsFile() {
    return patternParamsFile;
  }
  
  /**
  * @param patternParamsFile a pattern that are matched with the parameter file.
  */
  public void setPatternParamsFile(Pattern patternParamsFile) {
    this.patternParamsFile = patternParamsFile;
  }

  /**
  * @return a pattern that are matched with the picture file.
  */
  public Pattern getPatternPictureFile() {
    return patternPictureFile;
  }
  
  /**
  * @return a file that represents the picture.
  */
  public File getPictureFile() {
    return pictureFile;
  }
  
  /**
  * @return a string that represents the picture.
  */
  @Override
  public String toString() {
    return "Picture " + pictureFile.getAbsolutePath()
        + (null == paramFile ? "" : String.format(" (%s)",
        paramFile.getAbsolutePath()));
  }
  
  protected abstract void initDateTaken() throws Exception;
}
